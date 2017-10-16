package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.Datastore;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class TwitchStatusHandler {

    private static final Logger LOGGER = Logger.getLogger(TwitchStatusHandler.class.getSimpleName());

    private final Datastore store;

    public TwitchStatusHandler(final Datastore store) {
        this.store = store;
    }

    /**
     * This creates the URL = api.twitch.tv/kraken with desired streamer name("myChannel") from kfbot1.0.xml
     * Opens a connection, begins reading using BufferedReader brin, builds a String response based on API reply
     * nce response is done building, checks for "stream\:null" response - this means stream is not live
     * Creates Strings to hold content placed between int "bi" and int "ei" as per their defined index
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
            }

            else {
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
            int ei = response.toString().indexOf("T", bi);

            String s = response.toString().substring(bi, ei);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate begin = LocalDate.parse(s, formatter);
            LocalDate today = LocalDate.now();
            long gap = ChronoUnit.DAYS.between(begin, today);

            brin.close();
            return user + " has been following for " + gap + " days. Starting on " + begin + ".";

        } catch (FileNotFoundException e) {
            return "User " + user + "  is not following " + store.getConfiguration().joinedChannel;
        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }

        return "Unable to connect to Twitch server. Please try again later.";
    }
}
