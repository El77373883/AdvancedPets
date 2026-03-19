package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.hologram.Hologram;
import de.oliver.fancyholograms.api.data.TextHologramData;
import org.bukkit.Location;

import java.util.*;

public class HologramManager {

    private final AdvancedPets plugin;
    private final Map<UUID, Hologram> holograms = new HashMap<>();

    public HologramManager(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    public void createHologram(Pet pet) {
        if (!plugin.getConfig().getBoolean("holograms.enabled", true)) return;
        removeHologram(pet);
        Location loc = pet.getEntity().getLocation().add(0, 2.5, 0);
        List<String> lines = buildHologramLines(pet);
        TextHologramData data = new TextHologramData(
            "advancedpets_" + pet.getPetUUID().toString(),
            loc
        );
        data.setText(lines);
        Hologram hologram = FancyHologramsPlugin.get().getHologramManager().create(data);
        hologram.createHologram();
        holograms.put(pet.getPetUUID(), hologram);
    }

    public void updateHologram(Pet pet) {
        if (!pet.isSummoned() || pet.getEntity() == null) return;
        Hologram hologram = holograms.get(pet.getPetUUID());
        if (hologram == null) { createHologram(pet); return; }
        List<String> lines = buildHologramLines(pet);
        hologram.getData().setText(lines);
        hologram.setLocation(pet.getEntity().getLocation().add(0, 2.5, 0));
        hologram.updateHologram();
    }

    public void removeHologram(Pet pet) {
        Hologram hologram = holograms.remove(pet.getPetUUID());
        if (hologram != null) hologram.deleteHologram();
    }

    public void removeAllHolograms() {
        for (Hologram h : holograms.values()) h.deleteHologram();
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
        lines.add("§c⚔ §fDaño: §c" + pet.getDamage() + "  §a❤ §fVida: §a" + (int)pet.getHealth());

        if (plugin.getConfig().getBoolean("holograms.show-work", true)) {
            String workText = pet.getCurrentWork() == Pet.WorkType.NONE ? "§7Descansando" : "§e" + getWorkName(pet.getCurrentWork());
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
            lines.add("§7⚔ §fKills: §c" + pet.getKills() + "  §7XP: §b" + (int)pet.getXp());
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
        return "§c❤ " + bar.toString() + " §c❤";
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
}
