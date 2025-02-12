package com.ceraia.modules

import com.ceraia.Ceraia
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.jetbrains.annotations.NotNull

class SeatingModule(private val plugin: Ceraia) : CommandExecutor, TabCompleter, Listener {

    val chairs: MutableList<Chair> = mutableListOf()

    init {
        plugin.getCommand("sit")?.setExecutor(this)
        Bukkit.getPluginManager().registerEvents(this, plugin)

        // Check every tick if the player is still sitting on the chair
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val iterator = chairs.iterator()
            while (iterator.hasNext()) {
                val chair = iterator.next()
                if (chair.entity.passengers.isEmpty()) {
                    iterator.remove()
                    chair.entity.remove()
                    val player = chair.player
                    if (chair.isBlock) {
                        player.teleport(player.location.add(0.0, 0.5, 0.0))
                    }
                } else {
                    val player = chair.player
                    val entity = chair.entity
                    entity.setRotation(player.location.yaw, 0f)
                }
            }
        }, 0, 5)

        // Check every 5 seconds if there are any chairs that are not in the list
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            for (player in Bukkit.getOnlinePlayers()) {
                for (entity in player.getNearbyEntities(15.0, 15.0, 15.0)) {
                    if (entity is ArmorStand) {
                        var found = false
                        if (entity.passengers.isNotEmpty()) {
                            val passenger = entity.passengers[0] as Player
                            chairs.add(Chair(passenger, entity, false))
                            continue
                        }
                        for (chair in chairs) {
                            if (chair.entity == entity) {
                                found = true
                                break
                            }
                        }
                        if (!found && entity.scoreboardTags.contains("chair")) {
                            entity.remove()
                        }
                    }
                }
            }
        }, 0, 5 * 20)
    }

    override fun onCommand(@NotNull sender: CommandSender, @NotNull cmd: Command, @NotNull label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            return true
        }
        if (!sender.hasPermission("ceraia.sit")) {
            plugin.noPermission(sender)
            return true
        }

        sit(sender, sender.location.add(0.0, -0.3, 0.0), false)
        return true
    }

    override fun onTabComplete(@NotNull sender: CommandSender, @NotNull cmd: Command, @NotNull label: String, args: Array<String>): List<String> {
        return emptyList()
    }

    fun sit(player: Player, location: Location, block: Boolean) {
        if (player.location.add(0.0, -1.0, 0.0).block.type.isAir) {
            return
        }

        val entity = player.world.spawn(location, ArmorStand::class.java) { armorStand ->
            armorStand.isInvisible = true
            armorStand.setGravity(false)
            armorStand.isSilent = true
            armorStand.isInvulnerable = true
            armorStand.health = 20.0
            armorStand.absorptionAmount = 1000.0
            armorStand.addScoreboardTag("chair")
            armorStand.teleport(location.add(0.0, -1.6, 0.0))
            armorStand.addPassenger(player)
        }

        chairs.add(Chair(player, entity, block))
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        val clickedBlock = e.clickedBlock ?: return
        if (e.player.isSneaking || !e.player.inventory.itemInMainHand.type.isAir) {
            return
        }

        if (chairs.any { it.player == e.player }) {
            return
        }

        if (e.action.isLeftClick) {
            return
        }

        val player = e.player

        if (clickedBlock.type.toString().contains("SLAB")) {
            val slab = clickedBlock.blockData as Slab
            if (slab.type == Slab.Type.BOTTOM) {
                e.isCancelled = true
                sit(player, clickedBlock.location.add(0.5, 0.1, 0.5), true)
            }
        }

        if (clickedBlock.type.toString().contains("STAIRS")) {
            val stairs = clickedBlock.blockData as Stairs
            if (stairs.half == Bisected.Half.BOTTOM) {
                e.isCancelled = true
                sit(player, clickedBlock.location.add(0.5, 0.1, 0.5), true)
            }
        }
    }

    data class Chair(val player: Player, val entity: Entity, val isBlock: Boolean) {
        fun remove(chairs: MutableList<Chair>) {
            entity.remove()
            chairs.remove(this)
        }
    }
}
