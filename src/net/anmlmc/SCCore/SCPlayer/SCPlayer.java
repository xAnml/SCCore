package net.anmlmc.SCCore.SCPlayer;

import com.earth2me.essentials.PlayerExtension;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayerColl;
import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.MySQL.MySQL;
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

    public Rank getRank() {

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
        return null;
    }

    public void setRank(Rank rank) {

        if (!hasRank()) {
            try {

                mySQL.executeUpdate("INSERT INTO `SCPlayerInfo`(`UUID`, `Rank`) VALUES ('" + player.getUniqueId() +
                        "','" + rank.name().toUpperCase() + "')");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mySQL.executeUpdate("UPDATE SCPlayerInfo SET Rank='" + rank.name().toUpperCase() + "' WHERE UUID='" +
                        player.getUniqueId() + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasRank() {

        try {
            ResultSet rs = mySQL.getResultSet("SELECT Rank FROM SCPlayerInfo WHERE UUID='" + player.getUniqueId() +
                    "'");
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

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

        for (int i = getRank().getId(); i >= 0; i--) {
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

    public String getTag() {
        if (getRank() != null)
            return getRank().getTag().replace("%s", player.getName());
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

    public void reset() {
        setRank(Rank.DEFAULT);
    }

    public Faction getFaction() {
        Faction faction = MPlayerColl.get().get(player).getFaction();
        return faction;
    }


    public double getPower() {
        return getFaction().isNone() ? 10.0 : MPlayerColl.get().get(player).getPowerRounded();
    }

    public double getMaxPower() {
        return getFaction().isNone() ? 10.0 : MPlayerColl.get().get(player).getPowerMaxRounded();
    }

    public boolean isLockpicking() {
        if (scPlayerManager.getLockpicking().containsKey(player))
            return true;
        return false;
    }

    public boolean lockpickAttempt() {

        int random = (int) (Math.random() * 100) + 1;
        if (random <= getRank().getLockpickChance())
            return true;
        return false;
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

}

