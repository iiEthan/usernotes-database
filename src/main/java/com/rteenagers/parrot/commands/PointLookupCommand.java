package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import com.rteenagers.parrot.Utils;
import com.rteenagers.parrot.commands.manager.UsernotesCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PointLookupCommand extends UsernotesCommand {

    @Override
    public String name() {
        return "pointlookup";
    }

    @Override
    public String info() {
        return "/pointlookup [ban/mute] [id]";
    }

    @Override
    public String permission() {
        return "points.lookup";
    }

    public int argsCount() {
        return 2;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(args[0].equalsIgnoreCase("ban") || args[0].equalsIgnoreCase("mute"))) { // Check if punishment input is valid
            sender.sendMessage(ChatColor.RED + "Please provide a valid punishment to remove as the first argument. Usage is /removepoint [ban/mute] [id]");
            return;
        }

        if (!Utils.isInteger(args[1])) { // Check if point is valid
            sender.sendMessage(ChatColor.RED + "Please provide a valid number as the ID argument.");
            return;
        }

        try {
            DatabaseHandler.pointLookup(args[0], args[1], sender);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args) {
        ArrayList<String> arguments = new ArrayList<>();
        switch (args.length) {
            case 1:
                String[] punishments = {"ban", "mute"};
                for (String p : punishments) {
                    if (p.startsWith(args[0].toLowerCase())) {
                        arguments.add(p);
                    }
                }
                return arguments;
            case 2:
                if (args[1].equals("")) {
                    arguments.add("noteid");
                }
                return arguments;
            default:
                return new ArrayList<>();
        }
    }
}