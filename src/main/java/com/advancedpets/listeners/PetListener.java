package com.advancedpets.listeners;

import com.advancedpets.AdvancedPets;
import com.advancedpets.gui.FeedGUI;
import com.advancedpets.gui.InteractionGUI;
import com.advancedpets.models.Pet;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PetListener implements Listener {

    private final AdvancedPets plugin;

    public PetListener(AdvancedPets plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        Entity clicked = event.getRightClicked();

        for (Pet pet : plugin.getPetManager().getAllPets().values()) {
            if (pet.getEntity() == null ||
                !pet.getEntity().equals(clicked)) continue;

            // ✅ Dueño hace clic → abrir GUI de alimentar o alimentar directo
            if (pet.getOwnerUUID().equals(player.getUniqueId())) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() == Material.APPLE) {
                    plugin.getPetManager().feedPet(pet, player);
                    event.setCancelled(true);
                    return;
                }
                new FeedGUI(plugin, pet, player).open();
                event.setCancelled(true);
                return;
            }

            // ✅ Otro jugador agachado → abre menú de interacción
            if (player.isSneaking()) {
                new InteractionGUI(plugin, pet, player).open();
                event.setCancelled(true);
                return;
            }
            return;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // ✅ Asignar misión diaria
        plugin.getMissionManager().assignDailyMission(player.getUniqueId());

        // ✅ Re-spawnear mascota al entrar al servidor
        // con delay de 3 segundos para que el mundo cargue bien
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            plugin.getPetManager().respawnPetOnJoin(player);
            player.sendMessage("§6§l[AdvancedPets] §f¡Bienvenido! 🐾 " +
                "Tu mascota está siendo invocada...");
        }, 60L);

        // ✅ Avisar a mascotas de jugadores en línea
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Pet pet : plugin.getPetManager().getAllPets().values()) {
                if (!pet.isSummoned()) continue;
                Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
                if (owner != null && !owner.equals(player)) {
                    owner.sendMessage("§e" + pet.getName() +
                        " §fdice: §b¡Amo! §f" + player.getName() +
                        " §bentró al servidor! 👀");
                }
            }
        }, 80L);

        // ✅ Verificar cumpleaños
        checkBirthday(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) return;

        // ✅ Guardar estado antes de salir
        // NO despawnear — solo guardar para re-spawnear al volver
        plugin.getPetManager().savePet(pet);

        // ✅ Remover entidad del mundo pero mantener estado summoned
        // para que se re-spawnee cuando el jugador vuelva
        if (pet.isSummoned() && pet.getEntity() != null
            && !pet.getEntity().isDead()) {
            plugin.getHologramManager().removeHologram(pet);
            pet.getEntity().remove();
            pet.setEntity(null);
            // ✅ Mantener summoned = true para re-spawnear al volver
        }

        plugin.getPetManager().savePet(pet);
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() !=
            PlayerBedEnterEvent.BedEnterResult.OK) return;
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null || !pet.isSummoned()) return;
        pet.setSleeping(true);
        player.sendMessage("§8[AP] §7" + pet.getName() +
            " §fdice: §8¡Buenas noches amo! 💤 Zzzzz...");
        plugin.getHologramManager().updateHologram(pet);
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) return;
        pet.setSleeping(false);
        plugin.getHologramManager().updateHologram(pet);
        player.sendMessage("§e[AP] §f" + pet.getName() +
            " §fdice: §a¡Buenos días amo! ☀ ¡Listo para trabajar!");
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) return;

        // ✅ Re-spawnear mascota cuando el jugador reaparece
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pet.isSummoned()) {
                if (pet.getEntity() != null && !pet.getEntity().isDead()) {
                    plugin.getPetManager().despawnPet(pet);
                }
                pet.setSummoned(false);
                pet.setEntity(null);
                plugin.getPetManager().spawnPet(pet, player.getLocation());
            }
        }, 40L);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) return;

        String world = player.getWorld().getName();

        // ✅ Verificar si el mundo está en blacklist
        if (plugin.getConfig().getStringList("worlds.blacklist")
            .contains(world)) {
            if (pet.isSummoned()) {
                plugin.getPetManager().despawnPet(pet);
                pet.setSummoned(false);
            }
            player.sendMessage(
                "§c§l[AdvancedPets] §cTus mascotas no pueden " +
                "entrar a este mundo!");
        } else {
            // ✅ Re-spawnear en el nuevo mundo
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (pet.isSummoned() || pet.getEntity() == null) {
                    if (pet.getEntity() != null) {
                        plugin.getHologramManager().removeHologram(pet);
                        pet.getEntity().remove();
                        pet.setEntity(null);
                        pet.setSummoned(false);
                    }
                    plugin.getPetManager().spawnPet(pet,
                        player.getLocation());
                }
            }, 40L);
        }
    }

    private void checkBirthday(Player player) {
        if (!plugin.getConfig().getBoolean("birthday.enabled", true)) return;
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null || pet.getBirthdayDate() == null) return;
        String today = new java.text.SimpleDateFormat("MM-dd")
            .format(new java.util.Date());
        if (pet.getBirthdayDate().equals(today)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§r");
                player.sendMessage(
                    "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
                player.sendMessage(
                    "§e§l   🎂 ¡FELIZ CUMPLEAÑOS AMO! 🎂");
                player.sendMessage("§f  " + pet.getName() +
                    " te desea lo mejor!");
                player.sendMessage(
                    "§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
                player.sendMessage("§r");
                player.getWorld().spawnParticle(
                    Particle.FIREWORK,
                    player.getLocation().add(0, 1, 0),
                    100, 1, 1, 1, 0.3);
                player.playSound(player.getLocation(),
                    Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }, 60L);
        }
    }
}

