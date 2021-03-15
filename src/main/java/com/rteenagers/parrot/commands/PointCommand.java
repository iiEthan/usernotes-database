package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import com.rteenagers.parrot.Utils;
import com.rteenagers.parrot.commands.manager.UsernotesCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PointCommand extends UsernotesCommand {

    @Override
    public String name() {
        return "point";
    }

    @Override
    public String info() {
        return "/point [ban/mute/warn] [player] [amount] [reason]";
    }

    @Override
    public String permission() {
        return "points.edit";
    }

    @Override
    public int argsCount() {
        return 4;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Utils.isInteger(args[2])) { // Check if point is valid
            sender.sendMessage(ChatColor.RED + "Please provide a valid number as your third argument.");
            return;
        }

        // Parses reason into its own string
        StringBuilder reason = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }

        // ethan, please don't forget to fix this fucking mess of a code
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
                return;
        }

        try {
            DatabaseHandler.addPoints(args[1], punishmentType, String.valueOf(player), sender.getName(), reason.toString(), args[2], args[0].startsWith("warn"), sender);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args) {
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
                for (int i = 0; i < 10; i++) {
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
}
