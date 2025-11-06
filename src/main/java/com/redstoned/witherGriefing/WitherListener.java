package com.redstoned.witherGriefing;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
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

    @EventHandler
    public void onNametag(PlayerInteractAtEntityEvent event) {
        Entity clicked_entity = event.getRightClicked();
        if (clicked_entity.getType() != EntityType.WITHER) return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        item = item.isEmpty() ? event.getPlayer().getInventory().getItemInOffHand() : item;
        if (item.getType() != Material.NAME_TAG) return;

        Component tag_comp = item.getItemMeta().customName();
        if (tag_comp == null) return;

        String current_name = "";
        Component current_comp = clicked_entity.customName();
        if (current_comp != null) {
            current_name = PlainTextComponentSerializer.plainText().serialize(current_comp);
        }

        String tag_name = PlainTextComponentSerializer.plainText().serialize(tag_comp);

        if (!current_name.equals(this.plugin.grief_name) && this.plugin.grief_name.equals(tag_name)) {
            Location pos = clicked_entity.getLocation();
            Component fmsg = Component.text(
                String.format("%s created a griefing wither in ",
                    event.getPlayer().getName()
                ), NamedTextColor.RED)
                .append(
                    Component.text(
                        String.format("%s @ %d %d %d", pos.getWorld().getKey(), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()
                    ), Style.style(TextDecoration.UNDERLINED))
                    .hoverEvent(
                        HoverEvent.showText(Component.text("Teleport to Location"))
                    )
                    .clickEvent(
//                        ClickEvent.suggestCommand(String.format("/execute in %s run tp @s %d %d %d", pos.getWorld().getKey(), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ())
                        ClickEvent.suggestCommand(String.format("/co teleport %s %d %d %d", pos.getWorld().getName(), pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()))
                    )
                )
                .append(
                    Component.text(" [Revert]", NamedTextColor.DARK_RED)
                        .hoverEvent(
                            HoverEvent.showText(Component.text("Remove the wither's ability to grief"))
                        )
                        .clickEvent(
                            ClickEvent.callback(a -> {
                                clicked_entity.customName(null);
                                String msg = "Reverted wither griefing on: " + clicked_entity.getUniqueId();
                                a.sendMessage(Component.text(msg, NamedTextColor.GREEN, TextDecoration.ITALIC));
                                this.plugin.getLogger().info(msg);
                            })
                        )
                );

            Bukkit.broadcast(fmsg, "minecraft.command.teleport");
        }

    }
}
