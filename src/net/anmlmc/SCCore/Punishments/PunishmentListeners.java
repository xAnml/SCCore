package net.anmlmc.SCCore.Punishments;

import net.anmlmc.SCCore.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

/**
 * Created by Anml on 1/8/16.
 */
public class PunishmentListeners implements Listener {

    Main instance;
    PunishmentManager punishmentManager;

    public PunishmentListeners(Main instance) {
        this.instance = instance;
        punishmentManager = instance.getPunishmentManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerLogin(AsyncPlayerPreLoginEvent e) {
        List<Punishment> punishments = punishmentManager.getPunishments(e.getUniqueId());

        for (Punishment punishment : punishments) {
            if (punishment.getType().equals(PunishmentType.BAN) || punishment.getType().equals(PunishmentType.TEMPBAN)) {
                if (!punishment.hasExpired()) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, punishment.getMessage());
                    return;
                }
            }
        }

        punishmentManager.getCachedPunishments().put(e.getUniqueId(), punishments);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerQuit(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();

        if (punishmentManager.getCachedPunishments().containsKey(id)) {
            for (Punishment p : punishmentManager.getCachedPunishments().get(id)) {
                p.execute();
            }
            punishmentManager.getCachedPunishments().remove(id);
        }
    }
}