package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.BlueprintModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ModelManager implements Listener {

    private final AdvancedPets plugin;
    private final Map<UUID, String> petModels = new HashMap<>();
    private final Map<UUID, ActiveModel> activeModels = new HashMap<>();
    private boolean modelEngineEnabled = false;
    private boolean mythicMobsEnabled = false;

    // Modelos default por EntityType
    private static final Map<String, String> DEFAULT_MODELS = new HashMap<>();

    static {
        DEFAULT_MODELS.put("WOLF", "advpet_wolf");
        DEFAULT_MODELS.put("CAT", "advpet_cat");
        DEFAULT_MODELS.put("HORSE", "advpet_horse");
        DEFAULT_MODELS.put("COW", "advpet_cow");
        DEFAULT_MODELS.put("PIG", "advpet_pig");
        DEFAULT_MODELS.put("SHEEP", "advpet_sheep");
        DEFAULT_MODELS.put("CHICKEN", "advpet_chicken");
        DEFAULT_MODELS.put("LLAMA", "advpet_llama");
        DEFAULT_MODELS.put("RABBIT", "advpet_rabbit");
        DEFAULT_MODELS.put("FOX", "advpet_fox");
        DEFAULT_MODELS.put("TURTLE", "advpet_turtle");
        DEFAULT_MODELS.put("BEE", "advpet_bee");
        DEFAULT_MODELS.put("PANDA", "advpet_panda");
        DEFAULT_MODELS.put("PARROT", "advpet_parrot");
        DEFAULT_MODELS.put("DOLPHIN", "advpet_dolphin");
        DEFAULT_MODELS.put("FROG", "advpet_frog");
        DEFAULT_MODELS.put("CAMEL", "advpet_camel");
        DEFAULT_MODELS.put("IRON_GOLEM", "advpet_iron_golem");
        DEFAULT_MODELS.put("POLAR_BEAR", "advpet_polar_bear");
        DEFAULT_MODELS.put("SPIDER", "advpet_spider");
        DEFAULT_MODELS.put("CAVE_SPIDER", "advpet_cave_spider");
        DEFAULT_MODELS.put("PHANTOM", "advpet_phantom");
        DEFAULT_MODELS.put("PIGLIN", "advpet_piglin");
        DEFAULT_MODELS.put("SKELETON", "advpet_skeleton");
        DEFAULT_MODELS.put("ZOMBIE", "advpet_zombie");
        DEFAULT_MODELS.put("CREEPER", "advpet_creeper");
        DEFAULT_MODELS.put("BLAZE", "advpet_blaze");
        DEFAULT_MODELS.put("ENDERMAN", "advpet_enderman");
        DEFAULT_MODELS.put("WITCH", "advpet_witch");
        DEFAULT_MODELS.put("GUARDIAN", "advpet_guardian");
        DEFAULT_MODELS.put("PIGLIN_BRUTE", "advpet_piglin_brute");
        DEFAULT_MODELS.put("HOGLIN", "advpet_hoglin");
        DEFAULT_MODELS.put("RAVAGER", "advpet_ravager");
        DEFAULT_MODELS.put("VEX", "advpet_vex");
        DEFAULT_MODELS.put("VINDICATOR", "advpet_vindicator");
        DEFAULT_MODELS.put("SHULKER", "advpet_shulker");
        DEFAULT_MODELS.put("MAGMA_CUBE", "advpet_magma_cube");
        DEFAULT_MODELS.put("SLIME", "advpet_slime");
        DEFAULT_MODELS.put("ELDER_GUARDIAN", "advpet_elder_guardian");
        DEFAULT_MODELS.put("ENDER_DRAGON", "advpet_dragon");
        DEFAULT_MODELS.put("WITHER", "advpet_wither");
        DEFAULT_MODELS.put("WARDEN", "advpet_warden");
    }

    public ModelManager(AdvancedPets plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        checkDependencies();
        startAnimationTimer();
        startParticleTimer();
    }

    private void checkDependencies() {
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null
            && plugin.getConfig().getBoolean("modelengine.enabled", true)) {
            modelEngineEnabled = true;
            plugin.getLogger().info("§aModelEngine detectado y conectado!");
        } else {
            plugin.getLogger().warning("§eModelEngine no encontrado. Modelos custom desactivados.");
        }
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null
            && plugin.getConfig().getBoolean("mythicmobs.enabled", true)) {
            mythicMobsEnabled = true;
            plugin.getLogger().info("§aMythicMobs detectado y conectado!");
        } else {
            plugin.getLogger().warning("§eMythicMobs no encontrado.");
        }
    }

    // ─── APLICAR MODELO ───────────────────────────────────────────────

    public void applyModel(Pet pet, String modelId) {
        if (!modelEngineEnabled) return;
        if (pet.getEntity() == null || !pet.isSummoned()) return;

        removeModel(pet);

        try {
            BlueprintModel blueprint = ModelEngineAPI.getBlueprint(modelId);
            if (blueprint == null) {
                plugin.getLogger().warning("Modelo no encontrado: " + modelId);
                return;
            }
            ModeledEntity modeledEntity = ModelEngineAPI.getOrCreateModeledEntity(pet.getEntity());
            ActiveModel activeModel = ModelEngineAPI.createActiveModel(blueprint);
            modeledEntity.addModel(activeModel, true);
            activeModels.put(pet.getPetUUID(), activeModel);
            petModels.put(pet.getPetUUID(), modelId);

            // Aplicar animación idle
            playAnimation(pet, "idle", true);

            // Aplicar aura según rareza
            applyRarityEffect(pet);

            Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
            if (owner != null) {
                owner.sendMessage("§d§l[AdvancedPets] §f✨ Modelo §d§l" + modelId + " §faplicado a tu mascota!");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error al aplicar modelo " + modelId + ": " + e.getMessage());
        }
    }

    public void applyDefaultModel(Pet pet) {
        if (!modelEngineEnabled) return;
        String configModel = plugin.getConfig().getString(
            "modelengine.default-models." + pet.getEntityType().name());
        String modelId = configModel != null ? configModel :
            DEFAULT_MODELS.getOrDefault(pet.getEntityType().name(), "advpet_default");
        applyModel(pet, modelId);
    }

    public void removeModel(Pet pet) {
        if (!modelEngineEnabled) return;
        if (pet.getEntity() == null) return;
        try {
            ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(pet.getEntity());
            if (modeledEntity != null) {
                String currentModel = petModels.get(pet.getPetUUID());
                if (currentModel != null) modeledEntity.removeModel(currentModel);
            }
            activeModels.remove(pet.getPetUUID());
            petModels.remove(pet.getPetUUID());
        } catch (Exception e) {
            plugin.getLogger().warning("Error al remover modelo: " + e.getMessage());
        }
    }

    public void removeAllModels() {
        for (Pet pet : plugin.getPetManager().getAllPets().values()) {
            removeModel(pet);
        }
    }

    // ─── ANIMACIONES ──────────────────────────────────────────────────

    public void playAnimation(Pet pet, String animationName, boolean loop) {
        if (!modelEngineEnabled) return;
        ActiveModel model = activeModels.get(pet.getPetUUID());
        if (model == null) return;
        try {
            model.getAnimationHandler().playAnimation(animationName, 0.1, 0.1, 1.0, loop);
        } catch (Exception e) {
            // Animación no disponible, ignorar
        }
    }

    public void stopAnimation(Pet pet, String animationName) {
        if (!modelEngineEnabled) return;
        ActiveModel model = activeModels.get(pet.getPetUUID());
        if (model == null) return;
        try {
            model.getAnimationHandler().stopAnimation(animationName);
        } catch (Exception e) {
            // ignorar
        }
    }

    public void updateAnimation(Pet pet) {
        if (!modelEngineEnabled) return;
        if (pet.getEntity() == null || !pet.isSummoned()) return;

        if (pet.isSleeping()) {
            playAnimation(pet, "sleep", true);
            return;
        }

        switch (pet.getCurrentWork()) {
            case MINING:
                playAnimation(pet, "mine", true); break;
            case FARMING:
            case ATTACK:
            case PVP:
                playAnimation(pet, "attack", true); break;
            case HARVEST:
                playAnimation(pet, "harvest", true); break;
            case CHOP:
                playAnimation(pet, "chop", true); break;
            case COOK:
                playAnimation(pet, "cook", true); break;
            case EXPLORE:
                playAnimation(pet, "walk", true); break;
            default:
                // Seguir al amo
                Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
                if (owner != null) {
                    double dist = pet.getEntity().getLocation().distance(owner.getLocation());
                    if (dist > 3) playAnimation(pet, "walk", true);
                    else playAnimation(pet, "idle", true);
                }
                break;
        }
    }

    public void playLevelUpAnimation(Pet pet) {
        if (!modelEngineEnabled) return;
        playAnimation(pet, "levelup", false);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            playAnimation(pet, "idle", true), 60L);
    }

    public void playDanceAnimation(Pet pet) {
        if (!modelEngineEnabled) return;
        playAnimation(pet, "dance", false);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            playAnimation(pet, "idle", true), 100L);
    }

    public void playEatAnimation(Pet pet) {
        if (!modelEngineEnabled) return;
        playAnimation(pet, "eat", false);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            playAnimation(pet, "idle", true), 40L);
    }

    public void playAttackAnimation(Pet pet) {
        if (!modelEngineEnabled) return;
        playAnimation(pet, "attack", false);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            playAnimation(pet, "idle", true), 20L);
    }

    public void playDeathAnimation(Pet pet) {
        if (!modelEngineEnabled) return;
        playAnimation(pet, "death", false);
    }

    // ─── EFECTOS DE PARTÍCULAS EN EL MODELO ──────────────────────────

    private void applyRarityEffect(Pet pet) {
        if (pet.getEntity() == null) return;
        Location loc = pet.getEntity().getLocation().add(0, 1, 0);
        World world = loc.getWorld();
        if (world == null) return;

        switch (pet.getRarity()) {
            case RARE:
                world.spawnParticle(Particle.DUST, loc, 5, 0.3, 0.5, 0.3,
                    new Particle.DustOptions(Color.BLUE, 1.5f));
                break;
            case EPIC:
                world.spawnParticle(Particle.DUST, loc, 10, 0.3, 0.5, 0.3,
                    new Particle.DustOptions(Color.PURPLE, 2f));
                world.spawnParticle(Particle.WITCH, loc, 5, 0.3, 0.5, 0.3, 0);
                break;
            case LEGENDARY:
                world.spawnParticle(Particle.DUST, loc, 15, 0.5, 0.5, 0.5,
                    new Particle.DustOptions(Color.YELLOW, 2.5f));
                world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 5, 0.3, 0.5, 0.3, 0.1);
                world.spawnParticle(Particle.FLAME, loc, 3, 0.2, 0.3, 0.2, 0.02);
                break;
            default:
                break;
        }
    }

    private void startParticleTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : plugin.getPetManager().getAllPets().values()) {
                    if (!pet.isSummoned() || pet.getEntity() == null) continue;
                    if (pet.getRarity() == Pet.Rarity.COMMON) continue;
                    applyRarityEffect(pet);
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private void startAnimationTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : plugin.getPetManager().getAllPets().values()) {
                    if (!pet.isSummoned() || pet.getEntity() == null) continue;
                    updateAnimation(pet);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    // ─── MYTHICMOBS INTEGRATION ───────────────────────────────────────

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent event) {
        if (!mythicMobsEnabled) return;
        Entity killer = event.getKiller();
        if (!(killer instanceof Player)) return;
        Player player = (Player) killer;
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) return;
        pet.addXP(plugin.getConfig().getDouble("xp.per-kill", 10) * 2);
        pet.setKills(pet.getKills() + 1);
        player.sendMessage("§6§l[AdvancedPets] §e" + pet.getName() +
            " §fmató un MythicMob! §6+XP DOBLE 🌟");
        plugin.getPetManager().savePet(pet);
        plugin.getAchievementManager().checkAchievements(player, pet);
    }

    // ─── GETTERS ──────────────────────────────────────────────────────

    public boolean isModelEngineEnabled() { return modelEngineEnabled; }
    public boolean isMythicMobsEnabled() { return mythicMobsEnabled; }

    public String getActiveModel(Pet pet) {
        return petModels.getOrDefault(pet.getPetUUID(), "Ninguno");
    }

    public List<String> getAvailableModels() {
        List<String> models = new ArrayList<>();
        if (!modelEngineEnabled) return models;
        try {
            for (BlueprintModel blueprint : ModelEngineAPI.getBlueprints().values()) {
                models.add(blueprint.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error al obtener modelos: " + e.getMessage());
        }
        return models;
    }

    public static Map<String, String> getDefaultModels() {
        return DEFAULT_MODELS;
    }
}
