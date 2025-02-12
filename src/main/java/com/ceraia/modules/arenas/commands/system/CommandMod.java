package com.ceraia.modules.arenas.commands.system;

import com.ceraia.Ceraia;
import com.ceraia.modules.ceraia.types.CeraiaPlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandMod implements CommandExecutor, TabCompleter {

    private final Ceraia plugin;

    public CommandMod(Ceraia plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("xdbl.mod")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.no_permission"))));
            return true;
        }

        if (args.length == 0) {
            ModHelp(sender);
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("ban")) {
                ModHelp(sender);
                return true;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("ban")) {
                if (args[1].equalsIgnoreCase("pvp")) {
                    ModHelp(sender);
                    return true;
                }
                if (args[1].equalsIgnoreCase("arena")) {
                    ModHelp(sender);
                    return true;
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 1) {
            List<String> tabOptions = new ArrayList<>();
            tabOptions.add("ban");
            tabOptions.add("remove");
            return tabOptions;
        }
        if (args.length == 2) {
            List<String> tabOptions = new ArrayList<>();
            if (args[1].equalsIgnoreCase("ban")) tabOptions.add("pvp");
            tabOptions.add("arena");
            return tabOptions;
        }
        if (args.length == 3) {
            if (args[2].equalsIgnoreCase("pvp")) {
                List<String> tabOptions = new ArrayList<>();
                // If there is an argument, suggest online player names
                for (Player player : Bukkit.getOnlinePlayers()) {
                    tabOptions.add(player.getName());
                }
                return tabOptions;
            }
            if (args[2].equalsIgnoreCase("arena")) {
                List<String> tabOptions = new ArrayList<>();
                // If there is an argument, suggest all arena names
                Objects.requireNonNull(plugin.getArenaModule().getArenaManager()).getArenas().forEach(arena -> tabOptions.add(arena.getName()));
                return tabOptions;
            }
        }
        // If there is more than one argument, return an empty list
        return new ArrayList<>();
    }

    private void ModHelp(CommandSender sender) {
        plugin.getConfig().getStringList("messages.mod.help").forEach(s -> sender.sendMessage(MiniMessage.miniMessage().deserialize(s)));
    }
}
