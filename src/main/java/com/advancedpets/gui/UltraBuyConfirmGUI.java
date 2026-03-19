package com.advancedpets.gui;

import com.advancedpets.AdvancedPets;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class UltraBuyConfirmGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;
    private final UltraShopGUI.UltraEffect effect;

    public UltraBuyConfirmGUI(AdvancedPets plugin, Player player, UltraShopGUI.UltraEffect effect) {
        this.plugin = plugin;
        this.player = player;
        this.effect = effect;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 27,
            color("§6§l¿Comprar efecto ULTRA?"));

        // Fondo negro dorado
        for (int i = 0; i < 27; i++) {
            Material mat = i % 2 == 0 ?
                Material.BLACK_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE;
            ItemStack glass = new ItemStack(mat);
            ItemMeta m = glass.getItemMeta();
            m.setDisplayName(i % 2 == 0 ? "§8§l✦" : "§6§l✦");
            glass.setItemMeta(m);
            inv.setItem(i, glass);
        }

        // Info efecto
        ItemStack info = new ItemStack(effect.icon);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(color("§6§l" + effect.displayName));
        List<String> lore = new ArrayList<>();
        for (String d : effect.description) lore.add(color(d));
        lore.add(color(""));
        lore.add(color("§fPrecio: §6§l$" + (int) effect.price));
        lore.add(color("§fSaldo: §6$" + (int) plugin.getEconomyManager().getBalance(player)));
        im.setLore(lore);
        info.setItemMeta(im);
        inv.setItem(13, info);

        // SÍ — verde
        ItemStack yes = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta ym = yes.getItemMeta();
        ym.setDisplayName(color("§a§l✔ SÍ, COMPRAR"));
        ym.setLore(Arrays.asList(
            color("§7Confirmar compra del"),
            color("§7efecto §6" + effect.displayName)));
        yes.setItemMeta(ym);
        inv.setItem(11, yes);

        // NO — rojo
        ItemStack no = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta nm = no.getItemMeta();
        nm.setDisplayName(color("§c§l✘ CANCELAR"));
        no.setItemMeta(nm);
        inv.setItem(15, no);

        // REGRESAR — gris
        ItemStack back = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(color("§7§l🔙 REGRESAR"));
        back.setItemMeta(bm);
        inv.setItem(22, back);

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1.2f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§6§l¿Comprar efecto ULTRA?"))) return;
        event.setCancelled(true);

        if (event.getSlot() == 11) {
            if (!plugin.getEconomyManager().chargeMoney(clicker, effect.price)) {
                plugin.getMessageUtils().sendNoMoney(clicker);
                clicker.closeInventory();
                return;
            }
            // Guardar efecto comprado
            plugin.getConfig().set(
                "ultra-effects." + clicker.getUniqueId() + "." + effect.name(), true);
            // Activar automáticamente
            plugin.getConfig().set(
                "ultra-effects-active." + clicker.getUniqueId(), effect.name());
            plugin.saveConfig();

            clicker.sendMessage("§r");
            clicker.sendMessage("§6§l⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡");
            clicker.sendMessage("§e§l   👑 ¡EFECTO ULTRA COMPRADO! 👑");
            clicker.sendMessage("§f  Efecto: §6§l" + effect.displayName);
            clicker.sendMessage("§f  Estado: §a§lACTIVADO ✔");
            clicker.sendMessage("§6§l⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡");
            clicker.sendMessage("§r");
            clicker.playSound(clicker.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
            clicker.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                clicker.getLocation().add(0, 1, 0), 200, 1, 1, 1, 0.5);

            // Mensaje global
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("§6§l[AdvancedPets] §e⚡ " + clicker.getName() +
                    " §facaba de comprar el efecto §6§l" + effect.displayName + "§f! 👑");
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
            }
            clicker.closeInventory();

        } else if (event.getSlot() == 15) {
            clicker.sendMessage("§c§l[AdvancedPets] §fCompra cancelada.");
            clicker.closeInventory();

        } else if (event.getSlot() == 22) {
            clicker.closeInventory();
            new UltraShopGUI(plugin, clicker).open();
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
