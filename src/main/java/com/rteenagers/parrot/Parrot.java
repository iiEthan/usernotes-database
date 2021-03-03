package com.rteenagers.parrot;

import org.bukkit.plugin.java.JavaPlugin;

public final class Parrot extends JavaPlugin {

    @Override
    public void onEnable() {

        // Register command
        getCommand("notes").setExecutor(new NotesCommand());

        // Initialize database
        System.out.println("Initializing database...");
        FirebaseHandler.start();

        getLogger().info("Parrot has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Parrot has been disabled!");
    }
}
