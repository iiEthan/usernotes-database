package com.rteenagers.parrot;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class Usernotes extends JavaPlugin {

    String host, port, database, username, password;
    static Connection connection;

    @Override
    public void onEnable() {

        // Register command
        getCommand("notes").setExecutor(new NotesCommand());

        // Register Database
        host = "usernotes.ctlynjuzcvj9.us-east-1.rds.amazonaws.com";
        port = "5432";
        database = "postgres";
        username = "root";
        password = "WVZWLFup4OHPzbKHLg0T";

        try {
            DatabaseHandler.openConnection(host, port, database, username, password);
            getLogger().info("Connection to usernote database successfully made");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getLogger().info("Usernotes has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Usernotes has been disabled!");
    }
}
