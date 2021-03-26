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
import java.util.*;

public class PointCommand extends UsernotesCommand {

    @Override
    public String getName() {
        return "point";
    }

    @Override
    public String getInfo() {
        return "/point [ban/mute/warn] [player] [amount] [reason]";
    }

    @Override
    public String permission() {
        return "points.edit";
    }

    @Override
    public int getArgsCount() {
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
                return Arrays.asList("ban", "mute", "warnban", "warnmute");
            case 2:
                for (Player p : Bukkit.getOnlinePlayers()) {
                        arguments.add(p.getName());
                    }
                Collections.sort(arguments);
                return arguments;
            case 3:
                for (int i = 0; i < 10; i++) {
                    arguments.add(String.valueOf(i));
                }
                return arguments;
            case 4:
                return Collections.singletonList("reason");
            default:
                return new ArrayList<>();
        }
    }
}
