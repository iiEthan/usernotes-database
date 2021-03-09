package com.rteenagers.parrot;

import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHandler {

    static Connection connection;

    public static void openConnection(String host, String port, String database, String username, String password) throws SQLException,
            ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection("jdbc:postgresql://"
                        + host+ ":" + port + "/" + database,
                username, password);
    }

    public static void main(CommandSender sender, String... args) throws IOException {
    //temporary
        sender.sendMessage("it works woohoo");
    }
}
        /*String latestMutePoints = "0";
        String latestBanPoints = "0";

        sender.sendMessage(ChatColor.RED + "Mute Logs for " + ChatColor.DARK_AQUA + player + ChatColor.RED + ":");
        // Gets mute logs from spreadsheet
        for (List row : muteValues) {
            String userCheck = row.get(0).toString().toLowerCase();
            if (userCheck.equals(player.toLowerCase())) {

                if (NumberUtils.isNumber(row.get(3).toString())) { // Checks if latest mute point is a number (removes warnings/removed)
                    latestMutePoints = row.get(3).toString();
                }

                sender.sendMessage(ChatColor.GREEN + row.get(7).toString() + ": " +                                 // Date
                        ChatColor.BLUE + "Infraction: " + ChatColor.DARK_AQUA + row.get(1).toString() + " " +       // Infraction
                        ChatColor.BLUE + "Action: " + ChatColor.DARK_AQUA + row.get(6).toString() + " " +           // Action
                        ChatColor.BLUE + "Mod: " + ChatColor.DARK_AQUA + row.get(5).toString() + " ");              // Moderator
            }
        }

        sender.sendMessage(ChatColor.RED + "Ban Logs for " + ChatColor.DARK_AQUA + player + ChatColor.RED + ":");
        // Gets ban logs from spreadsheet
        for (List row : banValues) {
            String userCheck = row.get(0).toString().toLowerCase();
            if (userCheck.equals(player.toLowerCase())) {

                if (NumberUtils.isNumber(row.get(3).toString())) { // Checks if latest ban point is a number (removes warnings/removed)
                    latestBanPoints = row.get(3).toString();
                }

                sender.sendMessage(ChatColor.GREEN + row.get(7).toString() + ": " +                                 // Date
                        ChatColor.BLUE + "Infraction: " + ChatColor.DARK_AQUA + row.get(1).toString() + " " +       // Infraction
                        ChatColor.BLUE + "Action: " + ChatColor.DARK_AQUA + row.get(6).toString() + " " +           // Action
                        ChatColor.BLUE + "Mod: " + ChatColor.DARK_AQUA + row.get(5).toString() + " ");              // Moderator
            }
        }

        sender.sendMessage(ChatColor.BLUE + "Latest mute value: " + ChatColor.DARK_AQUA + latestMutePoints);
        sender.sendMessage(ChatColor.BLUE + "Latest ban value: " + ChatColor.DARK_AQUA + latestBanPoints);
    }*/


