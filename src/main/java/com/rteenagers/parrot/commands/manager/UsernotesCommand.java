package com.rteenagers.parrot.commands.manager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

// We are using a custom command class mainly for neatness, readability, and consistency
public abstract class UsernotesCommand {

    public UsernotesCommand() {
    }

    // Used for getCommand lookup
    public abstract String getName();

    // Gives back command usage
    public abstract String getInfo();

    // Ensures user has the proper permission to run command
    public abstract String permission();

    // Makes sure user provides enough subcommands
    public abstract int getArgsCount();

    // Same as onCommand method
    public abstract void execute(CommandSender sender, String[] args);

    // Subcommand tab completion
    public abstract List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args);

}
