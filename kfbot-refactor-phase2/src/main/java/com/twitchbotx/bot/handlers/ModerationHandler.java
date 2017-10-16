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
import org.w3c.dom.Element;

/**
 *
 * @author RaxaStudios
 */
public class ModerationHandler {
    private static final Logger LOGGER = Logger.getLogger(ModerationHandler.class.getSimpleName());

    private final PrintStream outstream;
    private final Datastore store;
    private String reason;
    private static final String BANNED_USERNAME = "(\\d{7}([A-z]{1})\\d{7}|\\d{14})";

    /**
     * Constructor for the handler
     *
     * @param store
     * The database access utility for reading/writing to the database
     *
     * @param stream
     * The outsteam to communicate to twitch
     */
    public ModerationHandler(final Datastore store, final PrintStream stream) {
        this.store = store;
        this.outstream = stream;
    }

    public String filterCheck(String msg) {
        for (int i = 0; i < store.getFilters().size(); i++) {
            final ConfigParameters.Filter filter = store.getFilters().get(i);
            if (!filter.disabled) {
                String filterName = filter.name;
                reason = filter.reason;
                if (msg.contains(filterName)) {
                    //Use for wildcard matching, ie links
                    return reason;
                } else if(msg.equals(filterName)){
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
            if (!filterCheck(msg).equals("no filter")) {
                System.out.println(reason);
                sendMessage(".timeout " + username + " 600 " + reason);
                return;
            }
            else if(userCheck(username)){
                sendMessage(".timeout " + username + " 600 Username caught by filter");
                return;
            }
            return;

        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }
        return;
    }
    
    private boolean userCheck(String username){
        final Pattern pattern = Pattern.compile(BANNED_USERNAME);
        final Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    private void sendMessage(final String msg) {
        final String message = msg;
        this.outstream.println("PRIVMSG #"
                + store.getConfiguration().joinedChannel
                + " "
                + ":"
                + message);
    }
}
