package com.rteenagers.parrot;

import litebans.api.Database;
import org.apache.commons.dbcp.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class DatabaseHandler {

    public static BasicDataSource dataSource;
    public static Connection connection;
    public static Statement statement;
    public static PreparedStatement preparedStatement;
    public static ResultSet rs;

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
    public static void openConnection() throws ClassNotFoundException, SQLException {
        BasicDataSource dataSource = DatabaseHandler.getDataSource();
            Class.forName("org.postgresql.Driver");
            connection = dataSource.getConnection();
    }

    // Creates the tables if there aren't any already
    public static void createTables() throws SQLException, ClassNotFoundException {
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

    public static void resetTables() throws SQLException, ClassNotFoundException {
        DatabaseHandler.openConnection();
        statement = connection.createStatement();

        try {
            statement.executeUpdate("DROP TABLE bans");
            statement.executeUpdate("DROP TABLE mutes");

            DatabaseHandler.createTables();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            statement.close();
            connection.close();
        }
    }

    public static void getPoints(String uuid, String player, CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(Usernotes.getInstance(), () -> {
            try {
                DatabaseHandler.openConnection();
                statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                for (String punishmentType : new String[]{"mute", "ban"}) {

                    // Checks decay
                    rs = statement.executeQuery("SELECT date FROM " + punishmentType + "s WHERE uuid='" + uuid + "' AND decayed = false");
                    checkDecay(rs, punishmentType + "s", uuid);

                    // Display points
                    rs = statement.executeQuery("SELECT * FROM " + punishmentType + "S WHERE uuid='" + uuid + "'");
                    if (!rs.isBeforeFirst()) { // Testing to see if any results were retrieved
                        sender.sendMessage(ChatColor.RED + "No " + punishmentType + " points were found for " + ChatColor.RED + player);
                    } else {
                        sender.sendMessage(ChatColor.RED + "\n" + punishmentType.substring(0, 1).toUpperCase() + punishmentType.substring(1) + " Logs for " + ChatColor.DARK_AQUA + player + ChatColor.RED + ": ");

                        while (rs.next()) {

                            int noteid = rs.getInt(punishmentType + "id");
                            int points = rs.getInt("points");
                            String reason = rs.getString("reason");
                            String mod = rs.getString("mod");
                            boolean warning = rs.getBoolean("warning");
                            boolean decayed = rs.getBoolean("decayed");
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("(MM/dd/yy)");
                            String date = simpleDateFormat.format(rs.getDate("date"));

                            String decayFormat = (decayed) ? ChatColor.STRIKETHROUGH + "" + ChatColor.RED + " DECAYED" : "";
                            String warningFormat = (warning) ? ChatColor.BOLD + "" + ChatColor.YELLOW + " (Warning)" : "";

                            sender.sendMessage(
                                    ChatColor.GREEN + punishmentType.toUpperCase() + " ID #" + noteid + " " + date + ":\n" +
                                    ChatColor.BLUE + "Infraction: " + ChatColor.DARK_AQUA + reason +
                                    ChatColor.BLUE + "Points: " + ChatColor.DARK_AQUA + points + " " +
                                    ChatColor.BLUE + "Mod: " + ChatColor.DARK_AQUA + mod + warningFormat + decayFormat
                            );
                        }
                    }
                }
            } catch (SQLException | ClassNotFoundException e) {
                sender.sendMessage("An error has occurred!");
                e.printStackTrace();
            } finally {
                try {
                    rs.close();
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void addPoints(String[] args, CommandSender sender, StringBuilder reason, Utils.FindOneCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(Usernotes.getInstance(), () -> {
            try {
                DatabaseHandler.openConnection();
                statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                @SuppressWarnings("deprecation")
                String uuid = String.valueOf(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
                String punishmentDB = args[0].contains("ban") ? "bans" : "mutes";
                String points = args[2];
                String mod = sender.getName();

                // check if users already are being punished to prevent double pointing
                // -f tag will ignore this check
                if (!reason.toString().contains(" -f")) {
                    if (punishmentDB.equals("bans")) {
                        if (Database.get().isPlayerBanned(UUID.fromString(uuid), null)) {
                            sender.sendMessage(ChatColor.RED + "User is already currently serving a ban punishment. Use the -f tag to force the punishment anyway.");
                            return;
                        } else {
                            if (Database.get().isPlayerMuted(UUID.fromString(uuid), null)) {
                                sender.sendMessage(ChatColor.RED + "User is already currently serving a mute punishment. Use the -f tag to force the punishment anyway.");
                                return;
                            }
                        }
                    }
                }

                // First, we need to check if the users points have decayed. This will be easier for when we parse the dataset later.
                rs = statement.executeQuery("SELECT date FROM " + punishmentDB + " WHERE uuid='" + uuid + "' AND decayed = false");
                checkDecay(rs, punishmentDB, uuid);

                // Remove flags from final reasoning
                String[] flagOptions = {"-f", "-s", "-w"};
                String finalReason = String.valueOf(reason);
                for (String flag : flagOptions) {
                    if (finalReason.contains(" " + flag)) {
                        finalReason = finalReason.replace(" " + flag, "");
                    }
                }

                // Add points to db
                String sql = "INSERT INTO " + punishmentDB + " (uuid, points, reason, mod, date, decayed, warning) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                preparedStatement.setString(1, uuid);
                preparedStatement.setInt(2, Integer.parseInt(points));
                preparedStatement.setString(3, finalReason);
                preparedStatement.setString(4, mod);
                preparedStatement.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                preparedStatement.setBoolean(6, false);
                preparedStatement.setBoolean(7, reason.toString().contains(" -w"));

                preparedStatement.executeUpdate();


                sender.sendMessage(ChatColor.GREEN + "Player " + ChatColor.RED + args[1] + ChatColor.GREEN + " has been given " + ChatColor.RED + points + ChatColor.GREEN + " point(s).");

                rs = statement.executeQuery("SELECT points FROM " + punishmentDB + " WHERE uuid='" + uuid + "' AND decayed = false");

                Bukkit.getScheduler().runTask(Usernotes.getInstance(), () -> {
                    try {
                        callback.onQueryDone(rs);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                try {
                    rs.close();
                    statement.close();
                    preparedStatement.close();
                    connection.close();
                } catch (SQLException f) {
                    f.printStackTrace();
                }
                sender.sendMessage("An error has occurred!");
            }
        });
    }

    public static void removePoints(String punishmentType, String id, CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(Usernotes.getInstance(), () -> {
            try {
                DatabaseHandler.openConnection();
                statement = connection.createStatement();
                rs = statement.executeQuery("SELECT " + punishmentType + "id FROM " + punishmentType + "s WHERE " + punishmentType + "id =" + Integer.parseInt(id));

            if (!rs.isBeforeFirst()) { // Check if ID exists
                sender.sendMessage(ChatColor.RED + "ID #" + id + " not found.");
            } else { // Removes the column from ID
                statement.executeUpdate("DELETE FROM " + punishmentType + "s WHERE " + punishmentType + "id='" + id + "'");
                sender.sendMessage(ChatColor.GREEN + "Removed " + punishmentType + " ID #" + id + " from database.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            sender.sendMessage("An error has occurred!");
        } finally {
                try {
                    rs.close();
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void pointLookup(String punishmentType, String id, CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(Usernotes.getInstance(), () -> {
            try {
                DatabaseHandler.openConnection();
                statement = connection.createStatement();

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
                    String username = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("uuid"))).getName();

                    //  If the point has decayed or is a warning, it will be added to the end of the line
                    String decayFormat = (decayed) ? ChatColor.STRIKETHROUGH + "" + ChatColor.RED + " DECAYED" : "";
                    String warningFormat = (warning) ? ChatColor.BOLD + "" + ChatColor.YELLOW + " (Warning)" : "";

                    sender.sendMessage(
                            ChatColor.GREEN + punishmentType.toUpperCase() + " ID #" + noteid + " " + date + ": \n" +
                                    ChatColor.BLUE + "Name: " + ChatColor.DARK_AQUA + username +
                                    ChatColor.BLUE + " Infraction: " + ChatColor.DARK_AQUA + reason +
                                    ChatColor.BLUE + "Points: " + ChatColor.DARK_AQUA + points +
                                    ChatColor.BLUE + " Mod: " + ChatColor.DARK_AQUA + mod + warningFormat + decayFormat);
                } else {
                    sender.sendMessage(ChatColor.RED + "ID #" + id + " not found.");
                }

            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                sender.sendMessage("An error has occurred!");
            } finally {
                try {
                    rs.close();
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void checkDecay(ResultSet rs, String punishmentDB, String uuid) {
        try {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void getPointList(String punishmentType, String pageNumber, CommandSender sender) {
        Bukkit.getScheduler().runTaskAsynchronously(Usernotes.getInstance(), () -> {
            try {
                DatabaseHandler.openConnection();
                statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

                int offset = (Integer.parseInt(pageNumber) - 1) * 5; // How many rows to ignore

                rs = statement.executeQuery("SELECT * from " + punishmentType + "s ORDER BY " + punishmentType + "id DESC LIMIT 5" + " OFFSET " + offset);

                if (!rs.isBeforeFirst()) { // Checks if points exist
                    sender.sendMessage(ChatColor.RED + "No " + punishmentType + " points were found in the database.");
                } else {
                    while (rs.next()) {

                        // Retrieves point information
                        int noteid = rs.getInt(punishmentType + "id");
                        int points = rs.getInt("points");
                        String reason = rs.getString("reason");
                        String mod = rs.getString("mod");
                        boolean warning = rs.getBoolean("warning");
                        boolean decayed = rs.getBoolean("decayed");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("(MM/dd/yy)");
                        String date = simpleDateFormat.format(rs.getDate("date"));
                        String username = Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("uuid"))).getName();

                        //  If the point has decayed or is a warning, it will be added to the end of the line
                        String decayFormat = (decayed) ? ChatColor.STRIKETHROUGH + "" + ChatColor.RED + " DECAYED" : "";
                        String warningFormat = (warning) ? ChatColor.BOLD + "" + ChatColor.YELLOW + " (Warning)" : "";

                        sender.sendMessage(
                                ChatColor.GREEN + punishmentType.toUpperCase() + " ID #" + noteid + " " + date + ": \n" +
                                        ChatColor.BLUE + "Name: " + ChatColor.DARK_AQUA + username +
                                        ChatColor.BLUE + " Infraction: " + ChatColor.DARK_AQUA + reason +
                                        ChatColor.BLUE + "Points: " + ChatColor.DARK_AQUA + points +
                                        ChatColor.BLUE + " Mod: " + ChatColor.DARK_AQUA + mod + warningFormat + decayFormat);
                    }
                }
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
                sender.sendMessage("An error has occurred!");
            } finally {
                try {
                    rs.close();
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}