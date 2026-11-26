package net.insprill.cjm.compatibility.supervanish

import net.insprill.cjm.CustomJoinMessages
import net.insprill.cjm.compatibility.hook.PluginHook

open class SuperVanishHook(plugin: CustomJoinMessages) : PluginHook {

    override val authHook = null
    override val vanishHook: net.insprill.cjm.compatibility.hook.VanishHook =
        if (org.bukkit.Bukkit.getPluginManager().getPlugin("SuperVanish")?.description?.main == "dev.philippcmd.supervanish.Main")
            JiuJiuSuperVanishHook(plugin)
        else SuperVanishVanishHook(plugin)
    override val jailHook = null

}
