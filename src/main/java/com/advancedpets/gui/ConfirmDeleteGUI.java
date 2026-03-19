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

public class ConfirmDeleteGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final Pet pet;

    public ConfirmDeleteGUI(AdvancedPets plugin, Pet pet, Player player) {
        this.plugin = plugin;
        this.pet = pet;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27,
            color("§c§l⚠ ¿Eliminar mascota?"));

        for (int i = 0; i < 27; i++) {
            ItemStack glass = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta m = glass.getItemMeta();
            m.setDisplayName("§c§l✘");
            glass.setItemMeta(m);
            inv.setItem(i, glass);
        }

        ItemStack warn = new ItemStack(Material.BARRIER);
        ItemMeta wm = warn.getItemMeta();
        wm.setDisplayName(color("§c§l⚠ ADVERTENCIA ⚠"));
        wm.setLore(Arrays.asList(
            color("§fEstás a punto de eliminar"),
            color("§e§l" + pet.getName()),
            color("§c§lEsta acción NO se puede deshacer!"),
            color(""),
            color("§7Nivel: §a" + pet.getLevel()),
            color("§7Kills: §c" + pet.getKills())
        ));
        warn.setItemMeta(wm);
        inv.setItem(13, warn);

        ItemStack yes = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta ym = yes.getItemMeta();
        ym.setDisplayName(color("§a§l✔ SÍ, ELIMINAR"));
        ym.setLore(Collections.singletonList(color("§7Click para confirmar eliminación")));
        yes.setItemMeta(ym);
        inv.setItem(11, yes);

        ItemStack no = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta nm = no.getItemMeta();
        nm.setDisplayName(color("§7§l🔙 CANCELAR"));
        nm.setLore(Collections.singletonList(color("§7Volver al menú principal")));
        no.setItemMeta(nm);
        inv.setItem(15, no);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 0.5f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§c§l⚠ ¿Eliminar mascota?"))) return;
        event.setCancelled(true);

        if (event.getSlot() == 11) {
            plugin.getPetManager().removePet(clicker.getUniqueId());
            clicker.sendMessage("§c§l[AdvancedPets] §fMascota §e" + pet.getName() + " §feliminada permanentemente.");
            clicker.playSound(clicker.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);
            clicker.closeInventory();
        } else if (event.getSlot() == 15) {
            clicker.closeInventory();
            new MainGUI(plugin, pet, clicker).open();
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
