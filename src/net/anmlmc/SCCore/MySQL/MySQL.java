package net.anmlmc.SCCore.MySQL;

import net.anmlmc.SCCore.Main;

import java.sql.*;

/**
 * Created by kishanpatel on 12/6/15.
 */

public class MySQL {

    private Connection connection;
    private Main instance;

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    public MySQL(Main instance) {
        this.instance = instance;

        host = instance.getConfig().getString("MySQL.Host");
        port = instance.getConfig().getString("MySQL.Port");
        database = instance.getConfig().getString("MySQL.Database");
        username = instance.getConfig().getString("MySQL.Username");
        password = instance.getConfig().getString("MySQL.Password");

        try {
            if (connection != null)
                return;

            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, username, password);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public ResultSet getResultSet(String qry) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(qry);
        return statement.executeQuery();
    }

    public int executeUpdate(String qry) throws SQLException {

        PreparedStatement statement = connection.prepareStatement(qry);
        int task = statement.executeUpdate();
        statement.close();
        return task;
    }
}
