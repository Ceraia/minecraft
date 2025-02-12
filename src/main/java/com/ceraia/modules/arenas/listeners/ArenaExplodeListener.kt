package com.ceraia.modules.arenas.listeners

import com.ceraia.Ceraia
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.type.RespawnAnchor
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent

class ArenaExplodeListener(private val plugin: Ceraia) : Listener {
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    private fun isInArena(player: Player): Boolean {
        return plugin.arenaModule.arenaManager!!.getArena(player) != null
    }

    // TNT
    @EventHandler
    fun onEntityExplode(e: EntityExplodeEvent) {
        var source: Player? = null
        if (e.entityType == EntityType.TNT) {
            val tnt = e.entity as TNTPrimed
            val entity = tnt.source as? Player ?: return
            source = entity
        }

        if (source == null || isInArena(source)) {
            return
        }
        e.blockList().clear()
    }

    // End crystal
    @EventHandler
    fun onHitCrystal(e: EntityDamageByEntityEvent) {
        val entity = e.entity
        val damager = e.damager

        val source: Player

        if (entity !is EnderCrystal) return

        if (damager is Player) {
            source = damager
        } else if (damager is Arrow) {
            val entity2 = damager.shooter as Entity? as? Player ?: return
            source = entity2
        } else {
            return
        }

        if (isInArena(source)) {
            return
        }

        e.isCancelled = true
        if (e.entity.isValid) e.entity.remove()
        e.entity.world.createExplosion(
            e.entity.location,
            6f,
            false,
            false
        )
    }

    // Respawn Anchor
    @EventHandler
    fun onFillAnchor(e: PlayerInteractEvent) {
        if (e.clickedBlock == null) return
        if (e.clickedBlock!!.type != Material.RESPAWN_ANCHOR) return

        val block = e.clickedBlock
        val data = block!!.blockData as RespawnAnchor
        if (data.charges < data.maximumCharges) return

        if (isInArena(e.player)) return

        e.isCancelled = true
        block.type = Material.AIR
        block.world.createExplosion(
            block.location,
            5f,
            false,
            false
        )
    }
}
