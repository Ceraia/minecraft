package com.ceraia.modules.arenas.commands.arena;

import com.ceraia.Ceraia;
import com.ceraia.modules.arenas.types.Arena;
import com.ceraia.modules.ceraia.types.CeraiaPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CommandArena implements CommandExecutor, TabCompleter {

    private final Ceraia plugin;

    public CommandArena(Ceraia plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(!sender.hasPermission("xdbl.arena")){
            sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.no_permission")));
            return true;
        }

        if (args.length == 0) {
            arenaHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            arenaList(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("scoreboard") || args[0].equalsIgnoreCase("top")) {
            Player p = (Player) sender;

            // Create and show a string list of the top 10 players with the highest elo
            List<Component> top = new ArrayList<>();
            AtomicInteger i = new AtomicInteger();
            i.set(1);

            top.add(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.scoreboard.top")));

            plugin.getPlayerManager().getCeraiaPlayers().stream().sorted(Comparator.comparingInt(CeraiaPlayer::getElo).reversed()).limit(10).forEach(ap -> {
                String playerName = Bukkit.getOfflinePlayer(ap.getUUID()).getName();
                int elo = ap.getElo();

                String medal; // Default medal color for players outside the top 3

                // Check for 1st, 2nd, and 3rd place
                if (i.get() == 1) {
                    medal = "<gold>"; // Gold for 1st place
                } else if (i.get() == 2) {
                    medal = "<#C0C0C0>"; // Silver for 2nd place
                } else if (i.get() == 3) {
                    medal = "<#cd7f32>"; // Bronze for 3rd place
                } else {
                    medal = "<white>"; // Default medal color for players outside the top 3
                }

                top.add(MiniMessage.miniMessage().deserialize(medal + i + " " + playerName + " <dark_gray>- <gray>" + elo + " ELO (" + (ap.getWins() + ap.getLosses()) + " games)"));
                i.getAndIncrement();
            });

            // Send the top 10 players with the highest elo to the player
            top.forEach(p::sendMessage);

            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            arenaDelete(sender, args);
            return true;
        }
        if (args[0].equalsIgnoreCase("create")) {
            arenaCreate(sender, args);
            return true;
        }
        if (args[0].equalsIgnoreCase("sp1")) {
            arenaSP1(sender, args);
            return true;
        }
        if (args[0].equalsIgnoreCase("sp2")) {
            arenaSP2(sender, args);
            return true;
        }
        if (args[0].equalsIgnoreCase("public")) {
            arenaPublic(sender, args);
            return true;
        }
        else {
            badUsage(sender);
            arenaHelp(sender);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> arenas = new ArrayList<>();

        if (args.length == 1) {
            return Arrays.asList("list", "delete", "public", "create", "sp1", "sp2", "top", "scoreboard");
        } else if (args.length == 2 && (
                args[0].equalsIgnoreCase("delete") ||
                        args[0].equalsIgnoreCase("public") ||
                        args[0].equalsIgnoreCase("sp1") ||
                        args[0].equalsIgnoreCase("sp2"))) {
            plugin.getArenaModule().getArenaManager().getArenas().forEach(a ->{
                if (a.getOwner().equals(sender.getName())) {
                    arenas.add(a.getName());
                }
            });
            return arenas;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            return Arrays.asList("<name>");
        } else if ((args.length == 3 && args[0].equalsIgnoreCase("public"))) {
            return Arrays.asList("true", "false");
        }
        return new ArrayList<>();
    }

    private void arenaSP1(CommandSender sender, String[] args) {
        if (args.length == 1) {
            badUsage(sender);
            return;
        }

        String name = args[1];

        Arena arena = plugin.getArenaModule().getArenaManager().getArena(name);
        if (arena == null) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.not_found"))
            );
            return;
        }
        if (!Objects.equals(arena.getOwner(), sender.getName())) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.not_yours"))
            );
            return;
        }
        arena.setSpawnPoint1(((Player) sender).getLocation());
        sender.sendMessage(
                MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.sp1.success"))
        );
    }

    private void arenaSP2(CommandSender sender, String[] args) {
        if (args.length == 1) {
            badUsage(sender);
            return;
        }

        String name = args[1];

        Arena arena = plugin.getArenaModule().getArenaManager().getArena(name);
        if (arena == null) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.not_found"))
            );
            return;
        }

        if (!Objects.equals(arena.getOwner(), sender.getName())) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.not_yours"))
            );
            return;
        }
        arena.setSpawnPoint2(((Player) sender).getLocation());
        sender.sendMessage(
                MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.sp2.success"))
        );
    }

    private void arenaCreate(CommandSender sender, String[] args) {
        if (args.length == 1) {
            badUsage(sender);
            return;
        }

        String name = args[1];

        // Check if the string is the same as <name>, if so state the user should put a name
        if (name.equalsIgnoreCase("<name>")) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.create.no_name"))
            );
            return;
        }
        // Check if the string is alphanumeric
        if (!name.matches("[a-zA-Z0-9]*")) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.create.alphanumeric"))
            );
            return;
        }

        // Check if the arena already exists
        if (plugin.getArenaModule().getArenaManager().getArenas().stream().filter(a -> a.getName().equalsIgnoreCase(name)).count() > 0) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.create.exists"))
            );
            return;
        }

        File file = new File(plugin.getDataFolder(), "data/arenas/" + name + ".yml");

        Arena arena = new Arena(plugin, name, sender.getName(), ((Player) sender).getLocation(), ((Player) sender).getLocation(), false, false, file);

        arena.setSpawnPoint1(((Player) sender).getLocation());
        arena.setSpawnPoint2(((Player) sender).getLocation());

        plugin.getArenaModule().getArenaManager().addArena(arena);

        sender.sendMessage(
                MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.create.success"))
        );
    }

    private void arenaDelete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            badUsage(sender);
            return;
        }

        String name = args[1];
        Arena arena = plugin.getArenaModule().getArenaManager().getArena(name);
        if (arena == null) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.not_found"))
            );
            return;
        }
        if (!arena.getOwner().equals(sender.getName())) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.not_yours"))
            );
            return;
        }
        if (arena.getState() != Arena.ArenaState.WAITING) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.delete.running"))
            );
            return;
        }

        arena.delete();
        plugin.getArenaModule().getArenaManager().removeArena(arena);

        sender.sendMessage(
                MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.delete.delete"))
        );
    }

    private void arenaPublic(CommandSender sender, String[] args) {
        if (args.length == 1) {
            badUsage(sender);
            return;
        }

        String name = args[1];
        Arena arena = plugin.getArenaModule().getArenaManager().getArena(name);
        if (arena == null) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.not_found"))
            );
            return;
        }
        if (!arena.getOwner().equals(sender.getName())) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.not_yours"))
            );
            return;
        }

        boolean isPublic = arena.isPublic();

        if (isPublic) {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.public_command.success_private"))
            );
        } else {
            sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.arena.public_command.success_public"))
            );
        }

        arena.setPublic(!isPublic);
    }

    private void badUsage(CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(plugin.getConfig().getString("messages.bad_usage")));
    }

    private void arenaList(CommandSender sender) {
        List<Arena> arenas = plugin.getArenaModule().getArenaManager().getArenas().stream().filter(arena -> arena.getOwner().equals(sender.getName())).collect(Collectors.toList());
        plugin.getConfig().getStringList("messages.arena.list").forEach(s -> {
            if (s.contains("%arenas%")) {
                arenas.forEach(a -> {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(s.replace("%arenas%", a.getName()) +
                            (a.getSpawnPoint1() != null ? " <dark_gray>(" + a.getSpawnPoint1().getBlockX() + ", " + a.getSpawnPoint1().getBlockY() + ", " + a.getSpawnPoint1().getBlockZ() + ")" : "")));
                });
            } else {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(s));
            }
        });
    }

    private void arenaHelp(CommandSender sender) {
        plugin.getConfig().getStringList("messages.arena.help").forEach(s -> {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(s));
        });
    }
}
