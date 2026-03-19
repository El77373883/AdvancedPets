package com.advancedpets.gui;

import com.advancedpets.AdvancedPets;
import com.advancedpets.managers.ModelManager;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ModelGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final Pet pet;
    private int page = 0;

    public ModelGUI(AdvancedPets plugin, Pet pet, Player player) {
        this.plugin = plugin;
        this.pet = pet;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54,
            color("§5§l✦ §d§lModelos Custom §5§l✦"));
        fillBorders(inv);

        // Header info
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(color("§d§l✨ MODELOS CUSTOM"));
        im.setLore(Arrays.asList(
            color("§7Elige un modelo para tu mascota"),
            color("§7Requiere: §eModelEngine + MythicMobs"),
            color(""),
            color("§fModelo actual: §d" + plugin.getModelManager().getActiveModel(pet)),
            color("§fMascota: §e" + pet.getName())
        ));
        info.setItemMeta(im);
        inv.setItem(4, info);

        // Modelo por defecto
        ItemStack defaultModel = new ItemStack(Material.SPAWNER);
        ItemMeta dm = defaultModel.getItemMeta();
        dm.setDisplayName(color("§a§l🔄 MODELO POR DEFECTO"));
        dm.setLore(Arrays.asList(
            color("§7Aplica el modelo default"),
            color("§7según el tipo de mob"),
            color(""),
            color("§eClick para aplicar")
        ));
        defaultModel.setItemMeta(dm);
        inv.setItem(10, defaultModel);

        // Quitar modelo
        ItemStack removeModel = new ItemStack(Material.BARRIER);
        ItemMeta rm = removeModel.getItemMeta();
        rm.setDisplayName(color("§c§l✘ QUITAR MODELO"));
        rm.setLore(Arrays.asList(
            color("§7Quitar el modelo custom"),
            color("§7y usar el mob vanilla"),
            color(""),
            color("§cClick para quitar")
        ));
        removeModel.setItemMeta(rm);
        inv.setItem(12, removeModel);

        // Lista de modelos disponibles
        List<String> models = plugin.getModelManager().getAvailableModels();
        int start = page * 21;
        int[] slots = {19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};

        for (int i = 0; i < slots.length; i++) {
            int modelIndex = start + i;
            if (modelIndex >= models.size()) break;
            String modelId = models.get(modelIndex);
            boolean isActive = plugin.getModelManager().getActiveModel(pet).equals(modelId);

            ItemStack modelItem = new ItemStack(isActive ?
                Material.LIME_STAINED_GLASS_PANE : Material.PURPLE_STAINED_GLASS_PANE);
            ItemMeta mm = modelItem.getItemMeta();
            mm.setDisplayName(color("§d§l" + modelId));
            mm.setLore(Arrays.asList(
                color("§7ID: §f" + modelId),
                color(""),
                color(isActive ? "§a§l✔ ACTIVO" : "§eClick para aplicar")
            ));
            modelItem.setItemMeta(mm);
            inv.setItem(slots[i], modelItem);
        }

        // Navegación páginas
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pm = prev.getItemMeta();
            pm.setDisplayName(color("§e§l◄ PÁGINA ANTERIOR"));
            prev.setItemMeta(pm);
            inv.setItem(45, prev);
        }
        if (start + 21 < models.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nm = next.getItemMeta();
            nm.setDisplayName(color("§e§l► PÁGINA SIGUIENTE"));
            next.setItemMeta(nm);
            inv.setItem(53, next);
        }

        // Regresar
        inv.setItem(49, makeItem(Material.ARROW, "§e§l🔙 REGRESAR",
            "§7Volver al menú principal"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1.3f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§5§l✦ §d§lModelos Custom §5§l✦"))) return;
        event.setCancelled(true);

        switch (event.getSlot()) {
            case 10:
                plugin.getModelManager().applyDefaultModel(pet);
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                clicker.closeInventory();
                open();
                break;
            case 12:
                plugin.getModelManager().removeModel(pet);
                clicker.sendMessage("§c§l[AdvancedPets] §fModelo quitado correctamente!");
                clicker.closeInventory();
                break;
            case 45:
                if (page > 0) { page--; open(); }
                break;
            case 49:
                clicker.closeInventory();
                new MainGUI(plugin, pet, clicker).open();
                break;
            case 53:
                page++;
                open();
                break;
            default:
                // Elegir modelo de la lista
                List<String> models = plugin.getModelManager().getAvailableModels();
                int[] slots = {19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43};
                for (int i = 0; i < slots.length; i++) {
                    if (slots[i] == event.getSlot()) {
                        int modelIndex = page * 21 + i;
                        if (modelIndex >= models.size()) break;
                        String modelId = models.get(modelIndex);
                        plugin.getModelManager().applyModel(pet, modelId);
                        clicker.playSound(clicker.getLocation(),
                            Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
                        clicker.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                            clicker.getLocation().add(0,1,0), 50, 0.5, 0.5, 0.5, 0.2);
                        clicker.closeInventory();
                        open();
                        break;
                    }
                }
                break;
        }
    }

    private void fillBorders(Inventory inv) {
        Material[] mats = {Material.PURPLE_STAINED_GLASS_PANE,
            Material.MAGENTA_STAINED_GLASS_PANE};
        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        int i = 0;
        for (int slot : border) {
            ItemStack glass = new ItemStack(mats[i % mats.length]);
            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName(color("§5§l✦"));
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
