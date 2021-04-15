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
        return "/point [ban/mute/ipban] [player] [amount] [reason]";
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
        String[] punishments = {"ban", "mute", "ipban"};
        if (!Arrays.asList(punishments).contains(args[0])) {
            sender.sendMessage(ChatColor.RED + "Invalid syntax. Please enter " + ChatColor.DARK_RED + "[ban/mute/ipban] " + ChatColor.RED + "as the first argument.");
            return;
        }

        if (!Utils.isInteger(args[2])) { // Check if point is valid
            sender.sendMessage(ChatColor.RED + "Invalid syntax. Please provide a valid number as your third argument.");
            return;
        }
        // Parses reason into its own string
        StringBuilder reason = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }
        String player = args[1];
        String punishmentDB = args[0].contains("ban") ? "bans" : "mutes";

        DatabaseHandler.addPoints(args, sender, reason, rs -> {
            try {

                // Gets the users total current points
                int points = 0;
                while (rs.next()) {
                    points += rs.getInt("points");
                }
                // Don't try to ban users with no points
                if (points < 1) {
                    sender.sendMessage(ChatColor.RED + "User has less than 1 point. No punishment will be given!");
                    return;
                }

                // Gives out the punishment -- should probably rework this monstrosity
                if (!reason.toString().contains(" -w")) {
                    String command = null;
                    if (punishmentDB.equals("bans")) {
                        // Applies the proper ban punishment to the user
                        if (args[0].equals("ipban")) {
                            command = "banip " + player + " " + reason;
                        } else if (points > 9) { // Permanent bans are special cases
                            command = "ban " + player + " " + reason;
                        } else { // Temp bans
                            command = "tempban " + player + " " + Utils.banValues.get(points) + " " + reason;
                        }

                    } else if (punishmentDB.equals("mutes")) {
                        // Applies the proper mute punishment to the user
                        if (points < 5) { // Tempmute, 1-4 points
                            command = "tempmute " + player + " " + Utils.muteValues.get(points) + " " + reason;
                        } else if (points > 7) { // Must perform two punishments here, tempban + perma mute
                            String commandBan = "tempban " + player + " 7d " + reason;
                            Bukkit.dispatchCommand(sender, commandBan);
                            command = "mute " + player + " " + reason;
                        } else { // Tempban, 5-7 points
                            command = "tempban " + player + " " + Utils.muteValues.get(points) + " " + reason;
                        }
                    }
                    // Remove -w and -f flags from final reasoning
                    String[] flagOptions = {"-f", "-w"};
                    for (String flag : flagOptions) {
                        if (command.contains(" " + flag)) {
                            command = command.replace(" " + flag, "");
                        }
                    }
                    Bukkit.dispatchCommand(sender, command);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                DatabaseHandler.rs.close();
                DatabaseHandler.statement.close();
                DatabaseHandler.connection.close();
            }
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String alias, String[] args) {
        ArrayList<String> arguments = new ArrayList<>();

        switch (args.length) {
            case 1:
                return Arrays.asList("ban", "mute", "ipban");
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
                return Collections.singletonList("<reason>");
            default:
                return Arrays.asList("-s", "-f", "-w");
        }
    }
}
