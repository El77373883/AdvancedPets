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

public class InspectGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final Pet pet;

    public InspectGUI(AdvancedPets plugin, Pet pet, Player player) {
        this.plugin = plugin;
        this.pet = pet;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        String rc = pet.getRarityColor();
        Material borderMat = pet.getRarity() == Pet.Rarity.LEGENDARY ? Material.GOLD_BLOCK :
                             pet.getRarity() == Pet.Rarity.EPIC ? Material.PURPLE_STAINED_GLASS_PANE :
                             pet.getRarity() == Pet.Rarity.RARE ? Material.BLUE_STAINED_GLASS_PANE :
                             Material.GRAY_STAINED_GLASS_PANE;

        Inventory inv = Bukkit.createInventory(null, 54,
            color("§5§l👁 Inspeccionando: §e" + pet.getName()));

        // Bordes según rareza
        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        for (int slot : border) {
            ItemStack glass = new ItemStack(borderMat);
            ItemMeta m = glass.getItemMeta();
            m.setDisplayName(rc + "§l✦");
            glass.setItemMeta(m);
            inv.setItem(slot, glass);
        }

        // Stats principales
        inv.setItem(10, makeInfo(Material.NAME_TAG, "§f§lNOMBRE", rc + "§l" + pet.getName()));
        inv.setItem(11, makeInfo(Material.SPAWNER, "§f§lTIPO", "§e" + pet.getEntityType().name()));
        inv.setItem(12, makeInfo(Material.NETHER_STAR, "§f§lRARESA", rc + "§l" + pet.getRarityName()));
        inv.setItem(13, makeInfo(Material.EXPERIENCE_BOTTLE, "§f§lNIVEL", "§a§l" + pet.getLevel()));
        inv.setItem(14, makeInfo(Material.BOOK, "§f§lXP", "§b" + (int)pet.getXp() + "§7/§b" + (int)pet.getXpNeeded()));
        inv.setItem(15, makeInfo(Material.REDSTONE, "§f§lVIDA", "§c" + (int)pet.getHealth() + "§7/§c" + (int)pet.getMaxHealth()));
        inv.setItem(16, makeInfo(Material.DIAMOND_SWORD, "§f§lDAÑO", "§c§l" + pet.getDamage()));

        inv.setItem(19, makeInfo(Material.BLAZE_POWDER, "§f§lPARTÍCULA", "§d" + pet.getParticleType().name()));
        inv.setItem(20, makeInfo(Material.IRON_PICKAXE, "§f§lTRABAJO", "§a" + pet.getCurrentWork().name()));
        inv.setItem(21, makeInfo(Material.SHIELD, "§f§lCOMBATE", pet.isCombatMode() ? "§a§lACTIVO ✔" : "§c§lDESACTIVO ✘"));
        inv.setItem(22, makeInfo(Material.SKULL_BANNER_PATTERN, "§f§lKILLS", "§c§l" + pet.getKills()));
        inv.setItem(23, makeInfo(Material.GOLDEN_APPLE, "§f§lHUMOR", pet.getMoodColor() + pet.getMood().name()));
        inv.setItem(24, makeInfo(Material.APPLE, "§f§lHAMBRE", "§6" + (int)pet.getHunger() + "%"));
        inv.setItem(25, makeInfo(Material.ENDER_EYE, "§f§lESTADO", pet.isSummoned() ? "§aInvocada ✔" : "§7Guardada"));

        inv.setItem(28, makeInfo(Material.HEART_OF_THE_SEA, "§f§lMODO SALUD", pet.isImmortal() ? "§aINMORTAL" : "§cMORTAL"));
        inv.setItem(29, makeInfo(Material.CHEST, "§f§lMOCHILA", "§7Ver inventario"));
        inv.setItem(30, makeInfo(Material.TOTEM_OF_UNDYING, "§f§lLOGROS",
            "§6" + plugin.getAchievementManager().getAchievementCount(pet.getOwnerUUID()) + " logros"));
        inv.setItem(31, makeInfo(Material.PLAYER_HEAD, "§f§lDUEÑO", "§e" + pet.getOwnerName()));
        if (pet.getClanName() != null) {
            inv.setItem(32, makeInfo(Material.BANNER, "§f§lCLAN", "§5" + pet.getClanName()));
        }

        // Cerrar
        ItemStack close = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(color("§c§l✘ CERRAR"));
        close.setItemMeta(cm);
        inv.setItem(49, close);

        player.openInventory(inv);
        player.sendMessage("§9§l[AdvancedPets] §f👁 Inspeccionando la mascota de §e" + pet.getOwnerName() + "§f...");
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§5§l👁 Inspeccionando: §e" + pet.getName()))) return;
        event.setCancelled(true);
        if (event.getSlot() == 49) clicker.closeInventory();
    }

    private ItemStack makeInfo(Material mat, String name, String value) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        meta.setLore(Collections.singletonList(color("§f" + value)));
        item.setItemMeta(meta);
        return item;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
