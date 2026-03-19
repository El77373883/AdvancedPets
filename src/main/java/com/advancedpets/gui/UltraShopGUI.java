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

public class UltraShopGUI implements Listener {

    private final AdvancedPets plugin;
    private final Player player;

    public enum UltraEffect {
        LIGHTNING_STORM("⚡ Tormenta de Rayos", 50000, Material.LIGHTNING_ROD,
            "§eCaen 5 rayos reales", "§ealrededor del enemigo muerto"),
        FIRE_APOCALYPSE("🔥 Apocalipsis de Fuego", 45000, Material.BLAZE_ROD,
            "§cExplosión de llamas", "§cgigante al matar"),
        DARK_SOUL("💀 Alma Oscura", 55000, Material.SOUL_LANTERN,
            "§3Cráneo de partículas", "§3de alma azul"),
        DEATH_TORNADO("🌪️ Tornado de la Muerte", 48000, Material.GRAY_DYE,
            "§7Remolino gigante", "§7de partículas"),
        DRAGON_ROAR("🐉 Rugido del Dragón", 60000, Material.DRAGON_BREATH,
            "§5Sonido de dragón", "§5+ partículas moradas"),
        STAR_NOVA("✨ Nova Estelar", 52000, Material.NETHER_STAR,
            "§eExplosión de estrellas", "§edoradas"),
        TSUNAMI("🌊 Tsunami", 44000, Material.WATER_BUCKET,
            "§9Ola gigante", "§9de partículas"),
        SUPERNOVA("💥 Supernova", 65000, Material.FIREWORK_ROCKET,
            "§dExplosión épica", "§dde colores mezclados"),
        DEATH_CURSE("☠ Maldición", 58000, Material.WITHER_SKELETON_SKULL,
            "§8Calaveras flotantes", "§8sobre el enemigo"),
        RAINBOW_EXPLOSION("🌈 Arcoíris Explosivo", 47000, Material.WHITE_DYE,
            "§fExplosión de todos", "§flos colores"),
        ROYAL_CROWN("👑 Corona Real", 70000, Material.GOLD_BLOCK,
            "§6Corona dorada", "§6sobre la víctima"),
        DARK_ECLIPSE("🌑 Eclipse Oscuro", 75000, Material.OBSIDIAN,
            "§8Todo se oscurece", "§8lluvia de partículas negras");

        public final String displayName;
        public final double price;
        public final Material icon;
        public final String[] description;

        UltraEffect(String displayName, double price, Material icon, String... description) {
            this.displayName = displayName;
            this.price = price;
            this.icon = icon;
            this.description = description;
        }
    }

    public UltraShopGUI(AdvancedPets plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open() {
        Inventory inv = Bukkit.createInventory(null, 54,
            color("§6§l👑 Efectos ULTRA PREMIUM 👑"));

        fillBorders(inv);

        // Título decorativo
        ItemStack title = new ItemStack(Material.BEACON);
        ItemMeta tm = title.getItemMeta();
        tm.setDisplayName(color("§6§l✦ EFECTOS ULTRA PREMIUM ✦"));
        tm.setLore(Arrays.asList(
            color("§7Efectos épicos al matar enemigos"),
            color("§7Solo los más ricos los tienen"),
            color(""),
            color("§fSaldo: §6§l$" + (int) plugin.getEconomyManager().getBalance(player))
        ));
        title.setItemMeta(tm);
        inv.setItem(4, title);

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        UltraEffect[] effects = UltraEffect.values();

        for (int i = 0; i < Math.min(effects.length, slots.length); i++) {
            UltraEffect effect = effects[i];
            boolean owned = hasEffect(player, effect);
            inv.setItem(slots[i], makeEffectItem(effect, owned));
        }

        // Botón regresar
        inv.setItem(49, makeItem(Material.ARROW, "§e§l🔙 REGRESAR",
            "§7Volver a la tienda principal"));

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
        player.sendMessage("§6§l[AdvancedPets] §e¡Bienvenido a la tienda ULTRA PREMIUM! 👑");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player clicker = (Player) event.getWhoClicked();
        if (!clicker.equals(player)) return;
        if (!event.getView().getTitle().equals(color("§6§l👑 Efectos ULTRA PREMIUM 👑"))) return;
        event.setCancelled(true);

        if (event.getSlot() == 49) {
            clicker.closeInventory();
            new ShopGUI(plugin, clicker).open();
            return;
        }

        int[] slots = {10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34};
        UltraEffect[] effects = UltraEffect.values();

        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == event.getSlot() && i < effects.length) {
                UltraEffect effect = effects[i];
                if (hasEffect(clicker, effect)) {
                    // Activar/desactivar efecto
                    toggleEffect(clicker, effect);
                } else {
                    // Comprar efecto
                    clicker.closeInventory();
                    new UltraBuyConfirmGUI(plugin, clicker, effect).open();
                }
                return;
            }
        }
    }

    private boolean hasEffect(Player player, UltraEffect effect) {
        return plugin.getConfig().getBoolean(
            "ultra-effects." + player.getUniqueId() + "." + effect.name(), false);
    }

    private void toggleEffect(Player player, UltraEffect effect) {
        String path = "ultra-effects-active." + player.getUniqueId();
        String current = plugin.getConfig().getString(path, "NONE");
        if (current.equals(effect.name())) {
            plugin.getConfig().set(path, "NONE");
            plugin.saveConfig();
            player.sendMessage("§c§l[AdvancedPets] §fEfecto §e" + effect.displayName + " §fdesactivado!");
        } else {
            plugin.getConfig().set(path, effect.name());
            plugin.saveConfig();
            player.sendMessage("§6§l[AdvancedPets] §fEfecto §e§l" + effect.displayName + " §f¡ACTIVADO! 🎆");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        }
        player.closeInventory();
        open();
    }

    public static String getActiveEffect(AdvancedPets plugin, Player player) {
        return plugin.getConfig().getString(
            "ultra-effects-active." + player.getUniqueId(), "NONE");
    }

    public static void playKillEffect(AdvancedPets plugin, Player killer, Location deathLoc) {
        String effectName = getActiveEffect(plugin, killer);
        if (effectName.equals("NONE")) return;
        try {
            UltraEffect effect = UltraEffect.valueOf(effectName);
            World world = deathLoc.getWorld();
            if (world == null) return;

            switch (effect) {
                case LIGHTNING_STORM:
                    for (int i = 0; i < 5; i++) {
                        Location l = deathLoc.clone().add(
                            (Math.random() - 0.5) * 4, 0, (Math.random() - 0.5) * 4);
                        world.strikeLightningEffect(l);
                        world.spawnParticle(Particle.ELECTRIC_SPARK, l, 30, 0.5, 1, 0.5, 0);
                    }
                    world.playSound(deathLoc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2f, 0.8f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case FIRE_APOCALYPSE:
                    world.spawnParticle(Particle.FLAME, deathLoc, 200, 2, 2, 2, 0.3);
                    world.spawnParticle(Particle.LAVA, deathLoc, 50, 1, 1, 1, 0.1);
                    world.createExplosion(deathLoc, 0f, false, false);
                    world.playSound(deathLoc, Sound.ENTITY_BLAZE_DEATH, 2f, 0.6f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case DARK_SOUL:
                    for (int y = 0; y < 3; y++) {
                        world.spawnParticle(Particle.SOUL, deathLoc.clone().add(0, y, 0),
                            20, 0.5, 0.1, 0.5, 0.02);
                    }
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, deathLoc.clone().add(0, 2, 0),
                        50, 0.3, 0.3, 0.3, 0.05);
                    world.playSound(deathLoc, Sound.ENTITY_WITHER_AMBIENT, 1f, 0.5f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case DEATH_TORNADO:
                    for (int i = 0; i < 30; i++) {
                        double angle = i * 12 * Math.PI / 180;
                        double radius = i * 0.1;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        world.spawnParticle(Particle.CLOUD,
                            deathLoc.clone().add(x, i * 0.1, z), 2, 0, 0, 0, 0);
                        world.spawnParticle(Particle.LARGE_SMOKE,
                            deathLoc.clone().add(x, i * 0.1, z), 1, 0, 0, 0, 0);
                    }
                    world.playSound(deathLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 0.5f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case DRAGON_ROAR:
                    world.spawnParticle(Particle.DRAGON_BREATH, deathLoc, 200, 2, 2, 2, 0.3);
                    world.spawnParticle(Particle.WITCH, deathLoc, 100, 1, 2, 1, 0.2);
                    world.playSound(deathLoc, Sound.ENTITY_ENDER_DRAGON_GROWL, 2f, 0.8f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case STAR_NOVA:
                    world.spawnParticle(Particle.TOTEM_OF_UNDYING, deathLoc, 300, 2, 2, 2, 0.5);
                    world.spawnParticle(Particle.ENCHANT, deathLoc, 100, 2, 2, 2, 1);
                    world.playSound(deathLoc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 2f, 1.2f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case TSUNAMI:
                    for (int x = -3; x <= 3; x++) {
                        for (int z = -3; z <= 3; z++) {
                            world.spawnParticle(Particle.SPLASH,
                                deathLoc.clone().add(x, 0, z), 10, 0, 1, 0, 0.3);
                            world.spawnParticle(Particle.BUBBLE_POP,
                                deathLoc.clone().add(x, 1, z), 5, 0, 0.5, 0, 0.1);
                        }
                    }
                    world.playSound(deathLoc, Sound.WEATHER_RAIN_ABOVE, 2f, 0.5f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case SUPERNOVA:
                    Random rand = new Random();
                    for (int i = 0; i < 20; i++) {
                        Color color = Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
                        world.spawnParticle(Particle.DUST, deathLoc, 15, 2, 2, 2,
                            new Particle.DustOptions(color, 3f));
                    }
                    world.spawnParticle(Particle.FIREWORK, deathLoc, 200, 3, 3, 3, 0.5);
                    world.playSound(deathLoc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 0.5f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case DEATH_CURSE:
                    for (int i = 0; i < 5; i++) {
                        world.spawnParticle(Particle.LARGE_SMOKE,
                            deathLoc.clone().add((Math.random()-0.5)*3, i*0.5, (Math.random()-0.5)*3),
                            10, 0.2, 0.1, 0.2, 0.02);
                    }
                    world.spawnParticle(Particle.WITCH, deathLoc.clone().add(0, 2, 0),
                        100, 0.5, 0.5, 0.5, 0.1);
                    world.playSound(deathLoc, Sound.ENTITY_WITHER_DEATH, 1f, 0.6f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case RAINBOW_EXPLOSION:
                    Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW,
                        Color.GREEN, Color.BLUE, Color.PURPLE, Color.FUCHSIA};
                    for (Color c : colors) {
                        world.spawnParticle(Particle.DUST, deathLoc, 30, 2, 2, 2,
                            new Particle.DustOptions(c, 2f));
                    }
                    world.playSound(deathLoc, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2f, 1f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case ROYAL_CROWN:
                    double[] angles = {0, 60, 120, 180, 240, 300};
                    for (double angle : angles) {
                        double rad = Math.toRadians(angle);
                        Location point = deathLoc.clone().add(
                            Math.cos(rad), 2.5, Math.sin(rad));
                        world.spawnParticle(Particle.TOTEM_OF_UNDYING, point, 20, 0.1, 0.1, 0.1, 0.1);
                        world.spawnParticle(Particle.DUST, point, 10, 0,0,0,
                            new Particle.DustOptions(Color.YELLOW, 2f));
                    }
                    world.playSound(deathLoc, Sound.BLOCK_BELL_USE, 2f, 0.8f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;

                case DARK_ECLIPSE:
                    for (Player nearby : world.getPlayers()) {
                        if (nearby.getLocation().distance(deathLoc) < 20) {
                            nearby.addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0));
                        }
                    }
                    world.spawnParticle(Particle.LARGE_SMOKE, deathLoc, 300, 5, 5, 5, 0.1);
                    world.spawnParticle(Particle.ASH, deathLoc, 200, 3, 3, 3, 0.3);
                    world.playSound(deathLoc, Sound.ENTITY_WITHER_SPAWN, 1f, 0.5f);
                    broadcastKillEffect(plugin, killer, effect);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void broadcastKillEffect(AdvancedPets plugin, Player killer, UltraEffect effect) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage("§6§l[AdvancedPets] §e⚡ " + killer.getName() +
                " §fusó el efecto §6§l" + effect.displayName + " §fen su kill! 💀");
        }
    }

    private ItemStack makeEffectItem(UltraEffect effect, boolean owned) {
        ItemStack item = new ItemStack(effect.icon);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color("§6§l" + effect.displayName));
        List<String> lore = new ArrayList<>();
        for (String d : effect.description) lore.add(color(d));
        lore.add(color(""));
        lore.add(color("§fPrecio: §6§l$" + (int) effect.price));
        lore.add(color(""));
        if (owned) {
            String active = plugin.getConfig().getString(
                "ultra-effects-active." + player.getUniqueId(), "NONE");
            if (active.equals(effect.name())) {
                lore.add(color("§a§l✔ ACTIVO — Click para desactivar"));
            } else {
                lore.add(color("§e§l✔ COMPRADO — Click para activar"));
            }
        } else {
            lore.add(color("§c§lNo comprado — Click para comprar"));
        }
        meta.setLore(lore);
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

    private void fillBorders(Inventory inv) {
        Material[] mats = {Material.BLACK_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE};
        int[] border = {0,1,2,3,4,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,49,50,51,52,53};
        int i = 0;
        for (int slot : border) {
            ItemStack glass = new ItemStack(mats[i % mats.length]);
            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName(color(i % 2 == 0 ? "§8§l✦" : "§6§l✦"));
            glass.setItemMeta(meta);
            inv.setItem(slot, glass);
            i++;
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
