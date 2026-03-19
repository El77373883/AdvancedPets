package com.advancedpets.gui;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WorksGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final Pet pet;

    public WorksGUI(AdvancedPets plugin, Pet pet, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.pet = pet;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54,
            color("§a§l⛏ §f§lTrabajos §a§l⛏"));
        fillBorders(inv);

        inv.setItem(10, makeItem(Material.IRON_PICKAXE, "§e§l⛏ MINAR", "§7Tu mascota minará ores", "§7automáticamente cerca de ti", "", "§eClick para activar"));
        inv.setItem(12, makeItem(Material.IRON_SWORD, "§c§l🗡 FARMEAR MOBS", "§7Tu mascota matará mobs", "§7y recogerá sus drops", "", "§eClick para activar"));
        inv.setItem(14, makeItem(Material.DIAMOND_SWORD, "§c§l⚔ PVP", "§7Tu mascota atacará", "§7jugadores enemigos", "", "§eClick para activar"));
        inv.setItem(16, makeItem(Material.BOW, "§6§l🏹 ATACAR MOBS", "§7Tu mascota atacará", "§7mobs cercanos", "", "§eClick para activar"));

        inv.setItem(28, makeItem(Material.BRICKS, "§7§l🧱 CONSTRUIR", "§7Tu mascota construirá", "§7una casa automáticamente", "", "§eClick para activar"));
        inv.setItem(30, makeItem(Material.EXPERIENCE_BOTTLE, "§b§l⭐ HACER EXP", "§7Tu mascota farmea", "§7experiencia para ti", "", "§eClick para activar"));
        inv.setItem(32, makeItem(Material.OAK_LOG, "§6§l🪓 TALAR", "§7Tu mascota talará", "§7árboles cercanos", "", "§eClick para activar"));
        inv.setItem(34, makeItem(Material.WHEAT, "§a§l🌾 COSECHAR", "§7Tu mascota cosechará", "§7cultivos automáticamente", "", "§eClick para activar"));

        inv.setItem(37, makeItem(Material.HOPPER, "§5§l🧲 RECOGER ITEMS", "§7Tu mascota recogerá", "§7items del suelo", "", "§eClick para activar"));
        inv.setItem(39, makeItem(Material.FURNACE, "§6§l🍳 COCINAR", "§7Tu mascota cocinará", "§7tu comida cruda", "", "§eClick para activar"));
        inv.setItem(41, makeItem(Material.MAP, "§3§l🗺 EXPLORAR CUEVAS", "§7Tu mascota explorará", "§7y traerá recursos", "", "§eClick para activar"));

        // Detener trabajo
        inv.setItem(43, makeItem(Material.RED_STAINED_GLASS_PANE, "§c§l✘ DETENER TRABAJO", "§7Click para detener", "§7el trabajo actual", "", "§fActual: §e" + pet.getCurrentWork().name()));

        inv.setItem(49, makeItem(Material.ARROW, "§e§l🔙 REGRESAR", "§7Volver al menú principal"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1.2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§a§l⛏ §f§lTrabajos §a§l⛏"))) return;
        event.setCancelled(true);

        Pet.WorkType work = null;
        switch (event.getSlot()) {
            case 10: work = Pet.WorkType.MINING; break;
            case 12: work = Pet.WorkType.FARMING; break;
            case 14: work = Pet.WorkType.PVP; break;
            case 16: work = Pet.WorkType.ATTACK; break;
            case 28: work = Pet.WorkType.BUILD; break;
            case 30: work = Pet.WorkType.EXP; break;
            case 32: work = Pet.WorkType.CHOP; break;
            case 34: work = Pet.WorkType.HARVEST; break;
            case 37: work = Pet.WorkType.COLLECT; break;
            case 39: work = Pet.WorkType.COOK; break;
            case 41: work = Pet.WorkType.EXPLORE; break;
            case 43:
                plugin.getWorkManager().stopWork(pet);
                clicker.sendMessage("§c§l[AdvancedPets] §fTrabajo detenido!");
                clicker.closeInventory();
                return;
            case 49:
                clicker.closeInventory();
                new MainGUI(plugin, pet, clicker).open();
                return;
        }
        if (work != null) {
            plugin.getWorkManager().startWork(pet, work, clicker);
            clicker.closeInventory();
        }
    }

    private void fillBorders(Inventory inv) {
        Material[] mats = {Material.GREEN_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE};
        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        int i = 0;
        for (int slot : border) {
            ItemStack glass = new ItemStack(mats[i % mats.length]);
            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName("§a§l✦");
            glass.setItemMeta(meta);
            inv.setItem(slot, glass);
            i++;
        }
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

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
