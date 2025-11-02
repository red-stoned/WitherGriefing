package com.redstoned.witherGriefing;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Optional;

public final class WitherGriefing extends JavaPlugin {

    public boolean isSafe(Entity e) {
        if (e == null || e.customName() == null) return true;

        return !PlainTextComponentSerializer.plainText().serialize(e.customName()).equals(this.grief_name);
    }

    public static Wither skullOwner(WitherSkull ws) {
        if (ws.getOwnerUniqueId() == null) return null;

        return (Wither) Bukkit.getEntity(ws.getOwnerUniqueId());
    }

    private String grief_name;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.grief_name = this.getConfig().getString("grief_name");
        getLogger().info("Withers that can grief should be named: " + this.grief_name);

        Plugin nmg = Bukkit.getPluginManager().getPlugin("NoMobGriefing");
        getServer().getPluginManager().registerEvents(new WitherListener(this, nmg), this);

        Optional<RegisteredListener> nmg_skull_listener = Arrays.stream(EntityExplodeEvent.getHandlerList().getRegisteredListeners()).filter(l -> l.getPlugin() == nmg && l.getExecutor().toString().contains("onWitherSkullExplode")).findFirst();
        if (nmg_skull_listener.isPresent()) {
            RegisteredListener l = nmg_skull_listener.get();
            getLogger().info(l.toString());
            EntityExplodeEvent.getHandlerList().unregister(l);
            getLogger().info("Successfully removed listener from NMG");
        } else {
            getLogger().severe("Could not find NMG handler to remove. Plugin missing? Load order wrong?");
        }
    }
}
