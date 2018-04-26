/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.TwitchBotX;
import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.gui.guiHandler;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 *
 * @author Raxa
 */
public class MarathonHandler {

    private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());

    private final Datastore store = guiHandler.bot.getStore();
    static String SQLURL = "";
    static String USER = "";
    static String PASS = "";
    static Connection con = null;
    static Statement stmt = null;
    static String sqlStatement = "";
    static boolean first = true;
    int minutes = 0;
    int hours = 12;
    TwitchMessenger messenger;

    public MarathonHandler(Datastore store, final PrintStream out) {
        this.messenger = new TwitchMessenger(out, store.getConfiguration().joinedChannel);
    }

    public void addPoints(String msg) {

        //$5 = 500 points = 1 minute
        //tier1 = 500 = 1 sub point, tier2 = 1000 = 2 sub points, tier3 = 2500 = 6 sub points
        //first 180 sub points are x2 
        int sep = msg.indexOf(" ");
        int amt = new Integer(msg.substring(sep + 1));
        String addP = "Update kfTimer SET points = points +" + amt + " WHERE indexID=\'0\'";

        //begin update sql point value
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            stmt.execute(addP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //begin get point value after update
        String getP = "SELECT points FROM kfTimer";
        int currentPoints = 0;
        try {
            ResultSet ap = stmt.executeQuery(getP);
            ResultSetMetaData apmd = ap.getMetaData();
            int apc = apmd.getColumnCount();

            while (ap.next()) {
                for (int i = 1; i <= apc; i++) {
                    currentPoints = ap.getInt(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //if points are greater than 660,000 
        //return and set time, this will equal the 30 hour cap
        //check hour value from SQL
        String getH = "SELECT hours FROM kfTimer";
        int hValue = 0;
        try {
            ResultSet hp = stmt.executeQuery(getH);
            ResultSetMetaData hmd = hp.getMetaData();
            int hc = hmd.getColumnCount();

            while (hp.next()) {
                for (int i = 1; i <= hc; i++) {
                    hValue = hp.getInt(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println(hValue + "HVALUE");
        boolean send = true;

        if (currentPoints > 660000 || hValue > 29) {
            currentPoints = 660000;
            if (first) {
                messenger.sendMessage("30 hour marathon cap has been reached! kffcCheer");
                first = false;
            }
            send = false;
        }

        //begin parse points to time value
        int minuteValue = 0;
        //set minute value from sql query
        String minValue = "SELECT minValue FROM kfTimer";
        try {
            ResultSet aM = stmt.executeQuery(minValue);
            while (aM.next()) {
                minuteValue = aM.getInt("minValue");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //set default value
        if (minuteValue == 0) {
            minuteValue = 500;
        }
        //sql query to get current values
        String tTime = "SELECT hours, minutes, seconds FROM kfTimer";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            ResultSet ap = stmt.executeQuery(tTime);
            while (ap.next()) {
                hours = Integer.parseInt(ap.getString("hours"));
                minutes = Integer.parseInt(ap.getString("minutes"));
            }
        } catch (IllegalArgumentException il) {
            System.out.println("Syntax: !s-addPoints [gameID] [points]");
        } catch (SQLException sql) {
            sql.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(hours + "HOURS " + currentPoints + "cP");
        while (currentPoints > (minuteValue - 1)) {
            currentPoints -= minuteValue;
            minutes++;
            if (minutes > 59) {
                hours++;
                minutes = 0;
            }
        }
        if (hours > 29) {
            hours = 30;
            minutes = 0;
        }
        System.out.println(hours + "H " + minutes + "M " + currentPoints + "cP");
        //format minutes
        String format = "";
        if (minutes < 10) {
            format = "0" + Integer.toString(minutes);
        } else {
            format = Integer.toString(minutes);
        }
        System.out.println("formatted minutes: " + format);
        //begin update time values and close connection
        String updateTime = "UPDATE kfTimer SET hours=\'" + hours + "\', minutes=\'" + format + "\', seconds=\'00\' WHERE indexID=\'0\'";
        System.out.println(updateTime);
        try {
            stmt.executeUpdate(updateTime);
            con.close();
            if (send) {
                if (amt > 0) {
                    messenger.sendMessage("Added " + amt + " points to the marathon timer.");
                } else if (amt == 0) {
                    messenger.sendMessage("Timer stats updated");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void totalTime() {
        String tTime = "SELECT hours, minutes, seconds FROM kfTimer";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            ResultSet ap = stmt.executeQuery(tTime);
            ResultSetMetaData apmd = ap.getMetaData();
            int apc = apmd.getColumnCount();
            StringBuilder amount = new StringBuilder();
            while (ap.next()) {
                for (int i = 1; i <= apc; i++) {
                    if (i != apc) {
                        amount.append(ap.getString(i) + ":");
                    } else {
                        amount.append(ap.getString(i));
                    }
                }
            }
            messenger.sendMessage("Total marathon time: " + amount);
            con.close();
        } catch (IllegalArgumentException il) {
            System.out.println("Syntax: !s-addPoints [gameID] [points]");
        } catch (SQLException sql) {
            sql.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startTimer() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();

            long now = System.currentTimeMillis();
            String resetTimer = "Update kfTimer SET startTime=\'" + now + "\', hours=\'12\', minutes=\'00\', seconds=\'00\', points=\'0\', baseTime=\'12:00:00\'  WHERE indexID=\'0\'";

            stmt.executeUpdate(resetTimer);
            con.close();
            messenger.sendMessage("Marathon timer reset and started!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setTime(String msg) {
        int sep = msg.indexOf(" ");
        int hr = msg.indexOf(":", sep + 2);
        int min = msg.indexOf(":", hr + 2);
        String hour = msg.substring(sep + 1, hr);
        String minute = msg.substring(hr + 1, min);
        String second = msg.substring(min + 1);
        hours = Integer.parseInt(hour);
        minutes = Integer.parseInt(minute);
        //String formattedTime = hour + ":" + minute + ":" + second;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            String setT = "";
            if (!second.equals("00")) {
                setT = "UPDATE kfTimer SET hours=\'" + hour + "\', minutes=\'" + minute + "\', seconds=\'" + second + "\' WHERE indexID=\'0\'";
            } else {
                setT = "UPDATE kfTimer SET hours=\'" + hour + "\', minutes=\'" + minute + "\' WHERE indexID=\'0\'";
            }
            stmt.executeUpdate(setT);
            con.close();
            addPoints("!addPoints 0");
            messenger.sendMessage("Time set to: " + hour + ":" + minute + ":" + second);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //set value of base time(aka marathon lowest end time, normally 12 hours)
    public void setBaseTime(String msg){
        int amt = Integer.parseInt(msg.substring(msg.indexOf(" ")));
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            String setM = "";
            setM = "UPDATE kfTimer SET baseTime=\'" + amt + "\' WHERE indexID=\'0\'";
            stmt.executeUpdate(setM);
            con.close();
            addPoints("!addPoints 0");
            messenger.sendMessage("Marathon base time set to " + amt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    //set the value of each minute in the SQL database
    public void setMinValue(String msg) {
        int amt = Integer.parseInt(msg.substring(msg.indexOf(" ")));
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            String setM = "";
            setM = "UPDATE kfTimer SET minValue=\'" + amt + "\' WHERE indexID=\'0\'";
            stmt.executeUpdate(setM);
            con.close();
            addPoints("!addPoints 0");
            messenger.sendMessage("Point/Minute value set to " + amt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
