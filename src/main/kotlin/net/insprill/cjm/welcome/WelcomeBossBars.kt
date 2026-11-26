package net.insprill.cjm.welcome

import net.insprill.cjm.CustomJoinMessages
import net.insprill.cjm.util.CrossPlatformScheduler
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** One bar per recipient; normal player operations run on their entity scheduler. */
class WelcomeBossBars(private val plugin: CustomJoinMessages, config: YamlConfiguration) : Listener, AutoCloseable {
    data class Notice(val id: UUID, val target: UUID, val name: String, val count: Int,
                      val test: Boolean, val remaining: Int)
    private val enabled = config.getBoolean("bossbar.enabled", true)
    private val template = config.getString("bossbar.title", "&b欢迎 {player} 加入啾啾服务器 &f| &e已有 {count} 人欢迎")!!
    private val color = BossBar.Color.valueOf(config.getString("bossbar.color", "PURPLE")!!.uppercase())
    private val overlay = BossBar.Overlay.valueOf(config.getString("bossbar.style", "PROGRESS")!!.uppercase())
    private val audience = config.getString("bossbar.audience", "ALL")!!.uppercase()
    private val progress = config.getDouble("bossbar.progress", 1.0).toFloat()
    private val rotateSeconds = config.getInt("bossbar.rotate-seconds", 5)
    private val bars = ConcurrentHashMap<UUID, BossBar>()
    private val serializer = LegacyComponentSerializer.legacyAmpersand()
    @Volatile private var closed = false
    init {
        require(audience in setOf("ALL", "NEWCOMER")) { "bossbar.audience 只能为ALL或NEWCOMER" }
        require(progress.isFinite() && progress in 0f..1f) { "bossbar.progress 须为0～1" }
        require(rotateSeconds in 1..60) { "bossbar.rotate-seconds 须为1～60秒" }
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }
    fun refresh(notices: List<Notice>) {
        if (closed || !enabled || (notices.isEmpty() && bars.isEmpty())) return
        val rotation = System.nanoTime() / (rotateSeconds * 1_000_000_000L)
        Bukkit.getOnlinePlayers().forEach { player ->
            CrossPlatformScheduler.runAtEntity(plugin, player, Runnable {
                if (closed || !player.isOnline) return@Runnable
                val candidates = if (!plugin.hookManager.isLoggedIn(player)) emptyList() else notices.filter {
                    (!it.test || player.hasPermission("jiujiuhuanying.test")) &&
                        (audience == "ALL" || it.target == player.uniqueId)
                }
                if (candidates.isEmpty()) {
                    bars.remove(player.uniqueId)?.let { player.hideBossBar(it) }
                } else {
                    val notice = candidates[(rotation % candidates.size).toInt()]
                    val title = serializer.deserialize(format(template, notice))
                    val bar = bars[player.uniqueId]
                    if (bar == null) {
                        val fresh = BossBar.bossBar(title, progress, color, overlay)
                        bars[player.uniqueId] = fresh
                        player.showBossBar(fresh)
                    } else bar.name(title)
                }
            })
        }
    }
    @EventHandler fun quit(event: PlayerQuitEvent) {
        bars.remove(event.player.uniqueId)?.let { event.player.hideBossBar(it) }
    }
    override fun close() {
        closed = true
        org.bukkit.event.HandlerList.unregisterAll(this)
        // Adventure audience removal sends UI packets without reading world state.
        bars.forEach { (id, bar) -> Bukkit.getPlayer(id)?.hideBossBar(bar) }
        bars.clear()
    }
    companion object {
        @JvmStatic fun format(template: String, notice: Notice): String = template
            .replace("{player}", notice.name).replace("{count}", notice.count.toString())
            .replace("{seconds}", notice.remaining.toString())
            .replace("{test}", if (notice.test) "测试" else "")
    }
}
