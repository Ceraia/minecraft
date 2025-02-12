package com.ceraia.modules.arenas.managers;

import com.ceraia.Ceraia;
import com.ceraia.modules.ceraia.types.CeraiaPlayer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class EloScoreboardManager {

    private final Ceraia plugin;

    public EloScoreboardManager(Ceraia plugin) {
        this.plugin = plugin;
    }

    public void updateScoreboard() {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboardDefault = scoreboardManager.getNewScoreboard();
        Objective objectivePlayerList = scoreboardDefault.registerNewObjective("eloObjectivePlayerList", "dummy", MiniMessage.miniMessage().deserialize("Top Arena Players"));
        Objective objectiveBelowName = scoreboardDefault.registerNewObjective("eloObjectiveBelowName", "dummy", MiniMessage.miniMessage().deserialize("<green>ELO"));

        // Get all online players and set their score to their Elo rating
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            CeraiaPlayer ceraiaPlayer = plugin.getPlayerManager().getCeraiaPlayer(onlinePlayer.getUniqueId());

            objectivePlayerList.getScore(onlinePlayer.getName()).setScore(ceraiaPlayer.getElo());
            objectiveBelowName.getScore(onlinePlayer.getName()).setScore(ceraiaPlayer.getElo());
            objectivePlayerList.setDisplaySlot(DisplaySlot.PLAYER_LIST);
            objectiveBelowName.setDisplaySlot(DisplaySlot.BELOW_NAME);

            onlinePlayer.setScoreboard(scoreboardDefault);
        }
    }
}
