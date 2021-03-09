package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.sql.Array;
import java.sql.SQLException;
import java.util.*;

public class PointCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 3) { // User must give an input
            sender.sendMessage(ChatColor.RED + "Please provide more arguments! Usage is /point [ban/mute/warn] [player] [amount] [reason]");
           return true;
        }

        UUID player = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
        try {
            DatabaseHandler.addNotes(String.valueOf(player));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("point")) {

            ArrayList<String> arguments = new ArrayList<>();
            switch (args.length) {
                case 1:
                    String[] punishments = {"ban", "mute", "warn"};
                    for (String p : punishments) {
                        if (p.startsWith(args[0].toLowerCase())) {
                            arguments.add(p);
                        }
                    }
                    return arguments;

                    case 2:
                    if (!args[1].equals("")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
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

                    case 3:
                    for (int i = 1; i<10; i++) {
                        arguments.add(String.valueOf(i));
                    }
                    Collections.sort(arguments);
                    return arguments;

                    case 4:
                        arguments.add("reason");
                        return arguments;

                default:
                    return new ArrayList<>();
            }
        }
        return null;
    }
}
