package net.insprill.cjm.compatibility.authme

import fr.xephi.authme.api.v3.AuthMeApi
import fr.xephi.authme.events.LoginEvent
import net.insprill.cjm.CustomJoinMessages
import net.insprill.cjm.compatibility.hook.AuthHook
import net.insprill.cjm.message.MessageAction
import net.insprill.cjm.util.CrossPlatformScheduler
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class AuthMeAuthHook(private val plugin: CustomJoinMessages) : AuthHook {

    private val loggedInPlayers = java.util.concurrent.ConcurrentHashMap.newKeySet<java.util.UUID>()

    override fun isLoggedIn(player: Player): Boolean {
        return loggedInPlayers.contains(player.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onLogin(e: LoginEvent) {
        handleJoin(e.player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(e: PlayerJoinEvent) {
        e.joinMessage = ""
        if (AuthMeApi.getInstance().isUnrestricted(e.player)) {
            handleJoin(e.player)
        }
    }

    private fun handleJoin(player: Player) {
        loggedInPlayers.add(player.uniqueId)
        val action = if (player.hasPlayedBefore()) MessageAction.JOIN else MessageAction.FIRST_JOIN
        plugin.messageSender.trySendMessages(player, action, true)
        plugin.welcomeRewards?.joined(player)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onQuit(e: PlayerQuitEvent) {
        val id = e.player.uniqueId
        CrossPlatformScheduler.runDelayed(plugin, { loggedInPlayers.remove(id) }, 1L)
    }

}
