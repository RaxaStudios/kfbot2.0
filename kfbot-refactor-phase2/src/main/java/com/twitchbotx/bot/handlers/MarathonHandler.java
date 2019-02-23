/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.TwitchBotX;
import com.twitchbotx.gui.controllers.DashboardController;
import com.twitchbotx.gui.guiHandler;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @author Raxa
 */
public class MarathonHandler {

    private static final Logger LOGGER = Logger.getLogger(TwitchBotX.class.getSimpleName());

    private final Datastore store = guiHandler.bot.getStore();

    String SQLURL = store.getConfiguration().sqlMURL;
    String USER = store.getConfiguration().sqlUser;
    String PASS = store.getConfiguration().sqlPass;

    static Connection con = null;
    static Statement stmt = null;
    static String sqlStatement = "";
    static boolean first = true;
    int minutes = 0;
    int hours = 12;
    int bitPool = 0;

    public MarathonHandler(Datastore store) {
        
    }

    public void addPoints(String msg) {

        //tier1 = 500 = 1 sub point, tier2 = 1000 = 2 sub points, tier3 = 2500 = 6 sub points
        /*
        ** 1 sub point = 5 minutes
        ** $2 = 1 minute
        ** 1 point = 1 second
         */
        int sep = msg.indexOf(" ");
        int amt = new Integer(msg.substring(sep + 1));
        boolean update = false;
        if (amt == 0) {
            update = true;
        }
        int pointHours = 0;
        int pointMins = 0;
        int preMins = 0;
        //get point value, add amt, update point value directly
        String getP = "SELECT points, baseHour, minutes FROM kfTimer";
        int currentPoints = 0;
        int baseHour = 0;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            ResultSet ap = stmt.executeQuery(getP);
            while (ap.next()) {
                currentPoints = ap.getInt("points");
                baseHour = ap.getInt("baseHour");
                preMins = ap.getInt("minutes");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("amt= " + amt + " currentPoints= " + currentPoints);
        amt = amt + currentPoints;
        int sendAmt = amt;
        //amt -> time value -> update sql total time
        while (amt > (59)) {
            amt -= 60;
            pointMins++;
            if (pointMins > 59) {
                pointHours++;
                pointMins = 0;
            }
        }
        int pointSeconds = amt;
        //find change in minutes
        int deltaMins = pointMins - preMins;
        System.out.println(pointHours + " " + pointMins);
        //get base hour and add to hours prior to update
        //take max hour setting from configuration 
        pointHours = pointHours + baseHour;
        int maxHour = store.getConfiguration().maxMarathonHour;
        if (pointHours > (maxHour - 1)) {
            pointHours = maxHour;
            pointMins = 0;
            pointSeconds = 0;
            deltaMins = 999;
        }

        //begin update sql point value
        String addP = "Update kfTimer SET points = \'" + sendAmt + "\', hours=\'" + pointHours + "\', minutes=\'" + pointMins + "\', seconds=\'" + pointSeconds + "\' WHERE indexID=\'0\'";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            stmt.execute(addP);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(addP);
        if (update) {
            sendEvent("Updating marathon timer");
        } else {
            if (deltaMins > 0 && deltaMins != 999) {
                sendEvent("Added " + deltaMins + " minutes to marathon");
                sendMessage("Added " + deltaMins + " minutes to marathon");
            } else if (deltaMins == 999) {
                sendEvent("Maximum hour reached");
            } else {
                sendEvent("Error occurred adding minutes to marathon");
            }
        }
    }

    //sub addition system -> addPoints
    public void addSub(int subPoints, boolean massGift, boolean gifted, int giftAmount) {
        //convert sub points to regular points
        //TODO utilize the setting made available in marathon controller for this
        //hard coded for 4/27 marathon 1 sub point = 5 minutes = (200points/1minute)*5 = 1000 points

        //get value of sub from sql, 1 sub point = subPointValue in minutes @ 60 points = 1 minute
        double subPointValue = 0;
        //set minute value from sql query
        String subValue = "SELECT subValue FROM kfTimer";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            ResultSet aM = stmt.executeQuery(subValue);
            while (aM.next()) {
                subPointValue = aM.getInt("subValue");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //set default value 1 sub point = 5 minutes
        if (subPointValue == 0) {
            subPointValue = 5;
        }

        int pointsToAdd = 0;
        double minutesToAdd = 0;
        // adjust for  sub gifts/mass gifts
        if (massGift) {
            minutesToAdd = subPoints * subPointValue * giftAmount;
        } else {
            minutesToAdd = subPoints * subPointValue;
        }
        // 1 sub point * minute value
        pointsToAdd = (int) minutesToAdd * 60; //convert minutes to seconds, 1 point = 1 sec
        addPoints("!addPoints " + pointsToAdd);

    } 

    //dollar addition system -> addPoints
    public void addDollars(int dollars) {
        //convert dollars to regular points
        //TODO utilize the setting made in marathon controller for this
        //hard coded for 4/27 marathon $2 = 1 minute = 200points

        //dollar value comes in as $10.00 = 10
        //get value of dollar from sql
        //$ amount and how many minutes equal 
        int dollarValue = 0;
        int dollarMinute = 0;
        //set minute value from sql query
        String minValue = "SELECT dollarValue, dollarMinute FROM kfTimer";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            ResultSet aM = stmt.executeQuery(minValue);
            while (aM.next()) {
                dollarValue = aM.getInt("dollarValue");
                dollarMinute = aM.getInt("dollarMinute");
            }
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //set default value of $2 = 1 minute
        if (dollarValue == 0) {
            dollarValue = 2;
        }
        if (dollarMinute == 0) {
            dollarMinute = 1;
        }

        int pointsToAdd = 0;
        double minutesToAdd = dollars * ((double) dollarMinute / dollarValue);
        pointsToAdd = (int) minutesToAdd * 60;

        addPoints("!addPoints " + pointsToAdd);
    }

    // bit addition system -> addPoints
    public void addBits(int bits) {
        int b = 0;
        // $1 = 100 bits, convert and send
        if (bits > 99) {
            b = (int) ((double) (bits) / 100);
        } else {
            bitPool = bitPool + bits;
            if (bitPool > 99) {
                b = b + 1;
                bitPool = bitPool - 100;
            }
        }
        addPoints("!addPoints " + b);
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
            sendMessage("Total marathon time: " + amount);
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
            String resetTimer = "Update kfTimer SET startTime=\'" + now + "\', hours=\'10\', minutes=\'00\', seconds=\'00\', points=\'0\', baseTime=\'10:00:00\'  WHERE indexID=\'0\'";

            stmt.executeUpdate(resetTimer);
            con.close();
            sendMessage("Marathon timer reset and started!");
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
            sendMessage("Time set to: " + hour + ":" + minute + ":" + second);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSubValue(String value) {
        //sql statement update subValue item
        String updateS = "Update kfTimer SET subValue = \'" + value + "\' WHERE indexID=\'0\'";

        //begin update sql point value
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            stmt.execute(updateS);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDollarValue(String dollar, String minute) {
        //sql statement update items dollarValue and dollarMinute
        String updateD = "Update kfTimer SET dollarValue = \'" + dollar + "\', dollarMinute = \'" + minute + "\' WHERE indexID=\'0\'";

        //begin update sql point value
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            stmt.execute(updateD);
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addMinutes(String minutes) {
        //convert to points and !addPoints
        double minutesToAdd = Double.parseDouble(minutes);
        double points = minutesToAdd * 60;
        int p = (int) points;
        System.out.println("adding " + p + " points/seconds" + " points=" + points);
        addPoints("!addPoints " + p);
    }

    //set value of base time(aka marathon lowest end time, normally 12 hours)
    public void setBaseTime(String msg) {
        String amt = msg.substring(msg.indexOf(" ") + 1);
        String hr = msg.substring(msg.indexOf(" "), msg.indexOf(":", msg.indexOf(" ")));
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            String setM = "";
            setM = "UPDATE kfTimer SET baseTime=\'" + amt + "\', baseHour\'" + hr + "\' WHERE indexID=\'0\'";
            stmt.executeUpdate(setM);
            con.close();
            addPoints("!addPoints 0");
            sendMessage("Marathon base time set to " + amt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //set the value of each minute in the SQL database
    public void setMinValue(String msg) {
        String amt = msg.substring(msg.indexOf(" ") + 1);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            String setM = "";
            setM = "UPDATE kfTimer SET minValue=\'" + amt + "\' WHERE indexID=\'0\'";
            stmt.executeUpdate(setM);
            con.close();
            addPoints("!addPoints 0");
            sendMessage("Point/Minute value set to " + amt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEvent(final String msg) {
        String event = msg;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                store.getEventList().addList(event);
            }
        });
    }

    private void sendMessage(final String msg) {
        DashboardController.wIRC.sendMessage(msg, true);
    }
}
