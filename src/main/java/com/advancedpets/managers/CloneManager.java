package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class CloneManager {

    private final AdvancedPets plugin;
    // ✅ Guardar clones activos por mascota
    private final Map<UUID, List<Entity>> activeClones = new HashMap<>();
    // ✅ Cooldown para no spawnear clones cada tick
    private final Map<UUID, Long> cloneCooldown = new HashMap<>();
    private static final long COOLDOWN_MS = 15000; // 15 segundos entre invocaciones

    public CloneManager(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    public void spawnClones(Pet pet, LivingEntity target, Player owner) {
        // ✅ Verificar cooldown
        long now = System.currentTimeMillis();
        Long lastUse = cloneCooldown.get(pet.getPetUUID());
        if (lastUse != null && now - lastUse < COOLDOWN_MS) return;

        // ✅ No spawnear si ya hay clones activos
        if (activeClones.containsKey(pet.getPetUUID()) &&
            !activeClones.get(pet.getPetUUID()).isEmpty()) return;

        cloneCooldown.put(pet.getPetUUID(), now);

        Location petLoc = pet.getEntity().getLocation();
        World world = petLoc.getWorld();
        if (world == null) return;

        List<Entity> clones = new ArrayList<>();

        // ✅ Spawnear 3 clones alrededor de la mascota
        for (int i = 0; i < 3; i++) {
            double angle = i * 120 * Math.PI / 180;
            double x = Math.cos(angle) * 2;
            double z = Math.sin(angle) * 2;
            Location cloneLoc = petLoc.clone().add(x, 0, z);

            Entity clone = world.spawnEntity(
                cloneLoc, pet.getEntityType());

            // ✅ Configurar clone
            if (clone instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) clone;
                living.setMaxHealth(10.0);
                living.setHealth(10.0);
                living.setCustomName(getCloneName(pet));
                living.setCustomNameVisible(true);
                if (clone instanceof Mob) {
                    ((Mob) clone).setAware(false);
                }
                living.setRemoveWhenFarAway(false);
            }
            clone.setPersistent(false);
            clones.add(clone);

            // ✅ Efecto de spawn del clone
            spawnCloneAppearEffect(pet, cloneLoc);
        }

        activeClones.put(pet.getPetUUID(), clones);

        // ✅ Mensaje al dueño
        owner.sendMessage("§5§l[AdvancedPets] §e" + pet.getName() +
            " §finvocó §53 clones §fpara atacar! 👥⚔");
        owner.playSound(owner.getLocation(),
            Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f);

        // ✅ Hacer que los clones ataquen al target
        startCloneAttack(pet, clones, target, owner);

        // ✅ Eliminar clones después de 10 segundos
        new BukkitRunnable() {
            @Override
            public void run() {
                removeClones(pet, owner);
            }
        }.runTaskLater(plugin, 200L); // 10 segundos
    }

    private void startCloneAttack(Pet pet, List<Entity> clones,
        LivingEntity target, Player owner) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                // ✅ Cancelar si los clones fueron eliminados
                if (ticks >= 180 || !activeClones.containsKey(
                    pet.getPetUUID())) {
                    cancel();
                    return;
                }
                // ✅ Atacar al target cada 1 segundo
                if (ticks % 20 == 0) {
                    for (Entity clone : clones) {
                        if (clone.isDead() || !clone.isValid()) continue;
                        if (target.isDead() || !target.isValid()) {
                            cancel();
                            return;
                        }
                        // ✅ Daño de los clones = mitad del daño de la mascota
                        target.damage(pet.getDamage() / 2.0, clone);
                        // ✅ Partículas de rareza al atacar
                        spawnRarityParticle(pet,
                            target.getLocation().add(0, 1, 0));
                        // ✅ Teletransportar clone cerca del target
                        Location attackLoc = target.getLocation()
                            .clone().add(
                                (Math.random() - 0.5) * 3,
                                0,
                                (Math.random() - 0.5) * 3);
                        clone.teleport(attackLoc);
                    }
                }
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    public void removeClones(Pet pet, Player owner) {
        List<Entity> clones = activeClones.remove(pet.getPetUUID());
        if (clones == null) return;
        for (Entity clone : clones) {
            if (clone.isDead() || !clone.isValid()) continue;
            // ✅ Explosión de partículas al eliminar clone
            spawnCloneDeathEffect(pet, clone.getLocation());
            clone.remove();
        }
        if (owner != null && owner.isOnline()) {
            owner.sendMessage("§8[AdvancedPets] §7Los clones de §e" +
                pet.getName() + " §7desaparecieron...");
        }
    }

    public void removeAllClones() {
        for (Map.Entry<UUID, List<Entity>> entry :
            activeClones.entrySet()) {
            for (Entity clone : entry.getValue()) {
                if (!clone.isDead() && clone.isValid()) {
                    spawnCloneDeathEffect(null, clone.getLocation());
                    clone.remove();
                }
            }
        }
        activeClones.clear();
    }

    // ✅ Partículas de aparición del clone
    private void spawnCloneAppearEffect(Pet pet, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.5f);
        spawnRarityParticle(pet, loc);
        world.spawnParticle(Particle.POOF, loc, 20,
            0.3, 0.5, 0.3, 0.1);
    }

    // ✅ Partículas de muerte del clone
    private void spawnCloneDeathEffect(Pet pet, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        world.playSound(loc, Sound.ENTITY_ENDERMAN_DEATH, 1f, 1.5f);
        if (pet != null) spawnRarityParticle(pet, loc);
        world.spawnParticle(Particle.EXPLOSION, loc, 3,
            0.3, 0.3, 0.3, 0);
        world.spawnParticle(Particle.POOF, loc, 30,
            0.5, 0.5, 0.5, 0.1);
    }

    // ✅ Partículas según rareza de la mascota
    private void spawnRarityParticle(Pet pet, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        switch (pet.getRarity()) {
            case COMMON:
                world.spawnParticle(Particle.POOF,
                    loc, 10, 0.3, 0.3, 0.3, 0.05);
                break;
            case RARE:
                world.spawnParticle(Particle.DUST,
                    loc, 15, 0.3, 0.5, 0.3,
                    new Particle.DustOptions(Color.BLUE, 2f));
                break;
            case EPIC:
                world.spawnParticle(Particle.DUST,
                    loc, 20, 0.3, 0.5, 0.3,
                    new Particle.DustOptions(Color.PURPLE, 2.5f));
                world.spawnParticle(Particle.WITCH,
                    loc, 10, 0.3, 0.5, 0.3, 0.1);
                break;
            case LEGENDARY:
                world.spawnParticle(Particle.DUST,
                    loc, 25, 0.5, 0.5, 0.5,
                    new Particle.DustOptions(Color.YELLOW, 3f));
                world.spawnParticle(Particle.TOTEM_OF_UNDYING,
                    loc, 15, 0.5, 0.5, 0.5, 0.2);
                world.spawnParticle(Particle.FLAME,
                    loc, 10, 0.3, 0.3, 0.3, 0.05);
                break;
        }
    }

    private String getCloneName(Pet pet) {
        String rc = pet.getRarityColor();
        return rc + "§l👥 Clone de " + pet.getName();
    }

    public boolean hasActiveClones(Pet pet) {
        return activeClones.containsKey(pet.getPetUUID()) &&
            !activeClones.get(pet.getPetUUID()).isEmpty();
    }

    public long getCooldownRemaining(Pet pet) {
        Long lastUse = cloneCooldown.get(pet.getPetUUID());
        if (lastUse == null) return 0;
        long remaining = COOLDOWN_MS - (System.currentTimeMillis() - lastUse);
        return Math.max(0, remaining / 1000);
    }
}
