package com.ceraia.modules.arenas.commands.arena;

import com.ceraia.Ceraia;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandGVG implements CommandExecutor, TabCompleter, Listener {

    private final Ceraia plugin;
    private final Map<Player, List<Player>> groups = new HashMap<>();
    private final Map<Player, Player> playersByGroup = new HashMap<>();

    private final Map<Player, Player> invites = new HashMap<>();

    public CommandGVG(Ceraia plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!sender.hasPermission("xdbl.gvg")){
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.no_permission"))));
            return true;
        }

        if (args.length == 0) {
            plugin.getConfig().getStringList("messages.gvg.help").forEach(s -> sender.sendMessage(MiniMessage.miniMessage().deserialize(s)));
            return true;
        }

        Player player = (Player) sender;

        if (args[0].equalsIgnoreCase("invite")) {
            if (playersByGroup.containsKey(player) && !groups.containsKey(player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.dont_have_group"))));
                return true;
            }

            if (args.length == 1) {
                badUsage(sender);
                return true;
            }

            List<Player> targets = new ArrayList<>();
            List<String> notOnline = new ArrayList<>();

            for (int i = 1; i < args.length; i++) {
                Player target = Bukkit.getPlayer(args[i]);
                if (target == null) {
                    notOnline.add(args[i]);
                } else {
                    targets.add(target);
                }

                if (playersByGroup.containsKey(target)) {
                    assert target != null;
                    sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.invite.already_in_group"))
                            .replace("%player%", target.getName())));
                    return true;
                }
            }

            if (targets.contains(player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.invite.cant_invite_yourself"))));
                return true;
            }

            if (notOnline.size() > 0) {
                System.out.println(notOnline);
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.player_offline"))
                        .replace("%player%", String.join(", ", notOnline))));
                return true;
            }

            for (Player target : targets) {
                Component message = Objects.requireNonNull(
                        MiniMessage.miniMessage().deserialize(
                                Objects.requireNonNull(
                                        plugin.getConfig().getString("messages.gvg.invite.invite_message")
                                ).replace("%inviter%", player.getName()
                                )
                        )
                );
                target.sendMessage(message);

                invites.put(target, player);
            }

            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.invite.invite_sent"))
                    .replace("%player%", targets.stream().map(Player::getName).collect(Collectors.joining(", ")))));
            return true;
        }
        else if (args[0].equalsIgnoreCase("accept")) {
            Player inviter = invites.get(player);

            if (inviter == null || !inviter.isOnline()) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.accept.invite_not_found"))));
                return true;
            }

            if (playersByGroup.containsKey(inviter) && !groups.containsKey(inviter)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.accept.invite_not_found"))));
                return true;
            }

            if (playersByGroup.containsKey(player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.accept.you_already_in_group"))));
                return true;
            }

            List<Player> group = groups.get(inviter);
            if (group == null) {
                group = new ArrayList<>();
                group.add(inviter);
                playersByGroup.put(inviter, inviter);
            }
            group.add(player);
            groups.put(inviter, group);

            playersByGroup.put(player, inviter);
            sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.accept.accepted"))));
            for (Player pl : group) {
                pl.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.accept.invite_accepted"))
                        .replace("%player%", player.getName())));
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("leave")) {
            leaveGang(player);
        }
        else if (args[0].equalsIgnoreCase("kick")) {
            if (!groups.containsKey(player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.kick.not_in_group"))));
                return true;
            }

            if (args.length == 1) {
                badUsage(sender);
                return true;
            }

            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.player_offline"))
                        .replace("%player%", args[1])));
                return true;
            }

            List<Player> group = groups.get(player);

            if (!group.contains(target)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.kick.player_not_in_group"))));
                return true;
            }

            group.remove(target);
            playersByGroup.remove(target);

            target.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.kick.you_kicked"))));

            if (group.size() <= 1) {

                groups.get(player).forEach(p -> {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.you_left"))));
                    playersByGroup.remove(p);
                    groups.remove(player);
                });

                groups.remove(player);
            } else {
                group.forEach(p -> p.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.kicked"))
                        .replace("%player%", target.getName()))));
            }
            return true;
        }
        else if (args[0].equalsIgnoreCase("fight")) {
            String playerName = args[1];
            Player invited = Bukkit.getPlayer(playerName);

            if (invited == null) {
                sender.sendMessage(
                        MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.player_offline"))
                                .replace("%player%", playerName)));
                return true;
            }

            if (invited == player) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.invite.invite_yourself"))));
                return true;
            }

            if (!groups.containsKey(player)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.not_in_group"))));
                return true;
            }

            if (!groups.containsKey(invited) || (groups.get(player).contains(invited) || groups.get(invited).contains(player))) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.player_not_in_group"))));
                return true;
            }

            plugin.getArenaModule().getArenaSelectGUI().openArenaList(player, invited);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
        if (args.length == 1) {
            return Arrays.asList("invite", "accept", "leave", "kick", "fight");
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("kick")) {
            return getPlayersByGroup((Player) sender).stream().map(Player::getName).collect(Collectors.toList());
        }
        else if (args.length == 2 && (args[0].equalsIgnoreCase("invite") ||args[0].equalsIgnoreCase("fight"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    private void leaveGang(Player player) {
        if (!playersByGroup.containsKey(player)) {
            player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.not_in_group"))));
            return;
        }

        Player owner = playersByGroup.get(player);
        List<Player> group = groups.get(owner);

        if (group != null || group.size() <= 2) {
            group.forEach(pl -> {
                pl.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.you_left"))));
                playersByGroup.remove(pl);
            });

            groups.remove(owner);
        } else {
            group.forEach(p -> player.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.gvg.leave.player_left_group"))
                    .replace("%player%", player.getName()))));

            group.remove(player);
            groups.put(owner, group);

            playersByGroup.remove(player);
        }
    }

    private void badUsage(CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.bad_usage"))));
    }

    public List<Player> getPlayersByGroup(Player player) {
        return groups.get(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (playersByGroup.containsKey(player)) {
            leaveGang(player);
        }
    }
}
