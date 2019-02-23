/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.handlers.MarathonHandler;
import com.twitchbotx.bot.handlers.sqlHandler;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class MarathonController implements Initializable {

    String SQLUSER;
    String SQLPASS;
    String SQLURL;
    Connection con = null;

    private Datastore store;

    ScreensController myController = new ScreensController();

    static Statement stmt = null;

    private final MarathonHandler mHandler = new MarathonHandler(guiHandler.bot.getStore());

    @FXML
    TextField addPointsText;

    @FXML
    TextField setHour;

    @FXML
    TextField setMin;

    @FXML
    TextField setSec;

    @FXML
    TextField setSubText;
    
    @FXML
    TextField setDollarText;
    
    @FXML
    TextField setDollarMinText;
    
    @FXML
    TextField setBaseHour;

    @FXML
    TextField setBaseMin;

    @FXML
    TextField setBaseSec;

    @FXML
    TextField setMinuteValue;
    
    @FXML
    TextField maxHour;

    @FXML
    Label currentBaseTime;

    @FXML
    Label currentTotalPoints;

    @FXML
    Label currentAddedTime;

    @FXML
    Label currentMinValue;

    @FXML
    Label currentSubPointValue;
    
    @FXML
    Label currentDollarValue;
    
    @FXML
    Label currentTotalTime;
    
    @FXML
    RadioButton marEnabled;

    @FXML
    RadioButton marDisabled;
    
    @FXML
    ToggleGroup Marathon;
    

        @FXML
    private void submitSubPoint(ActionEvent event){
        setSubText.selectAll();
        setSubText.copy();
        String amt = setSubText.getText();   
        mHandler.setSubValue(amt);
    }
    
    @FXML
    private void submitDollarPoint(ActionEvent event){
        setDollarText.selectAll();
        setDollarText.copy();
        String dollar = setDollarText.getText();
        setDollarMinText.selectAll();
        setDollarMinText.copy();
        String minute = setDollarMinText.getText();
        mHandler.setDollarValue(dollar, minute);
    }
    
    
    
    @FXML
    private void submitSetTime(ActionEvent event) {
        String msg;
        String hour;
        String min;
        String sec;
        setHour.selectAll();
        setHour.copy();
        hour = setHour.getText();
        setMin.selectAll();
        setMin.copy();
        min = setMin.getText();
        setSec.selectAll();
        setSec.copy();
        sec = setSec.getText();
        msg = "!setTime " + hour + ":" + min + ":" + sec;
        System.out.println(msg);
        mHandler.setTime(msg);
    }

    @FXML
    private void submitSetBaseTime(ActionEvent event) {
        String msg;
        String value;
        String hr;
        String min;
        String sec;
        setBaseHour.selectAll();
        setBaseHour.copy();
        hr = setBaseHour.getText();
        setBaseMin.selectAll();
        setBaseMin.copy();
        min = setBaseMin.getText();
        setBaseSec.selectAll();
        setBaseSec.copy();
        sec = setBaseSec.getText();
        value = hr + ":" + min + ":" + sec;
        msg = "!setBaseTime " + value;
        mHandler.setBaseTime(msg);
    }

    @FXML
    private void submitSetMinuteValue(ActionEvent event) {
        String msg = "";
        String value = "0";
        setMinuteValue.selectAll();
        setMinuteValue.copy();
        value = setMinuteValue.getText();
        msg = "!setMinValue " + value;
        mHandler.setMinValue(msg);
    }

    @FXML
    private void setMaxHour(){
        String hr = "";
        maxHour.selectAll();
        maxHour.copy();
        hr = maxHour.getText();
        store.modifyConfiguration("maxMarathonHour", hr);
        
    }
    
    @FXML
    private void addMinutes(ActionEvent event) {
        String msg = "";
        String points = "";
        addPointsText.selectAll();
        addPointsText.copy();
        points = addPointsText.getText();
        mHandler.addMinutes(points);
    }

    @FXML
    private void startTime(ActionEvent event) {
        mHandler.startTimer();
    }

    //open the connection
    //static login and table info
    //returns null if connection fails
    Statement connect() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLURL, SQLUSER, SQLPASS);
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

    @FXML
    private void refresh(ActionEvent event) {
        //update totals in Base Time, Total Points, Added Minutes

        //create connection
        Statement baseTime = connect();
        //begin get/set base time
        String getT = "SELECT baseTime FROM kfTimer";
        String timeText = "";
        try {
            ResultSet gT = baseTime.executeQuery(getT);
            ResultSetMetaData gTmd = gT.getMetaData();
            int gC = gTmd.getColumnCount();
            while (gT.next()) {
                for (int i = 1; i <= gC; i++) {
                    timeText = gT.getString(i);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        currentBaseTime.setText(timeText);
        closeConnection();
        
        
        //create points connection
        Statement points = connect();

        //begin get point value
        String getP = "SELECT points FROM kfTimer";
        int currentPoints = 0;
        try {
            ResultSet ap = points.executeQuery(getP);
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
        if (currentPoints > 1) {
            currentTotalPoints.setText(String.valueOf(currentPoints));
        } else if (currentPoints == 0) {
            currentTotalPoints.setText("0");
        } else {
            currentTotalPoints.setText("Error");
        }

        //added minutes calculated by points 500 points = 1 minute
        //test number
        //currentPoints = 500;
        int minuteValue = 500;
        int dollarValue = 0;
        int dollarMinute = 0;
        double subValue = 0;
        //set minute value from sql query
        String minValue = "SELECT minValue, dollarValue, dollarMinute, subValue FROM kfTimer";
        try {
            ResultSet aM = points.executeQuery(minValue);
            while (aM.next()) {
                minuteValue = aM.getInt("minValue");
                dollarValue = aM.getInt("dollarValue");
                dollarMinute = aM.getInt("dollarMinute");
                subValue = aM.getDouble("subValue");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //set default value
        if (minuteValue == 0) {
            minuteValue = 60;
        }
        currentMinValue.setText(String.valueOf(minuteValue));
        currentSubPointValue.setText(String.valueOf(subValue));
        currentDollarValue.setText("$"+String.valueOf(dollarValue)+"/"+String.valueOf(dollarMinute));
        
        
        int hours = 0;
        int minutes = 0;
        System.out.println(hours + "HOURS " + currentPoints + "cP");
        while (currentPoints > (minuteValue - 1)) {
            currentPoints -= minuteValue;
            minutes++;
            if (minutes > 59) {
                hours++;
                minutes = 0;
            }
        }
        System.out.println(hours + "H " + minutes + "M " + currentPoints + "cP");
        String hrFormat = "hrs";
        if (hours == 1) {
            hrFormat = "hr";
        }
        String minFormat = "mins";
        if (minutes == 1) {
            minFormat = "min";
        }
        currentAddedTime.setText(hours + " " + hrFormat + " " + minutes + " " + minFormat);

        //refresh current total time values
        String tTime = "SELECT hours, minutes, seconds FROM kfTimer";

        try {
            ResultSet aT = points.executeQuery(tTime);
            ResultSetMetaData apmd = aT.getMetaData();
            int apc = apmd.getColumnCount();
            StringBuilder amount = new StringBuilder();
            while (aT.next()) {
                for (int i = 1; i <= apc; i++) {
                    if (i != apc) {
                        amount.append(aT.getString(i) + ":");
                    } else {
                        amount.append(aT.getString(i));
                    }
                }
            }
            currentTotalTime.setText(amount.toString());
        } catch (IllegalArgumentException il) {
            System.out.println("Syntax: !s-addPoints [gameID] [points]");
        } catch (SQLException sql) {
            sql.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        closeConnection();
    }

    @FXML
    private void dash(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        store = guiHandler.bot.getStore();
        SQLUSER = store.getConfiguration().sqlUser;
        SQLPASS = store.getConfiguration().sqlPass;
        SQLURL = store.getConfiguration().sqlMURL;
        
        //initialize enable/disable buttons
        setRadios();
        addListener(Marathon, "mStatus");
    }

    private void setRadios(){
        if (store.getConfiguration().marathonStatus.equals("on")) {
            marEnabled.setSelected(true);
        } else {
            marDisabled.setSelected(true);
        }
    }
    
    /**
     * adds a listener to the radio button toggle group instant change in on/off
     * status for group
     * @param ToggleGroup to listen to
     * @param String config name associated with XML value
     */
    private void addListener(ToggleGroup group, String name ) {
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ob,
                    Toggle o, Toggle n) {

                RadioButton rb = (RadioButton) group.getSelectedToggle();

                if (rb != null) {
                    String s = rb.getText();
                    String enabled;
                    if (s.equals("Enabled")) {
                        enabled = "on";
                    } else {
                        enabled = "off";
                    }
                    store.modifyConfiguration(name, enabled);
                    System.out.println(name + ":" + enabled);
                }
            }

        });
    }
    
    
    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }

}
