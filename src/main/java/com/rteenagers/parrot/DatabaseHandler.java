package com.rteenagers.parrot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DatabaseHandler {

    static Connection connection;
    static Statement statement;
    // ENTER DB INFO BELOW IN THE FORMAT: jdbc:language://host:port/db?user=username&password=password
    public static String connectionURL = "jdbc:postgresql://usernotes.ctlynjuzcvj9.us-east-1.rds.amazonaws.com:5432/postgres?user=root&password=WVZWLFup4OHPzbKHLg0T";

    public static void openConnection() throws SQLException, ClassNotFoundException {

        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(DatabaseHandler.connectionURL);
        try {
            createTables();
        } catch (PSQLException e) {
            e.printStackTrace();
        }
    }

    // Creates the tables if there aren't any already (may need to remove foreign keys first then re-add them)
    private static void createTables() throws SQLException {
        statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS bans " +
                "(banID SERIAL PRIMARY KEY," +
                "uuid VARCHAR (50)," +
                "points INT," +
                "reason VARCHAR (255)," +
                "mod VARCHAR (50)," +
                "warning BOOLEAN," +
                "decayed BOOLEAN," +
                "date TIMESTAMP)"
        );
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS mutes " +
                "(muteID SERIAL PRIMARY KEY," +
                "uuid VARCHAR (50)," +
                "points INT," +
                "reason VARCHAR (255)," +
                "mod VARCHAR (50)," +
                "warning BOOLEAN," +
                "decayed BOOLEAN," +
                "date TIMESTAMP)"
        );

        statement.close();
        System.out.println("Created new tables");
    }

    public static void getPoints(String uuid, String player, CommandSender sender) throws SQLException {
        statement = connection.createStatement();

        ResultSet rs = statement.executeQuery("SELECT * FROM bans WHERE uuid='" + uuid + "'");
        if (rs.next()) {
            while (rs.next()) {
                sender.sendMessage(rs.getString("reason"));
            }
        } else {
            sender.sendMessage(ChatColor.RED + "No points were found for " + ChatColor.RED + player);
        }
        statement.close();
    }

    public static void addPoints(String player, String punishmentType, String uuid, String mod, String reason, String points, Boolean warning, CommandSender sender) throws SQLException {
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        // First, we need to check if the users points have decayed. This will be easier for when we parse the dataset later.
        ResultSet rs = statement.executeQuery("SELECT date FROM " + punishmentType + " WHERE uuid='" + uuid + "' AND decayed = false");

        if (rs.next()) { // Skip if there are no logs
            rs.last();

            // Converts and compares dates
            LocalDate then = LocalDate.parse(rs.getDate("date").toLocalDate().toString());//, formatter);
            LocalDate now = LocalDate.now();
            long daysBetween = ChronoUnit.DAYS.between(then, now);

            // If the previous punishment exceeds decay requirement, we will decay all of their points
            if (daysBetween > Utils.decayValues.get(punishmentType)) {
                statement.executeUpdate("UPDATE " + punishmentType +
                        " SET decayed = true" +
                        " WHERE uuid = '" + uuid + "' AND decayed = false");
            }
        }

        // Add points to db
        statement.executeUpdate("INSERT INTO " + punishmentType + " (uuid, points, reason, mod, date, decayed, warning) " +
                "VALUES ('" + uuid + "', " + points + ", '" + reason + "', '" + mod + "', current_timestamp, false, " + warning + ")");

        sender.sendMessage(ChatColor.GREEN + "Player " + ChatColor.RED + player + ChatColor.GREEN + " has been given " + ChatColor.RED + points + ChatColor.GREEN + " points.");

        // TODO: Remove points command, complete addPoints command

        // Gives out the punishment
        if (!warning) {
            if (Integer.parseInt(points) > 0) { // Do not ban people if they did not receive points
                if (punishmentType.equals("bans")) {
                    rs = statement.executeQuery("SELECT points FROM bans WHERE uuid='" + uuid + "' AND decayed = false");

                    int total = 0;
                    while (rs.next()) {
                        total += rs.getInt("points");
                        }

                    String command;
                    if (total > 9) { // Permanent bans are special cases
                        command = "ban " + player + " " + reason;
                    } else { // Temp bans
                        command = "tempban " + player + " " + Utils.banValues.get(total) + " " + reason;
                    }
                    Bukkit.dispatchCommand(sender, command);

                } else if (punishmentType.equals("mutes")) {
                    rs = statement.executeQuery("SELECT points FROM mutes WHERE uuid='" + uuid + "' AND decayed = false");

                    int total = 0;
                    while (rs.next()) {
                        total += rs.getInt("points");
                    }

                     // Punish the user with the appropriate mute point
                    if (total < 5) { // Tempmute
                        String command = "tempmute " + player + " " + Utils.muteValues.get(total) + " " + reason;
                        Bukkit.dispatchCommand(sender, command);
                    } else if (total > 7) { // Tempban + perma mute
                        String command = "tempban " + player + " 7d " + reason;
                        Bukkit.dispatchCommand(sender, command);
                        command = "mute " + player + " " + reason;
                        Bukkit.dispatchCommand(sender, command);
                    } else { // Tempban
                        String command = "tempban " + player + " " + Utils.muteValues.get(total) + " " + reason;
                        Bukkit.dispatchCommand(sender, command);
                    }
                }
            }
        }
        statement.close();
    }
}
        /*sender.sendMessage(ChatColor.RED + "Mute Logs for " + ChatColor.DARK_AQUA + player + ChatColor.RED + ":");

                sender.sendMessage(ChatColor.GREEN + row.get(7).toString() + ": " +                                 // Date
                        ChatColor.BLUE + "Infraction: " + ChatColor.DARK_AQUA + row.get(1).toString() + " " +       // Infraction
                        ChatColor.BLUE + "Action: " + ChatColor.DARK_AQUA + row.get(6).toString() + " " +           // Action
                        ChatColor.BLUE + "Mod: " + ChatColor.DARK_AQUA + row.get(5).toString() + " ");              // Moderator

        sender.sendMessage(ChatColor.RED + "Ban Logs for " + ChatColor.DARK_AQUA + player + ChatColor.RED + ":");
        // Gets ban logs from spreadsheet
        for (List row : banValues) {
            String userCheck = row.get(0).toString().toLowerCase();
            if (userCheck.equals(player.toLowerCase())) {

                sender.sendMessage(ChatColor.GREEN + row.get(7).toString() + ": " +                                 // Date
                        ChatColor.BLUE + "Infraction: " + ChatColor.DARK_AQUA + row.get(1).toString() + " " +       // Infraction
                        ChatColor.BLUE + "Action: " + ChatColor.DARK_AQUA + row.get(6).toString() + " " +           // Action
                        ChatColor.BLUE + "Mod: " + ChatColor.DARK_AQUA + row.get(5).toString() + " ");              // Moderator

        sender.sendMessage(ChatColor.BLUE + "Latest mute value: " + ChatColor.DARK_AQUA + latestMutePoints);
        sender.sendMessage(ChatColor.BLUE + "Latest ban value: " + ChatColor.DARK_AQUA + latestBanPoints);
    }*/