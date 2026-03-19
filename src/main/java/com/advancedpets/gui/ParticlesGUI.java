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

public class ParticlesGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final Pet pet;

    public ParticlesGUI(AdvancedPets plugin, Pet pet, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.pet = pet;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54,
            color("§d§l✦ §f§lPartículas §d§l✦"));
        fillBorders(inv);

        inv.setItem(10, makeItem(Material.GRAY_WOOL, "§f🌪 §lTORNADO", "§7Remolino de viento", "§eClick para elegir"));
        inv.setItem(11, makeItem(Material.YELLOW_WOOL, "§e⚡ §lRAYOS", "§7Relámpagos alrededor", "§eClick para elegir"));
        inv.setItem(12, makeItem(Material.GOLD_NUGGET, "§6💛 §lPEPITAS DE ORO", "§7Lluvia de oro", "§eClick para elegir"));
        inv.setItem(13, makeItem(Material.DIAMOND, "§b💎 §lDIAMANTES", "§7Partículas brillantes", "§eClick para elegir"));
        inv.setItem(14, makeItem(Material.BLAZE_POWDER, "§c🔥 §lLLAMAS", "§7Fuego girando", "§eClick para elegir"));
        inv.setItem(15, makeItem(Material.SNOWBALL, "§f❄ §lHIELO/NIEVE", "§7Copos flotando", "§eClick para elegir"));
        inv.setItem(16, makeItem(Material.WATER_BUCKET, "§9🌊 §lAGUA", "§7Burbujas alrededor", "§eClick para elegir"));

        inv.setItem(19, makeItem(Material.PURPLE_DYE, "§5💜 §lMAGIA OSCURA", "§7Partículas de brujería", "§eClick para elegir"));
        inv.setItem(20, makeItem(Material.NETHER_STAR, "§e✨ §lESTRELLAS", "§7Destellos dorados", "§eClick para elegir"));
        inv.setItem(21, makeItem(Material.REDSTONE, "§c🩸 §lSANGRE", "§7Partículas de daño", "§eClick para elegir"));
        inv.setItem(22, makeItem(Material.PINK_DYE, "§d🌸 §lFLORES", "§7Pétalos rosas", "§eClick para elegir"));
        inv.setItem(23, makeItem(Material.COAL, "§8☁ §lHUMO", "§7Humo gris espeso", "§eClick para elegir"));
        inv.setItem(24, makeItem(Material.WHITE_WOOL, "§f🌈 §lARCOÍRIS", "§7Todos los colores", "§eClick para elegir"));
        inv.setItem(25, makeItem(Material.GUNPOWDER, "§6💥 §lEXPLOSIÓN", "§7Pequeñas explosiones", "§eClick para elegir"));

        inv.setItem(31, makeItem(Material.SOUL_SAND, "§3👻 §lALMA/SOUL", "§7Llamas de alma azules", "§eClick para elegir"));

        // Botón quitar partículas
        ItemStack remove = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta rmeta = remove.getItemMeta();
        rmeta.setDisplayName(color("§c§l🚫 QUITAR PARTÍCULAS 🚫"));
        rmeta.setLore(Arrays.asList(color("§7Click para desactivar"), color("§7todas las partículas"), "", color("§cEliminará la partícula activa")));
        remove.setItemMeta(rmeta);
        inv.setItem(40, remove);

        // Botón regresar
        inv.setItem(49, makeItem(Material.ARROW, "§e§l🔙 REGRESAR", "§7Volver al menú principal"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§d§l✦ §f§lPartículas §d§l✦"))) return;
        event.setCancelled(true);

        Pet.ParticleType selected = null;
        switch (event.getSlot()) {
            case 10: selected = Pet.ParticleType.TORNADO; break;
            case 11: selected = Pet.ParticleType.LIGHTNING; break;
            case 12: selected = Pet.ParticleType.GOLD; break;
            case 13: selected = Pet.ParticleType.DIAMOND; break;
            case 14: selected = Pet.ParticleType.FLAME; break;
            case 15: selected = Pet.ParticleType.SNOW; break;
            case 16: selected = Pet.ParticleType.WATER; break;
            case 19: selected = Pet.ParticleType.MAGIC; break;
            case 20: selected = Pet.ParticleType.STAR; break;
            case 21: selected = Pet.ParticleType.BLOOD; break;
            case 22: selected = Pet.ParticleType.FLOWER; break;
            case 23: selected = Pet.ParticleType.SMOKE; break;
            case 24: selected = Pet.ParticleType.RAINBOW; break;
            case 25: selected = Pet.ParticleType.EXPLOSION; break;
            case 31: selected = Pet.ParticleType.SOUL; break;
            case 40:
                pet.setParticleType(Pet.ParticleType.NONE);
                plugin.getPetManager().savePet(pet);
                clicker.sendMessage("§c§l[AdvancedPets] §f🚫 Partículas eliminadas! Tu mascota está limpia ✨");
                clicker.playSound(clicker.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1f, 1f);
                clicker.closeInventory();
                return;
            case 49:
                clicker.closeInventory();
                new MainGUI(plugin, pet, clicker).open();
                return;
        }
        if (selected != null) {
            pet.setParticleType(selected);
            plugin.getPetManager().savePet(pet);
            clicker.sendMessage("§d§l[AdvancedPets] §f✨ Partícula §d§l" + selected.name() + " §factivada!");
            clicker.playSound(clicker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
            clicker.closeInventory();
        }
    }

    private void fillBorders(Inventory inv) {
        Material[] mats = {Material.PURPLE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE};
        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        int i = 0;
        for (int slot : border) {
            ItemStack glass = new ItemStack(mats[i % mats.length]);
            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName("§d§l✦");
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
