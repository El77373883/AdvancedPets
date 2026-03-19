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

public class FeedGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final Pet pet;

    public FeedGUI(AdvancedPets plugin, Pet pet, Player player) {
        this.plugin = plugin;
        this.pet = pet;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27,
            color("§6§l🍎 Alimentar: §e" + pet.getName()));

        // Fondo
        for (int i = 0; i < 27; i++) {
            ItemStack glass = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
            ItemMeta m = glass.getItemMeta();
            m.setDisplayName("§6§l✦");
            glass.setItemMeta(m);
            inv.setItem(i, glass);
        }

        // Info de hambre
        int hunger = (int) pet.getHunger();
        String bar = buildHungerBar(hunger);
        ItemStack info = new ItemStack(Material.PAPER);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(color("§6§lEstado de Hambre"));
        im.setLore(Arrays.asList(
            color("§f" + bar),
            color("§fHambre: §e" + hunger + "%"),
            color(""),
            color(hunger >= 80 ? "§a¡Bien alimentado! 😄" :
                  hunger >= 40 ? "§eAlgo de hambre... 😐" :
                  "§c¡Mucha hambre! 😭")
        ));
        info.setItemMeta(im);
        inv.setItem(4, info);

        // Slot de comida — manzana
        ItemStack slot = new ItemStack(Material.AIR);
        inv.setItem(13, slot);

        // Label del slot
        ItemStack label = new ItemStack(Material.APPLE);
        ItemMeta lm = label.getItemMeta();
        lm.setDisplayName(color("§a§lPon una 🍎 MANZANA aquí"));
        lm.setLore(Arrays.asList(
            color("§7Pon una manzana en este slot"),
            color("§7para alimentar a tu mascota"),
            color(""),
            color("§eClick con manzana en mano!")
        ));
        label.setItemMeta(lm);
        inv.setItem(11, label);

        // Botón regresar
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(color("§e§l🔙 REGRESAR"));
        back.setItemMeta(bm);
        inv.setItem(22, back);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1.2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§6§l🍎 Alimentar: §e" + pet.getName()))) return;

        if (event.getSlot() == 22) {
            event.setCancelled(true);
            clicker.closeInventory();
            new MainGUI(plugin, pet, clicker).open();
            return;
        }

        if (event.getSlot() == 13) {
            ItemStack cursor = event.getCursor();
            if (cursor != null && cursor.getType() == Material.APPLE) {
                event.setCancelled(true);
                cursor.setAmount(cursor.getAmount() - 1);
                plugin.getPetManager().feedPet(pet, clicker);
                clicker.closeInventory();
                open();
            } else {
                event.setCancelled(true);
                clicker.sendMessage("§c§l[AdvancedPets] §c¡Solo puedes dar manzanas! 🍎");
            }
            return;
        }
        event.setCancelled(true);
    }

    private String buildHungerBar(int hunger) {
        int filled = hunger / 10;
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < 10; i++) {
            if (i == filled) bar.append("§7");
            bar.append("█");
        }
        return bar.toString();
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
