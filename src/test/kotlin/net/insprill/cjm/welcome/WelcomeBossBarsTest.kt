package net.insprill.cjm.welcome

import net.insprill.cjm.CustomJoinMessages
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock
import java.util.UUID

class WelcomeBossBarsTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: CustomJoinMessages
    private var bars: WelcomeBossBars? = null
    private var rewards: WelcomeRewards? = null
    @BeforeEach fun setup() { server = MockBukkit.mock(); plugin = MockBukkit.load(CustomJoinMessages::class.java) }
    @AfterEach fun cleanup() { rewards?.close(); bars?.close(); MockBukkit.unmock() }
    @Test fun upgradeAddsSettingsWithoutReplacingCustomMoneyAndMessages() {
        plugin.welcomeRewards!!.close()
        val file = java.io.File(plugin.dataFolder, "welcome-rewards.yml")
        val config = YamlConfiguration.loadConfiguration(file)
        config.set("bossbar", null)
        config.set("reward.amount", 123)
        config.set("messages.welcomed", "&9{player} 欢迎你来我的新服")
        config.save(file)
        rewards = WelcomeRewards(plugin)
        val updated = YamlConfiguration.loadConfiguration(file)
        assertEquals(123, updated.getInt("reward.amount"))
        assertEquals("&9{player} 欢迎你来我的新服", updated.getString("messages.welcomed"))
        assertEquals("PURPLE", updated.getString("bossbar.color"))
        assertTrue(updated.getBoolean("bossbar.enabled"))
    }
    @Test fun customTitleAndDisabledBarAreRespected() {
        val player = server.addPlayer()
        val config = YamlConfiguration().apply {
            set("bossbar.enabled", false)
            set("bossbar.title", "&9{test} {player} 来了 ×{count} {seconds}s")
        }
        bars = WelcomeBossBars(plugin, config)
        val notice = WelcomeBossBars.Notice(UUID.randomUUID(), player.uniqueId, "新人", 3, true, 22)
        assertEquals("&9测试 新人 来了 ×3 22s", WelcomeBossBars.format(config.getString("bossbar.title")!!, notice))
        bars!!.refresh(listOf(notice)); server.scheduler.performTicks(2)
        assertEquals(0, player.activeBossBars().count())
    }
    @Test fun titleUpdatesAndExpiryRemovesOnlyWelcomeBar() {
        val player = server.addPlayer()
        bars = WelcomeBossBars(plugin, YamlConfiguration())
        val notice = WelcomeBossBars.Notice(UUID.randomUUID(), player.uniqueId, "新人", 0, false, 60)
        bars!!.refresh(listOf(notice)); server.scheduler.performTicks(2)
        val bar = player.activeBossBars().single()
        assertEquals(LegacyComponentSerializer.legacyAmpersand().deserialize("&b欢迎 新人 加入啾啾服务器 &f| &e已有 0 人欢迎"), bar.name())
        bars!!.refresh(listOf(notice.copy(count = 2))); server.scheduler.performTicks(2)
        assertEquals(1, player.activeBossBars().count())
        assertEquals(LegacyComponentSerializer.legacyAmpersand().deserialize("&b欢迎 新人 加入啾啾服务器 &f| &e已有 2 人欢迎"), bar.name())
        bars!!.refresh(emptyList()); server.scheduler.performTicks(2)
        assertEquals(0, player.activeBossBars().count())
    }
    @Test fun newcomerOnlyAndTestPermissionAreRespected() {
        val target = server.addPlayer(); val other = server.addPlayer()
        val config = YamlConfiguration().apply { set("bossbar.audience", "NEWCOMER") }
        bars = WelcomeBossBars(plugin, config)
        val notice = WelcomeBossBars.Notice(UUID.randomUUID(), target.uniqueId, "新人", 1, false, 30)
        bars!!.refresh(listOf(notice)); server.scheduler.performTicks(2)
        assertEquals(1, target.activeBossBars().count()); assertEquals(0, other.activeBossBars().count())
        bars!!.refresh(listOf(notice.copy(test = true))); server.scheduler.performTicks(2)
        assertEquals(0, target.activeBossBars().count())
    }
    @Test fun multipleNewcomersShareOneBarAndCloseRemovesIt() {
        val player = server.addPlayer()
        bars = WelcomeBossBars(plugin, YamlConfiguration())
        val a = WelcomeBossBars.Notice(UUID.randomUUID(), player.uniqueId, "甲", 1, false, 30)
        bars!!.refresh(listOf(a, a.copy(id = UUID.randomUUID(), name = "乙")))
        server.scheduler.performTicks(2)
        assertEquals(1, player.activeBossBars().count())
        bars!!.close(); assertEquals(0, player.activeBossBars().count())
    }
}
