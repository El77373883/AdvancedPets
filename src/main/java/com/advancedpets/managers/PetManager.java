package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PetManager {

    private final AdvancedPets plugin;
    private final Map<UUID, Pet> activePets = new HashMap<>();
    private final File dataFolder;

    public PetManager(AdvancedPets plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "pets");
        if (!dataFolder.exists()) dataFolder.mkdirs();
        loadAllPets();
        startHungerTimer();
        startMoodTimer();
        startParticleTimer();
        startFollowTimer();
        startPersistenceTimer();
        // ✅ ELIMINADO startSleepTimer() — mascota ya no duerme
    }

    public Pet getPet(UUID ownerUUID) {
        return activePets.get(ownerUUID);
    }

    public boolean hasPet(UUID ownerUUID) {
        return activePets.containsKey(ownerUUID);
    }

    public void addPet(Pet pet) {
        activePets.put(pet.getOwnerUUID(), pet);
        savePet(pet);
    }

    public void removePet(UUID ownerUUID) {
        Pet pet = activePets.get(ownerUUID);
        if (pet != null) {
            despawnPet(pet);
            activePets.remove(ownerUUID);
            File petFile = new File(dataFolder,
                ownerUUID.toString() + ".yml");
            if (petFile.exists()) petFile.delete();
        }
    }

    public void spawnPet(Pet pet, Location location) {
        if (pet.isSummoned()) return;
        Location spawnLoc = location.clone().add(5, 0, 0);
        Entity entity = spawnLoc.getWorld().spawnEntity(
            spawnLoc, pet.getEntityType());
        entity.setCustomName(formatPetName(pet));
        entity.setCustomNameVisible(false);

        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) entity;
            living.setMaxHealth(pet.getMaxHealth());
            living.setHealth(pet.getHealth());
            if (entity instanceof Mob) {
                Mob mob = (Mob) entity;
                // ✅ setAware TRUE para que se mueva
                mob.setAware(true);
            }
        }
        entity.setPersistent(true);
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).setRemoveWhenFarAway(false);
        }
        pet.setEntity(entity);
        pet.setLocation(spawnLoc);
        pet.setSummoned(true);
        // ✅ Mascota NUNCA duerme
        pet.setSleeping(false);
        plugin.getHologramManager().createHologram(pet);
        playSpawnEffects(pet, spawnLoc);
    }

    public void despawnPet(Pet pet) {
        if (!pet.isSummoned()) return;
        plugin.getHologramManager().removeHologram(pet);
        if (pet.getEntity() != null && !pet.getEntity().isDead()) {
            pet.getEntity().remove();
        }
        pet.setEntity(null);
        pet.setSummoned(false);
    }

    private void startFollowTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : activePets.values()) {
                    if (!pet.isSummoned() || pet.getEntity() == null) continue;

                    // ✅ ELIMINADO chequeo de sleeping
                    // mascota siempre se mueve

                    Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
                    if (owner == null || !owner.isOnline()) continue;

                    if (pet.getEntity().isDead() ||
                        !pet.getEntity().isValid()) {
                        pet.setSummoned(false);
                        pet.setEntity(null);
                        spawnPet(pet, owner.getLocation());
                        continue;
                    }

                    double dist = pet.getEntity().getLocation()
                        .distance(owner.getLocation());

                    if (dist > 15) {
                        // Teletransportar a 6 bloques
                        Location target = getOffsetLocation(
                            owner.getLocation(), 6);
                        pet.getEntity().teleport(target);
                    } else if (dist < 4) {
                        // Alejar si está muy cerca
                        Location away = getOffsetLocation(
                            owner.getLocation(), 6);
                        pet.getEntity().teleport(away);
                    }
                    // ✅ Hacer que el mob mire y siga al dueño
                    if (pet.getEntity() instanceof Mob) {
                        Mob mob = (Mob) pet.getEntity();
                        // ✅ Activar awareness para que se mueva
                        mob.setAware(true);
                    }
                    plugin.getHologramManager().updateHologram(pet);
                }
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }

    private void startPersistenceTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : activePets.values()) {
                    Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
                    if (owner == null || !owner.isOnline()) continue;
                    if (pet.isSummoned() && (pet.getEntity() == null
                        || pet.getEntity().isDead()
                        || !pet.getEntity().isValid())) {
                        pet.setSummoned(false);
                        pet.setEntity(null);
                        spawnPet(pet, owner.getLocation());
                        owner.sendMessage("§6§l[AdvancedPets] §e" +
                            pet.getName() +
                            " §fdice: §a¡Aquí estoy amo! 🐾");
                    }
                    // ✅ Asegurarse que NUNCA esté durmiendo
                    pet.setSleeping(false);
                }
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }

    private Location getOffsetLocation(Location base, double distance) {
        Random rand = new Random();
        double angle = rand.nextDouble() * 2 * Math.PI;
        double x = Math.cos(angle) * distance;
        double z = Math.sin(angle) * distance;
        Location target = base.clone().add(x, 0, z);
        target.setY(base.getWorld().getHighestBlockYAt(target) + 1);
        return target;
    }

    private void playSpawnEffects(Pet pet, Location location) {
        World world = location.getWorld();
        if (world == null) return;
        world.playSound(location, Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING,
            location, 100, 1, 1, 1, 0.3);
        world.spawnParticle(Particle.FLAME,
            location, 50, 0.5, 0.5, 0.5, 0.1);
        Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
        if (owner != null) {
            owner.sendMessage("§r");
            owner.sendMessage(
                "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
            owner.sendMessage("§e§l   ⭐ ¡MASCOTA INVOCADA! ⭐");
            owner.sendMessage(
                "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
            owner.sendMessage("§f  Nombre: " +
                pet.getRarityColor() + "§l" + pet.getName());
            owner.sendMessage("§f  Tipo:   §e" +
                pet.getEntityType().name());
            owner.sendMessage("§f  Rareza: " +
                pet.getRarityColor() + "§l" + pet.getRarityName());
            owner.sendMessage("§f  Nivel:  §a§l" + pet.getLevel());
            owner.sendMessage(
                "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
            owner.sendMessage("§r");
            if (pet.getRarity() == Pet.Rarity.LEGENDARY) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage("§r");
                    p.sendMessage(
                        "§6§l⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡");
                    p.sendMessage("§e§l  🐉 ¡" + owner.getName() +
                        " ha invocado una mascota LEGENDARIA!");
                    p.sendMessage("§f  Mascota: §6§l" +
                        pet.getName() + " §f(" +
                        pet.getEntityType().name() + ")");
                    p.sendMessage(
                        "§6§l⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡");
                    p.sendMessage("§r");
                    p.playSound(p.getLocation(),
                        Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                }
                world.spawnParticle(Particle.FIREWORK,
                    location, 200, 2, 2, 2, 0.5);
            }
        }
    }

    private String formatPetName(Pet pet) {
        return pet.getRarityColor() + "§l" + pet.getName() +
            " §7[Nv." + pet.getLevel() + "]";
    }

    private void startHungerTimer() {
        int interval = plugin.getConfig()
            .getInt("pets.hunger.interval-minutes", 10) * 60 * 20;
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : activePets.values()) {
                    if (!plugin.getConfig()
                        .getBoolean("pets.hunger.enabled", true)) continue;
                    pet.setHunger(Math.max(0, pet.getHunger() - 10));
                    Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
                    if (owner == null) continue;
                    if (pet.getHunger() <= 50 && pet.getHunger() > 20) {
                        pet.setMood(Pet.Mood.HUNGRY);
                        owner.sendMessage("§6§l[AdvancedPets] §e" +
                            pet.getName() +
                            " §fdice: §7¡Amo, tengo hambre! 🍖");
                        owner.playSound(owner.getLocation(),
                            Sound.ENTITY_WOLF_WHINE, 1f, 1f);
                    } else if (pet.getHunger() <= 20) {
                        owner.sendMessage("§c§l[AdvancedPets] §e" +
                            pet.getName() +
                            " §fdice: §c¡Me muero de hambre! 😭");
                        owner.playSound(owner.getLocation(),
                            Sound.ENTITY_WOLF_HURT, 1f, 0.8f);
                    }
                }
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    private void startMoodTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : activePets.values()) {
                    if (!pet.isSummoned()) continue;
                    Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
                    if (owner == null) continue;
                    int random = new Random().nextInt(100);
                    if (random < 5) {
                        String[] messages = {
                            "§e¡Te quiero mucho amo! ❤️",
                            "§a¡Soy la mejor mascota! 😏✨",
                            "§b¡Siempre estaré contigo amo! 🐾",
                            "§d¡Oye amo! ¿Jugamos? 🎮"
                        };
                        owner.sendMessage("§6§l[AdvancedPets] §e" +
                            pet.getName() + " §fdice: " +
                            messages[new Random().nextInt(messages.length)]);
                    }
                }
            }
        }.runTaskTimer(plugin, 200L, 200L);
    }

    private void startParticleTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : activePets.values()) {
                    if (!pet.isSummoned() || pet.getEntity() == null) continue;
                    if (!plugin.getConfig()
                        .getBoolean("particles.enabled", true)) continue;
                    spawnPetParticle(pet);
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    private void spawnPetParticle(Pet pet) {
        Location loc = pet.getEntity().getLocation().add(0, 1, 0);
        World world = loc.getWorld();
        if (world == null) return;
        switch (pet.getParticleType()) {
            case TORNADO:
                for (int i = 0; i < 10; i++) {
                    double angle = i * 36 * Math.PI / 180;
                    double x = Math.cos(angle);
                    double z = Math.sin(angle);
                    world.spawnParticle(Particle.CLOUD,
                        loc.clone().add(x, 0, z), 1, 0, 0, 0, 0);
                }
                break;
            case LIGHTNING:
                world.spawnParticle(Particle.ELECTRIC_SPARK,
                    loc, 5, 0.3, 0.5, 0.3, 0);
                break;
            case GOLD:
                world.spawnParticle(Particle.NAUTILUS,
                    loc, 5, 0.3, 0.5, 0.3, 0);
                break;
            case FLAME:
                world.spawnParticle(Particle.FLAME,
                    loc, 5, 0.2, 0.3, 0.2, 0.02);
                break;
            case MAGIC:
                world.spawnParticle(Particle.WITCH,
                    loc, 10, 0.3, 0.5, 0.3, 0);
                break;
            case STAR:
                world.spawnParticle(Particle.TOTEM_OF_UNDYING,
                    loc, 3, 0.3, 0.5, 0.3, 0.1);
                break;
            case SOUL:
                world.spawnParticle(Particle.SOUL,
                    loc, 5, 0.2, 0.3, 0.2, 0.02);
                break;
            case SNOW:
                world.spawnParticle(Particle.SNOWFLAKE,
                    loc, 5, 0.3, 0.5, 0.3, 0);
                break;
            case WATER:
                world.spawnParticle(Particle.BUBBLE_POP,
                    loc, 5, 0.3, 0.5, 0.3, 0);
                break;
            case BLOOD:
                world.spawnParticle(Particle.DAMAGE_INDICATOR,
                    loc, 5, 0.3, 0.5, 0.3, 0);
                break;
            case FLOWER:
                world.spawnParticle(Particle.CHERRY_LEAVES,
                    loc, 5, 0.3, 0.5, 0.3, 0);
                break;
            case SMOKE:
                world.spawnParticle(Particle.LARGE_SMOKE,
                    loc, 3, 0.2, 0.3, 0.2, 0);
                break;
            case EXPLOSION:
                world.spawnParticle(Particle.POOF,
                    loc, 5, 0.2, 0.3, 0.2, 0.05);
                break;
            case RAINBOW:
                world.spawnParticle(Particle.DUST,
                    loc, 5, 0.3, 0.5, 0.3,
                    new Particle.DustOptions(
                        Color.fromRGB(
                            new Random().nextInt(255),
                            new Random().nextInt(255),
                            new Random().nextInt(255)), 1.5f));
                break;
            case DIAMOND:
                world.spawnParticle(Particle.ENCHANT,
                    loc, 10, 0.3, 0.5, 0.3, 0.5);
                break;
            default:
                break;
        }
    }

    public void feedPet(Pet pet, Player owner) {
        pet.setHunger(Math.min(100, pet.getHunger() + 30));
        pet.setMood(Pet.Mood.HAPPY);
        if (pet.getEntity() != null) {
            Location loc = pet.getEntity().getLocation();
            loc.getWorld().spawnParticle(Particle.HEART,
                loc.add(0, 2, 0), 10, 0.5, 0.5, 0.5, 0);
        }
        owner.playSound(owner.getLocation(),
            Sound.ENTITY_GENERIC_EAT, 1f, 1f);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
            owner.playSound(owner.getLocation(),
                Sound.ENTITY_CAT_PURR, 1f, 1f), 20L);
        owner.sendMessage("§6§l[AdvancedPets] §e" + pet.getName() +
            " §fdice: §a¡Ñam ñam! ¡Gracias amo! 🍎❤️");
    }

    public void savePet(Pet pet) {
        File file = new File(dataFolder,
            pet.getOwnerUUID().toString() + ".yml");
        FileConfiguration config =
            YamlConfiguration.loadConfiguration(file);
        config.set("petUUID", pet.getPetUUID().toString());
        config.set("ownerName", pet.getOwnerName());
        config.set("name", pet.getName());
        config.set("entityType", pet.getEntityType().name());
        config.set("rarity", pet.getRarity().name());
        config.set("level", pet.getLevel());
        config.set("xp", pet.getXp());
        config.set("xpNeeded", pet.getXpNeeded());
        config.set("health", pet.getHealth());
        config.set("maxHealth", pet.getMaxHealth());
        config.set("damage", pet.getDamage());
        config.set("kills", pet.getKills());
        config.set("hunger", pet.getHunger());
        config.set("immortal", pet.isImmortal());
        config.set("combatMode", pet.isCombatMode());
        config.set("workMode", pet.isWorkMode());
        config.set("particleType", pet.getParticleType().name());
        config.set("birthdayDate", pet.getBirthdayDate());
        config.set("clanName", pet.getClanName());
        config.set("summoned", pet.isSummoned());
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveAllPets() {
        for (Pet pet : activePets.values()) savePet(pet);
    }

    private void loadAllPets() {
        if (!dataFolder.exists()) return;
        File[] files = dataFolder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            FileConfiguration config =
                YamlConfiguration.loadConfiguration(file);
            try {
                UUID ownerUUID = UUID.fromString(
                    file.getName().replace(".yml", ""));
                String ownerName =
                    config.getString("ownerName", "Unknown");
                String name = config.getString("name", "Pet");
                EntityType entityType = EntityType.valueOf(
                    config.getString("entityType", "WOLF"));
                Pet.Rarity rarity = Pet.Rarity.valueOf(
                    config.getString("rarity", "COMMON"));
                Pet pet = new Pet(ownerUUID, ownerName,
                    name, entityType, rarity);
                pet.setLevel(config.getInt("level", 1));
                pet.setXp(config.getDouble("xp", 0));
                pet.setXpNeeded(config.getDouble("xpNeeded", 100));
                pet.setHealth(config.getDouble("health", 20));
                pet.setMaxHealth(config.getDouble("maxHealth", 20));
                pet.setDamage(config.getInt("damage", 10));
                pet.setKills(config.getInt("kills", 0));
                pet.setHunger(config.getDouble("hunger", 100));
                pet.setImmortal(config.getBoolean("immortal", true));
                pet.setCombatMode(config.getBoolean("combatMode", true));
                pet.setWorkMode(config.getBoolean("workMode", true));
                pet.setParticleType(Pet.ParticleType.valueOf(
                    config.getString("particleType", "NONE")));
                pet.setBirthdayDate(
                    config.getString("birthdayDate", null));
                pet.setClanName(
                    config.getString("clanName", null));
                pet.setSummoned(
                    config.getBoolean("summoned", false));
                // ✅ SIEMPRE sleeping = false al cargar
                pet.setSleeping(false);
                activePets.put(ownerUUID, pet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void respawnPetOnJoin(Player player) {
        Pet pet = activePets.get(player.getUniqueId());
        if (pet == null) return;
        if (pet.isSummoned()) {
            pet.setSummoned(false);
            pet.setEntity(null);
            // ✅ SIEMPRE sleeping = false al respawnear
            pet.setSleeping(false);
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                spawnPet(pet, player.getLocation()), 40L);
        }
    }

    public Map<UUID, Pet> getAllPets() { return activePets; }
}
