package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.*;

import java.util.*;

public class HologramManager {

    private final AdvancedPets plugin;
    private final Map<UUID, List<ArmorStand>> holograms = new HashMap<>();

    // ✅ Altura del holograma según tamaño del mob
    private static final Map<String, Double> MOB_HEIGHT = new HashMap<>();

    static {
        // Mobs GRANDES — holograma bien arriba
        MOB_HEIGHT.put("ENDER_DRAGON", 5.0);
        MOB_HEIGHT.put("WITHER", 4.5);
        MOB_HEIGHT.put("WARDEN", 4.0);
        MOB_HEIGHT.put("IRON_GOLEM", 4.0);
        MOB_HEIGHT.put("RAVAGER", 3.5);
        MOB_HEIGHT.put("ELDER_GUARDIAN", 3.5);
        MOB_HEIGHT.put("GUARDIAN", 3.0);
        MOB_HEIGHT.put("HORSE", 3.2);
        MOB_HEIGHT.put("CAMEL", 3.5);
        MOB_HEIGHT.put("POLAR_BEAR", 3.2);
        MOB_HEIGHT.put("COW", 3.0);
        MOB_HEIGHT.put("LLAMA", 3.2);
        MOB_HEIGHT.put("PANDA", 3.0);
        MOB_HEIGHT.put("HOGLIN", 3.0);
        MOB_HEIGHT.put("PHANTOM", 3.0);
        MOB_HEIGHT.put("ENDERMAN", 4.0);

        // Mobs MEDIANOS
        MOB_HEIGHT.put("WOLF", 2.8);
        MOB_HEIGHT.put("ZOMBIE", 2.8);
        MOB_HEIGHT.put("SKELETON", 2.8);
        MOB_HEIGHT.put("CREEPER", 2.8);
        MOB_HEIGHT.put("ZOMBIE_VILLAGER", 2.8);
        MOB_HEIGHT.put("VILLAGER", 2.8);
        MOB_HEIGHT.put("PIGLIN", 2.8);
        MOB_HEIGHT.put("PIGLIN_BRUTE", 2.8);
        MOB_HEIGHT.put("WITCH", 2.8);
        MOB_HEIGHT.put("VINDICATOR", 2.8);
        MOB_HEIGHT.put("PILLAGER", 2.8);
        MOB_HEIGHT.put("VEX", 2.5);
        MOB_HEIGHT.put("BLAZE", 2.8);
        MOB_HEIGHT.put("SHULKER", 2.5);
        MOB_HEIGHT.put("SHEEP", 2.5);
        MOB_HEIGHT.put("PIG", 2.5);
        MOB_HEIGHT.put("SPIDER", 2.2);
        MOB_HEIGHT.put("CAVE_SPIDER", 2.0);

        // Mobs PEQUEÑOS — holograma más bajo
        MOB_HEIGHT.put("CAT", 2.0);
        MOB_HEIGHT.put("RABBIT", 1.8);
        MOB_HEIGHT.put("CHICKEN", 1.8);
        MOB_HEIGHT.put("COD", 1.5);
        MOB_HEIGHT.put("SQUID", 2.0);
        MOB_HEIGHT.put("FROG", 1.8);
        MOB_HEIGHT.put("BEE", 2.0);
        MOB_HEIGHT.put("ARMADILLO", 1.8);

        // Mobs MUY PEQUEÑOS — solo 1 línea encima
        // ✅ TORTUGA y mobs pegados al suelo
        MOB_HEIGHT.put("TURTLE", 1.5);
        MOB_HEIGHT.put("SLIME", 2.0);
        MOB_HEIGHT.put("MAGMA_CUBE", 2.0);
        MOB_HEIGHT.put("SNOW_GOLEM", 2.5);
    }

    // ✅ Mobs donde el holograma se ve muy feo
    // Solo mostrar nombre y nivel (1 línea)
    private static final Set<String> MINIMAL_HOLO = new HashSet<>(Arrays.asList(
        "TURTLE", "COD", "SALMON", "TROPICAL_FISH",
        "PUFFERFISH", "SQUID", "GLOW_SQUID",
        "BEE", "RABBIT", "CHICKEN", "FROG",
        "ARMADILLO", "CAVE_SPIDER", "SLIME",
        "MAGMA_CUBE"
    ));

    public HologramManager(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    public void createHologram(Pet pet) {
        if (!plugin.getConfig().getBoolean("holograms.enabled", true)) return;
        removeHologram(pet);
        if (pet.getEntity() == null) return;

        double height = getHologramHeight(pet);
        Location base = pet.getEntity().getLocation().add(0, height, 0);
        List<String> lines = buildHologramLines(pet);
        List<ArmorStand> stands = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            Location loc = base.clone().subtract(0, i * 0.28, 0);
            ArmorStand stand = (ArmorStand) loc.getWorld()
                .spawnEntity(loc, EntityType.ARMOR_STAND);
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setSmall(true);
            stand.setCustomNameVisible(true);
            stand.setCustomName(color(lines.get(i)));
            stand.setInvulnerable(true);
            stand.setMarker(true);
            // ✅ Persistente para que no desaparezca
            stand.setPersistent(true);
            stand.setRemoveWhenFarAway(false);
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
        List<ArmorStand> stands = holograms.remove(pet.getPetUUID());
        if (stands != null) stands.forEach(ArmorStand::remove);
    }

    public void removeAllHolograms() {
        for (List<ArmorStand> stands : holograms.values()) {
            stands.forEach(ArmorStand::remove);
        }
        holograms.clear();
    }

    // ✅ Obtener altura correcta según tipo de mob
    private double getHologramHeight(Pet pet) {
        String mobType = pet.getEntityType().name();
        return MOB_HEIGHT.getOrDefault(mobType, 2.8);
    }

    private List<String> buildHologramLines(Pet pet) {
        List<String> lines = new ArrayList<>();
        String rc = pet.getRarityColor();
        String mobType = pet.getEntityType().name();

        // ✅ Si es mob pequeño/feo con armorstand
        // solo mostrar nombre y nivel
        if (MINIMAL_HOLO.contains(mobType)) {
            lines.add(rc + "§l" + pet.getName() +
                " §7[Nv.§e" + pet.getLevel() + "§7]");
            if (pet.isSleeping()) lines.add("§8💤 Zzzzz...");
            return lines;
        }

        // ✅ Holograma completo para mobs normales/grandes
        String raritySymbol =
            pet.getRarity() == Pet.Rarity.LEGENDARY ? "⚡" :
            pet.getRarity() == Pet.Rarity.EPIC ? "✦" :
            pet.getRarity() == Pet.Rarity.RARE ? "✧" : "•";

        lines.add(rc + "§l" + raritySymbol + " " +
            pet.getRarityName() + " " + raritySymbol);
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
            lines.add("§7Estado: " + pet.getMoodColor() +
                getMoodName(pet.getMood()));
        }
        if (plugin.getConfig().getBoolean("holograms.show-particles", true)
            && pet.getParticleType() != Pet.ParticleType.NONE) {
            lines.add("§7✨ §fPartícula: §d" +
                pet.getParticleType().name());
        }
        if (plugin.getConfig().getBoolean("holograms.show-kills", true)) {
            lines.add("§7⚔ §fKills: §c" + pet.getKills() +
                "  §7XP: §b" + (int) pet.getXp());
        }
        lines.add("§8" + mobType.replace("_", " "));
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
