package net.insprill.cjm.welcome

import city.jiujiu.lib.services.CurrencyService
import net.insprill.cjm.CustomJoinMessages
import net.insprill.cjm.util.CrossPlatformScheduler
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/** All ledger IO/settlement runs on one bounded worker; Bukkit work returns to each entity. */
class WelcomeRewards(private val plugin: CustomJoinMessages) : AutoCloseable {
    private data class Window(val target: UUID, val name: String, val deadline: Long)
    private val windows = ConcurrentHashMap<UUID, Window>()
    private val inFlight = ConcurrentHashMap.newKeySet<String>()
    private val worker = ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, ArrayBlockingQueue(128),
        { task -> Thread(task, "JiuJiuhuanying-rewards").apply { isDaemon = true } }, ThreadPoolExecutor.AbortPolicy())
    private val config: YamlConfiguration
    private val amount: Double
    private val seconds: Int
    private val dailyLimit: Int
    private val zone: ZoneId
    @Volatile private var closed = false
    @Volatile private var failed = false
    private var ledger: WelcomeLedger? = null // worker-owned
    private var cleanup: CrossPlatformScheduler.CancelableTask? = null
    init {
        val file = java.io.File(plugin.dataFolder, "welcome-rewards.yml")
        if (!file.exists()) plugin.saveResource("welcome-rewards.yml", false)
        config = YamlConfiguration.loadConfiguration(file)
        amount = config.getDouble("reward.amount", 100.0)
        seconds = config.getInt("window-seconds", 60)
        dailyLimit = config.getInt("reward.daily-limit", 10)
        zone = ZoneId.of(config.getString("timezone", "Asia/Shanghai")!!)
        require(amount.isFinite() && amount > 0 && amount <= 1000000) { "reward.amount 必须在0以上、1000000以内" }
        require(seconds in 5..600 && dailyLimit in 1..10000) { "欢迎时限须为5～600秒，每日上限须为1～10000" }
        submit {
            try { ledger = WelcomeLedger(java.io.File(plugin.dataFolder, "data/welcome-rewards.journal").toPath()) }
            catch (error: Exception) { stopRewards(error) }
        }
        CrossPlatformScheduler.runAtFixedRate(plugin, Runnable {
            val now = System.nanoTime()
            windows.entries.removeIf { it.value.deadline <= now }
        }, 20L, 20L).also { cleanup = it }
    }

    /** Only real, authenticated first joins call this; fake vanish joins never create rewards. */
    fun joined(player: Player) {
        if (closed || failed || !config.getBoolean("enabled", true) || player.hasPlayedBefore()) return
        if (!plugin.hookManager.isLoggedIn(player) || plugin.hookManager.isVanished(player)) return
        val target = player.uniqueId
        val name = player.name
        submit {
            try {
                windows.entries.removeIf { it.value.deadline <= System.nanoTime() }
                if (closed || failed || windows.size >= 512 || ledger?.open(target) != true) return@submit
                val token = UUID.randomUUID()
                val window = Window(target, name, System.nanoTime() + TimeUnit.SECONDS.toNanos(seconds.toLong()))
                windows[token] = window
                CrossPlatformScheduler.runDelayed(plugin, Runnable {
                    Bukkit.getOnlinePlayers().forEach { recipient ->
                        CrossPlatformScheduler.runAtEntity(plugin, recipient, Runnable {
                            if (!recipient.isOnline || recipient.uniqueId == target || windows[token] != window) return@Runnable
                            if (!recipient.hasPermission("jiujiuhuanying.welcome") || !plugin.hookManager.isLoggedIn(recipient)) return@Runnable
                            val parts = TextComponent.fromLegacyText(text("invite", name))
                            parts.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cjm welcome $token") }
                            recipient.spigot().sendMessage(*parts)
                        })
                    }
                }, 1L)
            } catch (error: Exception) { stopRewards(error) }
        }
    }
    fun claim(player: Player, tokenText: String) {
        if (closed || failed || !config.getBoolean("enabled", true)) { player.sendMessage(text("unavailable")); return }
        if (!Bukkit.getPluginManager().isPluginEnabled("JiuJiuLib")) { player.sendMessage(text("unavailable")); return }
        if (!player.hasPermission("jiujiuhuanying.welcome") || !plugin.hookManager.isLoggedIn(player)) return
        val token = runCatching { UUID.fromString(tokenText) }.getOrNull()
        val window = token?.let { windows[it] }
        if (window == null || window.deadline <= System.nanoTime()) { player.sendMessage(text("expired")); return }
        val who = player.uniqueId
        val welcomerName = player.name
        if (who == window.target) { player.sendMessage(text("self")); return }
        val claimKey = "${window.target}|$who"
        if (!inFlight.add(claimKey)) { player.sendMessage(text("busy")); return }
        val accepted = submit(player) {
            try {
                if (closed || failed || window.deadline <= System.nanoTime()) { tell(who, "expired"); return@submit }
                val bank = CurrencyService.forPlugin(plugin)
                if (!bank.available("j")) { tell(who, "unavailable"); return@submit }
                if (ledger?.reserve(window.target, who, LocalDate.now(zone), dailyLimit, amount) != true) {
                    tell(who, "already-or-limit"); return@submit
                }
                // Intent is durable before calling the provider. Never retry failure/timeout/exception.
                var success = false
                try { if (!closed) success = bank.depositAndGetBalance(who, "j", amount).isSuccess }
                catch (error: Exception) { plugin.logger.severe("欢迎奖励需人工核对：new=${window.target}, who=$who, ${error.message}") }
                ledger!!.finish(window.target, who, success)
                if (!success) plugin.logger.severe("欢迎奖励未确认到账，禁止自动补发：new=${window.target}, who=$who")
                tell(who, if (success) "success" else "review", window.name)
                if (success) tell(window.target, "welcomed", welcomerName)
            } catch (error: Exception) { stopRewards(error); tell(who, "review", window.name) }
            catch (error: LinkageError) { stopRewards(IllegalStateException("JiuJiuLib版本不支持货币接口", error)); tell(who, "unavailable") }
            finally { inFlight.remove(claimKey) }
        }
        if (!accepted) inFlight.remove(claimKey)
    }
    private fun text(key: String, name: String = ""): String = ChatColor.translateAlternateColorCodes('&',
        config.getString("messages.$key", "&f[&4!&f] &c欢迎奖励暂不可用。")!!
            .replace("{player}", name).replace("{amount}", java.math.BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString())
            .replace("{seconds}", seconds.toString()).replace("{limit}", dailyLimit.toString()))
    private fun tell(who: UUID, key: String, name: String = "") {
        if (closed) return
        val player = Bukkit.getPlayer(who) ?: return
        CrossPlatformScheduler.runAtEntity(plugin, player, Runnable { if (!closed && player.isOnline) player.sendMessage(text(key, name)) })
    }
    private fun submit(player: Player? = null, task: () -> Unit): Boolean {
        if (closed || failed) return false
        return try { worker.execute { if (!closed && !failed) task() }; true }
        catch (_: RejectedExecutionException) { player?.sendMessage(text("busy")); false }
    }
    private fun stopRewards(error: Exception) {
        failed = true
        windows.clear()
        inFlight.clear()
        plugin.logger.log(java.util.logging.Level.SEVERE, "欢迎奖励暂停：持久记录或经济接口异常；请核对日志，勿删除奖励记录重试。", error)
    }
    override fun close() {
        closed = true
        windows.clear()
        inFlight.clear()
        cleanup?.cancel()
        worker.shutdownNow() // do not block the global/region thread awaiting IO
    }
}
