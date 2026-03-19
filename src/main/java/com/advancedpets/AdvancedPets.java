package com.advancedpets;

import com.advancedpets.commands.PetCommand;
import com.advancedpets.listeners.CombatListener;
import com.advancedpets.listeners.PetListener;
import com.advancedpets.listeners.PlayerListener;
import com.advancedpets.managers.*;
import com.advancedpets.utils.MessageUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedPets extends JavaPlugin {

    private static AdvancedPets instance;
    private Economy economy;
    private PetManager petManager;
    private EconomyManager economyManager;
    private HologramManager hologramManager;
    private WorkManager workManager;
    private ClanManager clanManager;
    private MissionManager missionManager;
    private AchievementManager achievementManager;
    private PlaceholderManager placeholderManager;
    private ModelManager modelManager;
    private CloneManager cloneManager;
    private BiomeManager biomeManager;
    private LearningManager learningManager;
    private MessageUtils messageUtils;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        messageUtils = new MessageUtils(this);

        printStartupMessage();

        if (!setupEconomy()) {
            getLogger().severe(
                "§cVault no encontrado! Desactivando...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // ✅ Inicializar todos los managers
        petManager = new PetManager(this);
        economyManager = new EconomyManager(this, economy);
        hologramManager = new HologramManager(this);
        workManager = new WorkManager(this);
        clanManager = new ClanManager(this);
        missionManager = new MissionManager(this);
        achievementManager = new AchievementManager(this);
        modelManager = new ModelManager(this);
        cloneManager = new CloneManager(this);
        biomeManager = new BiomeManager(this);
        learningManager = new LearningManager(this);

        // ✅ PlaceholderAPI
        if (Bukkit.getPluginManager()
            .getPlugin("PlaceholderAPI") != null) {
            placeholderManager = new PlaceholderManager(this);
            placeholderManager.register();
            getLogger().info("§aPlaceholderAPI conectado!");
        }

        // ✅ ModelEngine
        if (Bukkit.getPluginManager()
            .getPlugin("ModelEngine") != null) {
            getLogger().info("§aModelEngine conectado!");
        } else {
            getLogger().warning(
                "§eModelEngine no encontrado. Opcional.");
        }

        // ✅ MythicMobs
        if (Bukkit.getPluginManager()
            .getPlugin("MythicMobs") != null) {
            getLogger().info("§aMythicMobs conectado!");
        } else {
            getLogger().warning(
                "§eMythicMobs no encontrado. Opcional.");
        }

        // ✅ Registrar listeners
        getServer().getPluginManager().registerEvents(
            new PetListener(this), this);
        getServer().getPluginManager().registerEvents(
            new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(
            new CombatListener(this), this);

        // ✅ Registrar comandos
        getCommand("ap").setExecutor(new PetCommand(this));
        getCommand("ap").setTabCompleter(new PetCommand(this));

        getLogger().info(
            "§aAdvancedPets activado correctamente!");
    }

    @Override
    public void onDisable() {
        if (petManager != null) petManager.saveAllPets();
        if (hologramManager != null)
            hologramManager.removeAllHolograms();
        if (modelManager != null) modelManager.removeAllModels();
        if (cloneManager != null) cloneManager.removeAllClones();
        getLogger().info("§eAdvancedPets desactivado!");
    }

    private void printStartupMessage() {
        Bukkit.getConsoleSender().sendMessage("§r");
        Bukkit.getConsoleSender().sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        Bukkit.getConsoleSender().sendMessage(
            "§e§l       ⭐ ADVANCED PETS PREMIUM ⭐");
        Bukkit.getConsoleSender().sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        Bukkit.getConsoleSender().sendMessage(
            "§f  Plugin:  §eAdvancedPets §6v1.0.0");
        Bukkit.getConsoleSender().sendMessage(
            "§f  Autor:   §6§lsoyadrianyt001");
        Bukkit.getConsoleSender().sendMessage(
            "§f  Estado:  §a§lACTIVADO ✔");
        Bukkit.getConsoleSender().sendMessage(
            "§f  Vault:   §a§lCONECTADO ✔");
        Bukkit.getConsoleSender().sendMessage(
            "§f  ModelEng: " +
            (Bukkit.getPluginManager()
                .getPlugin("ModelEngine") != null ?
                "§a§lCONECTADO ✔" : "§e§lOPCIONAL"));
        Bukkit.getConsoleSender().sendMessage(
            "§f  MythicM:  " +
            (Bukkit.getPluginManager()
                .getPlugin("MythicMobs") != null ?
                "§a§lCONECTADO ✔" : "§e§lOPCIONAL"));
        Bukkit.getConsoleSender().sendMessage(
            "§f  PAPI:     " +
            (Bukkit.getPluginManager()
                .getPlugin("PlaceholderAPI") != null ?
                "§a§lCONECTADO ✔" : "§e§lOPCIONAL"));
        Bukkit.getConsoleSender().sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        Bukkit.getConsoleSender().sendMessage("§r");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager()
            .getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp =
            getServer().getServicesManager()
                .getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    // ✅ Todos los getters
    public static AdvancedPets getInstance() { return instance; }
    public Economy getEconomy() { return economy; }
    public PetManager getPetManager() { return petManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public WorkManager getWorkManager() { return workManager; }
    public ClanManager getClanManager() { return clanManager; }
    public MissionManager getMissionManager() { return missionManager; }
    public AchievementManager getAchievementManager() { return achievementManager; }
    public ModelManager getModelManager() { return modelManager; }
    public CloneManager getCloneManager() { return cloneManager; }
    public BiomeManager getBiomeManager() { return biomeManager; }
    // ✅ GETTER AGREGADO
    public LearningManager getLearningManager() { return learningManager; }
    public MessageUtils getMessageUtils() { return messageUtils; }
}
