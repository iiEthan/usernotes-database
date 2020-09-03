package com.rteenagers.parrot;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

public final class Parrot extends JavaPlugin {

    @Override
    public void onEnable() {

        PluginManager pm = getServer().getPluginManager();

        // Register our commands
        getCommand("pleasework").setExecutor(new SheetsFetcherCommand(this));


        getLogger().info("Parrot has been enabled!");

    }

    @Override
    public void onDisable() {
        getLogger().info("Parrot has been disabled!");
    }
}
