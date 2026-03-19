package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class AchievementManager {

    private final AdvancedPets plugin;
    private final Map<UUID, Set<String>> playerAchievements = new HashMap<>();

    public AchievementManager(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    public void checkAchievements(Player player, Pet pet) {
        checkKillAchievements(player, pet);
        checkLevelAchievements(player, pet);
        checkRarityAchievements(player, pet);
    }

    private void checkKillAchievements(Player player, Pet pet) {
        if (pet.getKills() >= 1) grantAchievement(player, pet, "primer_kill", "§6⭐ Primer Kill", "§fTu mascota hizo su primer kill!");
        if (pet.getKills() >= 10) grantAchievement(player, pet, "10_kills", "§6⭐ Asesino Novato", "§fTu mascota tiene 10 kills!");
        if (pet.getKills() >= 50) grantAchievement(player, pet, "50_kills", "§c⭐ Asesino Experto", "§fTu mascota tiene 50 kills!");
        if (pet.getKills() >= 100) grantAchievement(player, pet, "100_kills", "§4⭐ Máquina de Guerra", "§fTu mascota tiene 100 kills!");
        if (pet.getKills() >= 500) grantAchievement(player, pet, "500_kills", "§5⭐ LEGENDARIO EN COMBATE", "§fTu mascota tiene 500 kills!");
    }

    private void checkLevelAchievements(Player player, Pet pet) {
        if (pet.getLevel() >= 5) grantAchievement(player, pet, "nivel_5", "§a⭐ Nivel 5", "§fTu mascota llegó al nivel 5!");
        if (pet.getLevel() >= 10) grantAchievement(player, pet, "nivel_10", "§b⭐ Nivel 10", "§fTu mascota llegó al nivel 10!");
        if (pet.getLevel() >= 25) grantAchievement(player, pet, "nivel_25", "§d⭐ Nivel 25", "§fTu mascota llegó al nivel 25!");
        if (pet.getLevel() >= 50) grantAchievement(player, pet, "nivel_50", "§6⭐ Nivel MÁXIMO", "§fTu mascota llegó al nivel 50!");
    }

    private void checkRarityAchievements(Player player, Pet pet) {
        if (pet.getRarity() == Pet.Rarity.LEGENDARY) {
            grantAchievement(player, pet, "legendaria", "§6⚡ Mascota Legendaria", "§fTienes una mascota legendaria!");
        }
    }

    private void grantAchievement(Player player, Pet pet, String id, String title, String desc) {
        Set<String> achievements = playerAchievements.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        if (achievements.contains(id)) return;
        achievements.add(id);
        player.sendMessage("§r");
        player.sendMessage("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§e§l  🏆 LOGRO DESBLOQUEADO 🏆");
        player.sendMessage("§f  " + title);
        player.sendMessage("§7  " + desc);
        player.sendMessage("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§r");
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }

    public Set<String> getAchievements(UUID playerUUID) {
        return playerAchievements.getOrDefault(playerUUID, new HashSet<>());
    }

    public int getAchievementCount(UUID playerUUID) {
        return getAchievements(playerUUID).size();
    }
}
