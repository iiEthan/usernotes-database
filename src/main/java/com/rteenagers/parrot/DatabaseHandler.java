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
        for (String punishmentType : new String[]{"mute", "ban"}) {

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

    public static void addPoints(String[] args, CommandSender sender) throws SQLException {
        DatabaseHandler.openConnection();
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        // Parses reason into its own string
        StringBuilder reason = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }

        @SuppressWarnings("deprecation")
        String uuid = String.valueOf(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
        String punishmentDB = args[0].contains("ban") ? "bans" : "mutes";
        String player = args[1];
        String points = args[2];
        String mod = sender.getName();

        try {
            // First, we need to check if the users points have decayed. This will be easier for when we parse the dataset later.
            rs = statement.executeQuery("SELECT date FROM " + punishmentDB + " WHERE uuid='" + uuid + "' AND decayed = false");

            if (rs.next()) { // Make sure there are logs
                rs.last();

                // Converts and compares dates
                LocalDate then = LocalDate.parse(rs.getDate("date").toLocalDate().toString());
                LocalDate now = LocalDate.now();
                long daysBetween = ChronoUnit.DAYS.between(then, now);

                // If the previous punishment exceeds decay requirement, we will decay all of their current points
                if (daysBetween > Utils.decayValues.get(punishmentDB)) {
                    statement.executeUpdate("UPDATE " + punishmentDB +
                            " SET decayed = true" +
                            " WHERE uuid = '" + uuid + "' AND decayed = false");
                }
            }

            // Remove "-s" from reason to make points look nicer
            String finalReason = String.valueOf(reason);
            if (finalReason.contains("-s")) {
                finalReason = finalReason.replace(" -s", "");
            }

            // Add points to db
            statement.executeUpdate("INSERT INTO " + punishmentDB + " (uuid, points, reason, mod, date, decayed, warning) " +
                    "VALUES ('" + uuid + "', " + points + ", '" + finalReason + "', '" + mod + "', current_timestamp, false, " + args[0].startsWith("warn") + ")");

            sender.sendMessage(ChatColor.GREEN + "Player " + ChatColor.RED + player + ChatColor.GREEN + " has been given " + ChatColor.RED + points + ChatColor.GREEN + " point(s).");

            // Gives out the punishment -- should probably rework this monstrosity
            // Check if punishment is a warning
            if (!args[0].startsWith("warn")) {
                if (Integer.parseInt(points) > 0) { // Do not ban people if they did not receive points

                    // Gets the users total current points
                    int total = 0;
                    rs = statement.executeQuery("SELECT points FROM " + punishmentDB + " WHERE uuid='" + uuid + "' AND decayed = false");
                    while (rs.next()) {
                        total += rs.getInt("points");
                    }

                    // Don't try to ban users with no points
                    if (total < 1) {
                        return;
                    }

                    if (punishmentDB.equals("bans")) {
                        // Applies the proper ban punishment to the user
                        String command;
                        if (args[0].equals("ipban")) {
                            command = "banip " + player + " " + reason;
                        } else if (total > 9) { // Permanent bans are special cases
                            command = "ban " + player + " " + reason;
                        } else { // Temp bans
                            command = "tempban " + player + " " + Utils.banValues.get(total) + " " + reason;
                        }
                        Bukkit.dispatchCommand(sender, command);

                    } else if (punishmentDB.equals("mutes")) {
                        // Applies the proper mute punishment to the user
                        String command;
                        if (total < 5) { // Tempmute, 1-4 points
                             command = "tempmute " + player + " " + Utils.muteValues.get(total) + " " + reason;
                        } else if (total > 7) { // Must perform two punishments here, tempban + perma mute
                            String commandBan = "tempban " + player + " 7d " + reason;
                            Bukkit.dispatchCommand(sender, commandBan);
                            command = "mute " + player + " " + reason;
                        } else { // Tempban, 5-7 points
                            command = "tempban " + player + " " + Utils.muteValues.get(total) + " " + reason;
                        }
                        Bukkit.dispatchCommand(sender, command);
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