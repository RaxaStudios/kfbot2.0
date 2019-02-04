/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.gui.guiHandler;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import javafx.application.Platform;

/**
 *
 * @author Raxa
 */
public class SpoopathonHandler {

    // TODO points system for users 
    public static LinkedHashMap<String, Integer> MAP = new LinkedHashMap();
    private static sqlHandler sql = new sqlHandler(guiHandler.bot.getStore(), guiHandler.bot.getOut());
    private boolean found = false;
    private boolean removed = false;

    /**
     * Method to add points to a specified user
     *
     * @param user actual display name
     * @param amt Integer value to be set or added to userID
     */
    public void addVotes(String user, int amt) {
        found = false;
        removed = false;
        if (amt < 1) {
            sendEvent("Use !remVotes " + user + " " + amt + " or !remUser " + user);
            return;
        }
        MAP.entrySet().forEach((m) -> {
            if (user.equalsIgnoreCase(m.getKey())) {
                // if found increase points
                found = true;
                int points = 0;
                points = m.getValue() + amt;
                m.setValue(points);
            }
        });
        // if not found, create
        if (!found) {
            MAP.put(user, amt);
            sendEvent(user + " added to list with " + amt + " points");
        } else {
            sendEvent("Added " + amt + " points to " + user);
        }
        writeList(MAP);
    }

    // remove points 
    public boolean remVotes(String user, int amt) {
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
            sendMessage(user + " not found");
        }
        if (found && !removed) {
            sendEvent("Removed " + amt + " points from " + user);
            writeList(MAP);
            return true;
        } else if (removed) {
            sendEvent(user + " removed from list due to 0 points");
            writeList(MAP);
            return true;
        } else {
            sendEvent(user + " not found");
            return false;
        }
    }

    //easy remove of user
    public boolean remUser(String user) {
        if (MAP.remove(user) == null) {
            System.out.println(user + " not found");
            return false;
        } else {
            System.out.println("removed " + user);
            writeList(MAP);
            return true;
        }
    }

    // use vote to cast a vote using sqlHandler
    public void useVote(String user, String gameID, int votesToUse) {
        try {
            int votesAvailable = MAP.get(user);
            if (votesAvailable < votesToUse) {
                if (MAP.get(user) == null) {
                    sendMessage(user + " has no votes");
                } else {
                    sendMessage("@" + user + ", not enough votes, available: " + votesAvailable);
                }
            } else if (votesAvailable == votesToUse) {
                sendMessage(user + " used all available votes");
                sql.addPoints("!addPoints " + gameID + " " + votesToUse);
                MAP.remove(user);
            } else {
                int votesLeft = votesAvailable - votesToUse;
                sendMessage(user + " used " + votesToUse + " votes, " + votesLeft + " remaining");
                sql.addPoints("!addPoints " + gameID + " " + votesToUse);
                MAP.put(user, votesLeft);
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

    // allow for mods to use !votes [user] 
    // allow anyone to use !votes w/out param
    public int getVotes(String user) {
        // return current points of user
        return MAP.get(user);
    }

    public void setVotes(String user, int amt) {
        // if exists, overwrite
        MAP.put(user, amt);
        writeList(MAP);
        sendEvent(user + " points set to " + amt);
    }

    // find and replace instead of using userIds
    public void changeName(String old, String updated) {
        int points = MAP.get(old);
        MAP.remove(old);
        MAP.put(updated, points);
        sendEvent("Username change: " + old + " -> " + updated);
    }

    // methods to handle auto additions, subs and bits, donations not possible automatically
    public void handleSub(String user, int subPoints) {
        // resubber gets points based on tier sub point worth tier 1 = 1, 2 = 2, 3 = 6
        // ratio is per sub point amount ie tier 3 = 6 * pointValue
        int pointValue = Integer.parseInt(guiHandler.bot.getStore().getConfiguration().spoopSubValue) * subPoints;
        addVotes(user, pointValue);
    }

    public void handleSubGift(String user, int subPoints, int amt) {
        // gifter gets points based on tier and amount
        // take tier points * pointValue * amount
        int pointValue = Integer.parseInt(guiHandler.bot.getStore().getConfiguration().spoopSubValue) * subPoints * amt;
        addVotes(user, pointValue);
    }

    public void handleBits(String user, int bits) {
        // sender gets points if > min amount  then / ratio
        // bits are 1 cent = 1 bit
        // if min bit for 1 point is 500 bits, iterate until below base value 
        // ie if pointvalue = 500 bits, we have 2000 bits coming in, we should get 4 points
        int minBits = Integer.parseInt(guiHandler.bot.getStore().getConfiguration().spoopMinBits);
        if (bits >= minBits) {
            int pointValue = Integer.parseInt(guiHandler.bot.getStore().getConfiguration().spoopBitValue);
            int points = 0;
            while (bits >= pointValue) {
                points++;
                bits = bits - pointValue;
            }
            addVotes(user, points);
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
            //System.out.println(lResolved.toAbsolutePath());
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

    private void sendMessage(String msg) {
        guiHandler.messenger.sendMessage(msg);
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
