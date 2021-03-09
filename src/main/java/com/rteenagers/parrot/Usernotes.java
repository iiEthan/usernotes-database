package com.rteenagers.parrot;

import com.rteenagers.parrot.commands.PointCommand;
import com.rteenagers.parrot.commands.PointsCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;

@SuppressWarnings("ALL")
public final class Usernotes extends JavaPlugin {

    String host, port, database, username, password;

    @Override
    public void onEnable() {

        // Register commands
        getCommand("point").setExecutor(new PointCommand());
        getCommand("points").setExecutor(new PointsCommand());

        // Register Database
        try {
            DatabaseHandler.openConnection();
            getLogger().info("Connection to usernotes database successfully made");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        getLogger().info("Usernotes has been enabled!");
    }

    @Override
    public void onDisable() {
        // Close DB connection so on reload we dont have multiple idled connections
        try {
            DatabaseHandler.connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        getLogger().info("Usernotes has been disabled!");
    }
}
