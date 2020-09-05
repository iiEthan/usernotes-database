package com.rteenagers.parrot;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class SheetsQuickstart {
    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SheetsQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Prints mute/ban logs in spreadsheet:
     * https://docs.google.com/spreadsheets/d/1hN-TMFeBXOWET5AO8_AQ2ATMZboRLX5VWps2U71B73/edit
     */
    @SuppressWarnings("rawtypes")
    public static void main(CommandSender sender, String... args) throws IOException, GeneralSecurityException {
        String player = args[0];

        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1hN-TMFeBXOWET5AO8_AQ2ATMZboRLX5VWps2U71B73w";
        final String banRange = "Bans!A3:H";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange banResponse = service.spreadsheets().values()
                .get(spreadsheetId, banRange)
                .execute();
        List<List<Object>> banValues = banResponse.getValues();

        final String muteRange = "Mutes!A3:H";
        ValueRange muteResponse = service.spreadsheets().values()
                .get(spreadsheetId, muteRange)
                .execute();
        List<List<Object>> muteValues = muteResponse.getValues();

        String totalMutePoints = "0";
        String totalBanPoints = "0";

        sender.sendMessage(ChatColor.RED + "Mute Logs for " + ChatColor.DARK_AQUA + player + ChatColor.RED + ":");
        // Gets mute logs from spreadsheet
        for (List row : muteValues) {
            String userCheck = row.get(0).toString().toLowerCase();
            if (userCheck.equals(player.toLowerCase())) {
                totalMutePoints = row.get(3).toString();

                sender.sendMessage(ChatColor.GREEN + row.get(7).toString() + ": " +                            /* Date */
                        ChatColor.BLUE + "Infraction: " + ChatColor.DARK_AQUA + row.get(1).toString() + " " +       /* Infraction */
                        ChatColor.BLUE + "Action: " + ChatColor.DARK_AQUA + row.get(6).toString() + " " +           /* Action */
                        ChatColor.BLUE + "Mod: " + ChatColor.DARK_AQUA + row.get(5).toString() + " ");              /* Moderator */
            }
        }

        sender.sendMessage(ChatColor.RED + "Ban Logs for " + ChatColor.DARK_AQUA + player + ChatColor.RED + ":");
        // Gets ban logs from spreadsheet
        for (List row : banValues) {
            String userCheck = row.get(0).toString().toLowerCase();
            if (userCheck.equals(player.toLowerCase())) {
                totalBanPoints = row.get(3).toString();

                sender.sendMessage(ChatColor.GREEN + row.get(7).toString() + ": " +                            /* Date */
                        ChatColor.BLUE + "Infraction: " + ChatColor.DARK_AQUA + row.get(1).toString() + " " +       /* Infraction */
                        ChatColor.BLUE + "Action: " + ChatColor.DARK_AQUA + row.get(6).toString() + " " +           /* Action */
                        ChatColor.BLUE + "Mod: " + ChatColor.DARK_AQUA + row.get(5).toString() + " ");              /* Moderator */
            }
        }
        // These must be outside of the forEach loop because we only want them to print once
        sender.sendMessage(ChatColor.BLUE + "Total mute points: " + ChatColor.DARK_AQUA + totalMutePoints);
        sender.sendMessage(ChatColor.BLUE + "Total ban points: " + ChatColor.DARK_AQUA + totalBanPoints);
    }
}