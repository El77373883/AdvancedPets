package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;

import java.util.*;

public class LearningManager implements Listener {

    private final AdvancedPets plugin;
    // ✅ Contador de acciones del amo
    private final Map<UUID, Map<String, Integer>> ownerActions =
        new HashMap<>();

    // ✅ Thresholds para mejorar habilidades
    private static final int MINE_THRESHOLD = 50;
    private static final int FIGHT_THRESHOLD = 20;
    private static final int HARVEST_THRESHOLD = 30;

    public LearningManager(AdvancedPets plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // ✅ Amo mina → mascota aprende a minar mejor
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) return;

        List<Material> ores = Arrays.asList(
            Material.COAL_ORE, Material.IRON_ORE,
            Material.GOLD_ORE, Material.DIAMOND_ORE,
            Material.EMERALD_ORE, Material.REDSTONE_ORE,
            Material.COPPER_ORE, Material.LAPIS_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_DIAMOND_ORE
        );

        if (!ores.contains(event.getBlock().getType())) return;

        addAction(player.getUniqueId(), "MINE");
        int count = getActionCount(player.getUniqueId(), "MINE");

        if (count % MINE_THRESHOLD == 0) {
            // ✅ Mejorar velocidad de minado de la mascota
            pet.addXP(20);
            player.sendMessage("§e§l[AdvancedPets] §e" +
                pet.getName() +
                " §faprendió de ti! §a¡Ahora mina mejor! ⛏️");
            player.playSound(player.getLocation(),
                Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.5f);
            // ✅ Partículas de aprendizaje
            player.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                pet.getEntity() != null ?
                    pet.getEntity().getLocation().add(0,1,0) :
                    player.getLocation(),
                15, 0.3, 0.5, 0.3, 0);
        }
    }

    // ✅ Amo mata mobs → mascota aprende a pelear mejor
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player player = event.getEntity().getKiller();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) return;

        addAction(player.getUniqueId(), "FIGHT");
        int count = getActionCount(player.getUniqueId(), "FIGHT");

        if (count % FIGHT_THRESHOLD == 0) {
            // ✅ Aumentar daño de la mascota +1 (máx 30)
            if (pet.getDamage() < 30) {
                pet.setDamage(pet.getDamage() + 1);
                plugin.getPetManager().savePet(pet);
                player.sendMessage("§c§l[AdvancedPets] §e" +
                    pet.getName() +
                    " §faprendió de ti! §c¡Ahora hace +1 de daño! ⚔️");
                player.sendMessage("§f  Daño actual: §c§l" +
                    pet.getDamage());
                player.playSound(player.getLocation(),
                    Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
                player.getWorld().spawnParticle(
                    Particle.SWEEP_ATTACK,
                    pet.getEntity() != null ?
                        pet.getEntity().getLocation().add(0,1,0) :
                        player.getLocation(),
                    10, 0.3, 0.3, 0.3, 0);
            }
        }
    }

    // ✅ Amo cosecha → mascota aprende a cosechar mejor
    @EventHandler
    public void onPlayerHarvest(PlayerHarvestBlockEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) return;

        addAction(player.getUniqueId(), "HARVEST");
        int count = getActionCount(player.getUniqueId(), "HARVEST");

        if (count % HARVEST_THRESHOLD == 0) {
            // ✅ Dar XP extra a la mascota
            pet.addXP(15);
            plugin.getPetManager().savePet(pet);
            player.sendMessage("§a§l[AdvancedPets] §e" +
                pet.getName() +
                " §faprendió de ti! §a¡Ahora cosecha mejor! 🌾");
            player.playSound(player.getLocation(),
                Sound.BLOCK_CROP_BREAK, 1f, 1.5f);
            player.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                pet.getEntity() != null ?
                    pet.getEntity().getLocation().add(0,1,0) :
                    player.getLocation(),
                15, 0.3, 0.5, 0.3, 0);
        }
    }

    private void addAction(UUID uuid, String action) {
        ownerActions.computeIfAbsent(uuid, k -> new HashMap<>());
        Map<String, Integer> actions = ownerActions.get(uuid);
        actions.put(action, actions.getOrDefault(action, 0) + 1);
    }

    private int getActionCount(UUID uuid, String action) {
        if (!ownerActions.containsKey(uuid)) return 0;
        return ownerActions.get(uuid).getOrDefault(action, 0);
    }

    public Map<String, Integer> getOwnerStats(UUID uuid) {
        return ownerActions.getOrDefault(uuid, new HashMap<>());
    }
}
