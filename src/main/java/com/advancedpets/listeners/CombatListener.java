package com.advancedpets.listeners;

import com.advancedpets.AdvancedPets;
import com.advancedpets.gui.UltraShopGUI;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

public class CombatListener implements Listener {

    private final AdvancedPets plugin;

    public CombatListener(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    // ✅ PRIORIDAD HIGHEST para que se ejecute antes que otros plugins
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        // ✅ Si atacan al amo → mascota contraataca
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Pet pet = plugin.getPetManager().getPet(victim.getUniqueId());
            if (pet == null || !pet.isSummoned() || !pet.isCombatMode()) return;
            Entity attacker = event.getDamager();
            if (pet.getEntity() != null &&
                attacker.equals(pet.getEntity())) return;

            if (attacker instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) attacker;
                target.damage(pet.getDamage(), pet.getEntity());
                victim.sendMessage("§c§l[AdvancedPets] §e" +
                    pet.getName() +
                    " §fdice: §c¡NADIE toca a mi amo! ⚔ ¡A PELEAR!");
                // ✅ Sonido de ataque siempre
                playAttackSound(pet, victim.getLocation());
                if (target.isDead() || target.getHealth() <= 0) {
                    pet.setKills(pet.getKills() + 1);
                    onPetKill(victim, pet, target);
                }
            }
        }

        // ✅ Si el amo ataca → mascota ayuda
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Pet pet = plugin.getPetManager().getPet(attacker.getUniqueId());
            if (pet == null || !pet.isSummoned() || !pet.isCombatMode()) return;

            Entity victim = event.getEntity();
            if (victim.equals(attacker)) return;
            if (pet.getEntity() != null &&
                victim.equals(pet.getEntity())) return;

            if (victim instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) victim;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (target.isValid() && !target.isDead()) {
                        target.damage(pet.getDamage(),
                            pet.getEntity() != null ?
                            pet.getEntity() : attacker);
                        attacker.sendMessage("§7[AP] §e" +
                            pet.getName() +
                            " §fdice: §6¡Por mi amo daré todo! 💪🔥");
                        // ✅ Sonido de ataque siempre
                        playAttackSound(pet, target.getLocation());
                        if (target.isDead() ||
                            target.getHealth() <= 0) {
                            pet.setKills(pet.getKills() + 1);
                            onPetKill(attacker, pet, target);
                        }
                    }
                }, 1L);
            }
        }
    }

    // ✅ Sonido de ataque según tipo de mob
    private void playAttackSound(Pet pet, Location loc) {
        World world = loc.getWorld();
        if (world == null) return;
        Sound sound;
        switch (pet.getEntityType()) {
            case WOLF:
                sound = Sound.ENTITY_WOLF_GROWL; break;
            case CAT:
                sound = Sound.ENTITY_CAT_HISS; break;
            case TURTLE:
                // ✅ ARREGLADO — Tortuga tiene su sonido
                sound = Sound.ENTITY_TURTLE_AMBIENT_LAND; break;
            case ENDER_DRAGON:
                sound = Sound.ENTITY_ENDER_DRAGON_GROWL; break;
            case WITHER:
                sound = Sound.ENTITY_WITHER_SHOOT; break;
            case IRON_GOLEM:
                sound = Sound.ENTITY_IRON_GOLEM_ATTACK; break;
            case CREEPER:
                sound = Sound.ENTITY_CREEPER_PRIMED; break;
            case BLAZE:
                sound = Sound.ENTITY_BLAZE_SHOOT; break;
            case ENDERMAN:
                sound = Sound.ENTITY_ENDERMAN_SCREAM; break;
            case SKELETON:
                sound = Sound.ENTITY_SKELETON_SHOOT; break;
            case ZOMBIE:
            case ZOMBIE_VILLAGER:
            case HUSK:
            case DROWNED:
                sound = Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR; break;
            case WITCH:
                sound = Sound.ENTITY_WITCH_THROW; break;
            case SPIDER:
            case CAVE_SPIDER:
                sound = Sound.ENTITY_SPIDER_ATTACK; break;
            case WARDEN:
                sound = Sound.ENTITY_WARDEN_ATTACK_IMPACT; break;
            case RAVAGER:
                sound = Sound.ENTITY_RAVAGER_ATTACK; break;
            case VEX:
                sound = Sound.ENTITY_VEX_CHARGE; break;
            case VINDICATOR:
            case PILLAGER:
                sound = Sound.ENTITY_VINDICATOR_CELEBRATE; break;
            case BEE:
                sound = Sound.ENTITY_BEE_STING; break;
            case POLAR_BEAR:
                sound = Sound.ENTITY_POLAR_BEAR_WARNING; break;
            case HOGLIN:
                sound = Sound.ENTITY_HOGLIN_ATTACK; break;
            case PIGLIN:
            case PIGLIN_BRUTE:
                sound = Sound.ENTITY_PIGLIN_ANGRY; break;
            case PHANTOM:
                sound = Sound.ENTITY_PHANTOM_BITE; break;
            case GUARDIAN:
            case ELDER_GUARDIAN:
                sound = Sound.ENTITY_GUARDIAN_ATTACK; break;
            case SHULKER:
                sound = Sound.ENTITY_SHULKER_SHOOT; break;
            default:
                sound = Sound.ENTITY_PLAYER_ATTACK_SWEEP; break;
        }
        world.playSound(loc, sound, 1f, 1f);
        // ✅ Partículas de ataque siempre visibles
        world.spawnParticle(Particle.SWEEP_ATTACK, loc, 5,
            0.3, 0.3, 0.3, 0);
    }

    private void onPetKill(Player owner, Pet pet, LivingEntity killed) {
        plugin.getAchievementManager().checkAchievements(owner, pet);
        plugin.getMissionManager().addProgress(owner.getUniqueId(), 1);

        String[] insults = {
            "§c¡Eso es todo lo que tienes?! 😤",
            "§c¡Jajaja! ¡Vuelve cuando seas más fuerte! 💀",
            "§c¡Patético! ¡Ni siquiera sudé! 😎",
            "§c¡GG fácil! ¡Mi amo es el mejor! 🏆",
            "§c¡Eso te pasa por meterte con nosotros! ⚔"
        };
        owner.sendMessage("§e" + pet.getName() + " §fdice: " +
            insults[new java.util.Random().nextInt(insults.length)]);
        owner.getWorld().playSound(owner.getLocation(),
            Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        UltraShopGUI.playKillEffect(plugin, owner, killed.getLocation());
        plugin.getPetManager().savePet(pet);
    }

    // ✅ Mascota inmortal — cancelar daño correctamente
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        // ✅ Ignorar ArmorStands
        if (event.getEntity() instanceof ArmorStand) return;

        for (Pet pet : plugin.getPetManager().getAllPets().values()) {
            if (pet.getEntity() == null) continue;
            if (!pet.getEntity().equals(event.getEntity())) continue;

            if (pet.isImmortal()) {
                // ✅ Cancelar TODO el daño si es inmortal
                event.setCancelled(true);
                // ✅ Mostrar partículas de escudo
                Location loc = pet.getEntity().getLocation();
                loc.getWorld().spawnParticle(
                    Particle.BLOCK,
                    loc.add(0, 1, 0), 10,
                    0.3, 0.3, 0.3,
                    Material.IRON_BLOCK.createBlockData());
                return;
            }

            // ✅ Mascota mortal — recibir daño normal
            Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
            if (owner != null) {
                pet.setHealth(Math.max(0,
                    pet.getHealth() - event.getFinalDamage()));
                owner.sendMessage("§e" + pet.getName() +
                    " §fdice: §c¡AY! ¡Eso dolió! 😤 ¡Me las pagarás!");
                // ✅ Sonido de dolor
                pet.getEntity().getWorld().playSound(
                    pet.getEntity().getLocation(),
                    Sound.ENTITY_PLAYER_HURT, 1f, 1f);
                if (pet.getHealth() <= 0) {
                    owner.sendMessage(
                        "§c§l[AdvancedPets] §c¡Tu mascota ha muerto! 💀 " +
                        "¡Revívela con /ap revive!");
                    plugin.getPetManager().despawnPet(pet);
                }
            }
            return;
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Pet pet = plugin.getPetManager().getPet(dead.getUniqueId());
        if (pet == null || !pet.isSummoned() ||
            pet.getEntity() == null) return;
        Location loc = pet.getEntity().getLocation();
        World world = loc.getWorld();
        if (world == null) return;
        world.spawnParticle(Particle.HEART,
            loc.add(0, 2, 0), 20, 0.5, 0.5, 0.5, 0);
        world.playSound(loc, Sound.ENTITY_WOLF_WHINE, 1f, 0.6f);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getLocation().distance(loc) < 30) {
                p.sendMessage("§8[AP] §7La mascota de §f" +
                    dead.getName() + " §7llora... 😢💔");
            }
        }
    }

    @EventHandler
    public void onPlayerDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null || !pet.isSummoned()) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL ||
            event.getCause() == EntityDamageEvent.DamageCause.VOID) {
            player.sendMessage("§e" + pet.getName() +
                " §fdice: §c¡Amo cuidado! 😱 ¡Estás cayendo!");
        }
    }
}
