package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PointCommand implements TabExecutor {

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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("point")) {
            if (args.length == 1) {
                ArrayList<String> arguments = new ArrayList<>();
                if (!args[0].equals("")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                            arguments.add(p.getName());
                        }
                    }
                } else {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        arguments.add(p.getName());
                    }
                }
                Collections.sort(arguments);

                return arguments;
            }
            if (args.length > 1) {
                ArrayList<String> arguments = new ArrayList<String>();

                //TODO

                return arguments;
            }
        }
        return null;
    }
}
