package com.ceraia

import com.ceraia.modules.ceraia.managers.PlayerManager
import com.ceraia.metrics.Metrics
import com.ceraia.modules.*
import com.ceraia.modules.arenas.ArenaModule
import com.ceraia.modules.RaceModule
import com.ceraia.modules.SystemModule
import com.ceraia.util.ConfigHelper
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.UUID

class Ceraia : JavaPlugin() {
    private val plugin: Ceraia = this

    var metrics: Metrics? = null

    lateinit var playerManager: PlayerManager
        private set
    lateinit var seatingModule: SeatingModule
        private set
    lateinit var marriageModule: MarriageModule
        private set
    lateinit var systemModule: SystemModule
        private set
    lateinit var raceModule: RaceModule
        private set
    lateinit var configHelper: ConfigHelper
        private set
    lateinit var arenaModule: ArenaModule
        private set

    override fun onEnable() {
        metrics = Metrics(this, 20303)

        saveDefaultConfig()

        File(dataFolder, "data").mkdirs()
        File(dataFolder, "data/users").mkdirs()

        /*---------------------------------*/
        /*       Registering Managers      */
        /*---------------------------------*/
        playerManager = PlayerManager(plugin)

        /*---------------------------------*/
        /*             Modules             */
        /*---------------------------------*/
        seatingModule = SeatingModule(plugin)
        marriageModule = MarriageModule(plugin)
        systemModule = SystemModule(plugin)
        raceModule = RaceModule(plugin)
        arenaModule = ArenaModule(plugin)

        /*---------------------------------*/
        /*             Helpers             */
        /*---------------------------------*/
        configHelper = ConfigHelper(plugin)
    }

    override fun onDisable() {
        playerManager.savePlayers()
        metrics?.shutdown()
    }

    fun noPermission(player: Player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You don't have permission to execute this command."))
    }
}
