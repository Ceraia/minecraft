package dev.xdbl;

import dev.xdbl.commands.arena.*;
import dev.xdbl.commands.kingdoms.CommandKingdom;
import dev.xdbl.commands.misc.CommandMod;
import dev.xdbl.commands.misc.CommandSit;
import dev.xdbl.commands.misc.CommandVersion;
import dev.xdbl.listeners.*;
import dev.xdbl.managers.*;
import dev.xdbl.misc.Metrics;
import dev.xdbl.types.ArenaSelectGUI;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class Double extends JavaPlugin {

    private ArenaManager arenaManager;
    private InviteManager inviteManager;
    private ArenaSelectGUI arenaSelectGUI;
    private CommandGVG commandGVG;
    private PlayerManager playerManager;
    Metrics metrics;
    private EloScoreboardManager eloScoreBoardManager;
    private KingdomManager kingdomManager;
    private ChairManager chairManager;

    public void onEnable() {
        metrics = new Metrics(this, 20303);

        saveDefaultConfig();
        new File(getDataFolder(), "data/arenas").mkdirs();
        new File(getDataFolder(), "data/items").mkdirs();
        new File(getDataFolder(), "data/users").mkdirs();
        new File(getDataFolder(), "data/factions").mkdirs();

        // Managers
        this.kingdomManager = new KingdomManager(this);
        this.arenaManager = new ArenaManager(this);
        this.playerManager = new PlayerManager(this);
        this.eloScoreBoardManager = new EloScoreboardManager(this);
        this.inviteManager = new InviteManager();
        this.chairManager = new ChairManager(this);

        this.arenaSelectGUI = new ArenaSelectGUI(this);
        this.commandGVG = new CommandGVG(this);

        // Command
        CommandPVP commandPVP = new CommandPVP(this);
        CommandArena commandArena = new CommandArena(this);
        CommandMod commandMod = new CommandMod(this);
        CommandTop commandTop = new CommandTop(this);
        CommandProfile commandProfile = new CommandProfile(this);
        CommandVersion commandVersion = new CommandVersion(this);
        CommandKingdom commandKingdom = new CommandKingdom(this);
        CommandSit commandSit = new CommandSit(this);

        // Listeners
        new PlayerEloChangeListener(this);
        new ArenaFightListener(this);
        new PlayerInventoryListener(this);
        new ArenaBlockListener(this);
        new ArenaExplodeListener(this);
        new SpellsListener(this);
        new ReviveListener(this);

        // PvP Commands
        Objects.requireNonNull(getCommand("pvp")).setExecutor(commandPVP);
        Objects.requireNonNull(getCommand("arena")).setExecutor(commandArena);
        Objects.requireNonNull(getCommand("gvg")).setExecutor(commandGVG);
        Objects.requireNonNull(getCommand("top")).setExecutor(commandTop);
        Objects.requireNonNull(getCommand("leaderboard")).setExecutor(commandTop);
        Objects.requireNonNull(getCommand("profile")).setExecutor(commandProfile);
        Objects.requireNonNull(getCommand("stats")).setExecutor(commandProfile);

        // Misc
        Objects.requireNonNull(getCommand("mod")).setExecutor(commandMod);
        Objects.requireNonNull(getCommand("version")).setExecutor(commandVersion);
        Objects.requireNonNull(getCommand("sit")).setExecutor(commandSit);

        // Kingdom Commands
        Objects.requireNonNull(getCommand("kingdom")).setExecutor(commandKingdom);
    }

    public void onDisable() {
        metrics.shutdown();
        playerManager.savePlayers();
        kingdomManager.saveKingdoms();
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public InviteManager getInviteManager() {
        return inviteManager;
    }

    public ArenaSelectGUI getArenaSelectGUI() {
        return arenaSelectGUI;
    }

    public CommandGVG getGroupManager() {
        return commandGVG;
    }

    public KingdomManager getKingdomManager() {
        return kingdomManager;
    }

    public ChairManager getChairManager() {
        return chairManager;
    }

}
