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
        Material borderMat =
            pet.getRarity() == Pet.Rarity.LEGENDARY ? Material.GOLD_BLOCK :
            pet.getRarity() == Pet.Rarity.EPIC ? Material.PURPLE_STAINED_GLASS_PANE :
            pet.getRarity() == Pet.Rarity.RARE ? Material.BLUE_STAINED_GLASS_PANE :
            Material.GRAY_STAINED_GLASS_PANE;

        Inventory inv = Bukkit.createInventory(null, 54,
            color("§5§l👁 Inspeccionando: §e" + pet.getName()));

        // Bordes según rareza
        int[] border = {0,1,2,3,4,5,6,7,8,
                        9,17,18,26,27,35,
                        36,44,45,46,47,48,49,50,51,52,53};
        for (int slot : border) {
            ItemStack glass = new ItemStack(borderMat);
            ItemMeta m = glass.getItemMeta();
            m.setDisplayName(rc + "§l✦");
            glass.setItemMeta(m);
            inv.setItem(slot, glass);
        }

        // Header
        ItemStack header = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta hm = header.getItemMeta();
        hm.setDisplayName(color("§f§lMASCOTA DE §e" + pet.getOwnerName()));
        hm.setLore(Arrays.asList(
            color(rc + "§l" + pet.getName()),
            color("§7Solo lectura — No puedes modificar nada")
        ));
        header.setItemMeta(hm);
        inv.setItem(4, header);

        // Stats principales
        inv.setItem(10, makeInfo(Material.NAME_TAG,
            "§f§lNOMBRE", rc + "§l" + pet.getName()));
        inv.setItem(11, makeInfo(Material.SPAWNER,
            "§f§lTIPO", "§e" + pet.getEntityType().name()));
        inv.setItem(12, makeInfo(Material.NETHER_STAR,
            "§f§lRARESA", rc + "§l" + pet.getRarityName()));
        inv.setItem(13, makeInfo(Material.EXPERIENCE_BOTTLE,
            "§f§lNIVEL", "§a§l" + pet.getLevel()));
        inv.setItem(14, makeInfo(Material.BOOK,
            "§f§lXP", "§b" + (int)pet.getXp() +
            "§7/§b" + (int)pet.getXpNeeded()));
        inv.setItem(15, makeInfo(Material.REDSTONE,
            "§f§lVIDA", "§c" + (int)pet.getHealth() +
            "§7/§c" + (int)pet.getMaxHealth()));
        inv.setItem(16, makeInfo(Material.DIAMOND_SWORD,
            "§f§lDAÑO", "§c§l" + pet.getDamage()));

        inv.setItem(19, makeInfo(Material.BLAZE_POWDER,
            "§f§lPARTÍCULA", "§d" + pet.getParticleType().name()));
        inv.setItem(20, makeInfo(Material.IRON_PICKAXE,
            "§f§lTRABAJO", "§a" + pet.getCurrentWork().name()));
        inv.setItem(21, makeInfo(Material.SHIELD,
            "§f§lCOMBATE",
            pet.isCombatMode() ? "§a§lACTIVO ✔" : "§c§lDESACTIVO ✘"));
        inv.setItem(22, makeInfo(Material.SKELETON_SKULL,
            "§f§lKILLS", "§c§l" + pet.getKills()));
        inv.setItem(23, makeInfo(Material.GOLDEN_APPLE,
            "§f§lHUMOR", pet.getMoodColor() + pet.getMood().name()));
        inv.setItem(24, makeInfo(Material.APPLE,
            "§f§lHAMBRE", "§6" + (int)pet.getHunger() + "%"));
        inv.setItem(25, makeInfo(Material.ENDER_EYE,
            "§f§lESTADO",
            pet.isSummoned() ? "§aInvocada ✔" : "§7Guardada"));

        inv.setItem(28, makeInfo(Material.HEART_OF_THE_SEA,
            "§f§lSALUD",
            pet.isImmortal() ? "§aINMORTAL" : "§cMORTAL"));

        // Mochila
        inv.setItem(29, makeInfo(Material.CHEST,
            "§f§lMOCHILA", "§727 slots"));

        // Logros
        inv.setItem(30, makeInfo(Material.TOTEM_OF_UNDYING,
            "§f§lLOGROS", "§6" +
            plugin.getAchievementManager()
                .getAchievementCount(pet.getOwnerUUID()) + " logros"));

        // Dueño
        inv.setItem(31, makeInfo(Material.PLAYER_HEAD,
            "§f§lDUEÑO", "§e" + pet.getOwnerName()));

        // Clan ✅ CORREGIDO Material.BANNER → Material.WHITE_BANNER
        if (pet.getClanName() != null) {
            inv.setItem(32, makeInfo(Material.WHITE_BANNER,
                "§f§lCLAN", "§5" + pet.getClanName()));
        }

        // Modo trabajo
        inv.setItem(33, makeInfo(Material.IRON_PICKAXE,
            "§f§lMODO TRABAJO",
            pet.isWorkMode() ? "§aACTIVO ✔" : "§cDESACTIVO ✘"));

        // Partícula activa
        inv.setItem(34, makeInfo(Material.FIREWORK_STAR,
            "§f§lEFECTO ULTRA",
            "§d" + UltraShopGUI.getActiveEffect(plugin,
                Bukkit.getOfflinePlayer(pet.getOwnerUUID()).getPlayer() != null ?
                Bukkit.getPlayer(pet.getOwnerUUID()) :
                player)));

        // Barra de vida visual
        inv.setItem(37, makeInfo(Material.REDSTONE,
            "§f§lBARRA DE VIDA", buildHealthBar(pet)));

        // XP visual
        inv.setItem(38, makeInfo(Material.EXPERIENCE_BOTTLE,
            "§f§lBARRA DE XP", buildXPBar(pet)));

        // Hambre visual
        inv.setItem(39, makeInfo(Material.APPLE,
            "§f§lBARRA HAMBRE", buildHungerBar(pet)));

        // Sleeping
        inv.setItem(40, makeInfo(Material.BED,
            "§f§lDURMIENDO",
            pet.isSleeping() ? "§8§lSÍ 💤" : "§aDespierto ☀"));

        // Cerrar — vidrio rojo centro abajo
        ItemStack close = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cm = close.getItemMeta();
        cm.setDisplayName(color("§c§l✘ CERRAR"));
        cm.setLore(Collections.singletonList(color("§7Click para cerrar")));
        close.setItemMeta(cm);
        inv.setItem(49, close);

        player.openInventory(inv);
        player.sendMessage("§9§l[AdvancedPets] §f👁 Inspeccionando la mascota de §e"
            + pet.getOwnerName() + "§f...");
        player.playSound(player.getLocation(),
            Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(
            color("§5§l👁 Inspeccionando: §e" + pet.getName()))) return;
        event.setCancelled(true);
        if (event.getSlot() == 49) clicker.closeInventory();
    }

    private String buildHealthBar(Pet pet) {
        int bars = 10;
        int filled = (int) ((pet.getHealth() / pet.getMaxHealth()) * bars);
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < bars; i++) {
            if (i == filled) bar.append("§7");
            bar.append("█");
        }
        return bar + " §f" + (int)pet.getHealth() + "/" + (int)pet.getMaxHealth();
    }

    private String buildXPBar(Pet pet) {
        int bars = 10;
        int filled = (int) ((pet.getXp() / pet.getXpNeeded()) * bars);
        StringBuilder bar = new StringBuilder("§b");
        for (int i = 0; i < bars; i++) {
            if (i == filled) bar.append("§7");
            bar.append("█");
        }
        return bar + " §f" + (int)pet.getXp() + "/" + (int)pet.getXpNeeded();
    }

    private String buildHungerBar(Pet pet) {
        int bars = 10;
        int filled = (int) (pet.getHunger() / 10);
        StringBuilder bar = new StringBuilder("§6");
        for (int i = 0; i < bars; i++) {
            if (i == filled) bar.append("§7");
            bar.append("█");
        }
        return bar + " §f" + (int)pet.getHunger() + "%";
    }

    private ItemStack makeInfo(Material mat, String name, String value) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        meta.setLore(Arrays.asList(
            color("§f" + value),
            color(""),
            color("§8Solo lectura")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
