package com.advancedpets.listeners;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class PlayerListener implements Listener {

    private final AdvancedPets plugin;

    public PlayerListener(AdvancedPets plugin) {
        this.plugin = plugin;
        // ✅ Iniciar timer anti-anger para Warden
        startWardenAngerReset();
        startCreeperfuseReset();
    }

    // ✅ Timer para resetear anger del Warden cada 2 segundos
    private void startWardenAngerReset() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : plugin.getPetManager()
                    .getAllPets().values()) {
                    if (!pet.isSummoned() ||
                        pet.getEntity() == null) continue;
                    if (!(pet.getEntity() instanceof Warden))
                        continue;
                    Warden warden = (Warden) pet.getEntity();
                    // ✅ Resetear anger con todos los jugadores
                    for (Player p : pet.getEntity()
                        .getWorld().getPlayers()) {
                        warden.setAnger(p, 0);
                    }
                    warden.setTarget(null);
                }
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }

    // ✅ Timer para evitar que el Creeper explote
    private void startCreeperfuseReset() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Pet pet : plugin.getPetManager()
                    .getAllPets().values()) {
                    if (!pet.isSummoned() ||
                        pet.getEntity() == null) continue;
                    if (!(pet.getEntity() instanceof Creeper))
                        continue;
                    Creeper creeper = (Creeper) pet.getEntity();
                    // ✅ Cancelar mecha si se encendió
                    if (creeper.getState() ==
                        Creeper.State.PRIMED) {
                        creeper.setState(Creeper.State.IDLE);
                    }
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage().toLowerCase();
        Player sender = event.getPlayer();
        String[] badWords = {
            "idiota", "tonto", "malo", "noob",
            "estupido", "maldito"
        };
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (other.equals(sender)) continue;
            Pet pet = plugin.getPetManager()
                .getPet(other.getUniqueId());
            if (pet == null || !pet.isSummoned()) continue;
            for (String bad : badWords) {
                if (message.contains(bad) &&
                    message.contains(
                        other.getName().toLowerCase())) {
                    String[] defenses = {
                        "§c¡Oye! ¡No le hables así a mi amo! 😤",
                        "§c¡Cuidado! ¡Mi amo es el mejor! 💪",
                        "§c¡Repite eso y verás! ⚔"
                    };
                    Bukkit.getScheduler().runTask(plugin, () ->
                        Bukkit.broadcastMessage("§e" +
                            pet.getName() + " §fdice: " +
                            defenses[new Random()
                                .nextInt(defenses.length)]));
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager()
            .getPet(player.getUniqueId());
        if (pet == null || !pet.isSummoned() ||
            pet.getEntity() == null) return;
        for (Player other : player.getWorld().getPlayers()) {
            if (other.equals(player)) continue;
            if (player.getLocation()
                .distance(other.getLocation()) < 10) {
                if (other.getLastDamageCause() != null) {
                    pet.setMood(Pet.Mood.ANGRY);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLevelChange(
        PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager()
            .getPet(player.getUniqueId());
        if (pet == null || !pet.isSummoned() ||
            pet.getCurrentWork() != Pet.WorkType.EXP) return;
        pet.addXP(5);
        plugin.getHologramManager().updateHologram(pet);
        plugin.getAchievementManager()
            .checkAchievements(player, pet);
    }

    @EventHandler
    public void onPlayerWorldChange(
        PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager()
            .getPet(player.getUniqueId());
        if (pet == null || !pet.isSummoned()) return;
        String world = player.getWorld().getName();
        if (plugin.getConfig()
            .getStringList("worlds.blacklist")
            .contains(world)) {
            plugin.getPetManager().despawnPet(pet);
            player.sendMessage(
                "§c§l[AdvancedPets] §cTus mascotas no " +
                "pueden entrar a este mundo!");
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                plugin.getPetManager().spawnPet(
                    pet, player.getLocation()), 20L);
        }
    }
}
