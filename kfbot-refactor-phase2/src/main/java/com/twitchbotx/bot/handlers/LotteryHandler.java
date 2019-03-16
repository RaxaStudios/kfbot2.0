/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.CommandParser;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.controllers.DashboardController;
import com.twitchbotx.gui.guiHandler;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        private LinkedHashMap<String, Entrant<Integer, String>> MAP = new LinkedHashMap<>();
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
            return this.MAP;
        }

        public synchronized void setMap(LinkedHashMap<String, Entrant<Integer, String>> map) {
            this.MAP = map;
            writeMap(MAP);
        }

        public synchronized void writeMap(LinkedHashMap<String, Entrant<Integer, String>> map) {
            try {
                FileOutputStream fout = new FileOutputStream("Lottery.map");
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(map);
            } catch (IOException ie) {
                sendEvent("Error occurred trying to write lottery to file");
            }
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
            }
        }

        public synchronized void leaveLotto(String user) {
            MAP.remove(user);
            prevWinner.add(user);
            currPool.remove(user);
            sendMessage(user + " has been removed from the lottery");
        }

        private String tempName = "";

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
                // check for user in chat, choose again if not found per request
                if (tempName.equals(winner)) {
                    return "empty";
                }

                if (!userPresent(winner)) {
                    System.out.println("trying to remove " + winner);
                    MAP.remove(winner);
                    currPool.remove(winner);
                    tempName = winner;
                    drawLotto();
                    return "empty";
                } else {
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
                    });
                }
            } catch (NullPointerException | IllegalArgumentException ne) {
                sendMessage("Lottery is empty!");
            }
            return (winner);
        }

        //boolean value lottoOn will not clear MAP
        //value meant to prevent additional entries
        public synchronized void lottoOpen(String trailing) {
            lottoOn = true;
            guiHandler.bot.getStore().modifyConfiguration("lottoStatus", "on");
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
            guiHandler.bot.getStore().modifyConfiguration("lottoStatus", "off");
            sendMessage("Lottery has been closed");
        }

        public synchronized boolean getLottoStatus() {
            return lottoOn;
        }

        public synchronized void lottoEnable() {
            lottoOn = true;
            guiHandler.bot.getStore().modifyConfiguration("lottoStatus", "on");
        }

        public synchronized void lottoDisable() {
            lottoOn = false;
            guiHandler.bot.getStore().modifyConfiguration("lottoStatus", "off");
        }

        public synchronized String getLottoName() {
            return keyword;
        }

        private void sendMessage(final String msg) {
            DashboardController.wIRC.sendMessage(msg, true);
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

        public synchronized LinkedHashMap<String, Entrant<Integer, String>> getMapFromFile() {
            try {
                Path location = Paths.get("");
                Path lResolved = location.resolve("Lottery.map");
                FileInputStream fin = new FileInputStream(lResolved.toString());
                ObjectInputStream ois = new ObjectInputStream(fin);
                LinkedHashMap<String, Entrant<Integer, String>> m1 = (LinkedHashMap<String, Entrant<Integer, String>>) ois.readObject();
                m1.entrySet().forEach((m) -> {
                    currPool.add(m.getKey());
                });
                setMap(m1);
                System.out.println("Successfully set regular lottery from file");
                sendEvent("Successfully set regular lottery from file");
                return m1;
            } catch (IOException ie) {
                sendEvent("Map lottery file not found");
                ie.printStackTrace();
            } catch (Exception e) {
                sendEvent("Error occurred trying to get existing regular lottery");
            }
            return null;
        }

    }

    //Begin alternate lottery 
    //caster syntax !song-open !song-close !song-draw
    //viewer syntax !song {name of song} 
    public static class SongList {

        @GuardedBy("this")
        private final Random RNG = new Random();
        @GuardedBy("this")
        private LinkedHashMap<String, Entrant<Integer, String>> MAP = new LinkedHashMap<>();
        @GuardedBy("this")
        private int size = MAP.size();
        @GuardedBy("this")
        private final List<String> prevWinner = new java.util.ArrayList<>();
        @GuardedBy("this")
        private final List<String> currPool = new java.util.ArrayList<>();
        @GuardedBy("this")
        private final List<String> songList = guiHandler.songList;
        @GuardedBy("this")
        private List<String> currAdded = new java.util.ArrayList<>();
        @GuardedBy("this")
        private List<String> prevAdded = new java.util.ArrayList<>();
        @GuardedBy("this")
        private boolean songsOn = true;
        @GuardedBy("this")
        private String winner;

        public synchronized LinkedHashMap<String, Entrant<Integer, String>> getMap() {
            return this.MAP;
        }

        public synchronized void setMap(LinkedHashMap<String, Entrant<Integer, String>> map) {
            this.MAP = map;
            writeMap(MAP);
        }

        public synchronized void printMap() {
            MAP.entrySet().forEach((m) -> {
                System.out.println("Current map item: " + m.getKey() + "  current tickets: " + m.getValue().getContent());
            });
        }

        public synchronized void writeMap(LinkedHashMap<String, Entrant<Integer, String>> map) {
            try {
                FileOutputStream fout = new FileOutputStream("songLottery.map");
                ObjectOutputStream oos = new ObjectOutputStream(fout);
                oos.writeObject(map);
            } catch (IOException ie) {
                sendEvent("Error occurred trying to write lottery to file");
            }
        }

        public synchronized LinkedHashMap<String, Entrant<Integer, String>> getMapFromFile() {
            try {
                Path location = Paths.get("");
                Path lResolved = location.resolve("songLottery.map");
                FileInputStream fin = new FileInputStream(lResolved.toString());
                ObjectInputStream ois = new ObjectInputStream(fin);
                LinkedHashMap<String, Entrant<Integer, String>> m1 = (LinkedHashMap<String, Entrant<Integer, String>>) ois.readObject();
                m1.entrySet().forEach((m) -> {
                    System.out.println("Current map item: " + m.getKey() + "  current tickets: " + m.getValue().getContent());
                    currAdded.add(m.getValue().getContent());
                    prevAdded.add(m.getValue().getContent());
                    currPool.add(m.getKey());
                });
                setMap(m1);
                System.out.println("Successfully set song lottery from file");
                sendEvent("Successfully set song lottery from file");
                return m1;
            } catch (IOException ie) {
                sendEvent("Map lottery file not found");
                ie.printStackTrace();
            } catch (Exception e) {
                sendEvent("Error occurred trying to get existing song lottery");
            }
            return null;
        }

        public synchronized List<String> getCurr() {
            return currPool;
        }

        public synchronized List<String> getPrev() {
            return prevWinner;
        }

        public synchronized List<String> getSongList() {
            return songList;
        }

        public synchronized boolean addUser(String user, String content) {
            System.out.println("Curr pool: " + currPool);
            System.out.println("Prev add: " + prevAdded);
            String displayName = CommandParser.displayName;
            boolean nameCheck = user.equalsIgnoreCase(displayName);
            if (!nameCheck) {
                displayName = user;
            }
            int contentIndex = content.indexOf(" ");
            content = content.substring(contentIndex + 1, content.length());
            String songListName;
            int songListSize = songList.size();
            //check that content is a number between 1 and max song #
            try {
                int intContent = Integer.parseInt(content);
                if (intContent < 1 || intContent > songListSize) {
                    sendMessage("@" + displayName + ", song choice must be a number between 1 and " + songListSize);
                    System.out.println("@" + displayName + ", song choice must be a number between 1 and " + songListSize);
                    return false;
                } else {
                    //parse song list for corresponding number, send to songList check
                    songListName = songList.get(intContent);
                    int nameBeginIndex = songListName.indexOf(".") + 2;
                    songListName = songListName.substring(nameBeginIndex);
                }
            } catch (NumberFormatException e) {
                sendMessage("@" + displayName + ", song choice must be a number between 1 and " + songListSize);
                return false;
            }

            //add song to currAdded
            if (currPool.contains(user)) {
                sendMessage(displayName + " already entered in lottery");
                return false;
            } else if (currAdded.contains(songListName)) {
                sendMessage("@" + displayName + ", that song is already in the lottery, please choose a new song!");
                return false;
            } else if (prevAdded.contains(songListName)) {
                sendMessage("@" + displayName + ", that song has already been played today, please choose a new song!");
                return false;
            } else {
                // temporarily disallow users to enter until song system is reset
                if (prevWinner.contains(user)) {
                    sendMessage(displayName + ", limit is 1 win per stream");
                    return false;
                } else {
                    sendMessage(displayName + " added with song: " + songListName);
                    currPool.add(user);
                    currAdded.add(songListName);
                }
            }
            int ticketValue = 2;

            MAP.put(user, new Entrant(songListName));
            MAP.get(user).addTicket(ticketValue);
            writeMap(MAP);
            return true;
        }

        public synchronized void leaveSong(String user) {
            currAdded.remove(MAP.get(user).content);
            MAP.remove(user);
            currPool.remove(user);

            writeMap(MAP);
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
                // check for user in chat, choose again if not found per request
                if (!userPresent(winner)) {
                    System.out.println("trying to remove " + winner);
                    MAP.remove(winner);
                    currPool.remove(winner);
                    drawSong();
                }
                winnerSong = MAP.get(winner).getContent();
                sendMessage("Winner: " + winner + " kffcCheer Song choice: " + MAP.get(winner).getContent());
                prevWinner.add(winner);
                prevAdded.add(MAP.get(winner).getContent());
                currAdded.remove(MAP.get(winner).content);
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
                });
                writeMap(MAP);
            } catch (NullPointerException | IllegalArgumentException ne) {
                sendMessage("Song lottery is empty!");
            }
            return (winner + " song: " + winnerSong);
        }

        //boolean value songsOn will not clear MAP
        //value meant to prevent additional entries
        public synchronized void songOpen() {
            songsOn = true;
            guiHandler.bot.getStore().modifyConfiguration("songLottoStatus", "on");
            sendMessage("A lottery for !jd has opened, type '!song [song number]' to enter!");
        }

        public synchronized void songEnable() {
            songsOn = true;
            guiHandler.bot.getStore().modifyConfiguration("songLottoStatus", "on");
        }

        public synchronized void songDisable() {
            songsOn = false;
            guiHandler.bot.getStore().modifyConfiguration("songLottoStatus", "off");
        }

        public synchronized void songReset() {
            MAP.clear();
            writeMap(MAP);
            currPool.clear();
            currAdded.clear();
            prevAdded.clear();
            prevWinner.clear();
            songsOn = true;
            sendMessage("Song lottery has been reset");
        }

        public synchronized void songClose() {
            songsOn = false;
            guiHandler.bot.getStore().modifyConfiguration("songLottoStatus", "off");
            sendMessage("Song lottery has been closed");
        }

        public synchronized boolean getSongStatus() {
            return songsOn;
        }

        private void sendMessage(final String msg) {
            DashboardController.wIRC.sendMessage(msg, true);
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

    public static class Entrant<Integer, String> implements Serializable {

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

    private static void sendEvent(final String msg) {
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
        DashboardController.wIRC.sendMessage(msg, true);
    }

    /**
     * Method to check for user still in chat at time of drawing
     *
     * @param username
     *
     * @return boolean in chat
     */
    private static boolean userPresent(String user) {
        if (TwitchStatusHandler.userPresent(user)) {

            return true;
        } else {
            sendEvent(user + " was not found, redrawing");
            System.out.println(user + " was not found, redrawing");
            return false;
        }

    }
}
