package com.advancedpets.models;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class Pet {

    public enum Rarity { COMMON, RARE, EPIC, LEGENDARY }
    public enum Mood { HAPPY, TIRED, ANGRY, SAD, HUNGRY }
    public enum WorkType { NONE, MINING, FARMING, PVP, ATTACK, BUILD, EXP, CHOP, HARVEST, COLLECT, COOK, EXPLORE }
    public enum ParticleType { NONE, TORNADO, LIGHTNING, GOLD, DIAMOND, FLAME, SNOW, WATER, MAGIC, STAR, BLOOD, FLOWER, SMOKE, RAINBOW, EXPLOSION, SOUL }

    private UUID ownerUUID;
    private String ownerName;
    private String name;
    private EntityType entityType;
    private Rarity rarity;
    private Mood mood;
    private WorkType currentWork;
    private ParticleType particleType;

    private int level;
    private double xp;
    private double xpNeeded;
    private double health;
    private double maxHealth;
    private int damage;
    private int kills;
    private double hunger;
    private boolean immortal;
    private boolean combatMode;
    private boolean workMode;
    private boolean summoned;
    private boolean sleeping;

    private Entity entity;
    private Location location;
    private UUID petUUID;

    private String birthdayDate;
    private String clanName;

    public Pet(UUID ownerUUID, String ownerName, String name, EntityType entityType, Rarity rarity) {
        this.petUUID = UUID.randomUUID();
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        this.name = name;
        this.entityType = entityType;
        this.rarity = rarity;
        this.mood = Mood.HAPPY;
        this.currentWork = WorkType.NONE;
        this.particleType = ParticleType.NONE;
        this.level = 1;
        this.xp = 0;
        this.xpNeeded = 100;
        this.maxHealth = getHealthByRarity(rarity);
        this.health = maxHealth;
        this.damage = 10;
        this.kills = 0;
        this.hunger = 100.0;
        this.immortal = true;
        this.combatMode = true;
        this.workMode = true;
        this.summoned = false;
        this.sleeping = false;
    }

    private double getHealthByRarity(Rarity rarity) {
        switch (rarity) {
            case COMMON: return 20.0;
            case RARE: return 40.0;
            case EPIC: return 80.0;
            case LEGENDARY: return 200.0;
            default: return 20.0;
        }
    }

    public void addXP(double amount) {
        this.xp += amount;
        if (this.xp >= this.xpNeeded) {
            levelUp();
        }
    }

    public void levelUp() {
        this.level++;
        this.xp = 0;
        this.xpNeeded = this.level * 100;
        this.maxHealth += 5;
        this.health = this.maxHealth;
    }

    public String getRarityColor() {
        switch (rarity) {
            case COMMON: return "§f";
            case RARE: return "§9";
            case EPIC: return "§5";
            case LEGENDARY: return "§6";
            default: return "§f";
        }
    }

    public String getRarityName() {
        switch (rarity) {
            case COMMON: return "COMÚN";
            case RARE: return "RARA";
            case EPIC: return "ÉPICA";
            case LEGENDARY: return "LEGENDARIA";
            default: return "COMÚN";
        }
    }

    public String getMoodColor() {
        switch (mood) {
            case HAPPY: return "§a";
            case TIRED: return "§b";
            case ANGRY: return "§c";
            case SAD: return "§8";
            case HUNGRY: return "§6";
            default: return "§a";
        }
    }

    // Getters y Setters
    public UUID getPetUUID() { return petUUID; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }
    public Rarity getRarity() { return rarity; }
    public void setRarity(Rarity rarity) { this.rarity = rarity; }
    public Mood getMood() { return mood; }
    public void setMood(Mood mood) { this.mood = mood; }
    public WorkType getCurrentWork() { return currentWork; }
    public void setCurrentWork(WorkType currentWork) { this.currentWork = currentWork; }
    public ParticleType getParticleType() { return particleType; }
    public void setParticleType(ParticleType particleType) { this.particleType = particleType; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public double getXp() { return xp; }
    public void setXp(double xp) { this.xp = xp; }
    public double getXpNeeded() { return xpNeeded; }
    public void setXpNeeded(double xpNeeded) { this.xpNeeded = xpNeeded; }
    public double getHealth() { return health; }
    public void setHealth(double health) { this.health = health; }
    public double getMaxHealth() { return maxHealth; }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }
    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public double getHunger() { return hunger; }
    public void setHunger(double hunger) { this.hunger = hunger; }
    public boolean isImmortal() { return immortal; }
    public void setImmortal(boolean immortal) { this.immortal = immortal; }
    public boolean isCombatMode() { return combatMode; }
    public void setCombatMode(boolean combatMode) { this.combatMode = combatMode; }
    public boolean isWorkMode() { return workMode; }
    public void setWorkMode(boolean workMode) { this.workMode = workMode; }
    public boolean isSummoned() { return summoned; }
    public void setSummoned(boolean summoned) { this.summoned = summoned; }
    public boolean isSleeping() { return sleeping; }
    public void setSleeping(boolean sleeping) { this.sleeping = sleeping; }
    public Entity getEntity() { return entity; }
    public void setEntity(Entity entity) { this.entity = entity; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    public String getBirthdayDate() { return birthdayDate; }
    public void setBirthdayDate(String birthdayDate) { this.birthdayDate = birthdayDate; }
    public String getClanName() { return clanName; }
    public void setClanName(String clanName) { this.clanName = clanName; }
}
