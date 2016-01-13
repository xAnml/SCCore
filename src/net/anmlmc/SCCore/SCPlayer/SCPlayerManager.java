package net.anmlmc.SCCore.SCPlayer;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayerColl;
import com.massivecraft.massivecore.ps.PS;
import net.anmlmc.SCCore.Lockpicks.LockpickRunnable;
import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.Ranks.RankManager;
import net.anmlmc.SCCore.Stats.StatsManager;
import net.anmlmc.SCCore.Utils.Fanciful.FancyMessage;
import net.anmlmc.SCCore.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

/**
 * Created by Kishan on 12/2/15.
 */
public class SCPlayerManager implements Listener {

    private Main instance;
    private RankManager rankManager;
    private StatsManager statsManager;
    private Utils utils;
    private List<UUID> shoutCooldowns;
    private Map<UUID, LockpickRunnable> lockpicking;
    private Map<UUID, SCPlayer> scPlayers;

    public SCPlayerManager(Main instance) {
        this.instance = instance;
        rankManager = instance.getRankManager();
        statsManager = instance.getStatsManager();
        utils = instance.getUtils();
        shoutCooldowns = new ArrayList<>();
        scPlayers = new HashMap<>();
        lockpicking = new HashMap<>();
    }

    public List<UUID> getShoutCooldowns() {
        return shoutCooldowns;
    }

    public Map<UUID, LockpickRunnable> getLockpicking() {
        return lockpicking;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        Player player = e.getPlayer();
        addSCPlayer(player.getUniqueId());
        SCPlayer scPlayer = getSCPlayer(player.getUniqueId());
        staff(new FancyMessage("§9[STAFF] " + scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §econnected."));

        e.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(final PlayerQuitEvent e) {
        Player player = e.getPlayer();
        SCPlayer scPlayer = getSCPlayer(player.getUniqueId());

        e.setQuitMessage(null);

        if (scPlayer.isCombatTagged()) {
            player.setHealth(0);
            scPlayer.removeCombatTag();
            broadcast(new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §5has logged off while in combat!"));
        }

        staff(new FancyMessage("§9[STAFF] " + scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §edisconnected."));

        removeSCPlayer(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDamageByPlayer(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player == false || e.getDamager() instanceof Player == false) return;

        Player player = (Player) e.getEntity();
        Player target = (Player) e.getDamager();

        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(target.getLocation().getChunk()));


        if (faction.getName().equalsIgnoreCase("Safezone")) {
            return;
        }

        final Faction pFaction = MPlayerColl.get().get(player).getFaction();
        final Faction tFaction = MPlayerColl.get().get(target).getFaction();

        if (pFaction.getRelationTo(tFaction) == Rel.MEMBER && !pFaction.isNone()) {
            return;
        }
        if (pFaction.getRelationTo(tFaction) == Rel.ALLY) {
            return;
        }

        getSCPlayer(player.getUniqueId()).combatTag();
        getSCPlayer(target.getUniqueId()).combatTag();

    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(final AsyncPlayerChatEvent e) {
        e.setCancelled(true);

        Player player = e.getPlayer();
        SCPlayer scPlayer = getSCPlayer(player.getUniqueId());

        if (player.isOp()) e.setMessage(e.getMessage().replace('&', ChatColor.COLOR_CHAR));

        FancyMessage message = new FancyMessage(" §7- ").then(scPlayer.getTag()).tooltip(scPlayer.getHoverText())
                .then("§8: §7" + e.getMessage());

        for (final Player other : e.getRecipients()) {
            if (other.getWorld() != player.getWorld())
                continue;
            if (other.getLocation().distanceSquared(player.getLocation()) <= 900)
                message.send(other);
        }
    }

    public SCPlayer getSCPlayer(UUID uuid) {
        if (scPlayers.containsKey(uuid))
            return scPlayers.get(uuid);

        return new SCPlayer(instance, uuid);
    }

    public void addSCPlayer(UUID uuid) {
        if (scPlayers.containsKey(uuid))
            return;

        scPlayers.put(uuid, new SCPlayer(instance, uuid));
        rankManager.setRank(uuid, rankManager.getRank(uuid));
        statsManager.loadStats(uuid);

    }

    public void removeSCPlayer(UUID uuid) {

        if (scPlayers.containsKey(uuid))
            scPlayers.remove(uuid);

        rankManager.setSQLRank(uuid, rankManager.getRank(uuid));
        statsManager.unloadStats(uuid);
    }

    public void loadSCPlayers() {

        for (UUID uuid : scPlayers.keySet()) {
            removeSCPlayer(uuid);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            addSCPlayer(player.getUniqueId());
        }
    }

    public void staff(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("sccore.staff")) {
                p.sendMessage(message);
            }
        }
    }

    public void staff(FancyMessage message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("sccore.staff")) {
                message.send(p);
            }
        }
    }

    public void broadcast(FancyMessage message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            message.send(p);
        }
    }

    public void broadcast(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(message);
        }
    }

}