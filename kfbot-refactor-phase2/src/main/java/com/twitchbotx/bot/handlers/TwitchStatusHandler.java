package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.guiHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class TwitchStatusHandler {

    private static final Logger LOGGER = Logger.getLogger(TwitchStatusHandler.class.getSimpleName());

    private static Datastore store;

    public TwitchStatusHandler() {
        store = guiHandler.bot.getStore();
    }

    /**
     * This creates the URL = api.twitch.tv/kraken with desired streamer
     * name("myChannel") from kfbot1.0.xml Opens a connection, begins reading
     * using BufferedReader brin, builds a String response based on API reply
     * nce response is done building, checks for "stream\:null" response - this
     * means stream is not live Creates Strings to hold content placed between
     * int "bi" and int "ei" as per their defined index
     *
     * @param msg
     */
    public String uptime(final String msg) {
        try {
            String statusURL = store.getConfiguration().streamerStatus;
            statusURL = statusURL.replaceAll("#streamer", store.getConfiguration().joinedChannel);
            URL url = new URL(statusURL);
            URLConnection con = (URLConnection) url.openConnection();
            con.setRequestProperty("Accept", "application/vnd.twitchtv.v3+json");
            con.setRequestProperty("Authorization", store.getConfiguration().password);
            con.setRequestProperty("Client-ID", store.getConfiguration().clientID);
            BufferedReader brin = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = brin.readLine()) != null) {
                response.append(inputLine);
            }
            brin.close();
            if (response.toString().contains("\"stream\":null")) {
                return "Stream is not currently live.";
            } else {
                int bi = response.toString().indexOf("\"created_at\":") + 14;
                int ei = response.toString().indexOf("\",", bi);
                String s = response.toString().substring(bi, ei);
                Instant start = Instant.parse(s);
                Instant current = Instant.now();
                long gap = ChronoUnit.MILLIS.between(start, current);
                String upT = String.format("%d hours, %d minutes, %d seconds", new Object[]{
                    TimeUnit.MILLISECONDS.toHours(gap),
                    TimeUnit.MILLISECONDS.toMinutes(gap) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(gap)),
                    TimeUnit.MILLISECONDS.toSeconds(gap) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(gap))
                });
                return "Stream has been up for " + upT + ".";
            }
        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }

        return "Unable to connect to Twitch server. Please try again later.";
    }

    /*
** This delivers the original follow date
**
** @param user
** @return formated date of created_at per https://api.twitch.tv/kraken/users/test_user1/follows/channels/test_channel
**
     */
    public String followage(final String user) {
        try {
            String followURL = store.getConfiguration().followage;
            //test values
            //followURL = followURL.replaceAll("#user", "raxa");
            //followURL = followURL.replaceAll("#streamer", "kungfufruitcup");
            followURL = followURL.replaceAll("#user", user);
            followURL = followURL.replaceAll("#streamer", store.getConfiguration().joinedChannel);
            URL url = new URL(followURL);
            URLConnection con = (URLConnection) url.openConnection();
            con.setRequestProperty("Accept", "application/vnd.twitchtv.v3+json");
            con.setRequestProperty("Authorization", store.getConfiguration().password);
            con.setRequestProperty("Client-ID", store.getConfiguration().clientID);
            BufferedReader brin = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = brin.readLine()) != null) {
                response.append(inputLine);
            }

            int bi = response.toString().indexOf("\"created_at\":") + 14;
            int ei = response.toString().indexOf("\"", bi);
            String s = response.toString().substring(bi, ei);

            DateTimeFormatter full = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'");
            DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd, uuuu");
            ZoneId z = ZoneId.of("UTC-1");
            LocalDateTime begin = LocalDateTime.parse(s, full);
            begin.atZone(z);
            LocalDateTime today = LocalDateTime.now(z);
            long diff = ChronoUnit.MILLIS.between(begin, today);
            long diffDay = diff / (24 * 60 * 60 * 1000);
            diff = diff - (diffDay * 24 * 60 * 60 * 1000);
            long diffHours = diff / (60 * 60 * 1000);
            diff = diff - (diffHours * 60 * 60 * 1000);
            long diffMinutes = diff / (60 * 1000);
            diff = diff - (diffMinutes * 60 * 1000);
            long diffSeconds = diff / 1000;
            diff = diff - (diffSeconds * 1000);
            if (diffDay < 0 || diffHours < 0 || diffMinutes < 0) {
                diffDay = 0;
                diffHours = 0;
                diffMinutes = 0;
                diffSeconds = 0;
            }
            String beginFormatted = begin.format(format);
            String gap = diffDay + " days " + diffHours + " hours " + diffMinutes + " minutes " + diffSeconds + " seconds";
            brin.close();
            return user + " has been following for " + gap + ". Starting on " + beginFormatted + ".";

        } catch (FileNotFoundException e) {
            return "User " + user + "  is not following " + store.getConfiguration().joinedChannel;
        } catch (Exception e) {
            LOGGER.severe(e.toString());
            e.printStackTrace();
        }

        return "Unable to connect to Twitch server. Please try again later.";
    }

    /**
     * Find last played game for use with %game% variable, namely !follow
     *
     * @param Username sent name from the param of !follow
     *
     * @return String value of game
     */
    public String getLastGame(String username) {
        String game = "";
        try {
            String gameURL = store.getConfiguration().channelInfo;
            gameURL = gameURL.replaceAll("#user", username);
            URL url = new URL(gameURL);
            URLConnection con = (URLConnection) url.openConnection();
            con.setRequestProperty("Accept", "application/vnd.twitchtv.v3+json");
            con.setRequestProperty("Authorization", store.getConfiguration().password);
            con.setRequestProperty("Client-ID", store.getConfiguration().clientID);
            BufferedReader brin = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = brin.readLine()) != null) {
                response.append(inputLine);
            }
            brin.close();

            System.out.println("GAME RESPONSE: " + response);

            int bi = response.indexOf("\"game\"") + 8;
            int ei = response.indexOf("\",", bi);
            game = response.substring(bi, ei);
            System.out.println("Game found: " + game);

        } catch (Exception ie) {
            ie.printStackTrace();
        }
        return game;
    }

    //TODO implement highlight system and commands to google doc system
    // mod and viewer editions
    /*   public void highlight() {

        String uptime = uptime();
        if (!uptime.equals("0")) {
            sendMessage("Highlight marked suggested added at " + uptime);
            try {
                String googleSheetID = this.elements.configNode.getElementsByTagName("googleSheetID").item(0).getTextContent();
                String sheetAPI = "https://sheets.googleapis.com/v4/spreadsheets/" + googleSheetID + "/values/{range}:append";
                URL url = new URL(sheetAPI);
                URLConnection con = (URLConnection) url.openConnection();
                con.setRequestProperty("range", "M6:M20");
                con.setRequestProperty("majorDimension", "COLUMNS");
                BufferedReader sheetIn = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder values = new StringBuilder();
                String valueLines;
                while ((valueLines = sheetIn.readLine()) != null) {
                    values.append(valueLines);
                }
                sheetIn.close();
            } catch (IOException e) {
                LOGGER.severe(e.toString());
            }
        } else {
            sendMessage("Stream is not currently live.");
        }
    }
     */
    /**
     * run a check on twitch viewerlist url:
     * https://tmi.twitch.tv/group/user/channel/chatters
     *
     * @param user
     * @return if found in viewerlist
     */
    public static boolean userPresent(String user) {
        try {
            String channel = store.getConfiguration().joinedChannel;
            String nameURL = "https://tmi.twitch.tv/group/user/" + channel + "/chatters";
            URL url = new URL(nameURL);
            URLConnection con = (URLConnection) url.openConnection();
            BufferedReader brin = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = brin.readLine()) != null) {
                response.append(inputLine);
            }
            brin.close();

            //System.out.println("NAME RESPONSE: " + response);
            if (response.toString().contains("\"" + user.toLowerCase() + "\"")) {
                System.out.println("found user:" + user);
                return true;
            } else {
                System.out.println("did not find user:" + user);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
