package com.rteenagers.parrot;

import com.rteenagers.parrot.commands.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

@SuppressWarnings("ALL")
public final class Usernotes extends JavaPlugin {

    @Override
    public void onEnable() {

        // Register commands
        getCommand("point").setExecutor(new PointCommand());
        getCommand("points").setExecutor(new PointsCommand());
        getCommand("removepoint").setExecutor(new RemovePoint());
        getCommand("pointlookup").setExecutor(new PointLookup());
        getCommand("banleaderboard").setExecutor(new BanLeaderboard());

        // Register Database
        try {
            DatabaseHandler.createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getLogger().info("Connection to usernotes database successfully made");

        Utils.createHashes();

        getLogger().info("Usernotes has been enabled!");
    }

    @Override
    public void onDisable() {
        try {
            DatabaseHandler.dataSource.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        getLogger().info("Usernotes has been disabled!");
    }
}
