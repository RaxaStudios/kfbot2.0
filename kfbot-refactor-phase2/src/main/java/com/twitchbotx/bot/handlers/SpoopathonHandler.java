/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.gui.controllers.DashboardController;
import com.twitchbotx.gui.guiHandler;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import javafx.application.Platform;

/**
 *
 * @author Raxa
 */
public class SpoopathonHandler {

    // TODO points system for users 
    public static LinkedHashMap<String, Integer> MAP = new LinkedHashMap();
    private static sqlHandler sql = new sqlHandler(guiHandler.bot.getStore());
    private boolean found = false;
    private boolean removed = false;

    /**
     * Method to add points to a specified user
     *
     * @param userUpper actual display name
     * @param userLower lower case version
     * @param amt Integer value to be set or added to userID
     * @param chat send to chat boolean
     */
    public void addVotes(String userUpper, String userLower, int amt, boolean chat) {
        found = false;
        removed = false;
        if (amt < 1) {
            sendEvent("Use !remVotes " + userUpper + " " + amt + " or !remUser " + userUpper);
            if (chat) {
                sendMessage("Use !remVotes " + userUpper + " " + amt + " or !remUser " + userUpper);
            }
            return;
        }
        MAP.entrySet().forEach((m) -> {
            if (userLower.equalsIgnoreCase(m.getKey())) {
                // if found increase points
                found = true;
                int points = 0;
                points = m.getValue() + amt;
                m.setValue(points);
            }
        });
        // if not found, create
        if (!found) {
            MAP.put(userLower, amt);
            sendEvent(userUpper + " added to list with " + amt + " points");
            if (chat) {
                if (amt == 1) {
                    sendMessage("Added " + amt + " vote to " + userUpper + ", use !vote [gameID] [amount] to vote");
                } else {
                    sendMessage("Added " + amt + " votes to " + userUpper + ", use !vote [gameID] [amount] to vote");
                }
            }
        } else {
            sendEvent("Added " + amt + " points to " + userUpper);
            if (chat) {
                if (amt == 1) {
                    sendMessage("Added " + amt + " vote to " + userUpper + ", use !vote [gameID] [amount] to vote");
                } else {
                    sendMessage("Added " + amt + " votes to " + userUpper + ", use !vote [gameID] [amount] to vote");
                }
            }
        }
        writeList(MAP);
    }

    // remove points 
    // should only be used if useVotes is broken 
    // or to manually add points/remove votes from user 
    // ie donation situation
    public boolean remVotes(String user, int amt, boolean chat) {
        found = false;
        removed = false;
        try {
            int points = MAP.get(user) - amt;
            found = true;
            if (points < 1) {
                removed = true;
                MAP.remove(user);
            } else {
                removed = false;
                MAP.put(user, points);
            }
        } catch (NullPointerException ne) {
            ne.printStackTrace();
            if (chat) {
                // ignore for now douuble sends to chat 
            }
        }
        if (found && !removed) {
            sendEvent("Removed " + amt + " points from " + user);
            if (chat) {
                sendMessage("Removed " + amt + " votes from " + user);
            }
            writeList(MAP);
            return true;
        } else if (removed) {
            sendEvent(user + " removed from list due to 0 points");
            if (chat) {
                sendMessage("Removed all votes from " + user);
            }
            writeList(MAP);
            return true;
        } else {
            sendEvent(user + " not found");
            if (chat) {
                sendMessage(user + " not found");
            }
            return false;
        }
    }

    /* easy remove of user only available through chat command
    * !remUser [username]
    * should be used as a last resort if !votes or !remVotes is broken
     */
    public boolean remUser(String user) {
        if (MAP.remove(user) == null) {
            System.out.println(user + " not found");
            sendMessage(user + " not found");
            return false;
        } else {
            System.out.println("removed " + user);
            sendMessage(user + " removed");
            writeList(MAP);
            return true;
        }
    }

    enum gameList {
        RE4, AMN, SOMA, TEW2;
    }

    private boolean checkGame(String gameID) {
        for (gameList g : gameList.values()) {
            if (g.name().equals(gameID)) {
                return true;
            }
        }
        return false;
    }

    // use vote to cast a vote using sqlHandler
    public void useVote(String user, String gameID, int votesToUse) {
        try {
            // check if gameID is valid 
            if (!checkGame(gameID)) {
                String array = Arrays.toString(gameList.values());
                array = array.replace("[", "").replace("]", "");
                sendMessage("Invalid game ID, available games: " + array);
            } else {
                int votesAvailable = MAP.get(user);
                if (votesToUse == -777) {
                    if (MAP.get(user) == null) {
                        sendMessage(user + " has no votes");
                    } else {
                        sendMessage("@" + user + " used all " + votesAvailable + " votes");
                        sql.addPoints("!s-addPoints " + gameID + " " + votesAvailable);
                        MAP.remove(user);
                    }
                } else {
                    if (votesAvailable < votesToUse) {
                        if (MAP.get(user) == null) {
                            sendMessage(user + " has no votes");
                        } else {
                            sendMessage("@" + user + ", not enough votes available, you have: " + votesAvailable);
                        }
                    } else if (votesAvailable == votesToUse) {
                        sendMessage(user + " used all available votes");
                        sql.addPoints("!s-addPoints " + gameID + " " + votesToUse);
                        MAP.remove(user);
                    } else {
                        int votesLeft = votesAvailable - votesToUse;
                        sendMessage(user + " used " + votesToUse + " votes, " + votesLeft + " remaining");
                        sql.addPoints("!s-addPoints " + gameID + " " + votesToUse);
                        MAP.put(user, votesLeft);
                    }
                }
            }
        } catch (NullPointerException ne) {
            sendMessage(user + " has no votes");
        }
    }

    // Set map values on startup for restarting bot 
    public void setMap(LinkedHashMap<String, Integer> map) {
        MAP.putAll(map);
        /* MAP.entrySet().forEach((m) -> {
            System.out.println(m.getKey() + " : " + m.getValue());
        });*/
        System.out.println("Successfully set spoopathon user points from file");
        sendEvent("Successfully set spoopathon user points from file");
    }

    // getter for current MAP
    public LinkedHashMap<String, Integer> getMap() {
        return this.MAP;
    }

    public static void clearMap() {
        MAP.clear();
        // overwrite map file
        try {
            FileOutputStream fout = new FileOutputStream("spoopUser.map");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(MAP);
        } catch (IOException ie) {
            ie.printStackTrace();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    guiHandler.bot.getStore().getEventList().addList("Error occurred in clearing user file");
                }
            });
        }
    }

    // allow for mods to use !votes [user] 
    // allow anyone to use !votes w/out param
    public int getVotes(String user) {
        // return current points of user
        int votes;
        try {
            votes = MAP.get(user);
            System.out.println("user votes: " + user);

        } catch (Exception e) {
            //e.printStackTrace();
            votes = 0;
            // if user is not found, null pointer exception will be thrown, return 0
        }
        return votes;
    }

    public void setVotes(String user, int amt) {
        // if exists, overwrite
        MAP.put(user, amt);
        writeList(MAP);
        sendEvent(user + " votes set to " + amt);
    }

    // find and replace instead of using userIds
    public void changeName(String old, String updated) {
        int points = MAP.get(old);
        MAP.remove(old);
        MAP.put(updated, points);
        sendEvent("Username change: " + old + " -> " + updated);
    }

    // methods to handle auto additions, subs and bits, donations not possible automatically
    public void handleSub(String userUpper, String userLower, int subPoints) {
        // resubber gets points based on tier sub point worth tier 1 = 1, 2 = 2, 3 = 6
        // ratio is per sub point amount ie tier 3 = 6 * pointValue
        try {
            System.out.println("user: " + userUpper + " subPoints: " + subPoints);

            int pointValue = Integer.parseInt(guiHandler.bot.getStore().getConfiguration().spoopSubValue) * subPoints;
            addVotes(userUpper, userLower, pointValue, true);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage("Error occured with sub votes");
        }
    }

    public void handleSubGift(String userUpper, String userLower, int subPoints, int amt) {
        // gifter gets points based on tier and amount
        // take tier points * pointValue * amount
        try {
            System.out.println("user: " + userUpper + " subPoints: " + subPoints + " amt: " + amt);
            int pointValue = Integer.parseInt(guiHandler.bot.getStore().getConfiguration().spoopSubValue) * subPoints * amt;
            addVotes(userUpper, userLower, pointValue, true);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage("Error occured with mass sub gift votes");
        }
    }

    public void handleSingleGift(String userUpper, String userLower, int subPoints) {
        // gifter gets points based on tier
        // tier * pointValue
        try {
            System.out.println("user: " + userUpper + " subPoints: " + subPoints);
            int pointValue = Integer.parseInt(guiHandler.bot.getStore().getConfiguration().spoopSubValue) * subPoints;
            addVotes(userUpper, userLower, pointValue, true);
        } catch (Exception e) {
            System.out.println("error in singlegift");
            e.printStackTrace();
            try{
            sendMessage("Error occured with single gift votes");
            System.out.println("aftersend single gift");
            } catch(Exception ie){
                ie.printStackTrace();
            }
        }
    }

    public void handleBits(String userUpper, String userLower, int bits) {
        // sender gets points if > min amount  then / ratio
        // bits are 1 cent = 1 bit
        // if min bit for 1 point is 500 bits, iterate until below base value 
        // ie if pointvalue = 500 bits, we have 2000 bits coming in, we should get 4 points
        try {
            System.out.println("user: " + userUpper + " bits: " + bits);
            int minBits = Integer.parseInt(guiHandler.bot.getStore().getConfiguration().spoopBitValue);
            // if bits are less than bit value, ignore completely otherwise it will say 0 points
            if (bits >= minBits) {
                int pointValue = Integer.parseInt(guiHandler.bot.getStore().getConfiguration().spoopBitValue);
                int points = 0;
                while (bits >= pointValue) {
                    points++;
                    bits = bits - pointValue;
                }
                addVotes(userUpper, userLower, points, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage("Error occured with bit votes");
        }

    }

    public void writeList(LinkedHashMap<String, Integer> map) {
        try {
            FileOutputStream fout = new FileOutputStream("spoopUser.map");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(map);
        } catch (IOException ie) {
            sendEvent("Error occurred trying to write lottery to file");
        }
    }

    public synchronized void getMapFromFile() {
        try {
            Path location = Paths.get("");
            Path lResolved = location.resolve("spoopUser.map");
            FileInputStream fin = new FileInputStream(lResolved.toString());
            ObjectInputStream ois = new ObjectInputStream(fin);
            LinkedHashMap<String, Integer> m1 = (LinkedHashMap<String, Integer>) ois.readObject();
            setMap(m1);
        } catch (IOException ie) {
            sendEvent("Spoop user points file not found");
            ie.printStackTrace();
        } catch (Exception e) {
            sendEvent("Error occurred trying to get existing spoopathon user points");
        }
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
