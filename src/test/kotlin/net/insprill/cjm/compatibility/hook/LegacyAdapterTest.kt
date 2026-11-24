package net.insprill.cjm.compatibility.hook

import net.insprill.cjm.CustomJoinMessages
import net.insprill.cjm.compatibility.supervanish.SuperVanishVanishHook
import dev.philippcmd.supervanish.api.VanishStateChangeEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockbukkit.mockbukkit.MockBukkit

class LegacyAdapterTest {
    @Test fun originalAdapterProtectsMissingProviderAndKeepsOriginalIdentityAndCommands() {
        val server = MockBukkit.mock()
        try {
            val plugin = MockBukkit.load(CustomJoinMessages::class.java)
            val player = server.addPlayer()
            assertEquals("CustomJoinMessages", plugin.name)
            assertTrue(plugin.dataFolder.name.startsWith("CustomJoinMessages")) // MockBukkit appends version/random suffix
            assertNotNull(plugin.commandManager.getRootCommand("cjm"))
            val hook = SuperVanishVanishHook(plugin)
            assertTrue(hook.isVanished(player))
            plugin.config.set("Addons.Vanish.Fake-Messages.Enabled", false)
            hook.onVanishToggle(VanishStateChangeEvent(player, true))
        } finally { MockBukkit.unmock() }
    }
}
