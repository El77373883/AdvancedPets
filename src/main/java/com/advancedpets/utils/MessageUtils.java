package com.advancedpets.utils;

import com.advancedpets.AdvancedPets;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtils {

    private final AdvancedPets plugin;
    private final String prefix;

    public MessageUtils(AdvancedPets plugin) {
        this.plugin = plugin;
        this.prefix = color(plugin.getConfig().getString("prefix",
            "&6&l[&e&lAdvancedPets&6&l] &r"));
    }

    public void send(Player player, String message) {
        player.sendMessage(prefix + color(message));
    }

    public void sendRaw(Player player, String message) {
        player.sendMessage(color(message));
    }

    public void sendTitle(Player player, String title, String subtitle) {
        player.sendTitle(color(title), color(subtitle), 10, 60, 10);
    }

    public void sendNoPermission(Player player) {
        send(player, plugin.getConfig().getString("messages.no-permission",
            "&cNo tienes permiso!"));
    }

    public void sendNoMoney(Player player) {
        send(player, plugin.getConfig().getString("messages.no-money",
            "&cNo tienes suficiente dinero!"));
    }

    public void sendCreatorMessage(Player player) {
        player.sendMessage(color("§r"));
        player.sendMessage(color("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦"));
        player.sendMessage(color("§e§l       ⭐ ADVANCED PETS ⭐"));
        player.sendMessage(color("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦"));
        player.sendMessage(color("§r"));
        player.sendMessage(color("§d§l    🎮 PLUGIN CREADO CON ❤ POR 🎮"));
        player.sendMessage(color("§r"));
        player.sendMessage(color("§f§l   ╔══════════════════════════════╗"));
        player.sendMessage(color("§f§l   ║  §e§l★ §6§lsoyadrianyt001 §e§l★  §f§l  ║"));
        player.sendMessage(color("§f§l   ╚══════════════════════════════╝"));
        player.sendMessage(color("§r"));
        player.sendMessage(color("§b§l  ✦ §fEl mejor plugin de mascotas §b§l✦"));
        player.sendMessage(color("§b§l  ✦ §fPara Minecraft 1.21.1       §b§l✦"));
        player.sendMessage(color("§b§l  ✦ §fVersion: §a§l1.0.0 PREMIUM  §b§l✦"));
        player.sendMessage(color("§r"));
        player.sendMessage(color("§5§l  ⚡ §d100% Hecho a mano con amor ⚡"));
        player.sendMessage(color("§5§l  ⚡ §dEl plugin mas PERRON del servidor!"));
        player.sendMessage(color("§r"));
        player.sendMessage(color("§e§l  🐾 AdvancedPets — §6§lPREMIUM 🐾"));
        player.sendMessage(color("§r"));
        player.sendMessage(color("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦"));
    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
