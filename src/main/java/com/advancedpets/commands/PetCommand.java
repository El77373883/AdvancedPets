package com.advancedpets.commands;

import com.advancedpets.AdvancedPets;
import com.advancedpets.gui.MainGUI;
import com.advancedpets.gui.ShopGUI;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.*;

public class PetCommand implements CommandExecutor, TabCompleter {

    private final AdvancedPets plugin;

    public PetCommand(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command,
        String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSolo jugadores pueden usar este comando!");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            openMainOrShop(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "summon":
                cmdSummon(player); break;
            case "dismiss":
                cmdDismiss(player); break;
            case "stats":
                cmdStats(player); break;
            case "rename":
                cmdRename(player, args); break;
            case "shop":
                new ShopGUI(plugin, player).open(); break;
            case "mine":
                cmdWork(player, Pet.WorkType.MINING); break;
            case "farm":
                cmdWork(player, Pet.WorkType.FARMING); break;
            case "harvest":
                cmdWork(player, Pet.WorkType.HARVEST); break;
            case "collect":
                cmdWork(player, Pet.WorkType.COLLECT); break;
            case "chop":
                cmdWork(player, Pet.WorkType.CHOP); break;
            case "stop":
                cmdStopWork(player); break;
            case "evolve":
                cmdEvolve(player); break;
            case "top":
                cmdTop(player); break;
            case "reload":
                cmdReload(player); break;
            case "setbirthday":
                cmdSetBirthday(player, args); break;
            case "revive":
                cmdRevive(player); break;
            case "heal":
                cmdHeal(player); break;
            case "info":
                cmdInfo(player); break;
            case "creator":
                cmdCreator(player); break;
            case "admin":
                cmdAdmin(player, args); break;
            case "help":
                sendHelp(player); break;
            default:
                sendHelp(player); break;
        }
        return true;
    }

    private void openMainOrShop(Player player) {
        if (plugin.getPetManager().hasPet(player.getUniqueId())) {
            Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
            new MainGUI(plugin, pet, player).open();
        } else {
            new ShopGUI(plugin, player).open();
        }
    }

    private void cmdSummon(Player player) {
        if (!plugin.getPetManager().hasPet(player.getUniqueId())) {
            plugin.getMessageUtils().send(player,
                "&cNo tienes ninguna mascota! Usa &e/ap shop");
            return;
        }
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet.isSummoned()) {
            plugin.getMessageUtils().send(player,
                "&cTu mascota ya está invocada!");
            return;
        }
        plugin.getPetManager().spawnPet(pet, player.getLocation());
    }

    private void cmdDismiss(Player player) {
        if (!plugin.getPetManager().hasPet(player.getUniqueId())) {
            plugin.getMessageUtils().send(player,
                "&cNo tienes ninguna mascota!");
            return;
        }
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (!pet.isSummoned()) {
            plugin.getMessageUtils().send(player,
                "&cTu mascota no está invocada!");
            return;
        }
        plugin.getPetManager().despawnPet(pet);
        plugin.getMessageUtils().send(player,
            "&eMascota guardada correctamente! 📦");
    }

    private void cmdStats(Player player) {
        if (!plugin.getPetManager().hasPet(player.getUniqueId())) {
            plugin.getMessageUtils().send(player,
                "&cNo tienes ninguna mascota!");
            return;
        }
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        player.sendMessage("§r");
        player.sendMessage("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§e§l   📊 STATS DE " +
            pet.getName().toUpperCase());
        player.sendMessage("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§f  Nombre:  §e" + pet.getName());
        player.sendMessage("§f  Tipo:    §e" +
            pet.getEntityType().name());
        player.sendMessage("§f  Rareza:  " +
            pet.getRarityColor() + "§l" + pet.getRarityName());
        player.sendMessage("§f  Nivel:   §a§l" + pet.getLevel());
        player.sendMessage("§f  XP:      §b" + (int)pet.getXp() +
            "§7/§b" + (int)pet.getXpNeeded());
        player.sendMessage("§f  Vida:    §c" + (int)pet.getHealth() +
            "§7/§c" + (int)pet.getMaxHealth());
        player.sendMessage("§f  Daño:    §c" + pet.getDamage());
        player.sendMessage("§f  Kills:   §c" + pet.getKills());
        player.sendMessage("§f  Hambre:  §6" +
            (int)pet.getHunger() + "%");
        player.sendMessage("§f  Humor:   " +
            pet.getMoodColor() + pet.getMood().name());
        player.sendMessage("§f  Trabajo: §a" +
            pet.getCurrentWork().name());
        player.sendMessage("§f  Logros:  §6" +
            plugin.getAchievementManager()
                .getAchievementCount(player.getUniqueId()));
        if (pet.getBirthdayDate() != null) {
            player.sendMessage("§f  Cumple:  §d" +
                pet.getBirthdayDate());
        }
        if (pet.getClanName() != null) {
            player.sendMessage("§f  Clan:    §5" +
                pet.getClanName());
        }
        // ✅ Stats de aprendizaje
        Map<String, Integer> learning = plugin.getLearningManager()
            .getOwnerStats(player.getUniqueId());
        player.sendMessage("§f  Minados: §7" +
            learning.getOrDefault("MINE", 0));
        player.sendMessage("§f  Peleas:  §7" +
            learning.getOrDefault("FIGHT", 0));
        player.sendMessage("§f  Cosecha: §7" +
            learning.getOrDefault("HARVEST", 0));
        player.sendMessage("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§r");
    }

    private void cmdRename(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtils().send(player,
                "&cUso: &e/ap rename <nombre>");
            return;
        }
        if (!plugin.getPetManager().hasPet(player.getUniqueId())) {
            plugin.getMessageUtils().send(player,
                "&cNo tienes ninguna mascota!");
            return;
        }
        String name = String.join(" ",
            Arrays.copyOfRange(args, 1, args.length));
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        pet.setName(name);
        plugin.getPetManager().savePet(pet);
        plugin.getHologramManager().updateHologram(pet);
        plugin.getMessageUtils().send(player,
            "&aMascota renombrada a: &e" + name);
    }

    private void cmdWork(Player player, Pet.WorkType workType) {
        if (!plugin.getPetManager().hasPet(player.getUniqueId())) {
            plugin.getMessageUtils().send(player,
                "&cNo tienes ninguna mascota!");
            return;
        }
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (!pet.isSummoned()) {
            plugin.getMessageUtils().send(player,
                "&cPrimero invoca tu mascota con &e/ap summon");
            return;
        }
        plugin.getWorkManager().startWork(pet, workType, player);
    }

    private void cmdStopWork(Player player) {
        if (!plugin.getPetManager().hasPet(player.getUniqueId())) {
            plugin.getMessageUtils().send(player,
                "&cNo tienes ninguna mascota!");
            return;
        }
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        plugin.getWorkManager().stopWork(pet);
        plugin.getMessageUtils().send(player,
            "&eTrabajo detenido!");
    }

    private void cmdEvolve(Player player) {
        if (!plugin.getPetManager().hasPet(player.getUniqueId())) {
            plugin.getMessageUtils().send(player,
                "&cNo tienes ninguna mascota!");
            return;
        }
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet.getRarity() == Pet.Rarity.LEGENDARY) {
            plugin.getMessageUtils().send(player,
                "&cTu mascota ya es &6§lLEGENDARIA&c!");
            return;
        }
        if (pet.getLevel() < 10) {
            plugin.getMessageUtils().send(player,
                "&cNecesitas nivel &e10 &cpara evolucionar. " +
                "Nivel actual: &e" + pet.getLevel());
            return;
        }
        Pet.Rarity next =
            Pet.Rarity.values()[pet.getRarity().ordinal() + 1];
        pet.setRarity(next);
        pet.setLevel(1);
        pet.setXp(0);
        pet.setMaxHealth(pet.getMaxHealth() + 20);
        plugin.getPetManager().savePet(pet);
        plugin.getHologramManager().updateHologram(pet);
        player.sendMessage("§r");
        player.sendMessage(
            "§6§l⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡");
        player.sendMessage("§e§l   🌟 ¡EVOLUCIÓN COMPLETADA! 🌟");
        player.sendMessage("§f  Tu mascota ahora es: " +
            pet.getRarityColor() + "§l" + pet.getRarityName());
        player.sendMessage(
            "§6§l⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡⚡");
        player.sendMessage("§r");
        player.playSound(player.getLocation(),
            Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 0.8f);
        player.getWorld().spawnParticle(
            Particle.TOTEM_OF_UNDYING,
            player.getLocation().add(0,1,0),
            200, 1, 1, 1, 0.5);
    }

    private void cmdTop(Player player) {
        List<Pet> sorted = new ArrayList<>(
            plugin.getPetManager().getAllPets().values());
        sorted.sort((a, b) -> b.getKills() - a.getKills());
        player.sendMessage("§r");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage(
            "§e§l   🏆 TOP MASCOTAS DEL SERVIDOR");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        int pos = 1;
        for (Pet pet : sorted) {
            if (pos > 10) break;
            String medal = pos == 1 ? "§6🥇" :
                pos == 2 ? "§7🥈" :
                pos == 3 ? "§c🥉" : "§f#" + pos;
            player.sendMessage("§f " + medal + " §e" +
                pet.getName() + " §7(" +
                pet.getOwnerName() + ") §f- §cKills: " +
                pet.getKills() + " §f| §aNv." + pet.getLevel());
            pos++;
        }
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§r");
    }

    private void cmdReload(Player player) {
        if (!player.hasPermission("advancedpets.admin.reload")) {
            plugin.getMessageUtils().sendNoPermission(player);
            return;
        }
        plugin.reloadConfig();
        plugin.getMessageUtils().send(player,
            "&aPlugin recargado correctamente! ✔");
    }

    private void cmdSetBirthday(Player player, String[] args) {
        if (args.length < 2) {
            plugin.getMessageUtils().send(player,
                "&cUso: &e/ap setbirthday <MM-DD>");
            plugin.getMessageUtils().send(player,
                "&7Ejemplo: &e/ap setbirthday 03-18");
            return;
        }
        if (!plugin.getPetManager().hasPet(player.getUniqueId())) {
            plugin.getMessageUtils().send(player,
                "&cNo tienes ninguna mascota!");
            return;
        }
        String date = args[1];
        if (!date.matches("\\d{2}-\\d{2}")) {
            plugin.getMessageUtils().send(player,
                "&cFormato incorrecto! Usa: &eMM-DD");
            plugin.getMessageUtils().send(player,
                "&7Ejemplo: &e/ap setbirthday 03-18");
            return;
        }
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        pet.setBirthdayDate(date);
        plugin.getPetManager().savePet(pet);
        player.sendMessage("§r");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage(
            "§e§l   🎂 ¡CUMPLEAÑOS GUARDADO! 🎂");
        player.sendMessage("§f  Mascota: §e" + pet.getName());
        player.sendMessage("§f  Fecha:   §e" + date);
        player.sendMessage("§7  Cada año en esa fecha tu");
        player.sendMessage("§7  mascota te celebrará! 🎉");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§r");
        player.playSound(player.getLocation(),
            Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
    }

    private void cmdRevive(Player player) {
        if (!plugin.getPetManager().hasPet(player.getUniqueId())) {
            plugin.getMessageUtils().send(player,
                "&cNo tienes ninguna mascota!");
            return;
        }
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet.isSummoned()) {
            plugin.getMessageUtils().send(player,
                "&cTu mascota ya está viva!");
            return;
        }
        double cost = 500;
        if (!plugin.getEconomyManager().chargeMoney(player, cost)) {
            plugin.getMessageUtils().sendNoMoney(player);
            return;
        }
        pet.setHealth(pet.getMaxHealth());
        plugin.getPetManager().spawnPet(pet, player.getLocation());
        player.sendMessage("§a§l[AdvancedPets] §e" +
            pet.getName() +
            " §f¡ha sido revivido! ❤️ (-$" + (int)cost + ")");
        player.playSound(player.getLocation(),
            Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    private void cmdHeal(Player player) {
        if (!plugin.getPetManager().hasPet(player.getUniqueId())) {
            plugin.getMessageUtils().send(player,
                "&cNo tienes ninguna mascota!");
            return;
        }
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet.getHealth() >= pet.getMaxHealth()) {
            plugin.getMessageUtils().send(player,
                "&cTu mascota ya tiene vida completa!");
            return;
        }
        double cost = 200;
        if (!plugin.getEconomyManager().chargeMoney(player, cost)) {
            plugin.getMessageUtils().sendNoMoney(player);
            return;
        }
        pet.setHealth(pet.getMaxHealth());
        if (pet.getEntity() instanceof LivingEntity) {
            ((LivingEntity) pet.getEntity())
                .setHealth(pet.getMaxHealth());
        }
        player.sendMessage("§a§l[AdvancedPets] §e" +
            pet.getName() +
            " §f¡curado! ❤️ (-$" + (int)cost + ")");
        if (pet.getEntity() != null) {
            player.getWorld().spawnParticle(Particle.HEART,
                pet.getEntity().getLocation().add(0,1,0),
                20, 0.5, 0.5, 0.5, 0);
        }
        player.playSound(player.getLocation(),
            Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
    }

    private void cmdInfo(Player player) {
        player.sendMessage("§r");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage(
            "§e§l   🐾 ADVANCED PETS INFO");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§f  Version: §ev1.0.0 PREMIUM");
        player.sendMessage("§f  Autor:   §6§lsoyadrianyt001");
        player.sendMessage("§f  Mobs:    §e40+ disponibles");
        player.sendMessage(
            "§f  Rareza:  §fComún/Rara/Épica/Legendaria");
        player.sendMessage("§f  Efectos: §e12 Ultra Premium");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§7  Comandos: §e/ap help");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§r");
    }

    private void cmdCreator(Player player) {
        plugin.getMessageUtils().sendCreatorMessage(player);
        player.playSound(player.getLocation(),
            Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.getWorld().spawnParticle(
            Particle.FIREWORK,
            player.getLocation().add(0,1,0),
            100, 1, 1, 1, 0.3);
    }

    private void cmdAdmin(Player player, String[] args) {
        if (!player.hasPermission("advancedpets.admin")) {
            plugin.getMessageUtils().sendNoPermission(player);
            return;
        }
        if (args.length < 2) {
            sendAdminHelp(player);
            return;
        }
        switch (args[1].toLowerCase()) {
            case "give":
                if (args.length < 4) {
                    plugin.getMessageUtils().send(player,
                        "&cUso: &e/ap admin give <jugador> " +
                        "<tipo> [rareza]");
                    return;
                }
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    plugin.getMessageUtils().send(player,
                        "&cJugador no encontrado!");
                    return;
                }
                try {
                    EntityType type =
                        EntityType.valueOf(args[3].toUpperCase());
                    Pet.Rarity rarity = args.length >= 5 ?
                        Pet.Rarity.valueOf(args[4].toUpperCase()) :
                        Pet.Rarity.COMMON;
                    Pet newPet = new Pet(
                        target.getUniqueId(),
                        target.getName(),
                        target.getName() + "'s Pet",
                        type, rarity);
                    plugin.getPetManager().addPet(newPet);
                    plugin.getMessageUtils().send(player,
                        "&aMascota &e" + type.name() +
                        " &adada a &e" + target.getName());
                    plugin.getMessageUtils().send(target,
                        "&a¡El admin te dio una mascota &e" +
                        type.name() + "&a!");
                } catch (Exception e) {
                    plugin.getMessageUtils().send(player,
                        "&cTipo o rareza inválida!");
                }
                break;
            case "particles":
                if (args.length >= 3 &&
                    args[2].equalsIgnoreCase("off")) {
                    plugin.getConfig().set(
                        "particles.global", false);
                    plugin.saveConfig();
                    plugin.getMessageUtils().send(player,
                        "&cPartículas globales desactivadas!");
                } else {
                    plugin.getConfig().set(
                        "particles.global", true);
                    plugin.saveConfig();
                    plugin.getMessageUtils().send(player,
                        "&aPartículas globales activadas!");
                }
                break;
            case "reload":
                cmdReload(player);
                break;
            default:
                sendAdminHelp(player);
                break;
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§r");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage(
            "§e§l   🐾 ADVANCED PETS — AYUDA");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§e/ap §f— Abre el menú principal");
        player.sendMessage("§e/ap summon §f— Invocar mascota");
        player.sendMessage("§e/ap dismiss §f— Guardar mascota");
        player.sendMessage("§e/ap stats §f— Ver estadísticas");
        player.sendMessage(
            "§e/ap rename <nombre> §f— Renombrar");
        player.sendMessage("§e/ap shop §f— Tienda");
        player.sendMessage("§e/ap mine §f— Ordenar minar");
        player.sendMessage("§e/ap farm §f— Ordenar farmear");
        player.sendMessage(
            "§e/ap harvest §f— Ordenar cosechar");
        player.sendMessage(
            "§e/ap collect §f— Recoger items");
        player.sendMessage("§e/ap chop §f— Talar árboles");
        player.sendMessage("§e/ap stop §f— Detener trabajo");
        player.sendMessage(
            "§e/ap evolve §f— Evolucionar mascota");
        player.sendMessage("§e/ap top §f— Ranking servidor");
        player.sendMessage(
            "§e/ap setbirthday <MM-DD> §f— Cumpleaños");
        player.sendMessage("§e/ap revive §f— Revivir mascota");
        player.sendMessage("§e/ap heal §f— Curar mascota");
        player.sendMessage("§e/ap info §f— Info del plugin");
        player.sendMessage(
            "§e/ap creator §f— Ver el creador");
        player.sendMessage(
            "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
        player.sendMessage("§r");
    }

    private void sendAdminHelp(Player player) {
        player.sendMessage(
            "§c§l[Admin] §e/ap admin give " +
            "<jugador> <tipo> [rareza]");
        player.sendMessage(
            "§c§l[Admin] §e/ap admin particles <on/off>");
        player.sendMessage(
            "§c§l[Admin] §e/ap reload");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender,
        Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // ✅ Todos los comandos disponibles
            completions.addAll(Arrays.asList(
                "summon", "dismiss", "stats", "rename",
                "shop", "mine", "farm", "harvest",
                "collect", "chop", "stop", "evolve",
                "top", "reload", "setbirthday", "revive",
                "heal", "info", "creator", "admin", "help"
            ));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "admin":
                    completions.addAll(Arrays.asList(
                        "give", "particles", "reload"));
                    break;
                case "setbirthday":
                    completions.add("MM-DD");
                    completions.add("01-01");
                    completions.add("12-25");
                    break;
                case "rename":
                    completions.add("<nuevo nombre>");
                    break;
            }
        } else if (args.length == 3 &&
            args[0].equalsIgnoreCase("admin") &&
            args[1].equalsIgnoreCase("give")) {
            // ✅ Autocompletar jugadores online
            for (Player p : Bukkit.getOnlinePlayers()) {
                completions.add(p.getName());
            }
        } else if (args.length == 4 &&
            args[0].equalsIgnoreCase("admin") &&
            args[1].equalsIgnoreCase("give")) {
            // ✅ Autocompletar tipos de mob
            completions.addAll(Arrays.asList(
                "WOLF", "CAT", "HORSE", "ZOMBIE", "SKELETON",
                "CREEPER", "BLAZE", "ENDERMAN", "ENDER_DRAGON",
                "WITHER", "WARDEN", "IRON_GOLEM", "VILLAGER"
            ));
        } else if (args.length == 5 &&
            args[0].equalsIgnoreCase("admin") &&
            args[1].equalsIgnoreCase("give")) {
            // ✅ Autocompletar rareza
            completions.addAll(Arrays.asList(
                "COMMON", "RARE", "EPIC", "LEGENDARY"
            ));
        } else if (args.length == 3 &&
            args[0].equalsIgnoreCase("admin") &&
            args[1].equalsIgnoreCase("particles")) {
            completions.addAll(Arrays.asList("on", "off"));
        }

        // ✅ Filtrar por lo que ya escribió el jugador
        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(s ->
            !s.toLowerCase().startsWith(input));

        return completions;
    }
}
