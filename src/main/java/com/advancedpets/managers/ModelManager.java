package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ModelManager implements Listener {

    private final AdvancedPets plugin;
    private final Map<UUID, String> petModels = new HashMap<>();
    private boolean modelEngineEnabled = false;
    private boolean mythicMobsEnabled = false;

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
        DEFAULT_MODELS.put("BLAZE", "advpet_phoenix");
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
        startParticleTimer();
    }

    private void checkDependencies() {
        if (Bukkit.getPluginManager().getPlugin("ModelEngine") != null
            && plugin.getConfig().getBoolean("modelengine.enabled", true)) {
            modelEngineEnabled = true;
            plugin.getLogger().info("ModelEngine detectado y conectado!");
        } else {
            plugin.getLogger().warning("ModelEngine no encontrado. Modelos custom desactivados.");
        }
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null
            && plugin.getConfig().getBoolean("mythicmobs.enabled", true)) {
            mythicMobsEnabled = true;
            plugin.getLogger().info("MythicMobs detectado y conectado!");
        } else {
            plugin.getLogger().warning("MythicMobs no encontrado.");
        }
    }

    public void applyModel(Pet pet, String modelId) {
        if (!modelEngineEnabled) {
            Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
            if (owner != null)
                owner.sendMessage("§c§l[AdvancedPets] §cModelEngine no está instalado en el servidor!");
            return;
        }
        if (pet.getEntity() == null || !pet.isSummoned()) return;
        try {
            Class<?> apiClass = Class.forName("com.ticxo.modelengine.api.ModelEngineAPI");
            Object blueprint = apiClass.getMethod("getBlueprint", String.class).invoke(null, modelId);
            if (blueprint == null) {
                plugin.getLogger().warning("Modelo no encontrado: " + modelId);
                return;
            }
            Object modeledEntity = apiClass.getMethod("getOrCreateModeledEntity",
                org.bukkit.entity.Entity.class).invoke(null, pet.getEntity());
            Object activeModel = apiClass.getMethod("createActiveModel", blueprint.getClass())
                .invoke(null, blueprint);
            modeledEntity.getClass().getMethod("addModel", activeModel.getClass(), boolean.class)
                .invoke(modeledEntity, activeModel, true);
            petModels.put(pet.getPetUUID(), modelId);
            Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
            if (owner != null)
                owner.sendMessage("§d§l[AdvancedPets] §f✨ Modelo §d§l" + modelId + " §faplicado!");
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
        if (!modelEngineEnabled || pet.getEntity() == null) return;
        try {
            Class<?> apiClass = Class.forName("com.ticxo.modelengine.api.ModelEngineAPI");
            Object modeledEntity = apiClass.getMethod("getModeledEntity",
                org.bukkit.entity.Entity.class).invoke(null, pet.getEntity());
            if (modeledEntity != null) {
                String currentModel = petModels.get(pet.getPetUUID());
                if (currentModel != null)
                    modeledEntity.getClass().getMethod("removeModel", String.class)
                        .invoke(modeledEntity, currentModel);
            }
            petModels.remove(pet.getPetUUID());
        } catch (Exception e) {
            plugin.getLogger().warning("Error al remover modelo: " + e.getMessage());
        }
    }

    public void removeAllModels() {
        for (Pet pet : plugin.getPetManager().getAllPets().values()) {
            removeModel(pet);
        }
        petModels.clear();
    }

    public void playAnimation(Pet pet, String animationName) {
        if (!modelEngineEnabled || pet.getEntity() == null) return;
        try {
            Class<?> apiClass = Class.forName("com.ticxo.modelengine.api.ModelEngineAPI");
            Object modeledEntity = apiClass.getMethod("getModeledEntity",
                org.bukkit.entity.Entity.class).invoke(null, pet.getEntity());
            if (modeledEntity == null) return;
            String modelId = petModels.get(pet.getPetUUID());
            if (modelId == null) return;
            Object model = modeledEntity.getClass()
                .getMethod("getModel", String.class).invoke(modeledEntity, modelId);
            if (model == null) return;
            Object animHandler = model.getClass().getMethod("getAnimationHandler").invoke(model);
            animHandler.getClass().getMethod("playAnimation",
                String.class, double.class, double.class, double.class, boolean.class)
                .invoke(animHandler, animationName, 0.1, 0.1, 1.0, true);
        } catch (Exception e) {
            // Animacion no disponible, ignorar silenciosamente
        }
    }

    public void playLevelUpAnimation(Pet pet) {
        playAnimation(pet, "levelup");
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            playAnimation(pet, "idle"), 60L);
    }

    public void playDanceAnimation(Pet pet) {
        playAnimation(pet, "dance");
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            playAnimation(pet, "idle"), 100L);
    }

    public void playEatAnimation(Pet pet) {
        playAnimation(pet, "eat");
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            playAnimation(pet, "idle"), 40L);
    }

    public void playAttackAnimation(Pet pet) {
        playAnimation(pet, "attack");
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            playAnimation(pet, "idle"), 20L);
    }

    private void startParticleTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : plugin.getPetManager().getAllPets().values()) {
                    if (!pet.isSummoned() || pet.getEntity() == null) continue;
                    if (pet.getRarity() == Pet.Rarity.COMMON) continue;
                    applyRarityParticles(pet);
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private void applyRarityParticles(Pet pet) {
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

    public List<String> getAvailableModels() {
        List<String> models = new ArrayList<>(DEFAULT_MODELS.values());
        if (plugin.getConfig().getConfigurationSection("modelengine.default-models") != null) {
            models.addAll(plugin.getConfig()
                .getConfigurationSection("modelengine.default-models").getValues(false).values()
                .stream().map(Object::toString).collect(java.util.stream.Collectors.toList()));
        }
        return new ArrayList<>(new LinkedHashSet<>(models));
    }

    public String getActiveModel(Pet pet) {
        return petModels.getOrDefault(pet.getPetUUID(), "Ninguno");
    }

    public boolean isModelEngineEnabled() { return modelEngineEnabled; }
    public boolean isMythicMobsEnabled() { return mythicMobsEnabled; }
    public static Map<String, String> getDefaultModels() { return DEFAULT_MODELS; }
}

