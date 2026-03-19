package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HologramManager {

    private final AdvancedPets plugin;
    private final Map<UUID, List<org.bukkit.entity.ArmorStand>> holograms = new HashMap<>();

    public HologramManager(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    public void createHologram(Pet pet) {
        if (!plugin.getConfig().getBoolean("holograms.enabled", true)) return;
        removeHologram(pet);
        if (pet.getEntity() == null) return;

        Location base = pet.getEntity().getLocation().add(0, 2.8, 0);
        List<String> lines = buildHologramLines(pet);
        List<org.bukkit.entity.ArmorStand> stands = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            Location loc = base.clone().subtract(0, i * 0.28, 0);
            org.bukkit.entity.ArmorStand stand = (org.bukkit.entity.ArmorStand)
                loc.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.ARMOR_STAND);
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setSmall(true);
            stand.setCustomNameVisible(true);
            stand.setCustomName(color(lines.get(i)));
            stand.setInvulnerable(true);
            stand.setMarker(true);
            stands.add(stand);
        }
        holograms.put(pet.getPetUUID(), stands);
    }

    public void updateHologram(Pet pet) {
        if (!pet.isSummoned() || pet.getEntity() == null) return;
        removeHologram(pet);
        createHologram(pet);
    }

    public void removeHologram(Pet pet) {
        List<org.bukkit.entity.ArmorStand> stands = holograms.remove(pet.getPetUUID());
        if (stands != null) stands.forEach(org.bukkit.entity.ArmorStand::remove);
    }

    public void removeAllHolograms() {
        for (List<org.bukkit.entity.ArmorStand> stands : holograms.values()) {
            stands.forEach(org.bukkit.entity.ArmorStand::remove);
        }
        holograms.clear();
    }

    private List<String> buildHologramLines(Pet pet) {
        List<String> lines = new ArrayList<>();
        String rc = pet.getRarityColor();
        String raritySymbol = pet.getRarity() == Pet.Rarity.LEGENDARY ? "⚡" :
                             pet.getRarity() == Pet.Rarity.EPIC ? "✦" :
                             pet.getRarity() == Pet.Rarity.RARE ? "✧" : "•";

        lines.add(rc + "§l" + raritySymbol + " " + pet.getRarityName() + " " + raritySymbol);
        lines.add(buildHealthBar(pet));
        lines.add(rc + "§l" + pet.getName());
        lines.add("§7👑 §eNivel §6§l" + pet.getLevel());
        lines.add("§c⚔ §fDaño: §c" + pet.getDamage() +
            "  §a❤ §fVida: §a" + (int) pet.getHealth());

        if (plugin.getConfig().getBoolean("holograms.show-work", true)) {
            String workText = pet.getCurrentWork() == Pet.WorkType.NONE ?
                "§7Descansando" : "§e" + getWorkName(pet.getCurrentWork());
            lines.add("§7🔨 §fTrabajo: " + workText);
        }
        if (plugin.getConfig().getBoolean("holograms.show-mood", true)) {
            lines.add("§7Estado: " + pet.getMoodColor() + getMoodName(pet.getMood()));
        }
        if (plugin.getConfig().getBoolean("holograms.show-particles", true) &&
            pet.getParticleType() != Pet.ParticleType.NONE) {
            lines.add("§7✨ §fPartícula: §d" + pet.getParticleType().name());
        }
        if (plugin.getConfig().getBoolean("holograms.show-kills", true)) {
            lines.add("§7⚔ §fKills: §c" + pet.getKills() +
                "  §7XP: §b" + (int) pet.getXp());
        }
        lines.add("§8" + pet.getEntityType().name().replace("_", " "));
        if (pet.isSleeping()) lines.add("§8§l💤 Zzzzz...");

        return lines;
    }

    private String buildHealthBar(Pet pet) {
        int bars = 10;
        int filled = (int) ((pet.getHealth() / pet.getMaxHealth()) * bars);
        StringBuilder bar = new StringBuilder("§a");
        for (int i = 0; i < bars; i++) {
            if (i == filled) bar.append("§7");
            bar.append("█");
        }
        return "§c❤ " + bar + " §c❤";
    }

    private String getWorkName(Pet.WorkType work) {
        switch (work) {
            case MINING: return "Minando ⛏";
            case FARMING: return "Farmeando 🗡";
            case PVP: return "PVP ⚔";
            case ATTACK: return "Atacando 🏹";
            case BUILD: return "Construyendo 🧱";
            case EXP: return "Haciendo EXP ⭐";
            case CHOP: return "Talando 🪓";
            case HARVEST: return "Cosechando 🌾";
            case COLLECT: return "Recogiendo 🧲";
            case COOK: return "Cocinando 🍳";
            case EXPLORE: return "Explorando 🗺";
            default: return "Descansando";
        }
    }

    private String getMoodName(Pet.Mood mood) {
        switch (mood) {
            case HAPPY: return "§aFeliz 😄";
            case TIRED: return "§bCansado 😴";
            case ANGRY: return "§cEnojado 😡";
            case SAD: return "§8Triste 😢";
            case HUNGRY: return "§6Hambriento 🍖";
            default: return "§aFeliz 😄";
        }
    }

    private String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
