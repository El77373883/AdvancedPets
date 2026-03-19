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

public class MainGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final Pet pet;

    public MainGUI(AdvancedPets plugin, Pet pet, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.pet = pet;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54,
            color("§6§l✦ §e§lAdvancedPets §6§l✦ §fMenú Principal"));

        // Bordes con vidrios de colores
        fillBorders(inv);

        // Botones principales
        inv.setItem(10, makeItem(Material.WOLF_SPAWN_EGG, "§e§lTIPO DE MOB",
            "§7Click para elegir", "§7qué mob será tu mascota", "", "§fActual: §e" + pet.getEntityType().name()));

        inv.setItem(12, makeItem(Material.WRITABLE_BOOK, "§b§lCAMBIAR NOMBRE",
            "§7Click para cambiar", "§7el nombre de tu mascota", "", "§fActual: §e" + pet.getName()));

        inv.setItem(14, makeItem(Material.BLAZE_POWDER, "§d§lPARTÍCULAS ✨",
            "§7Click para elegir", "§7partículas de tu mascota", "", "§fActual: §d" + pet.getParticleType().name()));

        inv.setItem(16, makeItem(Material.IRON_PICKAXE, "§a§lTRABAJOS 🔨",
            "§7Click para ver", "§7los trabajos disponibles", "", "§fActual: §a" + pet.getCurrentWork().name()));

        inv.setItem(28, makeItem(Material.DIAMOND_SWORD, "§c§lDAÑO ⚔",
            "§7Usa las flechas para", "§7ajustar el daño", "", "§fDaño actual: §c" + pet.getDamage(),
            "§7Rango: 1-30"));

        inv.setItem(30, makeItem(Material.BOOK, "§e§lSTATS 📊",
            "§fNivel: §e" + pet.getLevel(),
            "§fXP: §b" + (int) pet.getXp() + "§7/§b" + (int) pet.getXpNeeded(),
            "§fKills: §c" + pet.getKills(),
            "§fRareza: " + pet.getRarityColor() + pet.getRarityName(),
            "§fHumor: " + pet.getMoodColor() + pet.getMood().name()));

        inv.setItem(32, makeItem(Material.EXPERIENCE_BOTTLE, "§a§lSUBIR NIVEL ⬆",
            "§7Click para subir", "§7de nivel manualmente",
            "", "§fNivel actual: §a" + pet.getLevel()));

        inv.setItem(34, makeItem(Material.HEART_OF_THE_SEA, "§c§lSALUD ❤",
            "§7Click para cambiar modo", "", "§fModo actual: " +
            (pet.isImmortal() ? "§a§lINMORTAL" : "§c§lMORTAL")));

        inv.setItem(37, makeItem(Material.IRON_SWORD, "§c§lMODO COMBATE ⚔",
            "§7Click para activar/desactivar", "", "§fEstado: " +
            (pet.isCombatMode() ? "§a§lACTIVO ✔" : "§c§lDESACTIVO ✘")));

        inv.setItem(39, makeItem(Material.IRON_PICKAXE, "§a§lMODO TRABAJO 🔧",
            "§7Click para activar/desactivar", "", "§fEstado: " +
            (pet.isWorkMode() ? "§a§lACTIVO ✔" : "§c§lDESACTIVO ✘")));

        inv.setItem(41, makeItem(Material.ENDER_EYE,
            pet.isSummoned() ? "§c§lGUARDAR MASCOTA 📦" : "§a§lINVOCAR MASCOTA 📦",
            "§7Click para " + (pet.isSummoned() ? "guardar" : "invocar"), "§7tu mascota"));

        inv.setItem(43, makeItem(Material.APPLE, "§6§lALIMENTAR 🍎",
            "§fHambre: §e" + (int) pet.getHunger() + "%",
            "§7Click para abrir", "§7menú de alimentar"));

        inv.setItem(46, makeItem(Material.GOLD_INGOT, "§6§lLOGROS 🏆",
            "§7Click para ver", "§7tus logros desbloqueados",
            "", "§fLogros: §6" + plugin.getAchievementManager().getAchievementCount(player.getUniqueId())));

        inv.setItem(48, makeItem(Material.NETHER_STAR, "§e§lRANKING 🌟",
            "§7Click para ver", "§7el ranking del servidor"));

        inv.setItem(50, makeItem(Material.COMPARATOR, "§7§lCONFIGURACIÓN ⚙",
            "§7Click para abrir", "§7opciones de configuración"));

        inv.setItem(52, makeItem(Material.BARRIER, "§c§lELIMINAR MASCOTA 🗑",
            "§7Click para eliminar", "§7tu mascota permanentemente",
            "", "§c§l⚠ Esta acción no se puede deshacer!"));

        // Flechas de daño
        inv.setItem(27, makeItem(Material.ARROW, "§7◄ Bajar daño", "§7Click para bajar el daño"));
        inv.setItem(29, makeItem(Material.ARROW, "§7► Subir daño", "§7Click para subir el daño"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (event.getView().getTitle().equals(color("§6§l✦ §e§lAdvancedPets §6§l✦ §fMenú Principal"))) {
            event.setCancelled(true);
            int slot = event.getSlot();
            switch (slot) {
                case 10: clicker.closeInventory(); new MobGUI(plugin, pet, clicker).open(); break;
                case 14: clicker.closeInventory(); new ParticlesGUI(plugin, pet, clicker).open(); break;
                case 16: clicker.closeInventory(); new WorksGUI(plugin, pet, clicker).open(); break;
                case 27:
                    pet.setDamage(Math.max(1, pet.getDamage() - 1));
                    plugin.getPetManager().savePet(pet);
                    clicker.closeInventory(); open(); break;
                case 29:
                    pet.setDamage(Math.min(30, pet.getDamage() + 1));
                    plugin.getPetManager().savePet(pet);
                    clicker.closeInventory(); open(); break;
                case 32:
                    pet.levelUp();
                    plugin.getPetManager().savePet(pet);
                    plugin.getHologramManager().updateHologram(pet);
                    clicker.sendMessage("§6§l[AdvancedPets] §e" + pet.getName() + " §fdice: §a¡YEAH! ¡Subí de nivel! 🌟 ¡Ahora soy más fuerte!");
                    clicker.playSound(clicker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    clicker.closeInventory(); open(); break;
                case 34:
                    pet.setImmortal(!pet.isImmortal());
                    plugin.getPetManager().savePet(pet);
                    clicker.closeInventory(); open(); break;
                case 37:
                    pet.setCombatMode(!pet.isCombatMode());
                    plugin.getPetManager().savePet(pet);
                    clicker.closeInventory(); open(); break;
                case 39:
                    pet.setWorkMode(!pet.isWorkMode());
                    plugin.getPetManager().savePet(pet);
                    clicker.closeInventory(); open(); break;
                case 41:
                    if (pet.isSummoned()) {
                        plugin.getPetManager().despawnPet(pet);
                        clicker.sendMessage("§e§l[AdvancedPets] §fMascota guardada! 📦");
                    } else {
                        plugin.getPetManager().spawnPet(pet, clicker.getLocation());
                    }
                    clicker.closeInventory(); break;
                case 43:
                    clicker.closeInventory();
                    new FeedGUI(plugin, pet, clicker).open(); break;
                case 50:
                    clicker.closeInventory();
                    new ConfigGUI(plugin, pet, clicker).open(); break;
                case 52:
                    clicker.closeInventory();
                    new ConfirmDeleteGUI(plugin, pet, clicker).open(); break;
            }
        }
    }

    private void fillBorders(Inventory inv) {
        ItemStack[] glasses = {
            makeGlass(Material.RED_STAINED_GLASS_PANE, "§c"),
            makeGlass(Material.ORANGE_STAINED_GLASS_PANE, "§6"),
            makeGlass(Material.YELLOW_STAINED_GLASS_PANE, "§e"),
            makeGlass(Material.LIME_STAINED_GLASS_PANE, "§a"),
            makeGlass(Material.CYAN_STAINED_GLASS_PANE, "§3"),
            makeGlass(Material.BLUE_STAINED_GLASS_PANE, "§9"),
            makeGlass(Material.PURPLE_STAINED_GLASS_PANE, "§5"),
            makeGlass(Material.MAGENTA_STAINED_GLASS_PANE, "§d")
        };
        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        int i = 0;
        for (int slot : border) {
            inv.setItem(slot, glasses[i % glasses.length]);
            i++;
        }
    }

    private ItemStack makeGlass(Material mat, String color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color + "§l✦");
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

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
