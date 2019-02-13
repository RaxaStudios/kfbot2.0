package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.controllers.DashboardController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

/**
 * This class handles all the youtube based queries from Twitch chat.
 *
 * It will essentially query youtube, and get the ID and information about the
 * video back.
 */
public final class YoutubeHandler {
    
    private static final Logger LOGGER = Logger.getLogger(YoutubeHandler.class.getSimpleName());
    private final Datastore store;


    public YoutubeHandler(final Datastore store) {
        this.store = store;
    }

    /**
     * This method sets youtube URL, reads, and sends out title
     *
     * @param request
     *
     */
    private void getYoutubeTitle(final String request) {
        try {
            String ytAPI = store.getConfiguration().youtubeTitle;
            ytAPI = ytAPI.replaceAll("#id", "&id=" + request);
            ytAPI = ytAPI.replaceAll("#key", "&key=" + store.getConfiguration().youtubeApi);
            URL url = new URL(ytAPI);
            System.out.println("Link: " + ytAPI);
            URLConnection con = (URLConnection) url.openConnection();
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = bufReader.readLine()) != null) {
                response.append(line);
            }
            bufReader.close();
            System.out.println("response: " + response);
            if (response.toString().contains("\"items\": []")) {
                sendMessage("Video not found.");
            } else {
                int bi = response.toString().indexOf("\"title\":") + 10;
                int ei = response.toString().indexOf("\",   ", bi);
                String s = response.toString().substring(bi, ei);
                if (s.length() > 0) {
                    sendMessage(s);
                }
            }
            
        } catch (IOException e) {
            LOGGER.info("GetTitle.GetTitle - error opening or reading URL: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * This method searches all messages in stream for youtube links Sends ID to
     * getYoutubeTitle method This method requires 11 character video ID
     *
     * @param msg A full message from Twitch IRC API from a particular user
     */
    public void handleLinkRequest(final String msg) {
        try {
            if (msg.contains("youtube.com")) {
                int startToken = msg.indexOf("youtube.com") + 20;
                int endToken = msg.indexOf("v=") + 13;
                String ytId = msg.substring(startToken, endToken);
                getYoutubeTitle(ytId);
            } else if (msg.contains("youtu.be")) {
                int startToken = msg.indexOf("youtu.be") + 9;
                int endToken = msg.lastIndexOf("/") + 12;
                String ytId = msg.substring(startToken, endToken);
                getYoutubeTitle(ytId);
            } else {
                return;
            }
            return;
        } catch (Exception e) {
            LOGGER.severe(e.toString());
            return;
        }
    }

    /**
     * This command will send a message out to a specific Twitch channel.
     *
     * It will also wrap the message in pretty text (> /me) before sending it
     * out.
     *
     * @param msg The message to be sent out to the channel
     */
    private void sendMessage(final String msg) {
       DashboardController.wIRC.sendMessage(msg, true);
    }

}
