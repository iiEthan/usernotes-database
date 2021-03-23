package com.rteenagers.parrot;

import org.apache.commons.dbcp.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DatabaseHandler {

    public static BasicDataSource dataSource;
    private static Connection connection;
    private static Statement statement;
    private static ResultSet rs;

    // Gets database pooling connection
    private static BasicDataSource getDataSource() {
        if (dataSource == null) {
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl("jdbc:postgresql://18.222.80.191:5432/tg_usernotes");
            ds.setUsername("tg_server");
            ds.setPassword("i!Lov3!c0ck!");

            // Prevents database from timing out. Not sure if all of these are necessary but I cba to test
            ds.setMinIdle(5);
            ds.setMaxIdle(10);
            ds.setMaxOpenPreparedStatements(100);
            ds.setTestOnBorrow(true);
            ds.setTestWhileIdle(true);
            ds.setTestOnReturn(true);
            ds.setValidationQuery("SELECT 1");
            dataSource = ds;
        }
        return dataSource;
    }

    // Connects to the database
    public static void openConnection() {
        BasicDataSource dataSource = DatabaseHandler.getDataSource();
        try {
            Class.forName("org.postgresql.Driver");
            connection = dataSource.getConnection();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Creates the tables if there aren't any already
    public static void createTables() throws SQLException {
        DatabaseHandler.openConnection();
        statement = connection.createStatement();

        try {
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
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            statement.close();
            connection.close();
        }
    }

    public static void getPoints(String uuid, String player, CommandSender sender) throws SQLException {
        DatabaseHandler.openConnection();
        statement = connection.createStatement();

    try {
        for (String punishmentType : new String[]{"ban", "mute"}) {

            // Display points
            rs = statement.executeQuery("SELECT * FROM " + punishmentType + "S WHERE uuid='" + uuid + "'");
            if (!rs.isBeforeFirst()) { // Testing to see if any results were retrieved
                sender.sendMessage(ChatColor.RED + "No " + punishmentType + " points were found for " + ChatColor.RED + player);
            } else {
                sender.sendMessage(ChatColor.RED + "\n" + punishmentType.substring(0,1).toUpperCase() + punishmentType.substring(1) + " Logs for " + ChatColor.DARK_AQUA + player + ChatColor.RED + ": ");

                while (rs.next()) {

                    int noteid = rs.getInt(punishmentType +"id");
                    int points = rs.getInt("points");
                    String reason = rs.getString("reason");
                    String mod = rs.getString("mod");
                    boolean warning = rs.getBoolean("warning");
                    boolean decayed = rs.getBoolean("decayed");
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("(MM/dd/yy)");
                    String date = simpleDateFormat.format(rs.getDate("date"));

                    String decayFormat = (decayed) ? ChatColor.STRIKETHROUGH + "" + ChatColor.RED + " DECAYED" : "";
                    String warningFormat = (warning) ? ChatColor.BOLD + "" + ChatColor.YELLOW + " (Warning)" : "";

                    sender.sendMessage(decayFormat +
                            ChatColor.GREEN + punishmentType.toUpperCase() + " ID #" + noteid + " " + date + ":\n" +
                            ChatColor.BLUE + "Infraction: " + ChatColor.DARK_AQUA + reason +
                            ChatColor.BLUE + "Points: " + ChatColor.DARK_AQUA + points + " " +
                            ChatColor.BLUE + "Mod: " + ChatColor.DARK_AQUA + mod + warningFormat + decayFormat
                    );
                }
            }
        }
    } catch (SQLException e) {
        sender.sendMessage("An error has occurred!");
        e.printStackTrace();
    } finally {
        rs.close();
        statement.close();
        connection.close();
    }

    connection.close();
    }

    public static void addPoints(String player, String punishmentType, String uuid, String mod, String reason, String points, Boolean warning, CommandSender sender) throws SQLException {
        DatabaseHandler.openConnection();
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        try {
            // First, we need to check if the users points have decayed. This will be easier for when we parse the dataset later.
            rs = statement.executeQuery("SELECT date FROM " + punishmentType + " WHERE uuid='" + uuid + "' AND decayed = false");

            if (rs.next()) { // Skip if there are no logs
                rs.last();

                // Converts and compares dates
                LocalDate then = LocalDate.parse(rs.getDate("date").toLocalDate().toString());
                LocalDate now = LocalDate.now();
                long daysBetween = ChronoUnit.DAYS.between(then, now);

                // If the previous punishment exceeds decay requirement, we will decay all of their current points
                if (daysBetween > Utils.decayValues.get(punishmentType)) {
                    statement.executeUpdate("UPDATE " + punishmentType +
                            " SET decayed = true" +
                            " WHERE uuid = '" + uuid + "' AND decayed = false");
                }
            }

            // Add points to db
            statement.executeUpdate("INSERT INTO " + punishmentType + " (uuid, points, reason, mod, date, decayed, warning) " +
                    "VALUES ('" + uuid + "', " + points + ", '" + reason + "', '" + mod + "', current_timestamp, false, " + warning + ")");

            sender.sendMessage(ChatColor.GREEN + "Player " + ChatColor.RED + player + ChatColor.GREEN + " has been given " + ChatColor.RED + points + ChatColor.GREEN + " point(s).");

            // Gives out the punishment -- should probably rework this monstrosity
            if (!warning) {
                if (Integer.parseInt(points) > 0) { // Do not ban people if they did not receive points

                    // Gets the users total current points
                    int total = 0;
                    while (rs.next()) {
                        total += rs.getInt("points");
                    }

                    if (punishmentType.equals("bans")) {
                        rs = statement.executeQuery("SELECT points FROM bans WHERE uuid='" + uuid + "' AND decayed = false");

                        // Applies the proper ban punishment to the user
                        String command;
                        if (total > 9) { // Permanent bans are special cases
                            command = "ban " + player + " " + reason;
                        } else { // Temp bans
                            command = "tempban " + player + " " + Utils.banValues.get(total) + " " + reason;
                        }
                        Bukkit.dispatchCommand(sender, command);

                    } else if (punishmentType.equals("mutes")) {
                        rs = statement.executeQuery("SELECT points FROM mutes WHERE uuid='" + uuid + "' AND decayed = false");

                        // Applies the proper mute punishment to the user
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
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage("An error has occurred!");
        } finally {
            rs.close();
            statement.close();
            connection.close();
        }
    }

    public static void removePoints(String punishmentType, String id, CommandSender sender) throws SQLException {
        DatabaseHandler.openConnection();
        statement = connection.createStatement();

        try {
            rs = statement.executeQuery("SELECT " + punishmentType + "id FROM " + punishmentType + "s WHERE " + punishmentType + "id =" + Integer.parseInt(id));

            if (!rs.isBeforeFirst()) { // Check if ID exists
                sender.sendMessage(ChatColor.RED + "ID #" + id + " not found.");
            } else { // Removes the column from ID
                statement.executeUpdate("DELETE FROM " + punishmentType + "s WHERE " + punishmentType + "id='" + id + "'");
                sender.sendMessage(ChatColor.GREEN + "Removed " + punishmentType + " ID #" + id + " from database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage("An error has occurred!");
        } finally {
            rs.close();
            statement.close();
            connection.close();
        }
    }

    public static void pointLookup(String punishmentType, String id, CommandSender sender) throws SQLException {
        DatabaseHandler.openConnection();
        statement = connection.createStatement();

        try {
            rs = statement.executeQuery("SELECT * FROM " + punishmentType + "s WHERE " + punishmentType + "id='" + id + "'");

            if (rs.next()) { // Check if point exists and goes to it
                // Retrieves point information
                int noteid = rs.getInt(punishmentType + "id");
                int points = rs.getInt("points");
                String reason = rs.getString("reason");
                String mod = rs.getString("mod");
                 boolean warning = rs.getBoolean("warning");
                boolean decayed = rs.getBoolean("decayed");
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("(MM/dd/yy)");
                String date = simpleDateFormat.format(rs.getDate("date"));

                //  If the point has decayed or is a warning, it will be added to the end of the line
                String decayFormat = (decayed) ? ChatColor.STRIKETHROUGH + "" + ChatColor.RED + " DECAYED" : "";
                String warningFormat = (warning) ? ChatColor.BOLD + "" + ChatColor.YELLOW + " (Warning)" : "";

                sender.sendMessage(
                                ChatColor.GREEN + punishmentType.toUpperCase() + " ID #" + noteid + " " + date + ": \n" +
                                ChatColor.BLUE + "Infraction: " + ChatColor.DARK_AQUA + reason +
                                ChatColor.BLUE + "Points: " + ChatColor.DARK_AQUA + points + " " +
                                ChatColor.BLUE + "Mod: " + ChatColor.DARK_AQUA + mod + warningFormat + decayFormat);
            } else {
                sender.sendMessage(ChatColor.RED + "ID #" + id + " not found.");
            }
        } catch (SQLException e) {
                e.printStackTrace();
                sender.sendMessage("An error has occurred!");
        } finally {
            rs.close();
            statement.close();
            connection.close();
        }
    }
}