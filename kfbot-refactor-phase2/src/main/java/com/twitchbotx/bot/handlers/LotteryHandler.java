/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.Datastore;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Raxa
 */
public class LotteryHandler {

    //lottery system, broadcaster starts a lottery with parameter word to enter in chat to enter the lottery
    //word set every new lottery, !draw # to choose # of winners, clear from array after picked
    //reset array every new lottery
    private static final Logger LOGGER = Logger.getLogger(TwitchStatusHandler.class.getSimpleName());

    private Datastore store;

    private PrintStream outstream;

    public LotteryHandler(final Datastore store,
            final PrintStream stream) {
        this.store = store;
        this.outstream = stream;

    }

    public List<String> entrants = new ArrayList<>();

    public void startLotto(String trailing) {
        entrants.clear();
        store.clearLotteryList();
        String auth;
        String keyword;
        try {
            auth = trailing.substring(trailing.indexOf(" ") + 1, trailing.indexOf(" ") + 3);
            //System.out.println(auth);

            if (!auth.equalsIgnoreCase("+a") && !auth.equalsIgnoreCase("+s")) {
                auth = "+a";
                keyword = trailing.substring(trailing.indexOf(" "), trailing.length());
            } else {
                keyword = trailing.substring(trailing.indexOf(auth) + 3, trailing.length());
            }
        } catch (StringIndexOutOfBoundsException e) {
            auth = "+a";
            keyword = trailing.substring(trailing.indexOf(" "), trailing.length());
        }

        System.out.println("auth found: " + auth + " keyword: " + keyword);
        store.setupLotto(auth, keyword);
        if (auth.equals("+s")) {
            sendMessage("A sub-only lottery has started! Subs can type " + keyword + " to enter!");
        } else {
            sendMessage("Lottery has started! Type " + keyword + " to enter!");
        }
    }

    //option to manually clear array of names
    public void clearLotto() {
        store.clearLotteryList();
        entrants.clear();
        sendMessage("Lottery cleared of all entries.");
    }

    //add usernames to list 
    public void enter(String username, boolean sub) {
        
        boolean subMode = store.getConfiguration().lottoAuth.equals("+s");
        boolean nonSubMode = store.getConfiguration().lottoAuth.equals("+a");
        if ((subMode && sub) || nonSubMode) {
            if (store.lotteryList().contains(username)) {
                sendMessage(username + " has already entered the lottery.");
            } else if(username.equalsIgnoreCase("sakurawindss")){
                sendMessage("lol");
            }
            else {
                store.addLotteryList(username);
                
                //entrants.add(username);
                System.out.println("Lotto names: " + store.lotteryList());
                sendMessage(username + " has been added!");
            }
        }
    }

    
    //draw # of winners
    public void drawWinner(String num) {
        if (num.equals("!draw")) {
            sendMessage("!draw requires a # of winners to draw.");
        } else {
            num = num.substring(num.indexOf(" ") + 1, num.length());
            int number = Integer.parseInt(num);
            Collections.shuffle(entrants);
            StringBuilder sb = new StringBuilder();
            if (entrants.isEmpty()) {
                sendMessage("Lottery is empty!");
            } else if (number > entrants.size()) {
                sendMessage("Not enough entries!");
            } else {
                for (int i = 0; i < number; i++) {
                    sb.append(entrants.get(i) + " ");
                    entrants.remove(i);
                }
                sendMessage("Winners: " + sb);
            }
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
        final String message = "/me > " + msg;
        if (!message.equals("/me > ")) {
            this.outstream.println("PRIVMSG #"
                    + store.getConfiguration().joinedChannel
                    + " "
                    + ":"
                    + message);
        }
    }

}
