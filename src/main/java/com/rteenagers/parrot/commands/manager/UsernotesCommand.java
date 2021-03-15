package com.rteenagers.parrot.commands.manager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class UsernotesCommand {

    public UsernotesCommand() {
    }

    public abstract String name();

    public abstract String info();

    public abstract String permission();

    public abstract int argsCount();

    public abstract void execute(CommandSender sender, String[] args);

    public abstract List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args);

}
