/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.gui.DashboardController;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Raxa
 *
 * TODO
 *
 * use this over FTP handler with php on website see SpotifyReader ->
 * ServerHandler.java for current version
 *
 */
public class sqlHandler {

    //SQL database
    String SQLURL;
    String USER;
    String PASS;
    TwitchMessenger messenger;
    Connection con = null;
    Statement stmt = null;
    String sqlStatement = "";

    private static final Logger LOGGER = Logger.getLogger(sqlHandler.class.getSimpleName());

    private Datastore store;

    private String gameName;
    private String gameID;
    private String gamePoints;

    public sqlHandler(final Datastore store,
            final PrintStream stream) {
        this.store = store;
        this.SQLURL = store.getConfiguration().sqlURL;
        this.USER = store.getConfiguration().sqlUser;
        this.PASS = store.getConfiguration().sqlPass;
        this.messenger = new TwitchMessenger(stream, store.getConfiguration().joinedChannel);
        this.gameID = "";
        this.gameName = "";
        this.gamePoints = "";
    }

    //open the connection
    //static login and table info
    //returns null if connection fails
    Statement connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, USER, PASS);
            stmt = con.createStatement();
            return stmt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    void closeConnection() {
        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(sqlHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addGame(String msg) {
        Statement add = connect();
        sqlStatement = "";
        closeConnection();
    }

    public void deleteGame(String msg) {
        Statement delete = connect();
        sqlStatement = " ";
        closeConnection();
    }

    public void setName(String msg) {
        Statement setName = connect();
        String oldName = msg.substring(msg.indexOf(" ") + 1, msg.indexOf(" ", msg.indexOf(" ") + 2));
        String newName = msg.substring(msg.indexOf(" ", msg.indexOf(oldName)) + 1, msg.length());
        sqlStatement = "UPDATE bot SET Game=\'" + newName + "\' WHERE Game=\'" + oldName + "\'";
        try {
            setName.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            LOGGER.severe(e.toString());
        }
        messenger.sendMessage("Renamed " + oldName + " to " + newName);
        closeConnection();
    }

    public void setPoints(String msg) {
        Statement setPoints = connect();
        String game = msg.substring(msg.indexOf(" ") + 1, msg.indexOf(" ", msg.indexOf(" ") + 2));
        String newPoints = msg.substring(msg.indexOf(" ", msg.indexOf(game)) + 1, msg.length());
        sqlStatement = "UPDATE bot SET Points=\'" + newPoints + "\' WHERE Game=\'" + game + "\'";
        try {
            setPoints.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            LOGGER.severe(e.toString());
        }
        messenger.sendMessage("Set " + game + " to " + newPoints);
        closeConnection();
    }

    /*
    ** get methods for points, name, id
     */
    public String getData(int index) {
        //index for sql query
        try {
            Statement getData = connect();
            String findData = "SELECT * FROM bot WHERE gameID=\'" + index + "\'";
            String name = "";
            int points = 0;
            int id = 0;
            try {
                ResultSet gd = getData.executeQuery(findData);
                while (gd.next()) {
                    name = gd.getString("Game");
                    points = gd.getInt("Points");
                    id = gd.getInt("gameID");
                }
                String returnData = "#" + id + " " + name + ": " + points;
                return returnData;
            } catch (SQLException sql) {
                LOGGER.severe(sql.toString());
            } finally {
                try {
                    getData.close();
                } catch (SQLException ex) {
                    Logger.getLogger(sqlHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } finally {
            closeConnection();
        }

        gamePoints = "1";
        gameName = "Name";
        gameID = "ID";
        String returnData = "#" + gameID + " " + gameName + ": " + gamePoints;
        return returnData;
    }

    public int getSize() {
        Statement getSize = connect();
        String findSize = "SELECT gameID FROM bot";
        int size = 0;
        try {
            ResultSet gs = getSize.executeQuery(findSize);
            while (gs.next()) {
                size = gs.getInt("gameID");
            }
            return size;
        } catch (SQLException sql) {
            LOGGER.severe(sql.toString());
        } finally {
            closeConnection();
            try {
                getSize.close();
            } catch (SQLException ex) {
                Logger.getLogger(sqlHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return size;
    }

    public void addPoints(String msg) {
        String enabled = this.store.getConfiguration().sStatus;
        System.out.println(enabled);
        if (enabled.equals("on")) {
            Statement addPoints = connect();
            String game = msg.substring(msg.indexOf(" ") + 1, msg.indexOf(" ", msg.indexOf(" ") + 2));
            int points = new Integer(msg.substring(msg.indexOf(" ", msg.indexOf(game)) + 1, msg.length()));
            String findPoints = "SELECT Points FROM bot WHERE Game=\'" + game + "\'";
            System.out.println("Points:" + points + " game:" + game);
            if (points == -1) {
                throw new IllegalArgumentException();
            }

            try {
                ResultSet ap = stmt.executeQuery(findPoints);
                int amount = 0;
                while (ap.next()) {
                    amount = ap.getInt(1);
                }
                amount += points;
                sqlStatement = "UPDATE bot SET Points=" + amount + " WHERE Game=\'" + game + "\'";
                addPoints.execute(sqlStatement);
                messenger.sendMessage(points + " added to " + game);
                sendEvent(game, points);
                closeConnection();
            } catch (IllegalArgumentException il) {
                messenger.sendMessage("Syntax: !s-addPoints [gameID] [points]");
            } catch (SQLException sql) {
                messenger.sendMessage("Syntax: !s-addPoints [gameID] [points]");
                LOGGER.severe(sql.toString());
            }
        }
    }

    public void getPoints(String msg, String username) {

        //cooldown check
        for (int j = 0; j < this.store.getCommands().size(); j++) {
            try {
                final ConfigParameters.Command command = store.getCommands().get(j);

                String cmd = "!points";
                if (cmd.contentEquals(command.name)) {
                    if (!username.contentEquals(store.getConfiguration().joinedChannel)) {
                        Calendar calendar = Calendar.getInstance();
                        java.util.Date now = calendar.getTime();
                        java.util.Date cdTime = new java.util.Date(0L);
                        System.out.println("CDUNTIL: " + command.cdUntil);
                        if (!command.cdUntil.isEmpty()) {
                            cdTime = new java.util.Date(Long.parseLong(command.cdUntil));
                        }
                        if (now.before(cdTime)) {
                            return;
                        }
                        cdTime = new java.util.Date(now.getTime() + Long.parseLong(command.cooldownInSec) * 1000L);
                        store.updateCooldownTimer(command.name, cdTime.getTime());
                        store.commit();
                    }
                }
            } catch (DOMException | NumberFormatException e) {
                LOGGER.severe(e.toString());
            }
        }

        try {
            Statement getPoints = connect();
            getPoints = con.createStatement();
            String wording = "";
            String param = msg.substring(msg.indexOf("!points") + 7, msg.length());
            param = param.replaceAll(" ", "");
            if (param.equals("") || param.isEmpty()) {
                sqlStatement = "SELECT Game, Points FROM bot";
                wording = "Spoopathon point totals: ";
            } else {
                sqlStatement = "SELECT Game, Points FROM bot WHERE game=\'" + param + "\'";
                wording = "Spoopathon point total for ";
            }
            ResultSet sv = getPoints.executeQuery(sqlStatement);
            ResultSetMetaData svmd = sv.getMetaData();
            int svcols = svmd.getColumnCount();
            int format = 0;
            StringBuilder sb = new StringBuilder();
            while (sv.next()) {
                for (int i = 1; i <= svcols; i++) {
                    if (i > 1) {
                        sb.append(":");
                    }
                    format++;
                    if ((format % 2) == 0) {
                        sb.append(sv.getString(i) + " ");
                    } else {
                        sb.append(sv.getString(i));
                    }
                }
            }
            messenger.sendMessage(wording + " " + sb);
            closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gameSearch(String msg, int points) {
        if (msg.contains("#")) {
            Statement gamePoints = connect();

            //check against valid game IDs
            String checkName = "SELECT Game FROM bot";
            String tGame = "";
            try {
                ResultSet cN = stmt.executeQuery(checkName);
                ResultSetMetaData cNmd = cN.getMetaData();
                int cNc = cNmd.getColumnCount();
                int gameCount = 0;
                while (cN.next()) {
                    for (int i = 1; i <= cNc; i++) {
                        if (msg.contains("#" + cN.getString(i))) {
                            tGame = cN.getString(i);
                            gameCount++;
                        }
                    }
                }
                if (gameCount > 1) {
                    messenger.sendMessage("Multiple games found in message");
                } else {
                    String findPoints = "SELECT Points FROM bot WHERE Game=\'" + tGame + "\'";
                    try {
                        ResultSet ap = stmt.executeQuery(findPoints);
                        int amount = 0;
                        while (ap.next()) {
                            amount = ap.getInt(1);
                        }
                        amount += points;
                        sqlStatement = "UPDATE bot SET Points=" + amount + " WHERE Game=\'" + tGame + "\'";
                        gamePoints.execute(sqlStatement);
                        messenger.sendMessage(points + " added to " + tGame);
                        closeConnection();
                    } catch (IllegalArgumentException il) {
                        //messenger.sendMessage("Syntax: !s-addPoints [gameID] [points]");
                        il.printStackTrace();
                    } catch (SQLException sql) {
                        LOGGER.severe(sql.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    /*
    ** This method will enable or disable the addPoints method
    ** meaning no bot messages in chat, no points added to system
    ** quick method to disable system for user side
     */
    public void sStatus(String msg) {
        String status = msg.substring(msg.indexOf(" ") + 1, msg.length());
        if (status.equals("on") || status.equals("off")) {
            store.modifyConfiguration("sStatus", status);
            messenger.sendMessage("Spoopathon system set to " + status);
        } else {
            messenger.sendMessage("Syntax: !s-status [on|off]");
        }

    }

    private void sendEvent(String game, int points) {
        String eventMsg = "Spoopathon Event: " + points + " added to  " + game;
        DashboardController dc = new DashboardController();
        dc.eventObLAdd(eventMsg);
    }

    public void download() {
        try {
            FileUtils.copyURLToFile(new URL("url to jar here"), new File("KFbot-1.1.jar"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
