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
        } catch (PSQLException ignored) {
        }
    }

    // Creates the tables if there aren't any already (may need to remove foreign keys first then re-add them)
    private static void createTables() throws SQLException {
        statement = connection.createStatement();
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS users " +
                "(uuid VARCHAR (36) PRIMARY KEY NOT NULL," +
                "banID INT," +
                "muteID INT)"
        );
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS bans " +
                "(banID INT PRIMARY KEY NOT NULL," +
                "uuid VARCHAR (50)," +
                "date TIMESTAMP," +
                "decayed BOOLEAN," +
                "mod VARCHAR (50)," +
                "reason VARCHAR (255))"
        );
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS mutes " +
                "(muteID INT PRIMARY KEY NOT NULL," +
                "uuid VARCHAR (50)," +
                "date TIMESTAMP," +
                "decayed BOOLEAN," +
                "mod VARCHAR (50)," +
                "reason VARCHAR (255))"
        );

        statement.executeUpdate("ALTER TABLE bans ADD CONSTRAINT bans_uuid_fk FOREIGN KEY (uuid) REFERENCES users (uuid)");
        statement.executeUpdate("ALTER TABLE mutes ADD CONSTRAINT mutes_uuid_fk FOREIGN KEY (uuid) REFERENCES users (uuid)");
        statement.executeUpdate("ALTER TABLE users ADD CONSTRAINT uuid_bans_fk FOREIGN KEY (banID) REFERENCES bans (banID)");
        statement.executeUpdate("ALTER TABLE users ADD CONSTRAINT uuid_mutes_fk FOREIGN KEY (muteID) REFERENCES mutes (muteID)");

        statement.close();
        System.out.println("Created new tables");
    }

    public static void getNotes(String uuid) {
        // TODO: CRY A LOT
        System.out.println(uuid);
    }

    public static void addNotes(String uuid) throws SQLException {
        statement = connection.createStatement();

        // Add user in table if not already in it
        statement.executeUpdate( "INSERT INTO users (uuid) " +
                "SELECT '" + uuid + "' WHERE NOT EXISTS (" +
                "select * FROM users WHERE uuid='" + uuid + "');");

        statement.close();
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


