package com.redstoned.witherGriefing;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.plugin.Plugin;

public class WitherListener implements Listener {
    private final WitherGriefing plugin;
    private final Plugin nmg_plugin;
    public WitherListener(WitherGriefing plugin, Plugin nmg_plugin) {
        this.plugin = plugin;
        this.nmg_plugin = nmg_plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDestroyEntity(EntityDamageByEntityEvent event) {
        if (!event.isCancelled()) return;

        Entity e = event.getDamager();
        if (e.getType() == EntityType.WITHER) {
            event.setCancelled(this.plugin.isSafe(e));
        } else if (e.getType() == EntityType.WITHER_SKULL) {
            Wither owner = WitherGriefing.skullOwner((WitherSkull) e);
            if (owner == null) return;

            event.setCancelled(this.plugin.isSafe(owner));
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!event.isCancelled()) return;

        Entity e = event.getRemover();
        if (e.getType() == EntityType.WITHER) {
            event.setCancelled(this.plugin.isSafe(e));
        } else if (e.getType() == EntityType.WITHER_SKULL) {
            Wither owner = WitherGriefing.skullOwner((WitherSkull) e);
            if (owner == null) return;

            event.setCancelled(this.plugin.isSafe(owner));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onWitherDestroy(EntityChangeBlockEvent event) {
        if (!event.isCancelled() || event.getEntityType() != EntityType.WITHER) return;

        event.setCancelled(this.plugin.isSafe(event.getEntity()));
    }

    // modified from NoMobGriefing :thumbsup:
    @EventHandler
    public void nmg_onWitherSkullExplode(EntityExplodeEvent event) {
        if (nmg_checkConfig(event.getEntity().getWorld(), "wither")) return;
        if (event.getEntityType() != EntityType.WITHER_SKULL && event.getEntityType() != EntityType.WITHER) return;

        Entity target = event.getEntityType() == EntityType.WITHER_SKULL ? WitherGriefing.skullOwner((WitherSkull)event.getEntity()) : event.getEntity();
        if (target == null) return;

        if (this.plugin.isSafe(target)) {
            event.blockList().clear();
        }
    }

    // Copied from NoMobGriefing
    private boolean nmg_checkConfig(World world, String configSection) {
        String path = world.getName() + "." + configSection;
        if (!this.nmg_plugin.getConfig().contains(path))
            this.nmg_plugin.getConfig().set(path, Boolean.TRUE);
        return this.nmg_plugin.getConfig().getBoolean(path);
    }
}
