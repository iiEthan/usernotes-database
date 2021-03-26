package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import com.rteenagers.parrot.commands.manager.UsernotesCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    public String getName() {
        return "points";
    }

    @Override
    public String getInfo() {
        return "points [user]";
    }

    @Override
    public String permission() {
        return "points.lookup.self";
    }

    @Override
    public int getArgsCount() {
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

        // Users require special perms to check other peoples points
        if (!sender.hasPermission("points.lookup.all")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        String target = args[0];

        @SuppressWarnings("deprecation")
        OfflinePlayer op = Bukkit.getOfflinePlayer(target); // Deprecated but should work without worry of it being removed, please replace if there's a better way to get someones UUID :^)
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

            for (Player p : Bukkit.getOnlinePlayers()) {
                arguments.add(p.getName());
            }
            Collections.sort(arguments);
            return arguments;
        } else {
            return new ArrayList<>();
        }
    }
}