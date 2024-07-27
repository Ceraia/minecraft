package com.axodouble.modules;

import com.axodouble.Double;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ModuleRaces implements CommandExecutor, TabCompleter, Listener {

    private final Double plugin;
    public List<Race> races;
    public Map<Player, Map<ItemStack, Race>> playerOpenGuis = new HashMap<>();

    public ModuleRaces(Double plugin) {
        this.plugin = plugin;
        this.races = new ArrayList<>();

        Objects.requireNonNull(plugin.getCommand("race")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("race")).setTabCompleter(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);

        loadRaces();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }
        if(args.length == 0) {
            openRaceGUI(player);
            return true;
        }

        switch (args[0]) {
            case "reload" -> {
                if (!sender.hasPermission("double.races.reload")) {
                    this.plugin.noPermission((Player) sender);
                    return true;
                }
                reloadRaces();
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Reloading races..."));
                return true;
            }
            case "become" -> {
                if (!sender.hasPermission("double.races.become")) {
                    this.plugin.noPermission((Player) sender);
                    return true;
                }

                if (args.length < 2) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Please specify what race you want to become."));
                    return true;
                }
                if (args.length > 2) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Too many arguments."));
                    return true;
                }
                // Find a race with the name
                Race selectedRace = null;
                for (Race race : races) {
                    if (Objects.equals(race.getName(), args[1])) {
                        selectedRace = race;
                    }
                }

                if (selectedRace == null) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Race not found!"));
                    return true;
                }
                else {
                    if (!sender.hasPermission("double.races.become." + args[1]) &&
                            !sender.hasPermission("double.races.become.*")) {
                        player.sendMessage(MiniMessage.miniMessage().deserialize("<red>You do not have permission to become this race"));
                        return true;
                    }
                    selectedRace.apply(player);
                    player.sendMessage(
                            MiniMessage.miniMessage().deserialize(
                                    "<green>Succesfully changed your race to a <white>" + selectedRace.getName()
                            ));
                    return true;
                }
            }
            case "gui" -> openRaceGUI(player);
            case "restore" -> {
                if (!sender.hasPermission("double.races.restore")) {
                    this.plugin.noPermission((Player) sender);
                    return true;
                }
                // Restore all races
                player.sendMessage(
                        MiniMessage.miniMessage().deserialize(
                                "<green>Restoring all default races..."
                        ));
                addDefaultRaces();
                return true;
            }
        }

        openRaceGUI(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        // Return a string list of all races
        List<String> tabOptions = new ArrayList<>();
        if (args.length == 1) {
            tabOptions.add("reload");
            tabOptions.add("become");
            tabOptions.add("gui");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("become")) {
                for (Race race : races) {
                    tabOptions.add(race.getName());
                }
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("become")) {
                tabOptions.add("reload");
                tabOptions.add("become");
            }
        }
        List<String> returnedOptions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[args.length - 1], tabOptions, returnedOptions);

        return returnedOptions;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
            return;
        } // If the item doesn't exist or is air, return

        if (Objects.requireNonNull(e.getInventory()).getType() == InventoryType.PLAYER) {
            return;
        } // If the inventory is the player's inventory, return

        Player player = (Player) e.getWhoClicked(); // Get the player who clicked

        if (Objects.equals(e.getView().title().toString(), MiniMessage.miniMessage().deserialize("<green>Select a race to become").toString())) {
            if (playerOpenGuis.containsKey(player)) {
                // Get the relevant slot and race that the player clicked on
                if(playerOpenGuis.get(player).containsKey(e.getCurrentItem())){
                    e.setCancelled(true);
                    playerOpenGuis.get(player).get(e.getCurrentItem()).apply(player);
                }
            }
        }
    }

    public void reloadRaces() {
        races.clear();
        loadRaces(true);
    }

    public void addDefaultRaces() {
        races.clear();
        {
            races.add(new Race(
                    "Halfling", // Name
                    0.54, // Scale
                    0.12, // Speed
                    14, // Health
                    0.42, // Jumpheight
                    0.9, // Damage
                    2.5, // Reach
                    5, // Attack Speed
                    "<gray>Nimble and stealthy,<newline><green>Halflings<gray> excel in evading danger.", // Lore
                    1, // Fall Damage Multiplier
                    5.2, // Mining Efficiency
                    0, // Armor
                    new ItemStack(Material.POTATO) // Item
            ));

            races.add(new Race(
                    "Gnome", // Name
                    0.6, // Scale
                    0.11, // Speed
                    16, // Health
                    0.42, // Jumpheight
                    0.95, // Damage
                    3, // Reach
                    4.5, // Attack Speed
                    "<gray>Clever and elusive,<newline><green>Gnomes<gray> use their fast attack to outwit foes.", // Lore
                    1, // Fall Damage Multiplier
                    6.65, // Mining Efficiency
                    0, // Armor
                    new ItemStack(Material.RED_MUSHROOM) // Item
            ));

            races.add(new Race(
                    "Dwarven", // Name
                    0.9, // Scale
                    0.1, // Speed
                    24, // Health
                    0.42, // Jumpheight
                    1, // Damage
                    4.5, // Reach
                    4, // Attack Speed
                    "<gray>Sturdy and relentless,<newline><green>Dwarves<gray> are master miners and warriors.", // Lore
                    1, // Fall Damage Multiplier
                    9.95, // Mining Efficiency
                    2, // Armor
                    new ItemStack(Material.IRON_ORE) // Item
            ));

            races.add(new Race(
                    "Short Human", // Name
                    0.95, // Scale
                    0.1, // Speed
                    20, // Health
                    0.42, // Jumpheight
                    1, // Damage
                    5, // Reach
                    4, // Attack Speed
                    "<gray>Balanced and adaptable,<newline><green>Humans<gray> thrive in any environment.", // Lore
                    1, // Fall Damage Multiplier
                    0, // Mining Efficiency
                    0, // Armor
                    new ItemStack(Material.BREAD) // Item
            ));

            races.add(new Race(
                    "Human", // Name
                    1, // Scale
                    0.1, // Speed
                    20, // Health
                    0.42, // Jumpheight
                    1, // Damage
                    5, // Reach
                    4, // Attack Speed
                    "<gray>Balanced and adaptable,<newline><green>Humans<gray> thrive in any environment.", // Lore
                    1, // Fall Damage Multiplier
                    0, // Mining Efficiency
                    0, // Armor
                    new ItemStack(Material.BREAD) // Item
            ));

            races.add(new Race(
                    "Tall Human", // Name
                    1.05, // Scale
                    0.1, // Speed
                    20, // Health
                    0.42, // Jumpheight
                    1, // Damage
                    5, // Reach
                    4, // Attack Speed
                    "<gray>Balanced and adaptable,<newline><green>Humans<gray> thrive in any environment.", // Lore
                    1, // Fall Damage Multiplier
                    0, // Mining Efficiency
                    0, // Armor
                    new ItemStack(Material.BREAD) // Item
            ));

            races.add(new Race(
                    "Half-Elf", // Name
                    1.11, // Scale
                    0.0975, // Speed
                    24, // Health
                    0.52, // Jumpheight
                    1, // Damage
                    5, // Reach
                    3.95, // Attack Speed
                    "<gray>Graceful but adaptable,<newline><green>Half-elves<gray> are the result of a Elven - Human relationship.", // Lore
                    0.925, // Fall Damage Multiplier
                    0, // Mining Efficiency
                    1, // Armor
                    new ItemStack(Material.APPLE) // Item
            ));

            races.add(new Race(
                    "Elven", // Name
                    1.11, // Scale
                    0.095, // Speed
                    26, // Health
                    0.63, // Jumpheight
                    1, // Damage
                    5, // Reach
                    3.9, // Attack Speed
                    "<gray>Graceful and wise,<newline><green>Elves<gray> are good fighters and excel in archery.", // Lore
                    0.75, // Fall Damage Multiplier
                    0, // Mining Efficiency
                    2, // Armor
                    new ItemStack(Material.BOW) // Item
            ));

            races.add(new Race(
                    "Half-Orc", // Name
                    1.15, // Scale
                    0.0925, // Speed
                    26, // Health
                    0.63, // Jumpheight
                    1, // Damage
                    5.5, // Reach
                    3.8, // Attack Speed
                    "<gray>Strong and fierce,<newline><green>Half-orcs<gray> are the result of a Orc - Human relationship.", // Lore
                    0.75, // Fall Damage Multiplier
                    0, // Mining Efficiency
                    3, // Armor
                    new ItemStack(Material.PORKCHOP) // Item
            ));

            races.add(new Race(
                    "Bugbear", // Name
                    1.33, // Scale
                    0.09, // Speed
                    28, // Health
                    0.63, // Jumpheight
                    1.25, // Damage
                    6.1, // Reach
                    3, // Attack Speed
                    "<gray>Fierce and powerful,<newline><green>Bugbears<gray> dominate in brute strength.", // Lore
                    0.75, // Fall Damage Multiplier
                    0, // Mining Efficiency
                    4, // Armor
                    new ItemStack(Material.BEEF) // Item
            ));
            saveAllRaces();
        }

    }

    public void loadRaces(boolean reload) {
        if(reload) plugin.getLogger().info("Reloading races...");
        else plugin.getLogger().info("Loading races...");

        // Load all races from the races.yml file
        File file = new File(plugin.getDataFolder(), "races.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection racesSection = config.getConfigurationSection("races");
        if (racesSection == null) {
            plugin.getLogger().warning("No races found in races.yml! Adding default races.");
            addDefaultRaces();

            return;
        }

        for (String raceName : racesSection.getKeys(false)) {
            String path = "races." + raceName;
            Race race = new Race(
                    raceName,
                    config.getDouble(path + ".scale", 1),
                    config.getDouble(path + ".speed", 0.1),
                    config.getInt(path + ".health", 20),
                    config.getDouble(path + ".jumpheight", 0.42),
                    config.getDouble(path + ".damage", 1),
                    config.getDouble(path + ".reach", 5),
                    config.getDouble(path + ".attackspeed", 4),
                    config.getString(path + ".lore", "<gray>No known lore..."),
                    config.getDouble(path + ".falldamagemultiplier", 1),
                    config.getDouble(path + ".miningefficiency", 0),
                    config.getDouble(path + ".armor", 0),
                    config.getItemStack(path + ".item", new ItemStack(Material.BREAD))
            );
            races.add(race);
        }
    }
    public void loadRaces(){
        loadRaces(false);
    }

    private void openRaceGUI(Player player) {
        {
            if (!player.hasPermission("double.races.become")) {
                this.plugin.noPermission(player);
                return;
            }
            List<Race> selectable = new ArrayList<>();
            for (Race race : races) {
                if(player.hasPermission("double.races.become." + race.getName()) ||
                        player.hasPermission("double.races.become.*")) selectable.add(race);
            }

            // Open a GUI with all selectable races
            int size = Math.max(9, (selectable.size() + 8) / 9 * 9);
            Inventory inv = Bukkit.createInventory(null, size, MiniMessage.miniMessage().deserialize("<green>Select a race to become"));

            Map<ItemStack, Race> raceSelectSlots = new HashMap<>();

            AtomicInteger i = new AtomicInteger(); // Slot
            selectable.forEach(race -> {
                ItemStack itemStack = race.getItem(); // Create the itemstack
                ItemMeta meta = itemStack.getItemMeta();
                meta.displayName(
                        MiniMessage.miniMessage().deserialize("<green>" +race.getName() + "</green>")
                );

                List<Component> lore = new ArrayList<>();

                Arrays.stream(race.getLore().split("<newline>")).toList().forEach(s -> lore.add(MiniMessage.miniMessage().deserialize(s)));
                //lore.add(MiniMessage.miniMessage().deserialize(race.getLore()));
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Scale : <green>" + race.getScale()));
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Speed : <green>" + race.getSpeed()));
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Health : <green>" + race.getHealth()));
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Jump Height : <green>" + race.getJumpHeight()));
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Damage : <green>"+race.getDamage()));
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Reach : <green>"+race.getReach()));
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Attack Speed : <green>"+race.getAttackSpeed()));
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Fall Damage Multiplier : <green>"+race.getFallDamageMultiplier()));
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Mining Efficiency : <green>"+race.getMiningEfficiency()));
                lore.add(MiniMessage.miniMessage().deserialize("<gray>Armor : <green>"+race.getArmor()));

                meta.lore(lore);

                itemStack.setItemMeta(meta);
                itemStack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

                inv.setItem(i.get(), itemStack);
                raceSelectSlots.put(itemStack, race);
                i.getAndIncrement();
            });
            playerOpenGuis.put(player, raceSelectSlots);
            player.openInventory(inv);
        }
    }

    public void saveAllRaces() {
        // Save the races to the races.yml file
        File file = new File(plugin.getDataFolder(), "races.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (Race race : races) {
            String path = "races." + race.getName();
            config.set(path + ".lore", race.getLore());
            config.set(path + ".scale", race.getScale());
            config.set(path + ".speed", race.getSpeed());
            config.set(path + ".health", race.getHealth());
            config.set(path + ".jumpheight", race.getJumpHeight());
            config.set(path + ".damage", race.getDamage());
            config.set(path + ".reach", race.getReach());
            config.set(path + ".attackspeed", race.getAttackSpeed());
            config.set(path + ".item", race.getItem());
            config.set(path + ".falldamagemultiplier", race.getFallDamageMultiplier());
            config.set(path + ".miningefficiency", race.getMiningEfficiency());
            config.set(path + ".armor", race.getArmor());
        }

        try {
            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Race {
        private final String name;
        private final double scale;
        private final double speed;
        private final int health;
        private final double jumpHeight;
        private final double damage;
        private final double reach;
        private final double attackSpeed;
        private final String lore;
        private final double fallDamageMultiplier ;
        private final double miningEfficiency ;
        private final double armor ;
        private final ItemStack item;

        public Race(String name,
                    double scale,
                    double speed,
                    int health,
                    double jumpHeight,
                    double damage,
                    double reach,
                    double attackSpeed,
                    String lore,
                    double fallDamageMultiplier,
                    double miningEfficiency,
                    double armor,
                    ItemStack item
        ) {
            this.name = name;
            this.scale = scale;
            this.speed = speed;
            this.health = health;
            this.jumpHeight = jumpHeight;
            this.damage = damage;
            this.reach = reach;
            this.attackSpeed = attackSpeed;
            this.lore = lore;
            this.fallDamageMultiplier = fallDamageMultiplier;
            this.miningEfficiency = miningEfficiency;
            this.armor = armor;
            this.item = item;
        }

        public String getName() {
            return name;
        }

        public double getScale() {
            return scale;
        }

        public double getSpeed() {
            return speed;
        }

        public int getHealth() {
            return health;
        }

        public double getJumpHeight() {
            return jumpHeight;
        }

        public double getDamage() {
            return damage;
        }

        public double getReach() {
            return reach;
        }

        public double getAttackSpeed() {
            return attackSpeed;
        }

        public ItemStack getItem() {
            return item;
        }

        public double getFallDamageMultiplier() {
            return fallDamageMultiplier;
        }

        public double getMiningEfficiency() {
            return miningEfficiency;
        }

        public double getArmor() {
            return armor;
        }

        public String getLore() {
            return lore;
        }

        public void apply(Player player) {
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_SCALE)).setBaseValue(scale);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(speed);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(health);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)).setBaseValue(jumpHeight);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE)).setBaseValue(jumpHeight * 7.145);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(damage);
            Objects.requireNonNull(player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE)).setBaseValue(reach);
            Objects.requireNonNull(player.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)).setBaseValue(reach);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(attackSpeed);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_FALL_DAMAGE_MULTIPLIER)).setBaseValue(fallDamageMultiplier);
            Objects.requireNonNull(player.getAttribute(Attribute.PLAYER_MINING_EFFICIENCY)).setBaseValue(miningEfficiency);
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ARMOR)).setBaseValue(armor);

        }
    }
}
