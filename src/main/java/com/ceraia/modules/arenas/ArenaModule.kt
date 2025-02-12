package com.ceraia.modules.arenas

import com.ceraia.Ceraia
import com.ceraia.metrics.Metrics
import com.ceraia.modules.arenas.commands.arena.*
import com.ceraia.modules.arenas.commands.system.CommandMod
import com.ceraia.modules.arenas.commands.system.CommandVersion
import com.ceraia.modules.arenas.listeners.*
import com.ceraia.modules.arenas.managers.ArenaManager
import com.ceraia.modules.arenas.managers.EloScoreboardManager
import com.ceraia.modules.arenas.managers.InviteManager
import com.ceraia.modules.arenas.types.ArenaSelectGUI
import com.ceraia.modules.ceraia.managers.PlayerManager
import java.io.File
import java.util.*

class ArenaModule(private val plugin: Ceraia) {
    var arenaManager: ArenaManager? = null
        private set
    var inviteManager: InviteManager? = null
        private set
    var arenaSelectGUI: ArenaSelectGUI? = null
        private set
    var groupManager: CommandGVG? = null
        private set
    var playerManager: PlayerManager? = null
        private set
    var metrics: Metrics? = null
    private var eloScoreBoardManager: EloScoreboardManager? = null
    init {
            File(plugin.dataFolder, "data/arena").mkdirs()
            File(plugin.dataFolder, "data/arena/arenas").mkdirs()
            File(plugin.dataFolder, "data/arena/items").mkdirs()

            // Managers
            this.arenaManager = ArenaManager(plugin)
            this.playerManager = PlayerManager(plugin)
            this.eloScoreBoardManager = EloScoreboardManager(plugin)
            this.inviteManager = InviteManager()

            this.arenaSelectGUI = ArenaSelectGUI(plugin)
            this.groupManager = CommandGVG(plugin)

            // Command
            val commandPVP = CommandPVP(plugin)
            val commandArena = CommandArena(plugin)
            val commandMod = CommandMod(plugin)
            val commandTop = CommandTop(plugin)
            val commandProfile = CommandProfile(plugin)
            val commandVersion = CommandVersion(plugin)

            // Listeners
            PlayerEloChangeListener(plugin)
            ArenaFightListener(plugin)
            PlayerInventoryListener(plugin)
            ArenaBlockListener(plugin)
            ArenaExplodeListener(plugin)

            // PvP Commands
            Objects.requireNonNull(plugin.getCommand("pvp"))?.setExecutor(commandPVP)
            Objects.requireNonNull(plugin.getCommand("arena"))?.setExecutor(commandArena)
            Objects.requireNonNull(plugin.getCommand("gvg"))?.setExecutor(groupManager)
            Objects.requireNonNull(plugin.getCommand("top"))?.setExecutor(commandTop)
            Objects.requireNonNull(plugin.getCommand("leaderboard"))?.setExecutor(commandTop)
            Objects.requireNonNull(plugin.getCommand("profile"))?.setExecutor(commandProfile)
            Objects.requireNonNull(plugin.getCommand("stats"))?.setExecutor(commandProfile)
    }

    fun calculateWinChance(player1: UUID, player2: UUID): Double {
        val elo1 = playerManager?.getPlayer(player1)?.elo
        val elo2 = playerManager?.getPlayer(player2)?.elo

        if ((elo1 == 0 || elo1 == null) || (elo2 == 0 || elo2 == null)) {
            return 0.5
        }

        return 1 / (1 + Math.pow(10.0, (elo2 - elo1) / 400.0))
    }
}