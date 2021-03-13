package com.rteenagers.parrot;

import com.rteenagers.parrot.commands.*;
import org.bukkit.plugin.java.JavaPlugin;

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
        DatabaseHandler.openConnection();
        getLogger().info("Connection to usernotes database successfully made");

        Utils.createHashes();

        getLogger().info("Usernotes has been enabled!");
    }

    @Override
    public void onDisable() {
        // Close DB connection so on reload we dont have multiple idled connections
       // try {
            //DatabaseHandler.connection.close();
        //} catch (SQLException throwables) {
        //    throwables.printStackTrace();
        //}
        getLogger().info("Usernotes has been disabled!");
    }
}
