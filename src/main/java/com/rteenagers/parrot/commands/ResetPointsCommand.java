package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import com.rteenagers.parrot.commands.manager.UsernotesCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResetPointsCommand extends UsernotesCommand {
    @Override
    public String getName() {
        return "resetpoints";
    }

    @Override
    public String getInfo() {
        return "/resetpoints confirm";
    }

    @Override
    public String permission() {
        return "points.admin";
    }

    @Override
    public int getArgsCount() {
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args) throws SQLException, ClassNotFoundException {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "WARNING: Proceeding will clear the entire usernotes database. Please type /reload confirm to confirm this command.");
            return;
        }

        DatabaseHandler.resetTables();
        sender.sendMessage(ChatColor.RED + "Points have been reset. All hail the great iiEthan.");

    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args) {
        return new ArrayList<>();
    }
}
