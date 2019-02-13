/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.controllers.DashboardController;
import com.twitchbotx.gui.guiHandler;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javafx.application.Platform;

/**
 *
 * @author RaxaStudios
 */
public class ModerationHandler implements ModerationListener{

    private static final Logger LOGGER = Logger.getLogger(ModerationHandler.class.getSimpleName());

    private Datastore store;
    private String reason;
    private String timeout;

    
    /**
     * Constructor for the handler
     *
     * @param store The database access utility for reading/writing to the
     * database
     *
     */
    public ModerationHandler(Datastore store) {
        this.store = guiHandler.bot.getStore();
    }

    @Override
    public void needUpdate(){
        System.out.println("Resetting store");
        this.store = guiHandler.bot.getStore();
    }
    
    public String filterCheck(String msg) {
        for (int i = 0; i < store.getFilters().size(); i++) {
            final ConfigParameters.Filter filter = store.getFilters().get(i);
            if (filter.enabled) {
                String filterName = filter.name;
                reason = filter.reason;
                timeout = filter.seconds;
                 if (msg.equals(filterName)) {
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

    public void handleTool(String username, String msg, String msgId) {
        try {
            //Timestamped chat log for testing purposes
            //Calendar cal = Calendar.getInstance();
            //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            //sendEvent(sdf.format(cal.getTime()) + " " + username + ": " + msg);
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
                regexCheck(username, msg, msgId);
            }
            return;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.severe(e.toString());
        }
    }

    private boolean userCheck(String username) {
        //ignore for now, future use to check usernames
        return false;
    }

    private void regexCheck(String user, String msg, String msgId) {
        Pattern pattern;
        Matcher matcher;
        boolean delete = false;
        for (int i = 0; i < store.getRegexes().size(); i++) {
            final ConfigParameters.FilterRegex filter = store.getRegexes().get(i);
            if (filter.enabled) {
                String content = filter.content;
                reason = filter.reason;
                timeout = filter.seconds;
                if (filter.seconds.equals("del")) {
                    delete = true;
                }
                pattern = Pattern.compile(content);
                matcher = pattern.matcher(msg);
                if (matcher.find()) {
                    if (delete) {
                        sendMessage(".delete " + msgId);
                        sendEvent(".delete user " + user + " message. Reason:" + reason);
                    } else {
                        sendMessage(".timeout " + user + " " + timeout + " " + reason);
                        sendEvent(".timeout " + user + " " + timeout + " " + reason);
                    }
                    return;
                }
            }
        }
    }

    private void sendMessage(final String msg) {
        DashboardController.wIRC.sendMessage(msg, false);
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

