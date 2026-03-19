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

public class BuyConfirmGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final EntityType type;
    private final Pet.Rarity rarity;
    private final double price;

    public BuyConfirmGUI(AdvancedPets plugin, Player player, EntityType type, Pet.Rarity rarity, double price) {
        this.plugin = plugin;
        this.player = player;
        this.type = type;
        this.rarity = rarity;
        this.price = price;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27,
            color("§6§l¿Comprar esta mascota?"));

        // Fondo de vidrios
        for (int i = 0; i < 27; i++) {
            ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
            ItemMeta m = glass.getItemMeta();
            m.setDisplayName(" ");
            glass.setItemMeta(m);
            inv.setItem(i, glass);
        }

        // Info de la mascota en el centro
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();
        String rc = rarity == Pet.Rarity.COMMON ? "§f" : rarity == Pet.Rarity.RARE ? "§9" :
                    rarity == Pet.Rarity.EPIC ? "§5" : "§6";
        im.setDisplayName(rc + "§l" + type.name().replace("_"," "));
        im.setLore(Arrays.asList(
            color("§7Rareza: " + rc + rarity.name()),
            color("§7Precio: §6§l$" + (int) price),
            color(""),
            color("§fSaldo: §6$" + (int) plugin.getEconomyManager().getBalance(player))
        ));
        info.setItemMeta(im);
        inv.setItem(13, info);

        // SÍ — Vidrio verde
        ItemStack yes = new ItemStack(Material.GREEN_STAINED_GLASS_PANE, 1);
        ItemMeta ym = yes.getItemMeta();
        ym.setDisplayName(color("§a§l✔ SÍ, COMPRAR"));
        ym.setLore(Arrays.asList(color("§7Click para confirmar"), color("§7la compra de tu mascota")));
        yes.setItemMeta(ym);
        inv.setItem(11, yes);

        // NO — Vidrio rojo
        ItemStack no = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta nm = no.getItemMeta();
        nm.setDisplayName(color("§c§l✘ CANCELAR"));
        nm.setLore(Arrays.asList(color("§7Click para cancelar"), color("§7la compra")));
        no.setItemMeta(nm);
        inv.setItem(15, no);

        // REGRESAR — Vidrio gris
        ItemStack back = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(color("§7§l🔙 REGRESAR"));
        bm.setLore(Collections.singletonList(color("§7Volver a la tienda")));
        back.setItemMeta(bm);
        inv.setItem(22, back);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§6§l¿Comprar esta mascota?"))) return;
        event.setCancelled(true);

        if (event.getSlot() == 11) {
            // Confirmar compra
            if (plugin.getPetManager().hasPet(player.getUniqueId())) {
                clicker.sendMessage("§c§l[AdvancedPets] §c¡Ya tienes una mascota! Elimínala primero.");
                clicker.closeInventory();
                return;
            }
            if (!plugin.getEconomyManager().chargeMoney(clicker, price)) {
                plugin.getMessageUtils().sendNoMoney(clicker);
                clicker.closeInventory();
                return;
            }
            Pet newPet = new Pet(clicker.getUniqueId(), clicker.getName(),
                clicker.getName() + "'s " + type.name(), type, rarity);
            plugin.getPetManager().addPet(newPet);
            plugin.getPetManager().spawnPet(newPet, clicker.getLocation());
            clicker.closeInventory();

        } else if (event.getSlot() == 15) {
            clicker.sendMessage("§c§l[AdvancedPets] §fCompra cancelada.");
            clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            clicker.closeInventory();

        } else if (event.getSlot() == 22) {
            clicker.closeInventory();
            new ShopGUI(plugin, clicker).open();
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
