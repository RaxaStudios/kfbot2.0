/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import java.io.PrintStream;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.guiHandler;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javafx.application.Platform;

/**
 *
 * @author RaxaStudios
 */
public class ModerationHandler {

    private static final Logger LOGGER = Logger.getLogger(ModerationHandler.class.getSimpleName());

    private final PrintStream outstream;
    private final Datastore store;
    private String reason;
    private String timeout;
    private static final String BANNED_USERNAME = "(\\d{7}([A-z]{1})\\d{7}|\\d{14})";

    /**
     * Constructor for the handler
     *
     * @param store The database access utility for reading/writing to the
     * database
     *
     * @param stream The outsteam to communicate to twitch
     */
    public ModerationHandler(final Datastore store, final PrintStream stream) {
        this.store = store;
        this.outstream = stream;
    }

    public String filterCheck(String msg) {
        for (int i = 0; i < store.getFilters().size(); i++) {
            final ConfigParameters.Filter filter = store.getFilters().get(i);
            if (filter.enabled) {
                String filterName = filter.name;
                reason = filter.reason;
                timeout = filter.seconds;
                if (msg.contains(filterName)) {
                    //Use for wildcard matching, ie links
                    return reason;
                } else if (msg.equals(filterName)) {
                    //Exact phrase matching
                    return reason;
                }
            } else {
                reason = "no filter";
                return reason;
            }
        }
        reason = "no filter";
        return reason;
    }

    public void handleTool(String username, String msg) {
        try {
            //Timestamped chat log for testing purposes
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sendEvent(sdf.format(cal.getTime()) + " " + username + ": " + msg);
            if (!filterCheck(msg).equals("no filter")) {
                System.out.println(reason);
                sendMessage(".timeout " + username + " " + timeout + " " + reason);
                sendEvent("timeout " + username + " " + timeout + " " + reason);
                return;
            } else if (userCheck(username)) {
                sendMessage(".timeout " + username + " 600 Username caught by filter");
                sendEvent(".timeout " + username + " 600 Username caught by filter");
                return;
            } else {
                regexCheck(username, msg);
            }
            return;

        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }
        return;
    }

    private boolean userCheck(String username) {
        final Pattern pattern = Pattern.compile(BANNED_USERNAME);
        final Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private void regexCheck(String user, String msg) {
        Pattern pattern;
        Matcher matcher;
        for (int i = 0; i < store.getRegexes().size(); i++) {
            final ConfigParameters.FilterRegex filter = store.getRegexes().get(i);
            if (filter.enabled) {
                String content = filter.content;
                reason = filter.reason;
                timeout = filter.seconds;
                pattern = Pattern.compile(content);
                matcher = pattern.matcher(msg);
                if (matcher.matches()) {
                    sendMessage(".timeout " + user + " " + timeout + " " + reason);
                    return;
                } else {
                    return;
                }
            }
        }
    }

    private void sendMessage(final String msg) {
        final String message = msg;
        guiHandler.bot.getOut().println("PRIVMSG #"
                + store.getConfiguration().joinedChannel
                + " "
                + ":"
                + message);
    }

    private void sendEvent(final String msg) {
        String event = msg;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                guiHandler.bot.getStore().getEventList().addList(event);
            }
        });
    }
}
