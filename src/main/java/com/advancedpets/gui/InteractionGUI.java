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

public class InteractionGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final Pet pet;

    public InteractionGUI(AdvancedPets plugin, Pet targetPet, Player player) {
        this.plugin = plugin;
        this.pet = targetPet;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27,
            color("§5§l🐾 ¿Qué deseas hacer?"));

        // Fondo negro
        for (int i = 0; i < 27; i++) {
            ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta m = glass.getItemMeta();
            m.setDisplayName(" ");
            glass.setItemMeta(m);
            inv.setItem(i, glass);
        }

        // Info mascota
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(color("§f§lMascota de §e" + pet.getOwnerName()));
        im.setLore(Arrays.asList(
            color(pet.getRarityColor() + "§l" + pet.getName()),
            color("§7Tipo: §f" + pet.getEntityType().name()),
            color("§7Rareza: " + pet.getRarityColor() + pet.getRarityName()),
            color("§7Nivel: §a" + pet.getLevel())
        ));
        info.setItemMeta(im);
        inv.setItem(4, info);

        // Inspeccionar — Vidrio azul
        inv.setItem(10, makeButton(Material.BLUE_STAINED_GLASS_PANE,
            "§9§l👁 INSPECCIONAR MASCOTA",
            "§7Ver todos los stats", "§7de esta mascota"));

        // Retar a duelo — Vidrio rojo
        inv.setItem(12, makeButton(Material.RED_STAINED_GLASS_PANE,
            "§c§l⚔ RETAR A DUELO",
            "§7Envía un reto de duelo", "§7al dueño de esta mascota"));

        // Solicitar adopción — Vidrio verde
        inv.setItem(14, makeButton(Material.GREEN_STAINED_GLASS_PANE,
            "§a§l🤝 SOLICITAR ADOPCIÓN",
            "§7Pide al dueño que", "§7te regale su mascota"));

        // Ofertar alquiler — Vidrio amarillo
        inv.setItem(16, makeButton(Material.YELLOW_STAINED_GLASS_PANE,
            "§e§l💰 OFERTAR ALQUILER",
            "§7Ofrece dinero para", "§7alquilar esta mascota"));

        // Cerrar — Vidrio gris
        inv.setItem(22, makeButton(Material.GRAY_STAINED_GLASS_PANE,
            "§7§l✘ CERRAR",
            "§7Cerrar este menú"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.3f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§5§l🐾 ¿Qué deseas hacer?"))) return;
        event.setCancelled(true);

        switch (event.getSlot()) {
            case 10:
                clicker.closeInventory();
                new InspectGUI(plugin, pet, clicker).open();
                break;
            case 12:
                clicker.closeInventory();
                Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
                if (owner != null) {
                    owner.sendMessage("§c§l[AdvancedPets] §e" + clicker.getName() + " §fte reta a un duelo de mascotas! ⚔");
                    clicker.sendMessage("§a§l[AdvancedPets] §fReto enviado a §e" + pet.getOwnerName());
                }
                break;
            case 14:
                clicker.closeInventory();
                Player owner2 = Bukkit.getPlayer(pet.getOwnerUUID());
                if (owner2 != null) {
                    owner2.sendMessage("§a§l[AdvancedPets] §e" + clicker.getName() + " §fquiere adoptar a §e" + pet.getName() + "§f! Usa §e/ap adopt accept §fpara aceptar.");
                    clicker.sendMessage("§a§l[AdvancedPets] §fSolicitud de adopción enviada a §e" + pet.getOwnerName());
                }
                break;
            case 16:
                clicker.closeInventory();
                Player owner3 = Bukkit.getPlayer(pet.getOwnerUUID());
                if (owner3 != null) {
                    owner3.sendMessage("§e§l[AdvancedPets] §e" + clicker.getName() + " §fquiere alquilar a §e" + pet.getName() + "§f!");
                    clicker.sendMessage("§e§l[AdvancedPets] §fOferta de alquiler enviada a §e" + pet.getOwnerName());
                }
                break;
            case 22:
                clicker.closeInventory();
                break;
        }
    }

    private ItemStack makeButton(Material mat, String name, String... lore) {
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
