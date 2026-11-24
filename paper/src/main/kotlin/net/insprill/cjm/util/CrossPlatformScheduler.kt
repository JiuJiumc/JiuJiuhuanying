package net.insprill.cjm.util

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import net.insprill.spigotutils.ServerEnvironment
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask

object CrossPlatformScheduler {
    private val folia = ServerEnvironment.isFolia()

    fun runAtEntity(plugin: Plugin, player: Player, runnable: Runnable) {
        if (folia) player.scheduler.runDelayed(plugin, { runnable.run() }, null, 1L)
        else Bukkit.getScheduler().runTaskLater(plugin, runnable, 1L)
    }
    fun runDelayed(plugin: Plugin, runnable: Runnable, delay: Long) {
        if (folia)
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, { runnable.run() }, delay)
        else
            Bukkit.getScheduler().runTaskLater(plugin, runnable, delay)
    }

    fun runAsync(plugin: Plugin, runnable: Runnable) {
        if (folia)
            Bukkit.getAsyncScheduler().runNow(plugin) { runnable.run() }
        else
            Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable)
    }

    fun runAtFixedRate(plugin: Plugin, runnable: Runnable, initialDelayTicks: Long, periodTicks: Long): CancelableTask {
        return if (folia) {
            val task = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, { runnable.run() }, initialDelayTicks, periodTicks)
            PaperCancelableTask(task)
        } else {
            val task = Bukkit.getScheduler().runTaskTimer(plugin, runnable, initialDelayTicks, periodTicks)
            BukkitCancelableTask(task)
        }
    }

    interface CancelableTask {
        fun cancel()
    }

    private class PaperCancelableTask(private val task: ScheduledTask) : CancelableTask {
        override fun cancel() {
            task.cancel()
        }
    }

    private class BukkitCancelableTask(private val task: BukkitTask) : CancelableTask {
        override fun cancel() {
            task.cancel()
        }
    }

}
