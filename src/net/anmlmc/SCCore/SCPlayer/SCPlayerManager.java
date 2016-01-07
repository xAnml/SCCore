package net.anmlmc.SCCore.SCPlayer;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayerColl;
import com.massivecraft.massivecore.ps.PS;
import net.anmlmc.SCCore.Lockpicks.LockpickRunnable;
import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.Punishments.PunishmentEntry;
import net.anmlmc.SCCore.Punishments.PunishmentType;
import net.anmlmc.SCCore.Ranks.Rank;
import net.anmlmc.SCCore.Utils.Fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;

/**
 * Created by Kishan on 12/2/15.
 */
public class SCPlayerManager implements Listener {

    private Main instance;
    private List<UUID> shoutCooldowns;
    private Map<Player, LockpickRunnable> lockpicking;
    private Map<Player, SCPlayer> scPlayers;
    private Map<Player, List<PunishmentEntry>> cachedPunishments;
    private Map<Player, Rank> cachedRanks;
    private HashMap<UUID, PermissionAttachment> permissions;

    public SCPlayerManager(Main instance) {
        this.instance = instance;
        shoutCooldowns = new ArrayList<>();
        scPlayers = new HashMap<>();
        cachedPunishments = new HashMap<>();
        cachedRanks = new HashMap<>();
        permissions = new HashMap<>();
        lockpicking = new HashMap<>();
    }

    public List<UUID> getShoutCooldowns() {
        return shoutCooldowns;
    }

    public Map<Player, LockpickRunnable> getLockpicking() {
        return lockpicking;
    }

    public Map<Player, Rank> getCachedRanks() {
        return cachedRanks;
    }

    public Map<Player, List<PunishmentEntry>> getCachedPunishments() {
        return cachedPunishments;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        Player player = e.getPlayer();

        SCPlayer scPlayer = getSCPlayer(player);

        for (PunishmentEntry entry : scPlayer.getPunishments(PunishmentType.BAN)) {
            if (!entry.hasExpired()) {
                String punisher = (entry.getPunisher() != null) ? getSCPlayer(Bukkit.getPlayer(entry.getPunisher()))
                        .getTag() : "§6Console";
                player.getPlayer().kickPlayer("§4You have been permanently banned!\n" + entry.getReason() + "§4- " +
                        punisher);
            }
        }

        e.setJoinMessage(null);
        FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §ehas logged in.");
        broadcast(message);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(final PlayerQuitEvent e) {
        Player player = e.getPlayer();
        SCPlayer scPlayer = getSCPlayer(player);

        e.setQuitMessage(null);
        FancyMessage message = new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §ehas logged off.");
        broadcast(message);

        if (scPlayer.isCombatTagged()) {
            player.setHealth(0);
            scPlayer.removeCombatTag();
            broadcast(new FancyMessage(scPlayer.getTag()).tooltip(scPlayer.getHoverText()).then(" §5has logged off while in combat!"));
        }

        removeSCPlayer(scPlayer);
    }

    @EventHandler
    public void onPlayerCommandPre(final PlayerCommandPreprocessEvent e) {
        if (getSCPlayer(e.getPlayer()).isCombatTagged()) {
            if (e.getMessage().equalsIgnoreCase("ct") || e.getMessage().equalsIgnoreCase("combattime"))
                return;

            e.setCancelled(true);
            e.getPlayer().sendMessage("§cYou are not permitted to execute commands while in combat.");
        }
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

        getSCPlayer(player).combatTag();
        getSCPlayer(target).combatTag();

    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(final PlayerDeathEvent e) {

        e.setDeathMessage(null);

        if (!(e.getEntity().getKiller() instanceof Player))
            return;

        Player playerKilled = e.getEntity();
        SCPlayer killed = getSCPlayer(playerKilled);
        Player playerKiller = e.getEntity().getKiller();
        SCPlayer killer = getSCPlayer(playerKiller);

        killed.setDeaths(killed.getDeaths() + 1);
        killer.setKills(killer.getKills() + 1);

        killed.removeCombatTag();

        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1);
        head.setDurability((short) 3);
        SkullMeta headMeta = (SkullMeta) head.getItemMeta();
        headMeta.setLore(Arrays.asList("§aKiller: " + killer.getTag()));
        headMeta.setOwner(playerKilled.getName());
        headMeta.setDisplayName(killed.getTag() + "§f's Head");
        head.setItemMeta(headMeta);
        e.getDrops().add(head);

        FancyMessage message = new FancyMessage(killed.getTag()).tooltip(killed.getHoverText()).then(" §7has " +
                "been killed by ").then(killer.getTag()).tooltip(killer.getHoverText()).then("§7.");
        broadcast(message);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(final AsyncPlayerChatEvent e) {
        e.setCancelled(true);

        Player player = e.getPlayer();
        SCPlayer scPlayer = getSCPlayer(player);

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

    public SCPlayer getSCPlayer(Player player) {
        if (scPlayers.containsKey(player))
            return scPlayers.get(player);

        if (player.isOnline()) {
            addSCPlayer(player);
            return getSCPlayer(player);
        }

        return new SCPlayer(player);
    }

    public void addSCPlayer(Player player) {
        if (scPlayers.containsKey(player))
            return;

        SCPlayer scPlayer = new SCPlayer(player);

        scPlayers.put(player, scPlayer);

        if (!scPlayer.hasSQLRank()) {
            cachedRanks.put(player, Rank.DEFAULT);
        } else {
            cachedRanks.put(player, scPlayer.getSQLRank());
        }

        cachedPunishments.put(player, scPlayer.getSQLPunishments());

        permissions.put(player.getUniqueId(), scPlayer.permissionAttachment());
    }

    public void removeSCPlayer(SCPlayer scPlayer) {

        Player player = scPlayer.getBase();

        if (!player.isOnline())
            return;

        player.removeAttachment(permissions.get(player));
        permissions.remove(player);

        scPlayer.setSQLRank(cachedRanks.get(player));
        scPlayer.setSQLPunishments(cachedPunishments.get(player));

        scPlayers.remove(scPlayer.getBase());
    }

    public void loadSCPlayers() {

        for (SCPlayer scPlayer : scPlayers.values()) {
            removeSCPlayer(scPlayer);
        }

        permissions.clear();
        cachedRanks.clear();
        cachedPunishments.clear();

        for (Player player : Bukkit.getOnlinePlayers()) {
            addSCPlayer(player);
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

    public Rank getRankById(int id) {
        for (Rank rank : Rank.values()) {
            if (rank.getId() == id)
                return rank;
        }
        return Rank.DEFAULT;
    }

    public Map<UUID, PermissionAttachment> getPermissionAttachments() {
        return permissions;
    }

    public void updatePermissions(Rank rank) {
        for (SCPlayer scPlayer : scPlayers.values()) {
            if (scPlayer.getCachedRank().equals(rank)) {
                if (permissions.containsKey(scPlayer.getBase().getUniqueId())) {
                    permissions.replace(scPlayer.getBase().getUniqueId(), scPlayer.permissionAttachment());
                }
            }
        }
    }

    public void updatePermissions(Player player) {
        if (!player.isOnline()) return;

        SCPlayer scPlayer = getSCPlayer(player);

        if (permissions.containsKey(player.getUniqueId())) {
            player.removeAttachment(permissions.get(player.getUniqueId()));
            permissions.replace(player.getUniqueId(), scPlayer.permissionAttachment());
        }
    }
}