package com.rteenagers.parrot;

import com.rteenagers.parrot.commands.manager.CommandManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class Usernotes extends JavaPlugin {
    private static Usernotes instance;
    public CommandManager commandManager;

    @Override
    public void onEnable() {

        // Register commands
        setInstance(this);
        commandManager = new CommandManager();
        commandManager.setup();

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

    public static Usernotes getInstance() {
        return instance;
    }

    public static void setInstance(Usernotes instance) {
        Usernotes.instance = instance;
    }
}
