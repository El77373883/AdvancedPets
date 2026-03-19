package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MissionManager {

    private final AdvancedPets plugin;
    private final Map<UUID, String> dailyMissions = new HashMap<>();
    private final Map<UUID, Integer> missionProgress = new HashMap<>();
    private final Map<UUID, Boolean> missionCompleted = new HashMap<>();

    private final String[] missionTypes = {
        "Mata 10 mobs con tu mascota",
        "Recoge 20 items con tu mascota",
        "Mina 5 ores con tu mascota",
        "Cosecha 10 cultivos con tu mascota",
        "Sube 1 nivel a tu mascota"
    };

    public MissionManager(AdvancedPets plugin) {
        this.plugin = plugin;
        startDailyReset();
    }

    public void assignDailyMission(UUID playerUUID) {
        if (!dailyMissions.containsKey(playerUUID)) {
            String mission = missionTypes[new Random().nextInt(missionTypes.length)];
            dailyMissions.put(playerUUID, mission);
            missionProgress.put(playerUUID, 0);
            missionCompleted.put(playerUUID, false);
        }
    }

    public String getDailyMission(UUID playerUUID) {
        return dailyMissions.getOrDefault(playerUUID, "Sin misión asignada");
    }

    public boolean isMissionCompleted(UUID playerUUID) {
        return missionCompleted.getOrDefault(playerUUID, false);
    }

    public void addProgress(UUID playerUUID, int amount) {
        if (isMissionCompleted(playerUUID)) return;
        int progress = missionProgress.getOrDefault(playerUUID, 0) + amount;
        missionProgress.put(playerUUID, progress);
        if (progress >= 10) completeMission(playerUUID);
    }

    public void completeMission(UUID playerUUID) {
        missionCompleted.put(playerUUID, true);
        Player player = plugin.getServer().getPlayer(playerUUID);
        if (player == null) return;
        double reward = plugin.getConfig().getDouble("missions.daily-reward", 500);
        plugin.getEconomyManager().giveMoney(player, reward);
        player.sendMessage("§r");
        player.sendMessage("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§e§l   🎯 ¡MISIÓN COMPLETADA! 🎯");
        player.sendMessage("§f  Recompensa: §6§l$" + reward);
        player.sendMessage("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§r");
    }

    public int getMissionProgress(UUID playerUUID) {
        return missionProgress.getOrDefault(playerUUID, 0);
    }

    private void startDailyReset() {
        new BukkitRunnable() {
            @Override
            public void run() {
                dailyMissions.clear();
                missionProgress.clear();
                missionCompleted.clear();
            }
        }.runTaskTimer(plugin, 20 * 60 * 60 * 24L, 20 * 60 * 60 * 24L);
    }
}
