package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import com.rteenagers.parrot.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PointCommand implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("points.edit")) {
            sender.sendMessage(ChatColor.RED + "You are not permitted to do this!");
            return true;
        }

        if (args.length < 4) { // User must give an input
            sender.sendMessage(ChatColor.RED + "Please provide more arguments! Usage is /point [ban/mute/warn] [player] [amount] [reason]");
           return true;
        }

        if (!Utils.isInteger(args[2])) { // Check if point is valid
            sender.sendMessage(ChatColor.RED + "Please provide a valid number as your third argument.");
            return true;
        }

        // Parses reason into its own string
        StringBuilder reason = new StringBuilder();
        for (int i = 3; i<args.length; i++) {
            reason.append(args[i]).append(" ");
        }

        @SuppressWarnings("deprecation")
        UUID player = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
        String punishmentType;
        switch (args[0]) {
            case "ban":

            case "warnban":
                punishmentType = "bans";
                break;

            case "mute":

            case "warnmute":
                punishmentType = "mutes";
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Invalid syntax. Please enter " + ChatColor.DARK_RED + "[ban/mute/warnban/warnmute] " + ChatColor.RED + "as the first argument.");
                return true;
            }

        try {
            DatabaseHandler.addPoints(args[1], punishmentType, String.valueOf(player), sender.getName(), reason.toString(), args[2], args[0].startsWith("warn"), sender);
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
                    String[] punishments = {"ban", "mute", "warnban", "warnmute"};
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
                    for (int i = 0; i<10; i++) {
                        arguments.add(String.valueOf(i));
                    }
                    Collections.sort(arguments);
                    return arguments;

                    case 4:
                        if (args[3].isEmpty()) {
                            arguments.add("reason");
                        }
                        return arguments;

                default:
                    return new ArrayList<>();
            }
        }
        return null;
    }
}
