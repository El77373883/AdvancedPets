package com.advancedpets.managers;

import com.advancedpets.AdvancedPets;
import com.advancedpets.models.Pet;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class EconomyManager {

    private final AdvancedPets plugin;
    private final Economy economy;

    public EconomyManager(AdvancedPets plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    public double getPetPrice(EntityType entityType, Pet.Rarity rarity) {
        String key = "prices.specific." + entityType.name();
        if (plugin.getConfig().contains(key)) {
            return plugin.getConfig().getDouble(key);
        }
        switch (rarity) {
            case COMMON: return plugin.getConfig().getDouble("prices.common", 500);
            case RARE: return plugin.getConfig().getDouble("prices.rare", 3000);
            case EPIC: return plugin.getConfig().getDouble("prices.epic", 9500);
            case LEGENDARY: return plugin.getConfig().getDouble("prices.legendary", 25000);
            default: return 500;
        }
    }

    public boolean hasEnoughMoney(Player player, double amount) {
        return economy.getBalance(player) >= amount;
    }

    public boolean chargeMoney(Player player, double amount) {
        if (!hasEnoughMoney(player, amount)) return false;
        economy.withdrawPlayer(player, amount);
        return true;
    }

    public void giveMoney(Player player, double amount) {
        economy.depositPlayer(player, amount);
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public String formatMoney(double amount) {
        return economy.format(amount);
    }
}
