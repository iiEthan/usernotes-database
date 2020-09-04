package com.rteenagers.parrot;

import org.bukkit.plugin.java.JavaPlugin;

public final class Parrot extends JavaPlugin {

    @Override
    public void onEnable() {

        // Register our commands
        getCommand("notes").setExecutor(new SheetsFetcherCommand());

        getLogger().info("Parrot has been enabled!");

    }

    @Override
    public void onDisable() {
        getLogger().info("Parrot has been disabled!");
    }
}
