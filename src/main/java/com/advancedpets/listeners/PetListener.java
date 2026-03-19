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
            if (pet.getEntity() == null || !pet.getEntity().equals(clicked)) continue;

            // Dueño hace clic → abre GUI de alimentar
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

            // Otro jugador agachado → abre menú de interacción
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
        plugin.getMissionManager().assignDailyMission(player.getUniqueId());

        // Avisar mascotas de jugadores en línea
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Pet pet : plugin.getPetManager().getAllPets().values()) {
                if (!pet.isSummoned()) continue;
                Player owner = Bukkit.getPlayer(pet.getOwnerUUID());
                if (owner != null && !owner.equals(player)) {
                    owner.sendMessage("§e" + pet.getName() + " §fdice: §b¡Amo! §f" + player.getName() + " §bentró al servidor! 👀");
                }
            }
        }, 40L);

        // Verificar cumpleaños
        checkBirthday(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet != null) {
            plugin.getPetManager().despawnPet(pet);
            plugin.getPetManager().savePet(pet);
        }
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null || !pet.isSummoned()) return;
        pet.setSleeping(true);
        player.sendMessage("§8[AP] §7" + pet.getName() + " §fdice: §8¡Buenas noches amo! 💤 Zzzzz...");
        plugin.getHologramManager().updateHologram(pet);
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null) return;
        pet.setSleeping(false);
        plugin.getHologramManager().updateHologram(pet);
        player.sendMessage("§e[AP] §f" + pet.getName() + " §fdice: §a¡Buenos días amo! ☀ ¡Listo para trabajar!");
    }

    private void checkBirthday(Player player) {
        if (!plugin.getConfig().getBoolean("birthday.enabled", true)) return;
        Pet pet = plugin.getPetManager().getPet(player.getUniqueId());
        if (pet == null || pet.getBirthdayDate() == null) return;
        String today = new java.text.SimpleDateFormat("MM-dd").format(new java.util.Date());
        if (pet.getBirthdayDate().equals(today)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage("§r");
                player.sendMessage("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
                player.sendMessage("§e§l   🎂 ¡FELIZ CUMPLEAÑOS AMO! 🎂");
                player.sendMessage("§f  " + pet.getName() + " te desea lo mejor!");
                player.sendMessage("§6§l✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦✦");
                player.sendMessage("§r");
                player.getWorld().spawnParticle(Particle.FIREWORK,
                    player.getLocation().add(0, 1, 0), 100, 1, 1, 1, 0.3);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }, 60L);
        }
    }
}
