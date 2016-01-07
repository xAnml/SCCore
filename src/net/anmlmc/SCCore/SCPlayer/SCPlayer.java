package net.anmlmc.SCCore.SCPlayer;

import com.earth2me.essentials.Console;
import com.earth2me.essentials.PlayerExtension;
import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.MySQL.MySQL;
import net.anmlmc.SCCore.Punishments.PunishmentEntry;
import net.anmlmc.SCCore.Punishments.PunishmentType;
import net.anmlmc.SCCore.Ranks.Rank;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by kishanpatel on 12/6/15.
 */

public class SCPlayer extends PlayerExtension {

    private Player player;
    private Main instance;
    private MySQL mySQL;
    private SCPlayerManager scPlayerManager;
    private Map<Player, BukkitRunnable> duelRequests;
    private boolean combatTagged;
    private BukkitTask combatTask;
    private long combatTime;

    public SCPlayer(Player player) {
        super(player);
        this.player = player;
        this.instance = Main.getInstance();
        mySQL = instance.getMySQL();
        scPlayerManager = instance.getSCPlayerManager();
        duelRequests = new HashMap<>();
        combatTagged = false;
        combatTime = 0;
    }

    /* SHOUT */
    public boolean isShoutCooldowned() {
        return scPlayerManager.getShoutCooldowns().contains(player.getUniqueId());
    }

    public void shoutCooldown() {
        if (!isShoutCooldowned()) {
            scPlayerManager.getShoutCooldowns().add(player.getUniqueId());

            instance.getServer().getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
                public void run() {
                    if (isShoutCooldowned())
                        scPlayerManager.getShoutCooldowns().remove(player.getUniqueId());
                }
            }, 300L);
        }
    }

    /* RANKS */
    public Rank getCachedRank() {
        if (scPlayerManager.getCachedRanks().containsKey(player))
            return scPlayerManager.getCachedRanks().get(player);

        return getSQLRank();
    }

    public void setCachedRank(Rank rank) {
        if (scPlayerManager.getCachedRanks().containsKey(player)) {
            scPlayerManager.getCachedRanks().replace(player, rank);
            return;
        }

        setSQLRank(rank);
    }

    public Rank getSQLRank() {

        try {
            ResultSet resultSet = mySQL.getResultSet("SELECT Rank FROM SCPlayerInfo WHERE UUID='" + player.getUniqueId
                    () +
                    "'");
            if (resultSet.next()) {
                for (Rank rank : Rank.values()) {
                    if (rank.name().equalsIgnoreCase(resultSet.getString("Rank"))) {
                        return rank;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Rank.DEFAULT;
    }

    public void setSQLRank(Rank rank) {

        if (!hasSQLRank()) {
            try {

                mySQL.executeUpdate("INSERT INTO `SCPlayerInfo`(`UUID`, `Rank`) VALUES ('" + player.getUniqueId() +
                        "','" + rank.name() + "')");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mySQL.executeUpdate("UPDATE SCPlayerInfo SET Rank='" + rank.name() + "' WHERE UUID='" +
                        player.getUniqueId() + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasSQLRank() {
        try {
            ResultSet rs = mySQL.getResultSet("SELECT Rank FROM SCPlayerInfo WHERE UUID='" + player.getUniqueId() +
                    "'");
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /* PERMISSIONS */
    public List<String> getPersonalPermissions() {
        List<String> permissions = new ArrayList<>();
        String path = "Permissions.Player." + player.getUniqueId();
        FileConfiguration config = Main.getInstance().getConfig();
        if (config.contains(path)) {
            for (String permission : config.getStringList(path)) {
                if (!permissions.contains(permission.toLowerCase()))
                    permissions.add(permission.toLowerCase());
            }
        }

        return permissions;
    }

    public List<String> getRankPermissions() {
        List<String> permissions = new ArrayList<>();

        for (int i = getCachedRank().getId(); i >= 0; i--) {
            for (String permission : scPlayerManager.getRankById(i).getPermissions()) {
                if (!permissions.contains(permission.toLowerCase()))
                    permissions.add(permission.toLowerCase());
            }
        }

        return permissions;
    }

    public boolean addPersonalPermission(String node) {
        node = node.toLowerCase();
        if (getPersonalPermissions().contains(node))
            return false;

        String path = "Permissions.Player." + player.getUniqueId();
        FileConfiguration config = Main.getInstance().getConfig();
        List<String> permissions = new ArrayList<>(getPersonalPermissions());
        permissions.add(node.toLowerCase());
        config.set(path, permissions);
        Main.getInstance().saveConfig();

        scPlayerManager.updatePermissions(player);

        return true;
    }

    public boolean removePersonalPermission(String node) {
        node = node.toLowerCase();
        if (!getPersonalPermissions().contains(node))
            return false;

        String path = "Permissions.Player." + player.getUniqueId();
        FileConfiguration config = Main.getInstance().getConfig();
        List<String> permissions = new ArrayList<>(getPersonalPermissions());
        permissions.remove(node.toLowerCase());
        config.set(path, permissions);
        Main.getInstance().saveConfig();

        scPlayerManager.updatePermissions(player);

        return true;
    }

    public PermissionAttachment permissionAttachment() {
        PermissionAttachment attachment = player.addAttachment(instance);

        for (String permission : getRankPermissions()) {
            attachment.setPermission(permission, true);
        }

        for (String permission : getPersonalPermissions()) {
            if (!attachment.getPermissions().containsKey(permission))
                attachment.setPermission(permission, true);
        }

        return attachment;
    }

    /* TAG */
    public String getTag() {
        if (getCachedRank() != null)
            return getCachedRank().getTag().replace("%s", player.getName());
        return "§f" + player.getName();
    }

    public List<String> getHoverText() {
        return Arrays.asList(
                "§bStats:",
                "   §aKills: §f" + getKills(),
                "   §aDeaths: §f" + getDeaths(),
                "   §aK/D: §f" + getKD(),
                "§bInfo:",
                "   §aBalance: §f$" + instance.getEssentials().getUser(player).getMoney(),
                "   §aFaction: §f", // ((getFaction().isNone() || getFaction() == null) ? "Wilderness" : getFaction()
                // .getName()),
                "   §aPower: §f",
                "§bDuels:",
                "   §aWins: §f" + getWins(),
                "   §aLosses: §f" + getLosses(),
                "   §aW/L: §f" + getWL());
    }

    /* STATS */
    public int getKills() {
        try {
            ResultSet resultSet = mySQL.getResultSet("SELECT Kills FROM SCPlayerInfo WHERE UUID='" + player.getUniqueId() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("Kills");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setKills(int value) {
        try {
            mySQL.executeUpdate("UPDATE SCPlayerInfo SET Kills='" + value + "' WHERE UUID='" + player.getUniqueId() + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getDeaths() {
        try {
            ResultSet resultSet = mySQL.getResultSet("SELECT Deaths FROM SCPlayerInfo WHERE UUID='" + player
                    .getUniqueId() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("Deaths");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setDeaths(int value) {
        try {
            mySQL.executeUpdate("UPDATE SCPlayerInfo SET Deaths='" + value + "' WHERE UUID='" + player.getUniqueId() +
                    "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getKD() {
        if (getDeaths() == 0) {
            return getKills();
        }

        DecimalFormat df = new DecimalFormat("#.##");
        double ratio = ((double) getKills()) / ((double) getDeaths());
        return Double.valueOf(df.format(ratio));
    }

    /* LOCKPICKS */
    public boolean isLockpicking() {
        return scPlayerManager.getLockpicking().containsKey(player);
    }

    public boolean lockpickAttempt() {

        int random = (int) (Math.random() * 100) + 1;
        return random <= getCachedRank().getLockpickChance();
    }

    public Map<Player, BukkitRunnable> getDuelRequests() {
        return duelRequests;
    }

    public void addDuelRequest(Player target) {

        BukkitRunnable request = new BukkitRunnable() {
            @Override
            public void run() {
                duelRequests.remove(target);
            }
        };

        duelRequests.put(target, request);
        request.runTaskLater(instance, 6000L);
    }

    public void removeDuelRequest(Player target) {
        if (!duelRequests.containsKey(target))
            return;

        BukkitRunnable task = duelRequests.get(target);
        if (task != null)
            task.cancel();

        duelRequests.remove(target);
    }

    /* DUEL ARENA STATS */
    public int getWins() {
        try {
            ResultSet resultSet = mySQL.getResultSet("SELECT Wins FROM SCPlayerInfo WHERE UUID='" + player.getUniqueId()
                    + "'");
            if (resultSet.next()) {
                return resultSet.getInt("Wins");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setWins(int value) {
        try {
            mySQL.executeUpdate("UPDATE SCPlayerInfo SET Wins='" + value + "' WHERE UUID='" + player.getUniqueId() +
                    "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getLosses() {
        try {
            ResultSet resultSet = mySQL.getResultSet("SELECT Losses FROM SCPlayerInfo WHERE UUID='" + player
                    .getUniqueId() + "'");
            if (resultSet.next()) {
                return resultSet.getInt("Losses");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setLosses(int value) {
        try {
            mySQL.executeUpdate("UPDATE SCPlayerInfo SET Losses='" + value + "' WHERE UUID='" + player.getUniqueId() +
                    "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getWL() {
        if (getLosses() == 0) {
            return getWins();
        }

        DecimalFormat df = new DecimalFormat("#.##");
        double ratio = ((double) getWins()) / ((double) getLosses());
        return Double.valueOf(df.format(ratio));
    }

    /* COMBAT */
    public boolean isCombatTagged() {
        return combatTagged;
    }

    public long getCombatTime() {
        if (combatTime == 0)
            return 0;
        return (System.currentTimeMillis() + combatTime) - System.currentTimeMillis();
    }

    public void combatTag() {

        if (combatTask != null)
            combatTask.cancel();

        if (!combatTagged) {
            combatTagged = true;
            player.sendMessage("§eYou are now in combat.");
        }

        combatTime = System.currentTimeMillis() + 8000;
        combatTask = new BukkitRunnable() {

            @Override
            public void run() {
                removeCombatTag();
            }

        }.runTaskLater(instance, 160L);

    }

    public void removeCombatTag() {

        combatTagged = false;
        combatTime = 0;

        if (combatTask != null) {
            combatTask.cancel();
            combatTask = null;
            player.sendMessage("§eYou have left combat.");
        }
    }

    /* PUNISHMENTS */

    public List<PunishmentEntry> getSQLPunishments() {
        List<PunishmentEntry> list = new ArrayList<>();

        try {
            ResultSet resultSet = mySQL.getResultSet("SELECT * FROM SCPunishments WHERE Target='" + player.getUniqueId() + "'");

            while (resultSet.next()) {
                PunishmentType type = PunishmentType.valueOf(resultSet.getString("Type"));
                UUID target = UUID.fromString(resultSet.getString("Target"));
                UUID punisher = !resultSet.getString("Punisher").equals(Console.NAME) ? UUID.fromString(resultSet
                        .getString("Punisher")) : null;
                long created = resultSet.getLong("Created");
                long expires = resultSet.getLong("Expires");
                String reason = resultSet.getString("Reason");

                PunishmentEntry entry = new PunishmentEntry(instance, type, target, punisher, created, expires, reason);
                entry.setExecuted(true);
                list.add(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void setSQLPunishments(List<PunishmentEntry> entries) {
        for (PunishmentEntry entry : entries) {
            entry.execute();
        }
    }

    public List<PunishmentEntry> getCachedPunishments() {
        if (scPlayerManager.getCachedPunishments().containsKey(player))
            return scPlayerManager.getCachedPunishments().get(player);

        return getSQLPunishments();
    }

    public void setCachedPunishments(List<PunishmentEntry> entries) {

        if (!player.isOnline()) {
            setSQLPunishments(entries);
            return;
        }

        getCachedPunishments().clear();

        for (PunishmentEntry entry : entries) {
            getCachedPunishments().add(entry);
        }
    }

    public List<PunishmentEntry> getPunishments(PunishmentType type) {
        List<PunishmentEntry> list = new ArrayList<>();

        if (player.isOnline()) {
            for (PunishmentEntry entry : getCachedPunishments())
                list.add(entry);
        } else {
            for (PunishmentEntry entry : getSQLPunishments()) {
                if (entry.getType().equals(type))
                    list.add(entry);
            }
        }

        return list;
    }


}

