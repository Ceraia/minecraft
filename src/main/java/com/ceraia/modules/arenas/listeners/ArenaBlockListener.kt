package com.ceraia.modules.arenas.listeners

import com.ceraia.Ceraia
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent

class ArenaBlockListener(private val plugin: Ceraia) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        val arena = plugin.arenaModule.arenaManager!!.getArena(e.player) ?: return

        arena.placeBlock(e.blockPlaced.location)
    }

    @EventHandler
    fun onBlockBreak(e: BlockBreakEvent) {
        val arena = plugin.arenaModule.arenaManager!!.getArena(e.player) ?: return

        if (arena.placedBlocks.contains(e.block.location)) {
            arena.removeBlock(e.block.location)
            return
        }

        e.isCancelled = true
    }
}
