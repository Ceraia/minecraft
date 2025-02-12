package com.ceraia.modules.arenas.commands.arena;

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

public class CommandProfile implements CommandExecutor, TabCompleter {

    private final Ceraia plugin;

    public CommandProfile(Ceraia plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("xdbl.pvp")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.no_permission"))));
            return true;
        }

        Player player;
        if (args.length == 1) {
            player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.bad_usage"))));
                return true;
            }
        } else {
            player = (Player) sender;
        }

        // Return the player's profile
        CeraiaPlayer ceraiaPlayer = plugin.getPlayerManager().getCeraiaPlayer(player.getUniqueId());
        plugin.getConfig().getStringList("messages.profile").forEach(s -> sender.sendMessage(MiniMessage.miniMessage().deserialize(s
                .replace("%player%", player.getName())
                .replace("%elo%", String.valueOf(ceraiaPlayer.getElo()))
                .replace("%wins%", String.valueOf(ceraiaPlayer.getWins()))
                .replace("%losses%", String.valueOf(ceraiaPlayer.getLosses()))
                .replace("%games%", String.valueOf(ceraiaPlayer.getWins() + ceraiaPlayer.getLosses())))));



        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        // Return a list of all players
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            Bukkit.getOnlinePlayers().forEach(p -> players.add(p.getName()));
            return players;
        }
        return new ArrayList<>();
    }


}
