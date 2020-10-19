package io.github.xiione;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Stores data for a plugin's connection to its configured MySQL server
 */
public class MySQL {
    private final JavaPlugin plugin;
    private FileConfiguration config;

    private Connection connection;
    private Statement statement;

    private String host, username, password, database, table;
    private int port;

    /**
     * @param plugin The running instance of the plugin
     */
    public MySQL(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        host = config.getString("mysql-host");
        port = config.getInt("mysql-port");

        username = config.getString("mysql-username");
        password = config.getString("mysql-password");

        database = config.getString("mysql-database");
        table = config.getString("mysql-database-table");
    }

    /**
     * Attempts a connection to the configured MySQL server
     *
     * @throws SQLException           if SQL-related error occurs
     * @throws ClassNotFoundException if JDBC drivers are not installed on the server
     */
    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database +
                    "?autoReconnect=true&useSSL=false", this.username, this.password);
        }
    }

    /**
     * @return The current connection to a MySQL server. null if connection has not been made
     */
    public Connection getConnection() {
        return this.connection;
    }


}
