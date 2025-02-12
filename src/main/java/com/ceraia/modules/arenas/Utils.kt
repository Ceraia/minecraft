package com.ceraia.modules.arenas

import com.ceraia.Ceraia
import com.ceraia.modules.arenas.types.Arena
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*

object Utils {
    @JvmStatic
    fun revertInventory(plugin: Ceraia, pl: Player, arena: Arena) {
        try {
            val file = File(plugin.dataFolder, "data/pinventory_" + arena.name + "_" + pl.name + ".yml")

            val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)

            val content = arrayOfNulls<ItemStack>(pl.inventory.contents.size)
            try {
                for (s in Objects.requireNonNull(config.getConfigurationSection("items"))?.getKeys(false)!!) {
                    val i = s.toInt()
                    content[i] = config.getItemStack("items.$s")
                }
            } catch (e: Exception) {
                println("Problem loading player inventories.")
            }

            pl.inventory.contents = content

            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Problem loading player inventory")
        }
    }

    @JvmStatic
    fun teleportPlayerToSpawn(plugin: Ceraia, player: Player, arena: Arena) {
        val useLocation = checkNotNull(plugin.config.getString("spawn_teleport.use"))
        if (useLocation.equals("command", ignoreCase = true)) {
            Objects.requireNonNull(plugin.config.getString("spawn_teleport.command"))?.let {
                Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    it
                        .replace("%player%", player.name)
                )
            }
        } else if (useLocation.equals("prior", ignoreCase = true)) {
            val l = arena.getPlayerPriorLocation(player)
            player.teleport(l)
        } else {
            val l = Location(
                Objects.requireNonNull(plugin.config.getString("spawn_teleport.location.world"))
                    ?.let { Bukkit.getWorld(it) },
                plugin.config.getDouble("spawn_teleport.location.x"),
                plugin.config.getDouble("spawn_teleport.location.y"),
                plugin.config.getDouble("spawn_teleport.location.z")
            )
            player.teleport(l)
        }
    }
}
