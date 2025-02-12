package com.ceraia.modules.arenas.listeners

import com.ceraia.Ceraia
import com.ceraia.modules.arenas.managers.EloScoreboardManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerEloChangeListener(private val plugin: Ceraia) : Listener {
    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler // Custom on player Elo change event to update the scoreboard
    fun onPlayerEloChange(event: PlayerEventListener?) {
        val eloScoreboardManager = EloScoreboardManager(plugin)
        eloScoreboardManager.updateScoreboard()
    }

    @EventHandler // On player join event to update the scoreboard
    fun onPlayerJoin(event: PlayerJoinEvent?) {
        val eloScoreboardManager = EloScoreboardManager(plugin)
        eloScoreboardManager.updateScoreboard()
    }
}
