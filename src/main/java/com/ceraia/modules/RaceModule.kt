package com.ceraia.modules

import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.PaginatedGui
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.util.StringUtil
import java.io.File
import java.util.*

class RaceModule(private val plugin: com.ceraia.Ceraia) : CommandExecutor, TabCompleter, Listener {
    private var races: MutableList<Race> = ArrayList()
    private var raceFactions: MutableList<RaceFaction> = ArrayList()


    init {
        plugin.getCommand("race")?.setExecutor(this)
        plugin.getCommand("race")?.tabCompleter = this
        Bukkit.getPluginManager().registerEvents(this, plugin)

        loadRaces()
        loadRaceFactions()
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            return true
        }

        if (!racesEnabled()) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Races are not enabled in the config!"))
            return true
        }

        if (args.isEmpty()) {
            openFactionGui(sender)
            return true
        }

        when (args[0]) {
            "reload" -> {
                if (!sender.hasPermission("ceraia.races.reload")) {
                    plugin.noPermission(sender)
                    return true
                }
                reloadRaces()
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Reloading races..."))
                return true
            }

            "become" -> {
                if (!sender.hasPermission("ceraia.races.become")) {
                    plugin.noPermission(sender)
                    return true
                }

                if (args.size < 2) {
                    sender.sendMessage(
                        MiniMessage.miniMessage().deserialize("<red>Please specify what race you want to become.")
                    )
                    return true
                }
                if (args.size > 2) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Too many arguments."))
                    return true
                }
                // Find a race with the name
                var selectedRace: Race? = null
                for (race in races) {
                    if (race.name == args[1]) {
                        selectedRace = race
                    }
                }

                if (selectedRace == null) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Race not found!"))
                    return true
                } else {
                    if (!sender.hasPermission("ceraia.races.become." + args[1]) &&
                        !sender.hasPermission("ceraia.races.become.*")
                    ) {
                        sender.sendMessage(
                            MiniMessage.miniMessage().deserialize("<red>You do not have permission to become this race")
                        )
                        return true
                    }
                    selectedRace.apply(sender)
                    sender.sendMessage(
                        MiniMessage.miniMessage().deserialize(
                            "<green>Succesfully changed your race to a <white>" + selectedRace.name
                        )
                    )
                    return true
                }
            }

            "gui" -> openFactionGui(sender)
            "restore" -> {
                if (!sender.hasPermission("ceraia.races.restore")) {
                    plugin.noPermission(sender)
                    return true
                }
                // Restore all races
                sender.sendMessage(
                    MiniMessage.miniMessage().deserialize(
                        "<green>Restoring all default races & factions..."
                    )
                )
                addDefaultRaces()
                addDefaultFactions()
                return true
            }

            "faction" -> {
                openFactionGui(sender)
                return true
            }
        }

        openFactionGui(sender)
        return true
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, label: String, args: Array<String>): List<String> {
        // Return a string list of all races
        val tabOptions: MutableList<String> = ArrayList()
        if (args.size == 1) {
            tabOptions.add("reload")
            tabOptions.add("become")
            tabOptions.add("gui")
        } else if (args.size == 2) {
            if (args[0].equals("become", ignoreCase = true)) {
                for (race in races) {
                    tabOptions.add(race.name)
                }
            }
        } else if (args.size == 3) {
            if (args[0].equals("become", ignoreCase = true)) {
                tabOptions.add("reload")
                tabOptions.add("become")
            }
        }
        val returnedOptions: MutableList<String> = ArrayList()
        StringUtil.copyPartialMatches(args[args.size - 1], tabOptions, returnedOptions)

        return returnedOptions
    }

    private fun racesEnabled(): Boolean {
        return plugin.config.getBoolean("races-enabled", true)
    }

    private fun reloadRaces() {
        loadRaces(true)
    }

    private fun addDefaultRaces() {
        run {
            races.add(
                Race(
                    "Halfling",  // Name
                    0.54,  // Scale
                    0.12,  // Speed
                    14,  // Health
                    0.42,  // Jumpheight
                    0.95,  // Damage
                    2.5,  // Reach
                    4.5,  // Attack Speed
                    "<gray>Nimble and stealthy,<newline><green>Halflings<gray> excel in evading danger.",  // Lore
                    1.0,  // Fall Damage Multiplier
                    5.2,  // Mining Efficiency
                    0.0,  // Armor
                    ItemStack(Material.POTATO) // Item
                )
            )
            races.add(
                Race(
                    "Gnome",  // Name
                    0.7,  // Scale
                    0.11,  // Speed
                    16,  // Health
                    0.42,  // Jumpheight
                    0.9,  // Damage
                    3.0,  // Reach
                    5.0,  // Attack Speed
                    "<gray>Clever and elusive,<newline><green>Gnomes<gray> use their fast attack to outwit foes.",  // Lore
                    1.0,  // Fall Damage Multiplier
                    6.65,  // Mining Efficiency
                    0.0,  // Armor
                    ItemStack(Material.RED_MUSHROOM) // Item
                )
            )

            races.add(
                Race(
                    "Dwarf",  // Name
                    0.9,  // Scale
                    0.1,  // Speed
                    24,  // Health
                    0.42,  // Jumpheight
                    1.0,  // Damage
                    4.5,  // Reach
                    4.0,  // Attack Speed
                    "<gray>Sturdy and relentless,<newline><green>Dwarves<gray> are master miners and warriors.",  // Lore
                    1.0,  // Fall Damage Multiplier
                    9.95,  // Mining Efficiency
                    2.0,  // Armor
                    ItemStack(Material.IRON_ORE) // Item
                )
            )

            races.add(
                Race(
                    "Short Human",  // Name
                    0.95,  // Scale
                    0.1,  // Speed
                    20,  // Health
                    0.42,  // Jumpheight
                    1.0,  // Damage
                    5.0,  // Reach
                    4.0,  // Attack Speed
                    "<gray>Balanced and adaptable,<newline><green>Humans<gray> thrive in any environment.",  // Lore
                    1.0,  // Fall Damage Multiplier
                    0.0,  // Mining Efficiency
                    0.0,  // Armor
                    ItemStack(Material.BREAD) // Item
                )
            )

            races.add(
                Race(
                    "Human",  // Name
                    1.0,  // Scale
                    0.1,  // Speed
                    20,  // Health
                    0.42,  // Jumpheight
                    1.0,  // Damage
                    5.0,  // Reach
                    4.0,  // Attack Speed
                    "<gray>Balanced and adaptable,<newline><green>Humans<gray> thrive in any environment.",  // Lore
                    1.0,  // Fall Damage Multiplier
                    0.0,  // Mining Efficiency
                    0.0,  // Armor
                    ItemStack(Material.BREAD) // Item
                )
            )

            races.add(
                Race(
                    "Tall Human",  // Name
                    1.05,  // Scale
                    0.1,  // Speed
                    20,  // Health
                    0.42,  // Jumpheight
                    1.0,  // Damage
                    5.0,  // Reach
                    4.0,  // Attack Speed
                    "<gray>Balanced and adaptable,<newline><green>Humans<gray> thrive in any environment.",  // Lore
                    1.0,  // Fall Damage Multiplier
                    0.0,  // Mining Efficiency
                    0.0,  // Armor
                    ItemStack(Material.BREAD) // Item
                )
            )

            races.add(
                Race(
                    "Satyr",  // Name
                    1.05,  // Scale
                    0.13,  // Speed
                    18,  // Health
                    0.52,  // Jumpheight
                    1.0,  // Damage
                    5.0,  // Reach
                    3.9,  // Attack Speed
                    "<gray>Fast and intelligent<newline><green>Satyrs<gray> are expert explorers and have seen far across the world.",  // Lore
                    1.0,  // Fall Damage Multiplier
                    0.0,  // Mining Efficiency
                    0.0,  // Armor
                    ItemStack(Material.RABBIT_FOOT) // Item
                )
            )

            races.add(
                Race(
                    "Githyanki",  // Name
                    1.07,  // Scale
                    0.975,  // Speed
                    22,  // Health
                    0.42,  // Jumpheight
                    1.0,  // Damage
                    5.0,  // Reach
                    4.2,  // Attack Speed
                    "<gray>Fierce and fast,<newline><green>Githyanki<gray> are veteran warriors.",  // Lore
                    1.0,  // Fall Damage Multiplier
                    0.0,  // Mining Efficiency
                    0.0,  // Armor
                    ItemStack(Material.NETHER_STAR) // Item
                )
            )

            races.add(
                Race(
                    "Half-Elf",  // Name
                    1.11,  // Scale
                    0.0975,  // Speed
                    24,  // Health
                    0.52,  // Jumpheight
                    1.0,  // Damage
                    5.0,  // Reach
                    3.95,  // Attack Speed
                    "<gray>Graceful but adaptable,<newline><green>Half-elves<gray> are the result of a Elven - Human relationship.",  // Lore
                    0.925,  // Fall Damage Multiplier
                    1.0,  // Mining Efficiency
                    1.0,  // Armor
                    ItemStack(Material.APPLE) // Item
                )
            )

            races.add(
                Race(
                    "Elf",  // Name
                    1.11,  // Scale
                    0.095,  // Speed
                    26,  // Health
                    0.63,  // Jumpheight
                    1.0,  // Damage
                    5.0,  // Reach
                    3.9,  // Attack Speed
                    "<gray>Graceful and wise,<newline><green>Elves<gray> are good fighters and excel in archery.",  // Lore
                    0.75,  // Fall Damage Multiplier
                    1.0,  // Mining Efficiency
                    2.0,  // Armor
                    ItemStack(Material.BOW) // Item
                )
            )

            races.add(
                Race(
                    "Half-Orc",  // Name
                    1.15,  // Scale
                    0.0925,  // Speed
                    26,  // Health
                    0.63,  // Jumpheight
                    1.0,  // Damage
                    5.5,  // Reach
                    3.8,  // Attack Speed
                    "<gray>Strong and fierce,<newline><green>Half-orcs<gray> are the result of a Orc - Human relationship.",  // Lore
                    0.75,  // Fall Damage Multiplier
                    2.0,  // Mining Efficiency
                    3.0,  // Armor
                    ItemStack(Material.PORKCHOP) // Item
                )
            )

            races.add(
                Race(
                    "Bugbear",  // Name
                    1.22,  // Scale
                    0.09,  // Speed
                    28,  // Health
                    0.63,  // Jumpheight
                    1.25,  // Damage
                    5.8,  // Reach
                    3.0,  // Attack Speed
                    "<gray>Fierce and powerful,<newline><green>Bugbears<gray> dominate in brute strength.",  // Lore
                    0.75,  // Fall Damage Multiplier
                    2.0,  // Mining Efficiency
                    4.0,  // Armor
                    ItemStack(Material.BEEF) // Item
                )
            )
            races.add(
                Race(
                    "Goliath",  // Name
                    1.33,  // Scale
                    0.08,  // Speed
                    30,  // Health
                    0.63,  // Jumpheight
                    1.25,  // Damage
                    6.1,  // Reach
                    3.0,  // Attack Speed
                    "<gray>Large and strong,<newline><green>Goliaths<gray> are often used in heavy labour.",  // Lore
                    0.75,  // Fall Damage Multiplier
                    0.0,  // Mining Efficiency
                    5.0,  // Armor
                    ItemStack(Material.OAK_SAPLING) // Item
                )
            )
        }
        saveAllRaces()
        loadRaces()
    }

    private fun addDefaultFactions() {
        run {
            raceFactions.add(
                RaceFaction(
                    "<yellow>Unaligned",
                    "<yellow>The Unaligned<gray>, formed by those with no king, a loose alliance.",
                    ItemStack(Material.IRON_ORE),
                    0,
                    0.0,
                    0.0,
                    0.0,
                    listOf(
                        "*"
                    )
                )
            )
            raceFactions.add(
                RaceFaction(
                    "<red>Gnomish Dynasty",
                    "<red>The Gnomish Dynasty<gray>, formed by the Gnomish, a monarchy.",
                    ItemStack(Material.RED_MUSHROOM),
                    0,
                    0.0,
                    0.0,
                    0.0,
                    mutableListOf("Gnome", "Satyr", "Githyanki", "Bugbear")
                )
            )
            raceFactions.add(
                RaceFaction(
                    "<blue>Republic of Man",
                    "<blue>The Republic of Man<gray>, formed by the Humans, a democratic republic.",
                    ItemStack(Material.BREAD),
                    0,
                    0.0,
                    0.0,
                    0.0,
                    mutableListOf("Dwarf", "Short Human", "Human", "Tall Human", "Half-Elf", "Half-Orc")
                )
            )
            raceFactions.add(
                RaceFaction(
                    "<green>Elven Demagogue",
                    "<green>The Elven Demagogue<gray>, formed by the Elves, a theocracy.",
                    ItemStack(Material.PORKCHOP),
                    0,
                    0.0,
                    0.0,
                    0.0,
                    mutableListOf("Halfling", "Half-Elf", "Elf", "Goliath")
                )
            )
        }
        saveAllFactions()
        loadRaceFactions()
    }

    @JvmOverloads
    fun loadRaces(reload: Boolean = false) {
        races.clear()
        if (reload) plugin.logger.info("Reloading races...")
        else plugin.logger.info("Loading races...")

        // Load all races from the races.yml file
        val file = File(plugin.dataFolder, "races.yml")
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        val racesSection = config.getConfigurationSection("races")
        if (racesSection == null) {
            plugin.logger.warning("No races found in races.yml! Adding default races.")
            addDefaultRaces()
            return
        }

        for (raceName in racesSection.getKeys(false)) {
            val path = "races.$raceName"
            val race = Race(
                raceName,
                config.getDouble("$path.scale", 1.0),
                config.getDouble("$path.speed", 0.1),
                config.getInt("$path.health", 20),
                config.getDouble("$path.jumpheight", 0.42),
                config.getDouble("$path.damage", 1.0),
                config.getDouble("$path.reach", 5.0),
                config.getDouble("$path.attackspeed", 4.0),
                config.getString("$path.lore", "<gray>No known lore...")!!,
                config.getDouble("$path.falldamagemultiplier", 1.0),
                config.getDouble("$path.miningefficiency", 0.0),
                config.getDouble("$path.armor", 0.0),
                config.getItemStack("$path.item", ItemStack(Material.BREAD))!!
            )
            races.add(race)
        }
    }

    @JvmOverloads
    fun loadRaceFactions(reload: Boolean = false) {
        raceFactions.clear()
        if (reload) plugin.logger.info("Reloading race factions...")
        else plugin.logger.info("Loading race factions...")

        // Load all factions from the races.yml file
        val file = File(plugin.dataFolder, "races.yml")
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        val factionsSection = config.getConfigurationSection("factions")
        if (factionsSection == null) {
            plugin.logger.warning("No factions found in races.yml! Adding default factions.")
            addDefaultFactions()
            return
        }

        for (factionName in factionsSection.getKeys(false)) {
            val path = "factions.$factionName"
            val faction = RaceFaction(
                factionName,
                config.getString("$path.lore", "<gray>No known lore...")!!,
                config.getItemStack("$path.item", ItemStack(Material.BREAD))!!,
                config.getInt("$path.health", 0),
                config.getDouble("$path.damage", 0.0),
                config.getDouble("$path.miningefficiency", 0.0),
                config.getDouble("$path.armor", 0.0),
                config.getStringList("$path.raceInhabitants")
            )
            raceFactions.add(faction)
        }
    }
    
    private fun openRaceGui(player: Player, faction: RaceFaction) {
        val gui : PaginatedGui = Gui.paginated()
            .title(Component.text("Select a race to become"))
            .disableAllInteractions()
            .rows(4)
            .create()

        gui.setItem(4, 3, ItemBuilder.from(Material.PAPER).name(Component.text("Previous")).asGuiItem { gui.previous() })
        gui.setItem(4, 7, ItemBuilder.from(Material.PAPER).name(Component.text("Next")).asGuiItem { gui.next() })

        val selectable: MutableList<Race> = ArrayList()
        for (race in races) {
            if ((player.hasPermission("ceraia.races.become." + race.name) ||
                        player.hasPermission("ceraia.races.become.*")) &&
                (faction.getRaceInhabitants().contains(race.name)
                        || faction.getRaceInhabitants().contains("*"))
            ) selectable.add(race)
        }

        selectable.forEach { race ->
            val lore: MutableList<Component> = ArrayList()

            lore.add(MiniMessage.miniMessage().deserialize("<gray>Scale : <green>" + race.scale))
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Speed : <green>" + race.speed))
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Health : <green>" + race.health))
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Jump Height : <green>" + race.jumpHeight))
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Damage : <green>" + race.damage))
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Reach : <green>" + race.reach))
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Attack Speed : <green>" + race.attackSpeed))
            lore.add(
                MiniMessage.miniMessage()
                    .deserialize("<gray>Fall Damage Multiplier : <green>" + race.fallDamageMultiplier)
            )
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Mining Efficiency : <green>" + race.miningEfficiency))
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Armor : <green>" + race.armor))

            gui.addItem(
                ItemBuilder.from(race.item)
                    .lore(lore as List<Component?>)
                    .name(MiniMessage.miniMessage().deserialize(race.name))
                    .asGuiItem {
                        plugin.playerManager.getCeraiaPlayer(player).setRace(race.name)
                        plugin.playerManager.getCeraiaPlayer(player).setFaction(faction.name)
                        faction.apply(player, race)

                        player.sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                "<green>Succesfully changed your faction to <white>" +
                                        faction.name +
                                        "<green>, and your race to " +
                                        race.name + "<green>!"
                            )
                        )
                        gui.close(player)
                    }
            )

        }

        gui.open(player)
}

    private fun openFactionGui(player: Player){
        val gui : PaginatedGui = Gui.paginated()
            .title(Component.text("Select a faction to join"))
            .disableAllInteractions()
            .rows(4)
            .create()

        gui.setItem(4, 3, ItemBuilder.from(Material.PAPER).name(Component.text("Previous")).asGuiItem { gui.previous() })
        gui.setItem(4, 7, ItemBuilder.from(Material.PAPER).name(Component.text("Next")).asGuiItem { gui.next() })

        val selectable: List<RaceFaction> = ArrayList(raceFactions)

        selectable.forEach { raceFaction ->
            val lore: MutableList<Component> = ArrayList()
            lore.add(MiniMessage.miniMessage().deserialize(raceFaction.lore))
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Health Bonus : <green>" + raceFaction.health))
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Damage Bonus : <green>" + raceFaction.damage))
            lore.add(
                MiniMessage.miniMessage()
                    .deserialize("<gray>Mining Efficiency Bonus : <green>" + raceFaction.miningEfficiency)
            )
            lore.add(MiniMessage.miniMessage().deserialize("<gray>Armor Bonus : <green>" + raceFaction.armor))

            gui.addItem(
                ItemBuilder.from(raceFaction.item)
                    .lore(lore as List<Component?>)
                    .name(MiniMessage.miniMessage().deserialize(raceFaction.name))
                    .asGuiItem {
                        openRaceGui(player, raceFaction)
                    })
        }

        gui.open(player)
    }

    private fun saveAllRaces() {
        // Save the races to the races.yml file
        val file = File(plugin.dataFolder, "races.yml")
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)

        for (race in races) {
            val path = "races." + race.name
            config["$path.lore"] = race.lore
            config["$path.scale"] = race.scale
            config["$path.speed"] = race.speed
            config["$path.health"] = race.health
            config["$path.jumpheight"] = race.jumpHeight
            config["$path.damage"] = race.damage
            config["$path.reach"] = race.reach
            config["$path.attackspeed"] = race.attackSpeed
            config["$path.item"] = race.item
            config["$path.falldamagemultiplier"] = race.fallDamageMultiplier
            config["$path.miningefficiency"] = race.miningEfficiency
            config["$path.armor"] = race.armor
        }

        try {
            config.save(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveAllFactions() {
        // Save the factions to the races.yml file
        val file = File(plugin.dataFolder, "races.yml")
        val config: FileConfiguration = YamlConfiguration.loadConfiguration(file)

        for (faction in raceFactions) {
            val path = "factions." + faction.name
            config["$path.lore"] = faction.lore
            config["$path.item"] = faction.item
            config["$path.health"] = faction.health
            config["$path.damage"] = faction.damage
            config["$path.miningefficiency"] = faction.miningEfficiency
            config["$path.armor"] = faction.armor
            config["$path.raceInhabitants"] = faction.getRaceInhabitants()
        }

        try {
            config.save(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class Race(
        val name: String,
        val scale: Double,
        val speed: Double,
        val health: Int,
        val jumpHeight: Double,
        val damage: Double,
        val reach: Double,
        val attackSpeed: Double,
        val lore: String,
        val fallDamageMultiplier: Double,
        val miningEfficiency: Double,
        val armor: Double,
        val item: ItemStack
    ) {
        fun apply(player: Player) {
            player.getAttribute(Attribute.SCALE)?.baseValue =
                scale
            player.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue =
                speed
            player.getAttribute(Attribute.MAX_HEALTH)?.baseValue =
                health.toDouble()
            player.getAttribute(Attribute.JUMP_STRENGTH)?.baseValue =
                jumpHeight
            player.getAttribute(Attribute.SAFE_FALL_DISTANCE)?.baseValue =
                jumpHeight * 7.145
            player.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue =
                damage
            player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)?.baseValue =
                reach
            player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)?.baseValue =
                reach
            player.getAttribute(Attribute.ATTACK_SPEED)?.baseValue =
                attackSpeed
            player.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER)?.baseValue =
                fallDamageMultiplier
            player.getAttribute(Attribute.MINING_EFFICIENCY)?.baseValue =
                miningEfficiency
            player.getAttribute(Attribute.ARMOR)?.baseValue =
                armor
        }
    }

    class RaceFaction(
        val name: String,
        val lore: String,
        val item: ItemStack,
        val health: Int,
        val damage: Double,
        val miningEfficiency: Double,
        val armor: Double,
        raceInhabitants: List<String>
    ) {
        private val raceInhabitants: MutableList<String> = ArrayList()

        init {
            this.raceInhabitants.addAll(raceInhabitants)
        }

        fun getRaceInhabitants(): List<String> {
            return raceInhabitants
        }

        fun apply(player: Player, race: Race) {
            player.getAttribute(Attribute.SCALE)?.baseValue =
                race.scale
            player.getAttribute(Attribute.MOVEMENT_SPEED)?.baseValue =
                race.speed
            player.getAttribute(Attribute.MAX_HEALTH)?.baseValue =
                (health + race.health).toDouble()
            player.getAttribute(Attribute.JUMP_STRENGTH)?.baseValue =
                race.jumpHeight
            player.getAttribute(Attribute.SAFE_FALL_DISTANCE)?.baseValue =
                race.jumpHeight * 7.145
            player.getAttribute(Attribute.ATTACK_DAMAGE)?.baseValue =
                damage + race.damage
            player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)?.baseValue =
                race.reach
            player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)?.baseValue =
                race.reach
            player.getAttribute(Attribute.ATTACK_SPEED)?.baseValue =
                race.attackSpeed
            player.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER)?.baseValue =
                race.fallDamageMultiplier
            player.getAttribute(Attribute.MINING_EFFICIENCY)?.baseValue =
                miningEfficiency + race.miningEfficiency
            player.getAttribute(Attribute.ARMOR)?.baseValue =
                armor + race.armor
        }
    }
}
