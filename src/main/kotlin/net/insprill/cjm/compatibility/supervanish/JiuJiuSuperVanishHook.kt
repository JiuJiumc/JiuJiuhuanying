package net.insprill.cjm.compatibility.supervanish

import dev.philippcmd.supervanish.Main
import dev.philippcmd.supervanish.api.VanishStateChangeEvent
import net.insprill.cjm.CustomJoinMessages
import net.insprill.cjm.compatibility.hook.VanishHook
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

/** Uses the installed dev.philippcmd plugin's public state/event API, never de.myzelyam. */
class JiuJiuSuperVanishHook(override val plugin: CustomJoinMessages) : VanishHook {
    override fun isVanished(player: Player): Boolean {
        val installed = Bukkit.getPluginManager().getPlugin("SuperVanish")
        // Fail closed when the selected provider disappears, so hidden players aren't exposed.
        if (installed !is Main || !installed.isEnabled) return true
        return installed.vanishManager.isVanished(player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun toggled(event: VanishStateChangeEvent) {
        handleToggle(event.player, event.isVanishing)
    }
}
