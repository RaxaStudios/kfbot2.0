/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.controllers.DashboardController;
import com.twitchbotx.gui.guiHandler;
import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.w3c.dom.DOMException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Raxa
 *
 *
 *
 * use this over FTP handler with php on website see SpotifyReader ->
 * ServerHandler.java for current version
 *
 */
public class sqlHandler {

    //SQL database
    String SQLURL;
    String SQLOverlay;
    String USER;
    String PASS;
    Connection con = null;
    Statement stmt = null;
    String sqlStatement = "";

    
    private static final Logger LOGGER = Logger.getLogger(sqlHandler.class.getSimpleName());

    private Datastore store;

    private String gameName;
    private String gameID;
    private String gamePoints;

    public sqlHandler(final Datastore store) {
        this.store = store;
        this.SQLURL = store.getConfiguration().sqlURL;
        this.USER = store.getConfiguration().sqlUser;
        this.PASS = store.getConfiguration().sqlPass;
        // this.messenger = new TwitchMessenger(stream, store.getConfiguration().joinedChannel);
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

    public boolean addGame(String msg) {
        Statement add = connect();
        String gameToAdd = msg.substring(msg.indexOf(" ") + 1);
        String sqlStatementAI;
        int value = 0;
        try {
            //reset auto_increment value using maxid + 1
            ResultSet rs = add.executeQuery("SELECT * FROM bot ORDER BY gameID DESC");
            while (rs.next()) {
                value = rs.getInt("gameID");
            }
            sqlStatementAI = "ALTER TABLE bot AUTO_INCREMENT=" + value;
            add.executeUpdate(sqlStatementAI);
            //execute insertion of value
            sqlStatement = "INSERT INTO bot (Game, Points) VALUES (\'" + gameToAdd + "\', \'0\')";
            add.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            LOGGER.severe(e.toString());
            e.printStackTrace();
        }
        sendMessage(gameToAdd + " added to options");
        closeConnection();
        sendEvent("Spoopathon Event: " + gameToAdd + " added to options");
        return true;
    }

    public boolean deleteGame(String msg) {
        Statement delete = connect();
        String gameToDelete = msg.substring(msg.indexOf(" ") + 1);
        sqlStatement = "DELETE FROM bot WHERE Game=\'" + gameToDelete + "\'";
        //sqlStatement = "DELETE FROM bot where gameID=\'0\'";
        try {
            delete.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            LOGGER.severe(e.toString());
        }
        sendMessage(gameToDelete + " deleted from options");
        closeConnection();
        sendEvent("Spoopathon Event: " + gameToDelete + " deleted from options");
        return true;
    }

    public boolean setName(String msg) {
        Statement setName = connect();
        String oldName = msg.substring(msg.indexOf(" ") + 1, msg.indexOf(" ", msg.indexOf(" ") + 2));
        String newName = msg.substring(msg.indexOf(" ", msg.indexOf(oldName)) + 1, msg.length());
        sqlStatement = "UPDATE bot SET Game=\'" + newName + "\' WHERE Game=\'" + oldName + "\'";
        try {
            setName.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            LOGGER.severe(e.toString());
        }
        sendMessage("Renamed " + oldName + " to " + newName);
        closeConnection();
        return true;
    }

    public boolean setPoints(String msg) {
        Statement setPoints = connect();
        String game = msg.substring(msg.indexOf(" ") + 1, msg.indexOf(" ", msg.indexOf(" ") + 2));
        String newPoints = msg.substring(msg.indexOf(" ", msg.indexOf(game)) + 1, msg.length());
        sqlStatement = "UPDATE bot SET Points=\'" + newPoints + "\' WHERE Game=\'" + game + "\'";
        try {
            setPoints.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            LOGGER.severe(e.toString());
        }
        sendMessage("Set " + game + " to " + newPoints);
        closeConnection();
        sendEvent("Spoopathon Event: " + game + " set to " + newPoints);
        return true;
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

    /**
     *
     * @param msg !s-addPoints [gameId] [points]
     * @return boolean success
     *
     * to be updated for alternate command input ie just !addPoints [game]
     * [points]
     */
    public boolean addPoints(String msg) {
        String enabled = this.store.getConfiguration().spoopathonStatus;
        
        if (enabled.equals("on")) {
            Statement addPoints = connect();
            String game = msg.substring(msg.indexOf(" ") + 1, msg.indexOf(" ", msg.indexOf(" ") + 2));
            int pPosition = msg.indexOf(" ", msg.indexOf(game));
            int points = new Integer(msg.substring(msg.indexOf(" ", msg.indexOf(game)) + 1, msg.length()));
            String findPoints = "SELECT Points FROM bot WHERE Game=\'" + game + "\'";
            System.out.println("Points:" + points + " game:" + game);
            if (pPosition == -1) {
                //parameter argument
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
                sendMessage(points + " added to " + game);
                sendEvent("Spoopathon Event: " + points + " added to  " + game);
                closeConnection();
                return true;
            } catch (IllegalArgumentException il) {
                sendMessage("Syntax: !s-addPoints [gameID] [points]");
                return false;
            } catch (SQLException sql) {
                sendMessage("Syntax: !s-addPoints [gameID] [points]");
                LOGGER.severe(sql.toString());
                return false;
            }
        }
        return false;
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
                        sb.append(": ");
                    }
                    format++;
                    if ((format % 2) == 0) {
                        sb.append(sv.getString(i) + " ");
                    } else {
                        sb.append(sv.getString(i));
                    }
                }
            }
            sendMessage(wording + " " + sb);
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
                    sendMessage("Multiple games found in message");
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
                        sendMessage(points + " added to " + tGame);
                        closeConnection();
                    } catch (IllegalArgumentException il) {
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
            sendMessage("Spoopathon system set to " + status);
        } else {
            sendMessage("Syntax: !s-status [on|off]");
        }

    }

  /*
    begin counter section
    */
    public boolean setCountValue(int value){
        Statement setCount = connectCounter();
        
        sqlStatement = "UPDATE botCount SET value=\'" + value + "\' WHERE 1";
        try {
            setCount.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe(e.toString());
        }
        closeCounterConnection();
        return true;
    }
    
    public boolean setCountText(String txt){        
        Statement setText = connectCounter();        
        sqlStatement = "UPDATE botCount SET text=\'" + txt + "\' WHERE 1";
        try {
            setText.executeUpdate(sqlStatement);  
        } catch (SQLException e) {
            e.printStackTrace();
            LOGGER.severe(e.toString());
        }
        closeCounterConnection();
        return true;
    }
    
    Statement connectCounter() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(store.getConfiguration().sqlCounter, USER, PASS);
            stmt = con.createStatement();
            return stmt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    void closeCounterConnection() {
        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(sqlHandler.class.getName()).log(Level.SEVERE, null, ex);
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

    public void download() {
        try {
            FileUtils.copyURLToFile(new URL("url to jar here"), new File("KFbot-1.1.jar"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
