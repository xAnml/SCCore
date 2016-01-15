package net.anmlmc.SCCore.Stats;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.MySQL.MySQL;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Anml on 1/12/16.
 */
public class StatsManager {

    private Main instance;
    private MySQL mySQL;
    private Map<UUID, Integer> kills;
    private Map<UUID, Integer> deaths;
    private Map<UUID, Integer> wins;
    private Map<UUID, Integer> losses;

    public StatsManager(Main instance) {
        this.instance = instance;
        mySQL = instance.getMySQL();
        kills = new HashMap<>();
        deaths = new HashMap<>();
        wins = new HashMap<>();
        losses = new HashMap<>();
    }

    public int getIntegerStat(UUID uuid, Stat stat) {

        if (stat.equals(Stat.KILLS)) {
            if (kills.containsKey(uuid)) {
                return kills.get(uuid);
            }
        } else if (stat.equals(Stat.DEATHS)) {
            if (deaths.containsKey(uuid)) {
                return deaths.get(uuid);
            }
        } else if (stat.equals(Stat.WINS)) {
            if (wins.containsKey(uuid)) {
                return wins.get(uuid);
            }
        } else if (stat.equals(Stat.LOSSES)) {
            if (losses.containsKey(uuid)) {
                return losses.get(uuid);
            }
        }

        try {
            ResultSet resultSet = mySQL.getResultSet("SELECT " + stat.getName() + " FROM SCPlayerInfo WHERE UUID='" + uuid + "'");
            if (resultSet.next()) {
                return resultSet.getInt(stat.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }


    public void setIntegerStat(UUID uuid, Stat stat, int value) {

        if (stat.equals(Stat.KILLS)) {
            if (kills.containsKey(uuid)) {
                kills.replace(uuid, value);
                return;
            }
            if (Bukkit.getPlayer(uuid) != null) {
                kills.put(uuid, value);
                return;
            }
        } else if (stat.equals(Stat.DEATHS)) {
            if (deaths.containsKey(uuid)) {
                deaths.replace(uuid, value);
                return;
            }
            if (Bukkit.getPlayer(uuid) != null) {
                deaths.put(uuid, value);
                return;
            }
        } else if (stat.equals(Stat.WINS)) {
            if (wins.containsKey(uuid)) {
                wins.replace(uuid, value);
                return;
            }
            if (Bukkit.getPlayer(uuid) != null) {
                wins.put(uuid, value);
                return;
            }
        } else if (stat.equals(Stat.LOSSES)) {
            if (losses.containsKey(uuid)) {
                losses.replace(uuid, value);
                return;
            }
            if (Bukkit.getPlayer(uuid) != null) {
                losses.put(uuid, value);
                return;
            }
        }


        setSQLIntegerStatistic(uuid, stat, value);
    }

    private void setSQLIntegerStatistic(UUID uuid, Stat stat, int value) {
        try {
            mySQL.executeUpdate("UPDATE SCPlayerInfo SET " + stat.getName() + "='" + value + "' WHERE UUID='" + uuid + "'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadStats(UUID uuid) {
        if (!Bukkit.getOfflinePlayer(uuid).isOnline())
            return;

        if (kills.containsKey(uuid))
            kills.remove(uuid);
        if (deaths.containsKey(uuid))
            deaths.remove(uuid);
        if (wins.containsKey(uuid))
            wins.remove(uuid);
        if (losses.containsKey(uuid))
            losses.remove(uuid);

        kills.put(uuid, getIntegerStat(uuid, Stat.KILLS));
        deaths.put(uuid, getIntegerStat(uuid, Stat.DEATHS));
        wins.put(uuid, getIntegerStat(uuid, Stat.WINS));
        losses.put(uuid, getIntegerStat(uuid, Stat.LOSSES));

    }

    public void unloadStats(UUID uuid) {
        if (kills.containsKey(uuid)) {
            setSQLIntegerStatistic(uuid, Stat.KILLS, kills.get(uuid));
            kills.remove(uuid);
        }
        if (deaths.containsKey(uuid)) {
            setSQLIntegerStatistic(uuid, Stat.DEATHS, deaths.get(uuid));
            deaths.remove(uuid);
        }
        if (wins.containsKey(uuid)) {
            setSQLIntegerStatistic(uuid, Stat.WINS, wins.get(uuid));
            wins.remove(uuid);
        }
        if (losses.containsKey(uuid)) {
            setSQLIntegerStatistic(uuid, Stat.LOSSES, losses.get(uuid));
            losses.remove(uuid);
        }

    }

    public double getKD(UUID uuid) {
        if (getIntegerStat(uuid, Stat.DEATHS) == 0) {
            return getIntegerStat(uuid, Stat.KILLS);
        }

        DecimalFormat df = new DecimalFormat("#.##");
        double ratio = ((double) getIntegerStat(uuid, Stat.KILLS)) / ((double) getIntegerStat(uuid, Stat.DEATHS));
        return Double.valueOf(df.format(ratio));
    }

    public double getWL(UUID uuid) {
        if (getIntegerStat(uuid, Stat.LOSSES) == 0) {
            return getIntegerStat(uuid, Stat.WINS);
        }

        DecimalFormat df = new DecimalFormat("#.##");
        double ratio = ((double) getIntegerStat(uuid, Stat.WINS)) / ((double) getIntegerStat(uuid, Stat.LOSSES));
        return Double.valueOf(df.format(ratio));
    }
}
