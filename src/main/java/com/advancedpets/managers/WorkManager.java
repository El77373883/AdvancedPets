package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class WorkManager {

    private final AdvancedPets plugin;
    private final Map<UUID, BukkitTask> workTasks = new HashMap<>();

    public WorkManager(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    public void startWork(Pet pet, Pet.WorkType workType, Player owner) {
        stopWork(pet);
        if (pet.getHunger() <= 0 &&
            plugin.getConfig().getBoolean("pets.hunger.refuse-work-when-hungry", true)) {
            owner.sendMessage("§c§l[AdvancedPets] §e" + pet.getName() +
                " §fdice: §c¡Tengo mucha hambre! 😭 ¡Aliméntame antes de trabajar!");
            return;
        }
        pet.setCurrentWork(workType);
        owner.sendMessage("§6§l[AdvancedPets] §e" + pet.getName() +
            " §fdice: §a¡Entendido amo! Empezando: §e" + getWorkName(workType) + " §a💪");
        owner.playSound(owner.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);

        long interval = getWorkInterval(workType);
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!pet.isSummoned() || pet.getEntity() == null || pet.isSleeping()) return;
                Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
                if (owner == null) return;
                executeWork(pet, workType, owner);
            }
        }.runTaskTimer(plugin, interval, interval);
        workTasks.put(pet.getPetUUID(), task);
    }

    public void stopWork(Pet pet) {
        BukkitTask task = workTasks.remove(pet.getPetUUID());
        if (task != null) task.cancel();
        pet.setCurrentWork(Pet.WorkType.NONE);
    }

    private void executeWork(Pet pet, Pet.WorkType workType, Player owner) {
        Location loc = pet.getEntity().getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        switch (workType) {
            case MINING: executeMining(pet, owner, loc, world); break;
            case FARMING: executeFarming(pet, owner, loc, world); break;
            case HARVEST: executeHarvest(pet, owner, loc, world); break;
            case COLLECT: executeCollect(pet, owner, loc, world); break;
            case CHOP: executeChop(pet, owner, loc, world); break;
            case EXP: executeExp(pet, owner); break;
            case EXPLORE: executeExplore(pet, owner, loc, world); break;
            case COOK: executeCook(pet, owner, loc, world); break;
            default: break;
        }
        pet.addXP(plugin.getConfig().getDouble("xp.per-mine", 5));
        plugin.getPetManager().savePet(pet);
    }

    private void executeMining(Pet pet, Player owner, Location loc, World world) {
        int radius = plugin.getConfig().getInt("works.mine.radius", 5);
        List<Material> mineables = Arrays.asList(
            Material.COAL_ORE, Material.IRON_ORE, Material.GOLD_ORE,
            Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.REDSTONE_ORE,
            Material.LAPIS_ORE, Material.COPPER_ORE,
            Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.DEEPSLATE_LAPIS_ORE, Material.DEEPSLATE_COPPER_ORE
        );
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.clone().add(x, y, z).getBlock();
                    if (mineables.contains(block.getType())) {
                        Collection<ItemStack> drops = block.getDrops(
                            new ItemStack(Material.DIAMOND_PICKAXE));
                        block.setType(Material.AIR);
                        for (ItemStack drop : drops) owner.getInventory().addItem(drop);
                        world.playSound(loc, Sound.BLOCK_STONE_BREAK, 1f, 1f);
                        owner.sendMessage("§7[AP] §e" + pet.getName() +
                            " §7minó: §f" + dropName(drops));
                        return;
                    }
                }
            }
        }
    }

    private void executeFarming(Pet pet, Player owner, Location loc, World world) {
        int radius = plugin.getConfig().getInt("works.farm.radius", 10);
        Collection<Entity> nearby = world.getNearbyEntities(loc, radius, radius, radius);
        for (Entity e : nearby) {
            if (e instanceof Monster && !e.equals(pet.getEntity())) {
                LivingEntity target = (LivingEntity) e;
                target.damage(pet.getDamage());
                if (target.isDead() || target.getHealth() <= 0) {
                    pet.setKills(pet.getKills() + 1);
                    owner.sendMessage("§7[AP] §e" + pet.getName() +
                        " §7eliminó: §c" + target.getType().name());
                }
                world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f);
                return;
            }
        }
    }

    private void executeHarvest(Pet pet, Player owner, Location loc, World world) {
        int radius = plugin.getConfig().getInt("works.harvest.radius", 8);
        List<Material> crops = Arrays.asList(
            Material.WHEAT, Material.CARROTS, Material.POTATOES,
            Material.BEETROOTS, Material.MELON, Material.PUMPKIN,
            Material.SUGAR_CANE, Material.BAMBOO
        );
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = loc.clone().add(x, 0, z).getBlock();
                if (crops.contains(block.getType())) {
                    Collection<ItemStack> drops = block.getDrops();
                    block.setType(Material.AIR);
                    for (ItemStack drop : drops) owner.getInventory().addItem(drop);
                    world.playSound(loc, Sound.BLOCK_CROP_BREAK, 1f, 1f);
                    owner.sendMessage("§7[AP] §e" + pet.getName() +
                        " §7cosechó: §a" + dropName(drops));
                    return;
                }
            }
        }
    }

    private void executeCollect(Pet pet, Player owner, Location loc, World world) {
        int radius = plugin.getConfig().getInt("works.collect.radius", 5);
        Collection<Entity> nearby = world.getNearbyEntities(loc, radius, radius, radius);
        for (Entity e : nearby) {
            if (e instanceof Item) {
                Item item = (Item) e;
                owner.getInventory().addItem(item.getItemStack());
                item.remove();
                owner.sendMessage("§7[AP] §e" + pet.getName() +
                    " §7recogió: §f" + item.getItemStack().getType().name());
                world.playSound(loc, Sound.ENTITY_ITEM_PICKUP, 1f, 1.5f);
                return;
            }
        }
    }

    private void executeChop(Pet pet, Player owner, Location loc, World world) {
        int radius = plugin.getConfig().getInt("works.chop.radius", 5);
        List<Material> logs = Arrays.asList(
            Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG, Material.CHERRY_LOG
        );
        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = loc.clone().add(x, y, z).getBlock();
                    if (logs.contains(block.getType())) {
                        Collection<ItemStack> drops = block.getDrops(
                            new ItemStack(Material.IRON_AXE));
                        block.setType(Material.AIR);
                        for (ItemStack drop : drops) owner.getInventory().addItem(drop);
                        world.playSound(loc, Sound.BLOCK_WOOD_BREAK, 1f, 1f);
                        owner.sendMessage("§7[AP] §e" + pet.getName() +
                            " §7taló: §6" + dropName(drops));
                        return;
                    }
                }
            }
        }
    }

    private void executeExp(Pet pet, Player owner) {
        owner.giveExp(5);
        owner.sendMessage("§7[AP] §e" + pet.getName() + " §7te dio §b5 XP ⭐");
        owner.playSound(owner.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1f);
    }

    private void executeExplore(Pet pet, Player owner, Location loc, World world) {
        List<Material> valuables = Arrays.asList(
            Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
            Material.IRON_INGOT, Material.COAL, Material.REDSTONE
        );
        ItemStack reward = new ItemStack(
            valuables.get(new Random().nextInt(valuables.size())),
            new Random().nextInt(3) + 1);
        owner.getInventory().addItem(reward);
        owner.sendMessage("§7[AP] §e" + pet.getName() +
            " §fdice: §a¡Amo encontré esto en las cuevas! 🗺 §f" +
            reward.getType().name() + " x" + reward.getAmount());
        world.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    private void executeCook(Pet pet, Player owner, Location loc, World world) {
        Map<Material, Material> cookMap = new HashMap<>();
        cookMap.put(Material.BEEF, Material.COOKED_BEEF);
        cookMap.put(Material.CHICKEN, Material.COOKED_CHICKEN);
        cookMap.put(Material.PORKCHOP, Material.COOKED_PORKCHOP);
        cookMap.put(Material.COD, Material.COOKED_COD);
        cookMap.put(Material.SALMON, Material.COOKED_SALMON);
        cookMap.put(Material.POTATO, Material.BAKED_POTATO);
        cookMap.put(Material.MUTTON, Material.COOKED_MUTTON);
        cookMap.put(Material.RABBIT, Material.COOKED_RABBIT);
        for (Map.Entry<Material, Material> entry : cookMap.entrySet()) {
            if (owner.getInventory().contains(entry.getKey())) {
                owner.getInventory().remove(new ItemStack(entry.getKey(), 1));
                owner.getInventory().addItem(new ItemStack(entry.getValue(), 1));
                owner.sendMessage("§7[AP] §e" + pet.getName() +
                    " §7cocinó: §6" + entry.getValue().name());
                world.playSound(loc, Sound.BLOCK_FURNACE_FIRE_CRACKLE, 1f, 1f);
                return;
            }
        }
    }

    private long getWorkInterval(Pet.WorkType workType) {
        switch (workType) {
            case MINING: return plugin.getConfig().getInt("works.mine.interval-seconds", 30) * 20L;
            case FARMING: return plugin.getConfig().getInt("works.farm.interval-seconds", 20) * 20L;
            case HARVEST: return plugin.getConfig().getInt("works.harvest.interval-seconds", 25) * 20L;
            case COLLECT: return plugin.getConfig().getInt("works.collect.interval-seconds", 10) * 20L;
            case CHOP: return plugin.getConfig().getInt("works.chop.interval-seconds", 30) * 20L;
            case EXP: return 100L;
            case EXPLORE: return plugin.getConfig().getInt("works.explore.interval-seconds", 120) * 20L;
            case COOK: return plugin.getConfig().getInt("works.cook.interval-seconds", 30) * 20L;
            default: return 40L;
        }
    }

    private String getWorkName(Pet.WorkType work) {
        switch (work) {
            case MINING: return "Minar ⛏";
            case FARMING: return "Farmear 🗡";
            case HARVEST: return "Cosechar 🌾";
            case COLLECT: return "Recoger items 🧲";
            case CHOP: return "Talar 🪓";
            case EXP: return "Hacer EXP ⭐";
            case EXPLORE: return "Explorar cuevas 🗺";
            case COOK: return "Cocinar 🍳";
            default: return "Trabajar";
        }
    }

    private String dropName(Collection<ItemStack> drops) {
        if (drops.isEmpty()) return "nada";
        ItemStack first = drops.iterator().next();
        return first.getType().name() + " x" + first.getAmount();
    }
}
