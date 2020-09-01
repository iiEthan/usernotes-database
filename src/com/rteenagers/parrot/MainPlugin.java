package com.rteenagers.parrot;

import org.bukkit.plugin.java.JavaPlugin;

public class MainPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Parrot has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Parrot has been disabled!");
    }
}
