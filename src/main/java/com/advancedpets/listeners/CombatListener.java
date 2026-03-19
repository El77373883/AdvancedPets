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

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        // ✅ Si atacan al amo → mascota contraataca
        if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Pet pet = plugin.getPetManager().getPet(victim.getUniqueId());
            if (pet == null || !pet.isSummoned() || !pet.isCombatMode()) return;
            Entity attacker = event.getDamager();

            // No contraatacar si el atacante es la propia mascota
            if (pet.getEntity() != null &&
                attacker.equals(pet.getEntity())) return;

            if (attacker instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) attacker;
                // ✅ Aplicar daño configurado
                target.damage(pet.getDamage(), pet.getEntity());
                victim.sendMessage("§c§l[AdvancedPets] §e" +
                    pet.getName() +
                    " §fdice: §c¡NADIE toca a mi amo! ⚔ ¡A PELEAR!");
                victim.getWorld().playSound(victim.getLocation(),
                    Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.8f);
                if (target.isDead() || target.getHealth() <= 0) {
                    pet.setKills(pet.getKills() + 1);
                    onPetKill(victim, pet, target);
                }
            }
        }

        // ✅ Si el amo ataca a alguien → mascota ayuda con daño
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Pet pet = plugin.getPetManager().getPet(attacker.getUniqueId());
            if (pet == null || !pet.isSummoned() || !pet.isCombatMode()) return;

            Entity victim = event.getEntity();

            // No atacar a sí mismo ni a su propia mascota
            if (victim.equals(attacker)) return;
            if (pet.getEntity() != null && victim.equals(pet.getEntity())) return;

            if (victim instanceof LivingEntity) {
                LivingEntity target = (LivingEntity) victim;
                // ✅ Programar el daño en el siguiente tick
                // para evitar conflictos con el evento
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (target.isValid() && !target.isDead()) {
                        // ✅ Daño real configurado en GUI
                        target.damage(pet.getDamage(),
                            pet.getEntity() != null ?
                            pet.getEntity() : attacker);
                        attacker.sendMessage("§7[AP] §e" +
                            pet.getName() +
                            " §fdice: §6¡Por mi amo daré todo! 💪🔥");
                        if (target.isDead() || target.getHealth() <= 0) {
                            pet.setKills(pet.getKills() + 1);
                            onPetKill(attacker, pet, target);
                        }
                    }
                }, 1L);
            }
        }
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

        // ✅ Efecto Ultra Premium al matar
        UltraShopGUI.playKillEffect(plugin, owner, killed.getLocation());

        plugin.getPetManager().savePet(pet);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        Pet pet = plugin.getPetManager().getPet(dead.getUniqueId());
        if (pet == null || !pet.isSummoned()) return;
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
    public void onPlayerDamage(EntityDamageEvent event) {
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

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        for (Pet pet : plugin.getPetManager().getAllPets().values()) {
            if (pet.getEntity() != null &&
                pet.getEntity().equals(event.getEntity())) {
                // ✅ Si es inmortal cancelar el daño
                if (pet.isImmortal()) {
                    event.setCancelled(true);
                    return;
                }
                Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
                if (owner != null) {
                    pet.setHealth(Math.max(0,
                        pet.getHealth() - event.getFinalDamage()));
                    owner.sendMessage("§e" + pet.getName() +
                        " §fdice: §c¡AY! ¡Eso dolió! 😤");
                    if (pet.getHealth() <= 0 && !pet.isImmortal()) {
                        owner.sendMessage(
                            "§c§l[AdvancedPets] §c¡Tu mascota ha muerto! 💀");
                        plugin.getPetManager().despawnPet(pet);
                    }
                }
                return;
            }
        }
    }
}
