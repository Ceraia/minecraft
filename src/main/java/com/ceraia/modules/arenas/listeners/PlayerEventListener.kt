package com.ceraia.modules.arenas.listeners

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerEventListener : Event() {
    companion object {
        private val handlers = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return handlers
        }
    }

    override fun getHandlers(): HandlerList {
        return handlers
    }
}