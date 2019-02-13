/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.controllers.DashboardController;

/**
 *
 * @author Raxa
 */
public class EventHandler {

    //TwitchMessenger messenger;
    Datastore store;

    // pre checked for if enabled = "on"
    public EventHandler(Datastore store) {
        this.store = store;
    }

    public void handleBits(String user, int amt) {
        // TODO allow for different settings in conjunction with bit response manager in GUI
        String sendMsg = store.getConfiguration().bitMessage;
        sendMsg = sendMsg.replace("%bits", String.valueOf(amt)).replace("%user", sendMsg);
        sendMessage(sendMsg);
    }

    public void handleRaid(String user, int viewers) {
        // TODO allow for different settings in conjunction with raid response manager in GUI
        String sendMsg = store.getConfiguration().raidMessage;
        sendMsg = sendMsg.replace("%user", user).replace("%viewers", String.valueOf(viewers));
        sendMessage(sendMsg);
    }
    
    // TODO absorb handleSub replies
    public void handleSub(String user, String months, String tier, boolean gifted){
        
    }
    
    private void sendMessage(final String msg) {
       DashboardController.wIRC.sendMessage(msg, true);
    }
}
