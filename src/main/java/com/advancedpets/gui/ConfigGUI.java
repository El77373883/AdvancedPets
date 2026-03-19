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

public class ConfigGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final Pet pet;

    public ConfigGUI(AdvancedPets plugin, Pet pet, Player player) {
        this.plugin = plugin;
        this.pet = pet;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54,
            color("§7§l⚙ Configuración — §e" + pet.getName()));
        fillBorders(inv);

        inv.setItem(10, makeItem(Material.GOLD_INGOT, "§6§l💰 PRECIO MASCOTA",
            "§7Solo admin puede cambiar", "§fPrecio actual: §6$" + (int) plugin.getEconomyManager().getPetPrice(pet.getEntityType(), pet.getRarity())));

        inv.setItem(12, makeItem(Material.DIAMOND_SWORD, "§c§l⚔ DAÑO",
            "§7◄ Click izq = Bajar",
            "§7► Click der = Subir",
            "§fDaño actual: §c" + pet.getDamage(),
            "§7Rango: 1-30"));

        inv.setItem(14, makeItem(Material.HEART_OF_THE_SEA, "§c§l❤ MODO SALUD",
            "§7Click para cambiar",
            "§fModo actual: " + (pet.isImmortal() ? "§a§lINMORTAL" : "§c§lMORTAL")));

        inv.setItem(16, makeItem(Material.APPLE, "§6§l🍎 TIEMPO DE HAMBRE",
            "§7Configurable en config.yml",
            "§fIntervalo: §e" + plugin.getConfig().getInt("pets.hunger.interval-minutes") + " minutos"));

        inv.setItem(28, makeItem(Material.CLOCK, "§b§l💤 HORA DE DORMIR",
            "§7Configurable en config.yml",
            "§fHora: §e" + plugin.getConfig().getInt("pets.sleep.hour") + ":00"));

        inv.setItem(30, makeItem(Material.BLAZE_POWDER, "§d§l✨ PARTÍCULAS",
            "§7Click para activar/desactivar",
            "§fEstado: " + (plugin.getConfig().getBoolean("particles.enabled") ? "§a§lACTIVO ✔" : "§c§lDESACTIVO ✘")));

        inv.setItem(32, makeItem(Material.NOTE_BLOCK, "§f§l🔊 SONIDOS",
            "§7Click para activar/desactivar",
            "§fEstado: " + (plugin.getConfig().getBoolean("sounds.enabled") ? "§a§lACTIVO ✔" : "§c§lDESACTIVO ✘")));

        inv.setItem(34, makeItem(Material.EXPERIENCE_BOTTLE, "§b§l⭐ XP POR NIVEL",
            "§7Configurable en config.yml",
            "§fXP necesaria: §b" + (int) pet.getXpNeeded()));

        inv.setItem(49, makeItem(Material.ARROW, "§e§l🔙 REGRESAR",
            "§7Volver al menú principal"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1f, 1f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§7§l⚙ Configuración — §e" + pet.getName()))) return;
        event.setCancelled(true);

        switch (event.getSlot()) {
            case 12:
                if (event.getClick() == ClickType.LEFT) pet.setDamage(Math.max(1, pet.getDamage() - 1));
                else pet.setDamage(Math.min(30, pet.getDamage() + 1));
                plugin.getPetManager().savePet(pet);
                clicker.closeInventory(); open(); break;
            case 14:
                pet.setImmortal(!pet.isImmortal());
                plugin.getPetManager().savePet(pet);
                clicker.closeInventory(); open(); break;
            case 30:
                boolean particles = plugin.getConfig().getBoolean("particles.enabled");
                plugin.getConfig().set("particles.enabled", !particles);
                plugin.saveConfig();
                clicker.closeInventory(); open(); break;
            case 32:
                boolean sounds = plugin.getConfig().getBoolean("sounds.enabled");
                plugin.getConfig().set("sounds.enabled", !sounds);
                plugin.saveConfig();
                clicker.closeInventory(); open(); break;
            case 49:
                clicker.closeInventory();
                new MainGUI(plugin, pet, clicker).open(); break;
        }
    }

    private void fillBorders(Inventory inv) {
        Material[] mats = {Material.GRAY_STAINED_GLASS_PANE, Material.LIGHT_GRAY_STAINED_GLASS_PANE};
        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        int i = 0;
        for (int slot : border) {
            ItemStack glass = new ItemStack(mats[i % mats.length]);
            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName("§7§l✦");
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
