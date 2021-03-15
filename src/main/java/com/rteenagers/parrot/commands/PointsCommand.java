package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import com.rteenagers.parrot.commands.manager.UsernotesCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PointsCommand extends UsernotesCommand {
    @Override
    public String name() {
        return "points";
    }

    @Override
    public String info() {
        return "points [user]";
    }

    @Override
    public String permission() {
        return "points.lookup";
    }

    @Override
    public int argsCount() {
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) { // If no input is provided, the sender will be checked
            @SuppressWarnings("deprecation")
            OfflinePlayer op = Bukkit.getOfflinePlayer(sender.getName());
            UUID uuid = op.getUniqueId();

            try {
                DatabaseHandler.getPoints(String.valueOf(uuid), op.getName(), sender);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return;
        }

        String target = args[0];

        @SuppressWarnings("deprecation")
        OfflinePlayer op = Bukkit.getOfflinePlayer(target); // Deprecated but should work without worry of it being removed, please replace if there's a better way :^)
        UUID uuid = op.getUniqueId();
        try {
            DatabaseHandler.getPoints(String.valueOf(uuid), target, sender);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args) {
        if (args.length == 1) {
            ArrayList<String> arguments = new ArrayList<>();
            if (!args[0].isEmpty()) {
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
            return new ArrayList<>();
        }
        return null;
    }
}