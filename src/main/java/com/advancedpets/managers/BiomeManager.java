package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BiomeManager {

    private final AdvancedPets plugin;
    private final Map<UUID, Biome> lastBiome = new HashMap<>();

    public BiomeManager(AdvancedPets plugin) {
        this.plugin = plugin;
        startBiomeTimer();
    }

    private void startBiomeTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : plugin.getPetManager()
                    .getAllPets().values()) {
                    if (!pet.isSummoned() ||
                        pet.getEntity() == null) continue;
                    Player owner = Bukkit.getPlayer(
                        pet.getOwnerUUID());
                    if (owner == null) continue;
                    Biome current = owner.getLocation()
                        .getBlock().getBiome();
                    Biome last = lastBiome.get(
                        pet.getOwnerUUID());
                    if (last != null && last == current) continue;
                    lastBiome.put(pet.getOwnerUUID(), current);
                    applyBiomeEffect(pet, owner, current);
                }
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }

    private void applyBiomeEffect(Pet pet, Player owner,
        Biome biome) {
        Location loc = pet.getEntity().getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        String biomeName = biome.name();

        // ✅ NIEVE / HIELO
        if (biomeName.contains("SNOWY") ||
            biomeName.contains("ICE") ||
            biomeName.contains("FROZEN") ||
            biomeName.contains("COLD")) {
            world.spawnParticle(Particle.SNOWFLAKE,
                loc.clone().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0.1);
            world.playSound(loc,
                Sound.BLOCK_SNOW_STEP, 0.5f, 1.5f);
            owner.sendMessage("§b§l[AdvancedPets] §e" +
                pet.getName() +
                " §fdice: §b¡Brrr! ¡Hace frío aquí amo! ❄️");
        }
        // ✅ NETHER
        else if (biomeName.contains("NETHER") ||
            biomeName.contains("BASALT") ||
            biomeName.contains("SOUL_SAND") ||
            biomeName.contains("CRIMSON") ||
            biomeName.contains("WARPED")) {
            world.spawnParticle(Particle.FLAME,
                loc.clone().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0.1);
            world.spawnParticle(Particle.LAVA,
                loc, 10, 0.3, 0.3, 0.3, 0);
            world.playSound(loc,
                Sound.AMBIENT_NETHER_WASTES_MOOD, 0.5f, 1f);
            owner.sendMessage("§c§l[AdvancedPets] §e" +
                pet.getName() +
                " §fdice: §c¡Amo esto está muy caliente! 🔥");
        }
        // ✅ OCÉANO / AGUA
        else if (biomeName.contains("OCEAN") ||
            biomeName.contains("BEACH") ||
            biomeName.contains("RIVER")) {
            world.spawnParticle(Particle.BUBBLE_POP,
                loc.clone().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0.1);
            world.spawnParticle(Particle.SPLASH,
                loc, 20, 0.5, 0.3, 0.5, 0.2);
            world.playSound(loc,
                Sound.AMBIENT_UNDERWATER_LOOP, 0.5f, 1f);
            owner.sendMessage("§9§l[AdvancedPets] §e" +
                pet.getName() +
                " §fdice: §9¡Amo escucho el mar! 🌊");
        }
        // ✅ DESIERTO
        else if (biomeName.contains("DESERT") ||
            biomeName.contains("BADLANDS") ||
            biomeName.contains("SAVANNA")) {
            world.spawnParticle(Particle.BLOCK,
                loc.clone().add(0, 1, 0), 30,
                0.5, 0.5, 0.5,
                Material.SAND.createBlockData());
            world.playSound(loc,
                Sound.BLOCK_SAND_STEP, 0.5f, 1f);
            owner.sendMessage("§e§l[AdvancedPets] §e" +
                pet.getName() +
                " §fdice: §e¡Amo tengo sed! 🌵");
        }
        // ✅ BOSQUE / JUNGLA
        else if (biomeName.contains("FOREST") ||
            biomeName.contains("JUNGLE") ||
            biomeName.contains("TAIGA") ||
            biomeName.contains("GROVE")) {
            world.spawnParticle(Particle.CHERRY_LEAVES,
                loc.clone().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0.1);
            world.spawnParticle(Particle.HAPPY_VILLAGER,
                loc, 10, 0.3, 0.3, 0.3, 0);
            world.playSound(loc,
                Sound.BLOCK_GRASS_STEP, 0.5f, 1f);
            owner.sendMessage("§a§l[AdvancedPets] §e" +
                pet.getName() +
                " §fdice: §a¡Amo me gusta este bosque! 🌲");
        }
        // ✅ END — CORREGIDO sonido
        else if (biomeName.contains("END")) {
            world.spawnParticle(Particle.DRAGON_BREATH,
                loc.clone().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0.1);
            world.spawnParticle(Particle.WITCH,
                loc, 20, 0.3, 0.5, 0.3, 0.1);
            // ✅ CORREGIDO — usar ENTITY_ENDER_DRAGON_AMBIENT
            world.playSound(loc,
                Sound.ENTITY_ENDER_DRAGON_AMBIENT, 0.5f, 1f);
            owner.sendMessage("§5§l[AdvancedPets] §e" +
                pet.getName() +
                " §fdice: §5¡Amo este lugar me da miedo! 👁️");
        }
        // ✅ LLANURA / DEFAULT
        else {
            world.spawnParticle(Particle.HAPPY_VILLAGER,
                loc.clone().add(0, 1, 0),
                10, 0.3, 0.3, 0.3, 0);
            world.playSound(loc,
                Sound.BLOCK_GRASS_STEP, 0.5f, 1f);
            owner.sendMessage("§a§l[AdvancedPets] §e" +
                pet.getName() +
                " §fdice: §a¡Qué lindo lugar amo! 🌿");
        }

        plugin.getHologramManager().updateHologram(pet);
    }

    public void clearBiome(UUID ownerUUID) {
        lastBiome.remove(ownerUUID);
    }
}
