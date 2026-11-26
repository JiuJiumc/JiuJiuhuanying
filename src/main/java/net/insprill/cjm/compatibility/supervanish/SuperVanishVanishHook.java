package net.insprill.cjm.compatibility.supervanish;

import dev.philippcmd.supervanish.Main;
import dev.philippcmd.supervanish.api.VanishStateChangeEvent;
import dev.philippcmd.supervanish.utils.VanishManager;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import net.insprill.cjm.CustomJoinMessages;
import net.insprill.cjm.compatibility.hook.VanishHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

/** 仅供配套 dev.philippcmd SuperVanish 修复版使用，保留 CJM 原消息与配置逻辑。 */
public final class SuperVanishVanishHook implements VanishHook {
    private final CustomJoinMessages plugin;
    private final Main dependency;
    private final VanishManager manager;
    private final AtomicBoolean warned = new AtomicBoolean();

    public SuperVanishVanishHook(CustomJoinMessages plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        Plugin candidate = Bukkit.getPluginManager().getPlugin("SuperVanish");
        dependency = candidate instanceof Main main ? main : null;
        manager = dependency == null ? null : dependency.getVanishManager();
        if (manager == null) {
            warnUnavailable();
        }
    }

    @Override
    public CustomJoinMessages getPlugin() {
        return plugin;
    }

    @Override
    public boolean isVanished(Player player) {
        Objects.requireNonNull(player, "player");
        if (!available()) {
            warnUnavailable();
            return true;
        }
        // UUID 在原进退服事件的实体上下文读取；状态表是 SV 的 ConcurrentHashMap。
        return manager.isVanished(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onVanishToggle(VanishStateChangeEvent event) {
        if (available()) {
            // 复用 CJM 的 Fake-Messages、Vanish/Unvanish、Private-Messages 原配置处理。
            handleToggle(event.getPlayer(), event.isVanishing());
        } else {
            warnUnavailable();
        }
    }

    private boolean available() {
        return dependency != null && manager != null && dependency.isEnabled();
    }

    private void warnUnavailable() {
        if (warned.compareAndSet(false, true)) {
            plugin.getLogger().severe("配套 SuperVanish 状态接口不可用，已暂停进退服公告以保护隐身玩家。请停服后安装配套的两个 JAR。");
        }
    }
}
