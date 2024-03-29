package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import com.rteenagers.parrot.Utils;
import com.rteenagers.parrot.commands.manager.UsernotesCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PointLookupCommand extends UsernotesCommand {

    @Override
    public String getName() {
        return "pointlookup";
    }

    @Override
    public String getInfo() {
        return "/pointlookup [ban/mute] [id]";
    }

    @Override
    public String permission() {
        return "points.lookup";
    }

    public int getArgsCount() {
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

        DatabaseHandler.pointLookup(args[0], args[1], sender);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args) {
        return switch (args.length) {
            case 1 -> Arrays.asList("ban", "mute");
            case 2 -> Collections.singletonList("noteid");
            default -> new ArrayList<>();
        };
    }
}
