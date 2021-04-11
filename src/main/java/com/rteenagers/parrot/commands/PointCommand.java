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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PointCommand extends UsernotesCommand {

    @Override
    public String getName() {
        return "point";
    }

    @Override
    public String getInfo() {
        return "/point [ban/mute/warnmute/warnban/ipban] [player] [amount] [reason]";
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

        // Check if correct punishment type is given
        String[] punishments = {"ban", "mute", "warnban", "warnmute", "ipban"};
        if (!Arrays.asList(punishments).contains(args[0])) {
            sender.sendMessage(ChatColor.RED + "Invalid syntax. Please enter " + ChatColor.DARK_RED + "[ban/mute/warnban/warnmute/ipban] " + ChatColor.RED + "as the first argument.");
            return;
        }

        if (!Utils.isInteger(args[2])) { // Check if point is valid
            sender.sendMessage(ChatColor.RED + "Invalid syntax. Please provide a valid number as your third argument.");
            return;
        }

        try {
            DatabaseHandler.addPoints(args, sender);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args) {
        ArrayList<String> arguments = new ArrayList<>();

        switch (args.length) {
            case 1:
                return Arrays.asList("ban", "mute", "warnban", "warnmute", "ipban");
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
            case 5:
                return Collections.singletonList("-s");
            default:
                return new ArrayList<>();
        }
    }
}
