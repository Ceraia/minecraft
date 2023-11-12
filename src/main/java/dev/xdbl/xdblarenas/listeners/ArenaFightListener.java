package dev.xdbl.xdblarenas.listeners;

import dev.xdbl.xdblarenas.XDBLArena;
import dev.xdbl.xdblarenas.arenas.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ArenaFightListener implements Listener {

    private final XDBLArena plugin;

    public ArenaFightListener(XDBLArena plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private boolean isInArena(Player player) {
        return plugin.getArenaManager().getArena(player) != null;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() == null || e.getEntity() == null) {
            return;
        }

        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity();
        Player damager = (Player) e.getDamager();

        Arena arena = plugin.getArenaManager().getArena(damager);

        if (arena == null) {
            return;
        }

        if (arena.getState() != Arena.ArenaState.RUNNING) {
            e.setCancelled(true);
            return;
        }

        if (arena.getTeam1().contains(damager) && arena.getTeam1().contains(player)) {
            e.setCancelled(true);
            return;
        }

        if (arena.getTeam2().contains(damager) && arena.getTeam2().contains(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity();

        if (!isInArena(player)) {
            return;
        }

        double healthAfter = player.getHealth() - e.getFinalDamage();
        if (healthAfter <= 0) {
            if (player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING ||
                    player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) {
                return;
            }
            
            e.setCancelled(true);
            player.setHealth(player.getHealthScale());

            // END OF FIGHT

            // Get player that killed the player
            Player killer = null;
            if (e instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
                if (event.getDamager() instanceof Player) {
                    killer = (Player) event.getDamager();

                    if(killer != null) {
                        UUID killerUUID = killer.getUniqueId();
                        UUID victimUUID = player.getUniqueId();

                        // Get the win chance
                        int winChance = plugin.getPlayerManager().CalculateWinChance(killerUUID, victimUUID);

                        // Announce the winner and the win chance in chat
                        Bukkit.broadcastMessage(
                                plugin.getConfig().getString("messages.fight.end_global")
                                        .replace("%winner%", killer.getName())
                                        .replace("%loser%", player.getName())
                                        .replace("%elo%", String.valueOf(plugin.getPlayerManager().getArenaPlayer(victimUUID).getElo()))
                                        .replace("%winchance%", String.valueOf(winChance))
                                        .replace("%arena%", plugin.getArenaManager().getArena(player).getName())
                                        .replace("&", "§")

                        );

                        // Handle ELO calculations
                        plugin.getPlayerManager().PlayerKill(killerUUID, victimUUID);
                    }
                }
            }

            Arena arena = plugin.getArenaManager().getArena(player);

            arena.end(player, false);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!isInArena(e.getEntity())) {
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(e.getEntity());

        Location loc = e.getEntity().getLocation();

        e.getEntity().spigot().respawn();
        new BukkitRunnable() {
            @Override
            public void run() {
                e.getEntity().teleport(loc);
                arena.end(e.getEntity(), false);
            }
        }.runTaskLater(plugin, 5L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!isInArena(e.getPlayer())) {
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(e.getPlayer());
        arena.end(e.getPlayer(), true);
    }
}