package com.rteenagers.parrot.commands;

import com.rteenagers.parrot.DatabaseHandler;
import com.rteenagers.parrot.commands.manager.UsernotesCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BanLeaderboardCommand extends UsernotesCommand {

    @Override
    public String name() {
        return "banleaderboard";
    }

    @Override
    public String info() {
        return "/banleaderboard";
    }

    @Override
    public String permission() {
        return "points.leaderboard";
    }

    @Override
    public int argsCount() {
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            DatabaseHandler.banLeaderboard(sender);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args) {
        // No subcommands, we do not want any tab completion
        return new ArrayList<>();
    }
}