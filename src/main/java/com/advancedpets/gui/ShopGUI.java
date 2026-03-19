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

public class ShopGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private Pet.Rarity currentTab = Pet.Rarity.COMMON;

    private static final Map<EntityType, Pet.Rarity> MOB_RARITY = new LinkedHashMap<>();

    static {
        // Comunes
        for (EntityType t : new EntityType[]{
            EntityType.WOLF, EntityType.CAT, EntityType.HORSE,
            EntityType.COW, EntityType.PIG, EntityType.SHEEP,
            EntityType.CHICKEN, EntityType.LLAMA, EntityType.RABBIT,
            EntityType.FOX, EntityType.TURTLE, EntityType.BEE,
            EntityType.PANDA, EntityType.PARROT, EntityType.COD,
            EntityType.SQUID, EntityType.DOLPHIN, EntityType.FROG,
            EntityType.CAMEL, EntityType.ARMADILLO})
            MOB_RARITY.put(t, Pet.Rarity.COMMON);
        // Raras
        for (EntityType t : new EntityType[]{
            EntityType.IRON_GOLEM, EntityType.POLAR_BEAR,
            EntityType.ZOMBIE_VILLAGER, EntityType.SPIDER,
            EntityType.CAVE_SPIDER, EntityType.PHANTOM,
            EntityType.PIGLIN, EntityType.SNOW_GOLEM})
            MOB_RARITY.put(t, Pet.Rarity.RARE);
        // Épicas
        for (EntityType t : new EntityType[]{
            EntityType.SKELETON, EntityType.ZOMBIE,
            EntityType.CREEPER, EntityType.BLAZE,
            EntityType.ENDERMAN, EntityType.WITCH,
            EntityType.GUARDIAN, EntityType.PIGLIN_BRUTE,
            EntityType.HOGLIN, EntityType.RAVAGER,
            EntityType.VEX, EntityType.VINDICATOR,
            EntityType.SHULKER, EntityType.MAGMA_CUBE,
            EntityType.SLIME, EntityType.ELDER_GUARDIAN})
            MOB_RARITY.put(t, Pet.Rarity.EPIC);
        // Legendarias
        for (EntityType t : new EntityType[]{
            EntityType.ENDER_DRAGON, EntityType.WITHER,
            EntityType.WARDEN})
            MOB_RARITY.put(t, Pet.Rarity.LEGENDARY);
    }

    public ShopGUI(AdvancedPets plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54,
            color("§6§l✦ §e§lTienda de Mascotas §6§l✦"));
        fillBorders(inv);

        // Pestañas de rareza
        inv.setItem(0, makeTab(Material.WHITE_STAINED_GLASS_PANE,
            "§f§lCOMÚN", currentTab == Pet.Rarity.COMMON));
        inv.setItem(1, makeTab(Material.BLUE_STAINED_GLASS_PANE,
            "§9§lRARA", currentTab == Pet.Rarity.RARE));
        inv.setItem(2, makeTab(Material.PURPLE_STAINED_GLASS_PANE,
            "§5§lÉPICA", currentTab == Pet.Rarity.EPIC));
        inv.setItem(3, makeTab(Material.GOLD_BLOCK,
            "§6§lLEGENDARIA", currentTab == Pet.Rarity.LEGENDARY));

        // Botón tienda ultra premium
        ItemStack ultra = new ItemStack(Material.BEACON);
        ItemMeta um = ultra.getItemMeta();
        um.setDisplayName(color("§6§l👑 EFECTOS ULTRA PREMIUM"));
        um.setLore(Arrays.asList(
            color("§7Efectos épicos al matar"),
            color("§7Solo los más ricos los tienen"),
            color(""),
            color("§eClick para ver!")
        ));
        ultra.setItemMeta(um);
        inv.setItem(4, ultra);

        // Llenar mobs según tab
        List<EntityType> mobs = getMobsByRarity(currentTab);
        int[] slots = {10,11,12,13,14,15,16,
                       19,20,21,22,23,24,25,
                       28,29,30,31,32,33,34};
        for (int i = 0; i < Math.min(mobs.size(), slots.length); i++) {
            EntityType type = mobs.get(i);
            double price = plugin.getEconomyManager()
                .getPetPrice(type, currentTab);
            inv.setItem(slots[i], makeMobItem(type, currentTab, price));
        }

        // Saldo del jugador
        ItemStack balance = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta bm = balance.getItemMeta();
        bm.setDisplayName(color("§6§l💰 TU SALDO"));
        bm.setLore(Arrays.asList(
            color("§f$" + (int) plugin.getEconomyManager().getBalance(player)),
            color(""),
            color("§7Gana dinero jugando!")
        ));
        balance.setItemMeta(bm);
        inv.setItem(49, balance);

        player.openInventory(inv);
        player.playSound(player.getLocation(),
            Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(
            color("§6§l✦ §e§lTienda de Mascotas §6§l✦"))) return;
        event.setCancelled(true);

        // Pestañas
        if (event.getSlot() == 0) { currentTab = Pet.Rarity.COMMON; open(); return; }
        if (event.getSlot() == 1) { currentTab = Pet.Rarity.RARE; open(); return; }
        if (event.getSlot() == 2) { currentTab = Pet.Rarity.EPIC; open(); return; }
        if (event.getSlot() == 3) { currentTab = Pet.Rarity.LEGENDARY; open(); return; }

        // Ultra premium
        if (event.getSlot() == 4) {
            clicker.closeInventory();
            new UltraShopGUI(plugin, clicker).open();
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (clicked.getItemMeta() == null) return;

        List<EntityType> mobs = getMobsByRarity(currentTab);
        int[] slots = {10,11,12,13,14,15,16,
                       19,20,21,22,23,24,25,
                       28,29,30,31,32,33,34};
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == event.getSlot() && i < mobs.size()) {
                EntityType type = mobs.get(i);
                double price = plugin.getEconomyManager()
                    .getPetPrice(type, currentTab);
                clicker.closeInventory();
                new BuyConfirmGUI(plugin, clicker, type, currentTab, price).open();
                return;
            }
        }
    }

    // ✅ Método público estático para MobGUI
    public static List<EntityType> getMobsStatic(Pet.Rarity rarity) {
        List<EntityType> list = new ArrayList<>();
        for (Map.Entry<EntityType, Pet.Rarity> e : MOB_RARITY.entrySet()) {
            if (e.getValue() == rarity) list.add(e.getKey());
        }
        return list;
    }

    private List<EntityType> getMobsByRarity(Pet.Rarity rarity) {
        return getMobsStatic(rarity);
    }

    // ✅ Método público para MobGUI
    public Material getMobIconPublic(EntityType type) {
        return getMobIcon(type);
    }

    private ItemStack makeMobItem(EntityType type, Pet.Rarity rarity, double price) {
        Material icon = getMobIcon(type);
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        String rc = rarity == Pet.Rarity.COMMON ? "§f" :
                    rarity == Pet.Rarity.RARE ? "§9" :
                    rarity == Pet.Rarity.EPIC ? "§5" : "§6";
        meta.setDisplayName(rc + "§l" + type.name().replace("_", " "));
        meta.setLore(Arrays.asList(
            color("§7Rareza: " + rc + rarity.name()),
            color("§7Precio: §6§l$" + (int) price),
            color(""),
            color("§eClick para comprar!")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private Material getMobIcon(EntityType type) {
        switch (type) {
            case WOLF: return Material.BONE;
            case CAT: return Material.COD;
            case HORSE: return Material.SADDLE;
            case COW: return Material.LEATHER;
            case PIG: return Material.PORKCHOP;
            case SHEEP: return Material.WHITE_WOOL;
            case CHICKEN: return Material.FEATHER;
            case LLAMA: return Material.WHITE_CARPET; // ✅ CORREGIDO
            case RABBIT: return Material.RABBIT_FOOT;
            case FOX: return Material.SWEET_BERRIES;
            case TURTLE: return Material.TURTLE_SCUTE; // ✅ CORREGIDO
            case BEE: return Material.HONEYCOMB;
            case PANDA: return Material.BAMBOO;
            case PARROT: return Material.COOKIE;
            case SQUID: return Material.INK_SAC;
            case DOLPHIN: return Material.COD;
            case FROG: return Material.SLIME_BALL;
            case CAMEL: return Material.SAND;
            case ARMADILLO: return Material.ARMADILLO_SCUTE;
            case COD: return Material.COD;
            case IRON_GOLEM: return Material.IRON_BLOCK;
            case POLAR_BEAR: return Material.SNOWBALL;
            case ZOMBIE_VILLAGER: return Material.ROTTEN_FLESH;
            case SPIDER: return Material.SPIDER_EYE;
            case CAVE_SPIDER: return Material.FERMENTED_SPIDER_EYE;
            case PHANTOM: return Material.PHANTOM_MEMBRANE;
            case PIGLIN: return Material.GOLD_INGOT;
            case SNOW_GOLEM: return Material.SNOWBALL;
            case SKELETON: return Material.BONE;
            case ZOMBIE: return Material.ROTTEN_FLESH;
            case CREEPER: return Material.GUNPOWDER;
            case BLAZE: return Material.BLAZE_ROD;
            case ENDERMAN: return Material.ENDER_PEARL;
            case WITCH: return Material.GLASS_BOTTLE;
            case GUARDIAN: return Material.PRISMARINE_SHARD;
            case PIGLIN_BRUTE: return Material.GOLDEN_AXE;
            case HOGLIN: return Material.PORKCHOP;
            case RAVAGER: return Material.SADDLE;
            case VEX: return Material.IRON_SWORD;
            case VINDICATOR: return Material.IRON_AXE;
            case SHULKER: return Material.SHULKER_SHELL;
            case MAGMA_CUBE: return Material.MAGMA_CREAM;
            case SLIME: return Material.SLIME_BALL;
            case ELDER_GUARDIAN: return Material.SPONGE;
            case ENDER_DRAGON: return Material.DRAGON_EGG;
            case WITHER: return Material.NETHER_STAR;
            case WARDEN: return Material.SCULK;
            default: return Material.SPAWNER;
        }
    }

    private ItemStack makeTab(Material mat, String name, boolean active) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name + (active ? " §7[ACTIVO]" : "")));
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorders(Inventory inv) {
        Material[] mats = {
            Material.YELLOW_STAINED_GLASS_PANE,
            Material.ORANGE_STAINED_GLASS_PANE};
        int[] border = {8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        int i = 0;
        for (int slot : border) {
            ItemStack glass = new ItemStack(mats[i % mats.length]);
            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName("§6§l✦");
            glass.setItemMeta(meta);
            inv.setItem(slot, glass);
            i++;
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
