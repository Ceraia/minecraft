package com.ceraia.modules.arenas.types;

import com.ceraia.Ceraia;
import com.ceraia.modules.arenas.managers.InviteManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ArenaSelectGUI implements Listener {
    private static Component INVENTORY_NAME_ARENAS;
    private static Component INVENTORY_NAME_TOTEMS;
    private final Ceraia plugin;
    private final Map<Player, Map<Integer, Arena>> selectingArenaCache = new HashMap<>();

    public ArenaSelectGUI(Ceraia plugin) {
        this.plugin = plugin;

        INVENTORY_NAME_ARENAS = MiniMessage.miniMessage().deserialize("Quick Text Arena Title");
        INVENTORY_NAME_TOTEMS = MiniMessage.miniMessage().deserialize("Quick Text Totem Title");

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openArenaList(Player inviter, Player invited) { // Let the player select what arena to fight in
        // Add the player invitee and inviter
        InviteManager.Invite invite = new InviteManager.Invite(inviter, invited);

        plugin.getArenaModule().getInviteManager().selectingInvites.put(inviter, invite);

        // Get a list of all arenas accessible to the player
        List<Arena> arenas = plugin.getArenaModule().getArenaManager().getArenas()
                .stream().filter(a -> a.isPublic() || a.getOwner().equals(inviter.getName())).toList();

        // Size is from arenas.size() and must be devidable by 9
        int size = Math.max(9, (arenas.size() + 8) / 9 * 9);

        // Create the inventory
        Inventory inv = Bukkit.createInventory(null, size, MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.arena_select_gui.inventory_name"))));

        // Create a map of the slot and the arena
        Map<Integer, Arena> arenasSelectSlots = new HashMap<>();

        int i = 0; // Slot
        for (Arena a : arenas.stream().filter(a -> a.getState() == Arena.ArenaState.WAITING).toList()) { // Filter out arenas that are not ready
            ItemStack itemStack = new ItemStack(Objects.requireNonNull(Material.getMaterial(
                    Objects.requireNonNull(plugin.getConfig().getString("messages.arena_select_gui.arena_item.item"))
            ))); // Create the itemstack
            ItemMeta meta = itemStack.getItemMeta();
            meta.displayName(
                    MiniMessage.miniMessage().deserialize(
                    Objects.requireNonNull(plugin.getConfig().getString("messages.arena_select_gui.arena_item.name"))
                    .replace("%arena_name%", a.getName())
                    .replace("%arena_owner%", a.getOwner())
                    )
            );

            List<Component> lore = new ArrayList<>();
            for (String s : plugin.getConfig().getStringList("messages.arena_select_gui.arena_item.lore")) {
                lore.add(MiniMessage.miniMessage().deserialize(s.replace("%arena_name%", a.getName())
                        .replace("%arena_owner%", a.getOwner())
                        .replace("%totems%", a.totems ? "<green>enabled</green>" : "<red>disabled</red>")
                ));
            }

            meta.lore(lore);

            itemStack.setItemMeta(meta);

            inv.setItem(i, itemStack);
            arenasSelectSlots.put(i, a);

            i++;
        }

        // Put the map in the cache
        selectingArenaCache.put(inviter, arenasSelectSlots);

        // Open the inventory
        inviter.openInventory(inv);
    }

    public void openTotemEnabled(Player inviter, Arena arena){ // Let the player select whether to enable or disable totems in the fight
        int size = 9;

        Inventory inv = Bukkit.createInventory(null, size, MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.totem_select_gui.inventory_name"))));

        ItemStack itemStackEnable = new ItemStack(Objects.requireNonNull(Material.getMaterial(
                Objects.requireNonNull(plugin.getConfig().getString("messages.totem_select_gui.items.enable.item"))
        ))); // Create the itemstack
        ItemMeta metaEnable = itemStackEnable.getItemMeta();

        metaEnable.displayName(
                MiniMessage.miniMessage().deserialize(
                        Objects.requireNonNull(plugin.getConfig().getString("messages.totem_select_gui.items.enable.name"))
                )
        );

        List<Component> loreEnable = new ArrayList<>();
        for (String s : plugin.getConfig().getStringList("messages.totem_select_gui.items.enable.lore")) {
            loreEnable.add(MiniMessage.miniMessage().deserialize(s));
        }

        metaEnable.lore(loreEnable);

        itemStackEnable.setItemMeta(metaEnable);

        inv.setItem(1, itemStackEnable);

        ItemStack itemStackDisable = new ItemStack(Objects.requireNonNull(Material.getMaterial(
                Objects.requireNonNull(plugin.getConfig().getString("messages.totem_select_gui.items.disable.item"))
        ))); // Create the itemstack
        ItemMeta metaDisable = itemStackDisable.getItemMeta();
        metaDisable.displayName(
                MiniMessage.miniMessage().deserialize(
                        Objects.requireNonNull(plugin.getConfig().getString("messages.totem_select_gui.items.disable.name"))
                )
        );

        List<Component> loreDisable = new ArrayList<>();
        for (String s : plugin.getConfig().getStringList("messages.totem_select_gui.items.disable.lore")) {
            loreDisable.add(MiniMessage.miniMessage().deserialize(s));
        }

        metaDisable.lore(loreDisable);

        itemStackDisable.setItemMeta(metaDisable);

        inv.setItem(7, itemStackDisable);

        // Set the center slot to the arena that was selected
        ItemStack itemStackArena = new ItemStack(Objects.requireNonNull(Material.getMaterial(
                Objects.requireNonNull(plugin.getConfig().getString("messages.arena_select_gui.arena_item.item"))
        ))); // Create the itemstack

        ItemMeta metaArena = itemStackArena.getItemMeta();
        metaArena.displayName(
                MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.arena_select_gui.arena_item.name"))
                        .replace("%arena_name%", arena.getName())
                        .replace("%arena_owner%", arena.getOwner()))

        );

        List<Component> loreArena = new ArrayList<>();
        for (String s : plugin.getConfig().getStringList("messages.arena_select_gui.arena_item.lore")) {
            loreArena.add(MiniMessage.miniMessage().deserialize(s.replace("%arena_name%", arena.getName())
                    .replace("%arena_owner%", arena.getOwner())
            ));
        }

        metaArena.lore(loreArena);

        inviter.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) {
            return;
        } // If the item doesn't exist or is air, return

        if (Objects.requireNonNull(e.getInventory()).getType() == InventoryType.PLAYER) {
            return;
        } // If the inventory is the player's inventory, return

        Player inviter = (Player) e.getWhoClicked(); // Get the player who clicked

        if (Objects.equals(e.getView().title().toString(), INVENTORY_NAME_ARENAS.toString())) {
            e.setCancelled(true); // The inventory name is from the plugin, so cancel the event

            int slot = e.getSlot();
            Arena arena = selectingArenaCache.get(inviter).get(slot); // Get the specific player and then the arena from the cache

            InviteManager.Invite invite = plugin.getArenaModule().getInviteManager().selectingInvites.get(inviter); // Get the invite from the selectingInvites map
            invite.arena = arena; // Set the arena in the invite


            if (arena == null || arena.getState() != Arena.ArenaState.WAITING) { // Somehow arena doesn't work, purely debug
                inviter.sendMessage(
                        MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.arena_select_gui.arena_not_ready"))))
                ;
                return;
            }


            openTotemEnabled(inviter, arena);
        }
        if (Objects.equals(e.getView().title().toString(), INVENTORY_NAME_TOTEMS.toString())) {
            e.setCancelled(true); // The inventory name is from the plugin, so cancel the event

            int slot = e.getSlot();

            // Get the invite from the selectingInvites map
            InviteManager.Invite invite = plugin.getArenaModule().getInviteManager().selectingInvites.get(inviter);

            // Get the arena from the invite
            Arena arena = invite.arena;

            // Set the totems in the invite
            arena.totems = slot == 1;

            inviter.sendMessage(
                    MiniMessage.miniMessage().deserialize(Objects.requireNonNull(plugin.getConfig().getString("messages.pvp.invite.invite_sent"))
                            .replace("%player%", invite.invited.getName())))
            ; // Send the invite confirmation message

            String invite_message = Objects.requireNonNull(plugin.getConfig().getString("messages.arena_select_gui.invite_message"))
                    .replace("%inviter%", inviter.getName())
                    .replace("%arena_name%", arena.getName())
                    .replace("%winchance%", plugin.getArenaModule().calculateWinChance(inviter.getUniqueId(), invite.invited.getUniqueId()) + "%")
                    .replace("%totems%", arena.totems ? "<green>enabled</green>" : "<red>disabled</red>");
            // Get the invite message from the config and replace the placeholders

            Objects.requireNonNull(plugin.getServer().getPlayer(invite.invited.getUniqueId())).sendMessage(MiniMessage.miniMessage().deserialize(invite_message));// Send the actual message


            invite.arena = arena;

            plugin.getArenaModule().getInviteManager().selectingInvites.remove(inviter); // Remove the invite from the selectingInvites map
            plugin.getArenaModule().getInviteManager().invites.put(invite.invited, invite); // Put the invite in the invites map

            inviter.closeInventory(); // Close the inventory
        }
    }
}
