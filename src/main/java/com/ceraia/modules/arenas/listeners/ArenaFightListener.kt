package com.ceraia.modules.arenas.listeners

import com.ceraia.Ceraia
import com.ceraia.modules.arenas.types.Arena
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class ArenaFightListener(private val plugin: Ceraia) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private fun isInArena(player: Player): Boolean {
        return plugin.arenaModule.arenaManager?.getArena(player) != null
    }

    @EventHandler
    fun onHit(e: EntityDamageByEntityEvent) {
        val damager = e.damager as? Player ?: return
        val player = e.entity as? Player ?: return

        if (!isInArena(damager) && !isInArena(player)) {
            return
        }

        if (((isInArena(damager) && !isInArena(player)) || (!isInArena(damager) && isInArena(player))) || plugin.arenaModule.arenaManager?.getArena(
                damager
            )?.name != plugin.arenaModule.arenaManager?.getArena(player)?.name
        ) {
            e.isCancelled = true
            return
        }

        val arena: Arena = plugin.arenaModule.arenaManager?.getArena(damager) ?: return

        if (arena.state != Arena.ArenaState.RUNNING) {
            e.isCancelled = true
            return
        }

        if (arena.team1.contains(damager) && arena.team1.contains(player)) {
            e.isCancelled = true
            return
        }

        if (arena.team2.contains(damager) && arena.team2.contains(player)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onDamage(e: EntityDamageEvent) {
        val player = e.entity as? Player ?: return

        if (!isInArena(player)) {
            return
        }

        // Get player that hurt the player
        var killer: Player? = null
        if (e is EntityDamageByEntityEvent) {
            // If the damage is caused by a player
            if (e.damager is Player) {
                plugin.logger.info("Player")
                killer = e.damager as Player
            } else if (e.damager is Projectile) {
                plugin.logger.info("Arrow")
                val projectile = e.damager as Projectile
                if (projectile.shooter is Player) {
                    killer = projectile.shooter as Player
                }
            } else if (e.damager.type == EntityType.TNT) {
                plugin.logger.info("TNT")
                if (e.damager.customName() != null) {
                    killer = Bukkit.getPlayer(Objects.requireNonNull(e.damager.customName()).toString())
                }
            }
        }

        val arena: Arena = plugin.arenaModule.arenaManager?.getArena(player) ?: return

        val healthAfter: Double = player.health - e.finalDamage
        if (healthAfter <= 0) {
            // Check if during the fight totems are allowed

            var invite = plugin.arenaModule.inviteManager!!.invites[player]

            if (invite == null) invite = plugin.arenaModule.inviteManager!!.selectingInvites[killer]

            if (invite != null) {
                if (arena.totems) {
                    if (player.inventory.itemInMainHand.type == Material.TOTEM_OF_UNDYING || player.inventory.itemInOffHand.type == Material.TOTEM_OF_UNDYING) {
                        return
                    }
                }
            }

            e.isCancelled = true
            player.health = player.healthScale

            arena.end(player, false)
        }
    }

    @EventHandler
    fun onDeath(e: PlayerDeathEvent) {
        if (!isInArena(e.entity)) {
            return
        }
        val arena = plugin.arenaModule.arenaManager?.getArena(e.entity) ?: return

        val loc = e.entity.location

        var killer = e.entity.killer

        if (killer == null) {
            killer = Bukkit.getPlayer(
                Objects.requireNonNull(Objects.requireNonNull(e.entity.lastDamageCause)?.entity?.customName()).toString()
            )
        }
        if (killer != null) {
            matchEnd(e.entity, killer)
        }

        e.entity.spigot().respawn()
        object : BukkitRunnable() {
            override fun run() {
                e.entity.teleport(loc)
                arena.end(e.entity, false)
            }
        }.runTaskLater(plugin, 5L)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        if (!isInArena(e.player)) {
            return
        }

        val arena = plugin.arenaModule.arenaManager?.getArena(e.player) ?: return

        // Check in which team the player is
        if (arena.team1.contains(e.player)) {
            matchEnd(e.player, arena.team2[0])
        } else {
            matchEnd(e.player, arena.team1[0])
        }

        arena.end(e.player, true)
    }

    private fun matchEnd(loser: Player, winner: Player) {
        val winnerUUID = winner.uniqueId
        val loserUUID = loser.uniqueId

        // Get the win chance
        val winChance = plugin.arenaModule.calculateWinChance(winnerUUID, loserUUID).toInt()

        // Announce the winner and the win chance in chat
        Bukkit.broadcast(
            MiniMessage.miniMessage().deserialize(
                plugin.config.getString("messages.fight.end_global")!!
                    .replace("%winner%", winner.name).replace("%loser%", loser.name)
                    .replace("%elo%", plugin.playerManager.getCeraiaPlayer(loserUUID).elo.toString())
                    .replace("%winchance%", winChance.toString())
                    .replace("%arena%", plugin.arenaModule.arenaManager?.getArena(loser)?.name ?: "")
            )
        )

        // Handle ELO calculations
        // #TODO: Re-implement ELO calculations
        //plugin.getPlayerManager().PlayerKill(winnerUUID, loserUUID);
    }
}