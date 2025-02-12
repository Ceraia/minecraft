package com.ceraia.modules.arenas.commands.system;

import com.ceraia.Ceraia;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandVersion implements CommandExecutor, TabCompleter {

    private final Ceraia plugin;

    public CommandVersion(Ceraia plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        sender.sendMessage(
            MiniMessage.miniMessage().deserialize("<green>Running <white>Ceraia <green>v" + plugin.getPluginMeta().getVersion() + " <green>by <white>Axodouble")
        );
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        return new ArrayList<>();
    }
}
