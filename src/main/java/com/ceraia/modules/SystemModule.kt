package com.ceraia.modules

import com.ceraia.Ceraia
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.util.StringUtil

class SystemModule(private val plugin: Ceraia) : CommandExecutor, TabCompleter, Listener {

    init {
        // Register the commands
        plugin.getCommand("jump")?.setExecutor(this)
        plugin.getCommand("mod")?.setExecutor(this)
        plugin.getCommand("version")?.setExecutor(this)
        plugin.getCommand("day")?.setExecutor(this)
        plugin.getCommand("night")?.setExecutor(this)
        plugin.getCommand("discord")?.setExecutor(this)
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        when (command.name.lowercase()) {
            "jump", "j" -> {
                if (sender is Player) {
                    jump(sender)
                }
                return true
            }
            "version" -> {
                sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                        "<green>Running <white>Ceraia <green>v${plugin.pluginMeta.version} <green>by <white>Axoceraia"
                    )
                )
                return true
            }
            "day", "noon" -> {
                if (!sender.hasPermission("ceraia.time.day") &&
                    !sender.hasPermission("ceraia.time.*")
                ) {
                    plugin.noPermission(sender as Player)
                    return true
                }
                if (sender is Player) {
                    val time = if (command.name.equals("noon", ignoreCase = true)) 6000 else 0
                    sender.world.time = time.toLong()
                    sender.world.weatherDuration = 1
                    sender.world.clearWeatherDuration = 15 * 60 * 20
                }
                return true
            }
            "night" -> {
                if (!sender.hasPermission("ceraia.time.night") &&
                    !sender.hasPermission("ceraia.time.*")
                ) {
                    plugin.noPermission(sender as Player)
                    return true
                }
                if (sender is Player) {
                    sender.world.time = 13000
                }
                return true
            }
            "discord" -> {
                if (!sender.hasPermission("ceraia.discord")) {
                    plugin.noPermission(sender as Player)
                    return true
                }
                sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                        "<green>Click <white><click:copy_to_clipboard:" +
                                plugin.config.getString("DISCORD_INVITE", "EXAMPLE_INVITE") +
                                "><hover:show_text:" +
                                plugin.config.getString("DISCORD_INVITE", "EXAMPLE_INVITE") +
                                ">this</click></white> to copy the Discord link, or click here: <white>" +
                                plugin.config.getString("DISCORD_INVITE", "EXAMPLE_INVITE")
                    )
                )
                return true
            }
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): List<String> {
        val tabOptions = mutableListOf<String>()

        return StringUtil.copyPartialMatches(args.last(), tabOptions, ArrayList())
    }

    private fun jump(player: Player) {
        if (!player.hasPermission("ceraia.jump")) {
            plugin.noPermission(player)
            return
        }
        // Get the block the player is looking at even if it is out of reach
        val block = player.getTargetBlockExact(1000)

        // Teleport the player on top of the block if it is not null
        if (block != null) {
            // Check if there is air above the block
            if (block.getRelative(0, 1, 0).isEmpty) {
                player.teleport(block.location.add(0.5, 1.0, 0.5))
            } else {
                // Get the highest block above the block
                var highestBlock = block.getRelative(0, 1, 0)
                while (highestBlock.getRelative(0, 1, 0).type != Material.AIR) {
                    highestBlock = highestBlock.getRelative(0, 1, 0)
                }
                player.teleport(highestBlock.location.add(0.5, 1.0, 0.5))
            }
        }
    }
}