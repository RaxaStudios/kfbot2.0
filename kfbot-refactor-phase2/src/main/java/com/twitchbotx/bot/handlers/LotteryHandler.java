/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.CommandParser;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.gui.guiHandler;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.annotation.concurrent.GuardedBy;

/**
 *
 * @author Raxa
 */
public class LotteryHandler {

    //lottery system, broadcaster starts a lottery with parameter word to enter in chat to enter the lottery
    //word set every new lottery, !draw # to choose # of winners, clear from array after picked
    //reset array every new lottery
    private static final Logger LOGGER = Logger.getLogger(TwitchStatusHandler.class.getSimpleName());

    private Datastore store = guiHandler.bot.getStore();

    public LotteryHandler() {

    }

    //Begin lottery with ticket class
    //caster syntax !lottery-open [auth] [keyword] !lottery-close !draw
    //viewer syntax keyword in chat
    public static class Lotto {

        @GuardedBy("this")
        private final Random RNG = new Random();
        @GuardedBy("this")
        private final LinkedHashMap<String, Entrant<Integer, String>> MAP = new LinkedHashMap<>();
        @GuardedBy("this")
        private int size = MAP.size();
        @GuardedBy("this")
        private final List<String> prevWinner = new java.util.ArrayList<>();
        @GuardedBy("this")
        private final List<String> currPool = new java.util.ArrayList<>();
        @GuardedBy("this")
        private boolean lottoOn = true;
        @GuardedBy("this")
        private String winner;
        @GuardedBy("this")
        private String keyword;
        @GuardedBy("this")
        private boolean subOnly = false;

        public synchronized LinkedHashMap<String, Entrant<Integer, String>> getMap() {
            return MAP;
        }

        public synchronized List<String> getCurr() {
            return currPool;
        }

        public synchronized List<String> getPrev() {
            return prevWinner;
        }

        public synchronized void addUser(String user, boolean sub) {
            if (subOnly && sub || !subOnly) {
                String displayName = CommandParser.displayName;
                boolean nameCheck = user.equalsIgnoreCase(displayName);
                if (!nameCheck) {
                    displayName = user;
                }
                int tempCountFix = 0;
                for (String check : currPool) {
                    if (check.equals(user)) {
                        if (tempCountFix < 1) {
                            tempCountFix++;
                            sendMessage("User " + displayName + " already entered");
                            return;
                        } else {
                            return;
                        }
                    }
                }
                currPool.add(user);
                int ticketValue = 2;
                tempCountFix = 0;
                boolean p = true;
                for (String u : prevWinner) {
                    if (u.equals(user)) {
                        if (tempCountFix < 1) {
                            p = false;
                            sendMessage("User " + displayName + " re-added");
                            ticketValue = 1;
                            tempCountFix++;
                        }
                    }
                }
                if (p && tempCountFix == 0) {
                    sendMessage(displayName + " added");
                }
                MAP.put(user, new Entrant());
                MAP.get(user).addTicket(ticketValue);
                MAP.entrySet().forEach((m) -> {
                    System.out.println("Current map item: " + m.getKey() + "  current tickets: " + m.getValue().getTicket());
                });
            }
        }

        public synchronized void leaveLotto(String user) {
            MAP.remove(user);
            prevWinner.add(user);
            currPool.remove(user);
            sendMessage(user + " has been removed from the lottery");
        }

        public synchronized String drawLotto() {
            int r = 0;
            size = MAP.size();
            List<String> sA = new java.util.ArrayList<>();
            for (int i = 0; i < size; i++) {
                MAP.entrySet().forEach((m) -> {
                    int tempTickets = m.getValue().getTicket();
                    while (tempTickets != 0) {
                        sA.add(m.getKey());
                        tempTickets--;
                    }
                });
            }
            try {
                Collections.shuffle(sA);
                r = RNG.nextInt(sA.size());
                winner = sA.get(r);
                sendMessage("Winner: " + winner + " kffcCheer");
                prevWinner.add(winner);
                MAP.remove(winner);
                currPool.remove(winner);
                MAP.entrySet().forEach((m) -> {
                    int ticketValue = 2;
                    for (String c : prevWinner) {
                        if (c.equals(m.getKey())) {
                            ticketValue = 1;
                        }
                    }
                    m.getValue().addTicket(ticketValue);
                    System.out.println(m.getKey() + " tickets: " + m.getValue().getTicket());
                });
            } catch (NullPointerException | IllegalArgumentException ne) {
                sendMessage("Lottery is empty!");
            }
            return (winner);
        }

        //boolean value lottoOn will not clear MAP
        //value meant to prevent additional entries
        public synchronized void lottoOpen(String trailing) {
            lottoOn = true;
            subOnly = false;
            String auth;
            try {
                auth = trailing.substring(trailing.indexOf(" ") + 1, trailing.indexOf(" ") + 3);
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
            //System.out.println("auth found: " + auth + " keyword: " + keyword);
            if (auth.equals("+s")) {
                subOnly = true;
                sendMessage("A sub-only lottery has started! Subs can type " + keyword + " to enter!");
            } else {
                subOnly = false;
                sendMessage("Lottery has started! Type " + keyword + " to enter!");
            }
        }

        public synchronized void lottoClear() {
            MAP.clear();
            currPool.clear();
            sendMessage("Lottery pool has been cleared");
        }

        public synchronized void lottoClose() {
            lottoOn = false;
            sendMessage("Lottery has been closed");
        }

        public synchronized boolean getLottoStatus() {
            return lottoOn;
        }

        public synchronized String getLottoName() {
            return keyword;
        }

        private void sendMessage(String msg) {
            final String message = "/me > " + msg;
            if (!message.equals("/me > ")) {
                guiHandler.bot.getOut().println("PRIVMSG #"
                        + guiHandler.bot.getStore().getConfiguration().joinedChannel
                        + " "
                        + ":"
                        + message);
            }
        }

    }

    //Begin alternate lottery 
    //caster syntax !song-open !song-close !song-draw
    //viewer syntax !song {name of song} 
    public static class SongList {

        @GuardedBy("this")
        private final Random RNG = new Random();
        @GuardedBy("this")
        private final LinkedHashMap<String, Entrant<Integer, String>> MAP = new LinkedHashMap<>();
        @GuardedBy("this")
        private int size = MAP.size();
        @GuardedBy("this")
        private final List<String> prevWinner = new java.util.ArrayList<>();
        @GuardedBy("this")
        private final List<String> currPool = new java.util.ArrayList<>();
        @GuardedBy("this")
        private boolean songsOn = true;
        @GuardedBy("this")
        private String winner;

        public synchronized LinkedHashMap<String, Entrant<Integer, String>> getMap() {
            return MAP;
        }

        public synchronized List<String> getCurr() {
            return currPool;
        }

        public synchronized List<String> getPrev() {
            return prevWinner;
        }

        public synchronized void addUser(String user, String content) {
            String displayName = CommandParser.displayName;
            boolean nameCheck = user.equalsIgnoreCase(displayName);
            if (!nameCheck) {
                displayName = user;
            }
            int contentIndex = content.indexOf(" ");
            content = content.substring(contentIndex, content.length());
            int tempCountFix = 0;
            for (String check : currPool) {
                if (check.equals(user)) {
                    if (tempCountFix < 1) {
                        tempCountFix++;
                        sendMessage("User " + displayName + " already entered with song: " + MAP.get(user).getContent());
                        return;
                    } else {
                        return;
                    }
                }
            }
            currPool.add(user);
            int ticketValue = 2;
            tempCountFix = 0;
            boolean p = true;
            for (String u : prevWinner) {
                if (u.equals(user)) {
                    if (tempCountFix < 1) {
                        p = false;
                        sendMessage("User " + displayName + " re-added with song: " + content);
                        ticketValue = 1;
                        tempCountFix++;
                    }
                }
            }
            if (p && tempCountFix == 0) {
                sendMessage(displayName + " added with song: " + content);
            }
            MAP.put(user, new Entrant(content));
            MAP.get(user).addTicket(ticketValue);
            MAP.entrySet().forEach((m) -> {
                // System.out.println("Current map item: " + m.getKey() + "  current tickets: " + m.getValue().getTicket());
            });
        }

        public synchronized void leaveSong(String user) {
            MAP.remove(user);
            prevWinner.add(user);
            currPool.remove(user);
            sendMessage(user + " removed the song lottery");
        }

        public synchronized String drawSong() {
            String winnerSong = "";
            int r = 0;
            size = MAP.size();
            List<String> sA = new java.util.ArrayList<>();
            for (int i = 0; i < size; i++) {
                MAP.entrySet().forEach((m) -> {
                    int tempTickets = m.getValue().getTicket();
                    while (tempTickets != 0) {
                        sA.add(m.getKey());
                        tempTickets--;
                    }
                });
            }
            try {
                Collections.shuffle(sA);
                r = RNG.nextInt(sA.size());
                winner = sA.get(r);
                winnerSong = MAP.get(winner).getContent();
                sendMessage("Winner: " + winner + " kffcCheer Song choice: " + MAP.get(winner).getContent());
                prevWinner.add(winner);
                MAP.remove(winner);
                currPool.remove(winner);
                MAP.entrySet().forEach((m) -> {
                    int ticketValue = 2;
                    for (String c : prevWinner) {
                        if (c.equals(m.getKey())) {
                            ticketValue = 1;
                        }
                    }
                    m.getValue().addTicket(ticketValue);
                    //System.out.println(m.getKey() + " index: " + m.getValue().getIndex() + " content: " + m.getValue().getContent() + " ticket: " + m.getValue().getTicket());
                });
            } catch (NullPointerException | IllegalArgumentException ne) {
                sendMessage("Song lottery is empty!");
            }
            return (winner + " song: " + winnerSong);
        }

        //boolean value songsOn will not clear MAP
        //value meant to prevent additional entries
        public synchronized void songOpen() {
            songsOn = true;
            sendMessage("A lottery for !jd has opened, type '!song [song name]' to enter!");
        }

        public synchronized void songClear() {
            MAP.clear();
            currPool.clear();
        }

        public synchronized void songClose() {
            songsOn = false;
            sendMessage("Song lottery has been closed");
        }

        public synchronized boolean getSongStatus() {
            return songsOn;
        }

        private void sendMessage(final String msg) {
            final String message = "/me > " + msg;
            if (!message.equals("/me > ")) {
                guiHandler.bot.getOut().println("PRIVMSG #"
                        + guiHandler.bot.getStore().getConfiguration().joinedChannel
                        + " "
                        + ":"
                        + message);
            }
        }

    }

    public static class Entrant<Integer, String> {

        private String content;
        public int ticket;

        public Entrant(String content) {
            this.content = content;
            this.ticket = 0;
        }

        public Entrant() {
            this.ticket = 0;
        }

        public void addTicket(int pastWinner) {
            this.ticket += pastWinner;
        }

        public String getContent() {
            return this.content;
        }

        public int getTicket() {
            return this.ticket;
        }
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
            guiHandler.bot.getOut().println("PRIVMSG #"
                    + store.getConfiguration().joinedChannel
                    + " "
                    + ":"
                    + message);
        }
    }

}
