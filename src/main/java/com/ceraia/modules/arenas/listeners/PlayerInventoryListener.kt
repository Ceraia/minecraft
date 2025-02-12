package com.ceraia.modules.arenas.listeners

import com.ceraia.Ceraia
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent

class PlayerInventoryListener(private val plugin: Ceraia) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private fun isInArena(player: Player): Boolean {
        return plugin.arenaModule.arenaManager!!.getArena(player) != null
    }

    @EventHandler
    fun onDropItem(e: PlayerDropItemEvent) {
        if (isInArena(e.player)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onClickInventory(e: InventoryClickEvent) {
        val p = e.whoClicked as Player

        if (!isInArena(p)) {
            return
        }

        if (e.view.topInventory.type == InventoryType.PLAYER
            || e.view.topInventory.type ==
            InventoryType.CRAFTING
        ) {
            return
        }

        e.isCancelled = true
    }
}
