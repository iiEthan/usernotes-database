package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class PointCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) { // User must give an input
            sender.sendMessage(ChatColor.RED + "Please provide more arguments!");
           return true;
        }

        Arrays.toString(args);

        DatabaseHandler.main(sender, args);

        return true;
    }
}
