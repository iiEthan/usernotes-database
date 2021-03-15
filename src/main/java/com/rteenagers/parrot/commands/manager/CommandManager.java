package com.rteenagers.parrot.commands.manager;

import com.rteenagers.parrot.Usernotes;
import com.rteenagers.parrot.commands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class CommandManager implements TabExecutor {
    private final Usernotes plugin = Usernotes.getInstance();
    private final ArrayList<UsernotesCommand> commands = new ArrayList<>();

    public void setup() {
        plugin.getCommand("pointlookup").setExecutor(this);
        plugin.getCommand("point").setExecutor(this);
        plugin.getCommand("points").setExecutor(this);
        plugin.getCommand("removepoint").setExecutor(this);
        plugin.getCommand("banleaderboard").setExecutor(this);

        this.commands.add(new PointLookupCommand());
        this.commands.add(new PointCommand());
        this.commands.add(new PointsCommand());
        this.commands.add(new RemovePointCommand());
        this.commands.add(new BanLeaderboardCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        UsernotesCommand usernotesCommand = getCommand(command.getName());
        assert usernotesCommand != null;

        // Check if user has permission to send command
        if (!sender.hasPermission(usernotesCommand.permission())) {
            sender.sendMessage(ChatColor.RED + "You are not permitted to do this!");
            return true;
        }

        // Makes sure that there is enough subcommands supplied
        if (args.length < usernotesCommand.argsCount()) {
            sender.sendMessage(ChatColor.RED + "Please provide more arguments! Usage is " + usernotesCommand.info());
            return true;
        }

        usernotesCommand.execute(sender, args);

        return true;
    }

    // Gets command from input. We cannot use command.getName() and whatnot because we are using a custom UsernotesCommand class
    private UsernotesCommand getCommand(String name) {
        for (UsernotesCommand uc : this.commands) {
            if (uc.name().equalsIgnoreCase(name)) {
                return uc;
            }
        }
        return null;
    }

    // how the fuck is this even working i'm just shocked
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        UsernotesCommand usernotesCommand = getCommand(command.getName());
        return usernotesCommand.onTabComplete(sender, command, command.getLabel(), alias, args);
    }
}

