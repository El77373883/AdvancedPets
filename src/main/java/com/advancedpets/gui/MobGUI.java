package com.advancedpets.gui;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MobGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final Pet pet;
    private Pet.Rarity currentTab = Pet.Rarity.COMMON;

    public MobGUI(AdvancedPets plugin, Pet pet, Player player) {
        this.plugin = plugin;
        this.pet = pet;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54,
            color("§e§l🐾 Elegir Tipo de Mob"));
        fillBorders(inv);

        // Pestañas
        inv.setItem(0, makeTab(Material.WHITE_STAINED_GLASS_PANE, "§f§lCOMÚN", currentTab == Pet.Rarity.COMMON));
        inv.setItem(1, makeTab(Material.BLUE_STAINED_GLASS_PANE, "§9§lRARA", currentTab == Pet.Rarity.RARE));
        inv.setItem(2, makeTab(Material.PURPLE_STAINED_GLASS_PANE, "§5§lÉPICA", currentTab == Pet.Rarity.EPIC));
        inv.setItem(3, makeTab(Material.GOLD_BLOCK, "§6§lLEGENDARIA", currentTab == Pet.Rarity.LEGENDARY));

        // Mobs según tab
        List<EntityType> mobs = ShopGUI.getMobsStatic(currentTab);
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        for (int i = 0; i < Math.min(mobs.size(), slots.length); i++) {
            EntityType type = mobs.get(i);
            inv.setItem(slots[i], makeMobItem(type));
        }

        inv.setItem(49, makeItem(Material.ARROW, "§e§l🔙 REGRESAR", "§7Volver al menú principal"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§e§l🐾 Elegir Tipo de Mob"))) return;
        event.setCancelled(true);

        if (event.getSlot() == 0) { currentTab = Pet.Rarity.COMMON; open(); return; }
        if (event.getSlot() == 1) { currentTab = Pet.Rarity.RARE; open(); return; }
        if (event.getSlot() == 2) { currentTab = Pet.Rarity.EPIC; open(); return; }
        if (event.getSlot() == 3) { currentTab = Pet.Rarity.LEGENDARY; open(); return; }
        if (event.getSlot() == 49) { clicker.closeInventory(); new MainGUI(plugin, pet, clicker).open(); return; }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getItemMeta() == null) return;

        List<EntityType> mobs = ShopGUI.getMobsStatic(currentTab);
        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == event.getSlot() && i < mobs.size()) {
                EntityType type = mobs.get(i);
                if (pet.isSummoned()) plugin.getPetManager().despawnPet(pet);
                pet.setEntityType(type);
                plugin.getPetManager().savePet(pet);
                plugin.getPetManager().spawnPet(pet, clicker.getLocation());
                clicker.sendMessage("§e§l[AdvancedPets] §fTipo de mascota cambiado a: §e" + type.name());
                clicker.closeInventory();
                return;
            }
        }
    }

    private ItemStack makeMobItem(EntityType type) {
        Material icon = new ShopGUI(plugin, player).getMobIconPublic(type);
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color("§e§l" + type.name().replace("_", " ")));
        meta.setLore(Arrays.asList(
            color("§7Click para cambiar"),
            color("§7tu mascota a este tipo"),
            color(""),
            color(pet.getEntityType() == type ? "§a§l✔ SELECCIONADO" : "§eClick para elegir")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeTab(Material mat, String name, boolean active) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name + (active ? " §7[ACTIVO]" : "")));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        List<String> loreList = new ArrayList<>();
        for (String l : lore) loreList.add(color(l));
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorders(Inventory inv) {
        Material[] mats = {Material.YELLOW_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE};
        int[] border = {8,9,17,18,26,27,35,36,44,45,46,47,48,50,51,52,53};
        int i = 0;
        for (int slot : border) {
            ItemStack glass = new ItemStack(mats[i % mats.length]);
            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName("§e§l✦");
            glass.setItemMeta(meta);
            inv.setItem(slot, glass);
            i++;
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
