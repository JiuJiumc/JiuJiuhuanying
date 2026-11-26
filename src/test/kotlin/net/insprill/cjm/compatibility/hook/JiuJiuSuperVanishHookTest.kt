package net.insprill.cjm.compatibility.hook

import net.insprill.cjm.CustomJoinMessages
import net.insprill.cjm.compatibility.supervanish.JiuJiuSuperVanishHook
import dev.philippcmd.supervanish.api.VanishStateChangeEvent
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockbukkit.mockbukkit.MockBukkit

class JiuJiuSuperVanishHookTest {
    @Test fun missingSelectedProviderFailsClosedWithoutCallingOtherVanishApi() {
        val server = MockBukkit.mock()
        try {
            val plugin = MockBukkit.load(CustomJoinMessages::class.java)
            val player = server.addPlayer()
            val hook = JiuJiuSuperVanishHook(plugin)
            assertTrue(hook.isVanished(player))
            plugin.config.set("Addons.Vanish.Fake-Messages.Enabled", false)
            hook.toggled(VanishStateChangeEvent(player, true))
        } finally { MockBukkit.unmock() }
    }
}
