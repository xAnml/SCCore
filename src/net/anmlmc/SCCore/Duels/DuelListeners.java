package net.anmlmc.SCCore.Duels;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.SCPlayer.SCPlayer;
import net.anmlmc.SCCore.SCPlayer.SCPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Created by Anml on 1/3/16.
 */
public class DuelListeners implements Listener {

    private Main instance;
    private SCPlayerManager scPlayerManager;
    private ArenaManager arenaManager;
    private Arena arena;

    public DuelListeners(Main instance) {
        this.instance = instance;
        scPlayerManager = instance.getSCPlayerManager();
        arenaManager = instance.getArenaManager();
        arena = arenaManager.getArena();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(final PlayerTeleportEvent e) {

        if (e.getPlayer().hasPermission("sccore.arena"))
            return;

        if (arenaManager.insideBorders(e.getTo()) && !arena.getArenaPlayers().contains(e.getPlayer())) {
            e.getPlayer().sendMessage("§cYou are not permitted to teleport into the duel arena.");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent e) {
        if (arena.isRunning() && arena.getArenaPlayers().contains(e.getPlayer())) {
            arena.forceEnd();
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            SCPlayer scPlayer = scPlayerManager.getSCPlayer(player.getUniqueId());
            if (scPlayer.getDuelRequests().containsKey(e.getPlayer())) {
                scPlayer.removeDuelRequest(e.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamageByPlayer(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player == false || e.getDamager() instanceof Player == false) return;

        if (arena.getArenaPlayers().contains(e.getEntity()) && !arena.getArenaPlayers().contains(e.getDamager()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final PlayerDeathEvent e) {
        if (arena.getArenaPlayers().contains(e.getEntity())) {
            arena.endMatch(e.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommandPre(final PlayerCommandPreprocessEvent e) {

        if (arena.getArenaPlayers().contains(e.getPlayer())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYou are not permitted to execute commands while dueling.");
        }
    }

}
