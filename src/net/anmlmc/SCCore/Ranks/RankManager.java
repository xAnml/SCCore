package net.anmlmc.SCCore.Ranks;

import net.anmlmc.SCCore.Main;
import net.anmlmc.SCCore.MySQL.MySQL;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Anml on 1/12/16.
 */

public class RankManager {

    private Main instance;
    private MySQL mySQL;
    private Map<UUID, Rank> players;

    public RankManager(Main instance) {
        this.instance = instance;
        mySQL = instance.getMySQL();
        players = new HashMap<>();
    }

    public Map<UUID, Rank> getPlayers() {
        return players;
    }

    public Rank getRankById(int id) {
        for (Rank rank : Rank.values()) {
            if (rank.getId() == id)
                return rank;
        }
        return Rank.DEFAULT;
    }

    public Rank getRank(UUID uuid) {
        if (players.containsKey(uuid))
            return players.get(uuid);

        try {
            ResultSet resultSet = mySQL.getResultSet("SELECT Rank FROM SCPlayerInfo WHERE UUID='" + uuid + "'");
            if (resultSet.next()) {
                return Rank.valueOf(resultSet.getString("Rank"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Rank.DEFAULT;
    }


    public void setRank(UUID uuid, Rank rank) {
        if (players.containsKey(uuid)) {
            players.replace(uuid, rank);
            return;
        }

        if (Bukkit.getPlayer(uuid) != null) {
            players.put(uuid, rank);
            return;
        }

        setSQLRank(uuid, rank);
    }

    public void setSQLRank(UUID uuid, Rank rank) {

        if (!hasRank(uuid)) {
            try {

                mySQL.executeUpdate("INSERT INTO `SCPlayerInfo`(`UUID`, `Rank`) VALUES ('" + uuid + "','" + rank.name() + "')");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mySQL.executeUpdate("UPDATE SCPlayerInfo SET Rank='" + rank.name() + "' WHERE UUID='" + uuid + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasRank(UUID uuid) {
        try {
            ResultSet rs = mySQL.getResultSet("SELECT Rank FROM SCPlayerInfo WHERE UUID='" + uuid + "'");
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}
