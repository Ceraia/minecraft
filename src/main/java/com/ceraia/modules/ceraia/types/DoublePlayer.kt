package com.ceraia.modules.ceraia.types

import com.ceraia.Ceraia
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.UUID

class CeraiaPlayer(
    private val plugin: Ceraia,
    val name: String,
    private var race: String,
    private var faction: String?,
    private var marriedName: String?,
    val uuid: UUID,
    var elo: Int,
    private var arenaBanned: Boolean,
    private var pvpBanned: Boolean,
    var wins: Int,
    var losses: Int,
    private val parents: MutableList<String>,
    private var children: MutableList<String>,
    private val configFile: File,
) {
    fun getUUID(): UUID = uuid

    fun togglePvpBan(): Boolean {
        pvpBanned = !pvpBanned
        savePlayer()
        return pvpBanned
    }

    fun toggleArenaBan(): Boolean {
        arenaBanned = !arenaBanned
        savePlayer()
        return arenaBanned
    }

    fun savePlayer() {
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(configFile)
        config.set("name", name)
        config.set("race", race)
        config.set("married", marriedName)
        config.set("uuid", uuid.toString())
        config.set("elo", elo)
        config.set("arenabanned", arenaBanned)
        config.set("pvpbanned", pvpBanned)
        config.set("wins", wins)
        config.set("losses", losses)
        config.set("parents", parents)
        config.set("children", children)

        try {
            config.save(configFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun divorce() {
        marriedName = null
        savePlayer()
    }

    fun disown(name: String){
        children.remove(name)
        parents.remove(name)
        savePlayer()
    }

    fun addChild(name: String){
        children.add(name)
        savePlayer()
    }

    fun addParent(name: String){
        parents.add(name)
        savePlayer()
    }

    fun marry(name: String) {
        marriedName = name
        savePlayer()
    }

    fun getMarriedName(): String? = marriedName

    fun isMarried(): Boolean = marriedName != null

    fun getPartner(): String? = marriedName

    fun setFaction(faction: String) {
        this.faction = faction
        savePlayer()
    }

    fun setRace(race: String) {
        this.race = race
        savePlayer()
    }

    fun addWin() {
        wins++
        savePlayer()
    }

    fun addLoss() {
        losses++
        savePlayer()
    }
}