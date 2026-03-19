package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import org.bukkit.entity.Player;

import java.util.*;

public class ClanManager {

    private final AdvancedPets plugin;
    private final Map<String, List<UUID>> clans = new HashMap<>();

    public ClanManager(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    public boolean createClan(String clanName, Player leader) {
        if (clans.containsKey(clanName)) {
            leader.sendMessage("§c§l[AdvancedPets] §cEse clan ya existe!");
            return false;
        }
        Pet pet = plugin.getPetManager().getPet(leader.getUniqueId());
        if (pet == null) {
            leader.sendMessage("§c§l[AdvancedPets] §cNo tienes una mascota!");
            return false;
        }
        List<UUID> members = new ArrayList<>();
        members.add(leader.getUniqueId());
        clans.put(clanName, members);
        pet.setClanName(clanName);
        leader.sendMessage("§6§l[AdvancedPets] §a¡Clan §e" + clanName + " §acreado exitosamente! 🌟");
        return true;
    }

    public boolean joinClan(String clanName, Player player) {
        List<UUID> members = clans.get(clanName);
        if (members == null) {
            player.sendMessage("§c§l[AdvancedPets] §cEse clan no existe!");
            return false;
        }
        int maxSize = plugin.getConfig().getInt("clan.max-size", 3);
        if (members.size() >= maxSize) {
            player.sendMessage("§c§l[AdvancedPets] §cEl clan está lleno! (máx " + maxSize + ")");
            return false;
        }
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) {
            player.sendMessage("§c§l[AdvancedPets] §cNo tienes una mascota!");
            return false;
        }
        members.add(player.getUniqueId());
        pet.setClanName(clanName);
        player.sendMessage("§6§l[AdvancedPets] §a¡Te uniste al clan §e" + clanName + "§a! 🐾");
        return true;
    }

    public void leaveClan(Player player) {
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null || pet.getClanName() == null) {
            player.sendMessage("§c§l[AdvancedPets] §cNo perteneces a ningún clan!");
            return;
        }
        String clanName = pet.getClanName();
        List<UUID> members = clans.get(clanName);
        if (members != null) members.remove(player.getUniqueId());
        pet.setClanName(null);
        player.sendMessage("§6§l[AdvancedPets] §eSaliste del clan §c" + clanName);
    }

    public List<UUID> getClanMembers(String clanName) {
        return clans.getOrDefault(clanName, new ArrayList<>());
    }

    public Map<String, List<UUID>> getAllClans() { return clans; }
}
