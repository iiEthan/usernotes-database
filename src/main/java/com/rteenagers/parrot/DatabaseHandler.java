package com.rteenagers.parrot;

import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
                "points VARCHAR (50)," +
                "reason VARCHAR (255)," +
                "mod VARCHAR (50)," +
                "warning BOOLEAN," +
                "decayed BOOLEAN," +
                "date TIMESTAMP)"
        );
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS mutes " +
                "(muteID SERIAL PRIMARY KEY," +
                "uuid VARCHAR (50)," +
                "points VARCHAR (50)," +
                "reason VARCHAR (255)," +
                "mod VARCHAR (50)," +
                "warning BOOLEAN," +
                "decayed BOOLEAN," +
                "date TIMESTAMP)"
        );

        statement.close();
        System.out.println("Created new tables");
    }

    public static void getNotes(String uuid) {
        // TODO: CRY A LOT
        System.out.println(uuid);
    }

    public static void addPoint(String punishmentType, String uuid, String mod, String reason, String points, Boolean warning) throws SQLException {
        statement = connection.createStatement();

        // Add ban to db
        statement.executeUpdate("INSERT INTO " + punishmentType + " (uuid, points, reason, mod, date, decayed, warning) " +
        "VALUES ('" + uuid + "', '" + points + "', '" + reason + "', '"  + mod + "', current_timestamp, false, " + warning + ")");

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