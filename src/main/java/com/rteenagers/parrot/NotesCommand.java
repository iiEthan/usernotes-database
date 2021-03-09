package com.rteenagers.parrot;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Arrays;

public class NotesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) { // User must give an input
            sender.sendMessage(ChatColor.RED + "Please provide a player to scan");
           return true;
        }

        Arrays.toString(args);

        try {
            DatabaseHandler.main(sender, args);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
