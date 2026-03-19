package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderManager extends PlaceholderExpansion {

    private final AdvancedPets plugin;

    public PlaceholderManager(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "advancedpets"; }
    @Override public @NotNull String getAuthor() { return "soyadrianyt001"; }
    @Override public @NotNull String getVersion() { return "1.0.0"; }
    @Override public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) return "Sin mascota";
        switch (params.toLowerCase()) {
            case "nombre": return pet.getName();
            case "nivel": return String.valueOf(pet.getLevel());
            case "xp": return String.valueOf((int) pet.getXp());
            case "xp_max": return String.valueOf((int) pet.getXpNeeded());
            case "rareza": return pet.getRarityName();
            case "tipo": return pet.getEntityType().name();
            case "vida": return String.valueOf((int) pet.getHealth());
            case "vida_max": return String.valueOf((int) pet.getMaxHealth());
            case "daño": return String.valueOf(pet.getDamage());
            case "trabajo": return pet.getCurrentWork().name();
            case "particula": return pet.getParticleType().name();
            case "kills": return String.valueOf(pet.getKills());
            case "humor": return pet.getMood().name();
            case "hambre": return String.valueOf((int) pet.getHunger());
            case "estado": return pet.isSummoned() ? "Invocada" : "Guardada";
            case "logros": return String.valueOf(plugin.getAchievementManager().getAchievementCount(player.getUniqueId()));
            case "ranking": return getRanking(player);
            default: return "";
        }
    }

    private String getRanking(Player player) {
        int pos = 1;
        int myKills = 0;
        Pet myPet = plugin.getPetManager().getPet(player.getUniqueId());
        if (myPet != null) myKills = myPet.getKills();
        for (Pet pet : plugin.getPetManager().getAllPets().values()) {
            if (!pet.getOwnerUUID().equals(player.getUniqueId()) && pet.getKills() > myKills) pos++;
        }
        return String.valueOf(pos);
    }
}
