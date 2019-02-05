/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class ScreenEditController implements Initializable {

    ScreensController myController = new ScreensController();
    Datastore store;
    Connection con = null;
    Statement stmt = null;
    String sqlStatement = "";
    String SQLOverlay;
    String PASS;
    String USER;
    
    @FXML
    Label submitStatus;

    @FXML
    RadioButton set1;

    @FXML
    RadioButton set2;

    @FXML
    ToggleGroup setGroup;

    @FXML
    TextArea alertText1;

    @FXML
    TextArea alertText2;

    @FXML
    MenuItem song;

    @FXML
    MenuItem regular;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        submitStatus.setText("Ready to update");
        store = guiHandler.bot.getStore();
        SQLOverlay = store.getConfiguration().sqlOverlay;
        PASS = store.getConfiguration().sqlPass;
        USER = store.getConfiguration().sqlUser;
        // grab text from store 
        ConfigParameters.Alerts alert1 = store.getAlerts().get(0);
        ConfigParameters.Alerts alert2 = store.getAlerts().get(1);
        String set1Text = alert1.name;
        set1.setText(set1Text);
        String set2Text = alert2.name;
        set2.setText(set2Text);
        boolean set1Enabled = alert1.enabled;
        if (set1Enabled) {
            setGroup.selectToggle(set1);
            alertText1.setText(alert1.text1);
            alertText2.setText(alert1.text2);
        } else {
            setGroup.selectToggle(set2);
            alertText1.setText(alert2.text1);
            alertText2.setText(alert2.text2);
        }

        setGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ob,
                    Toggle o, Toggle n) {

                RadioButton rb = (RadioButton) setGroup.getSelectedToggle();

                if (rb != null) {
                    String s = rb.getText();

                    // change the label 
                    if (s.equals(alert1.name)) {
                        alertText1.setText(alert1.text1);
                        alertText2.setText(alert1.text2);
                    } else if (s.equals(alert2.name)) {
                        alertText1.setText(alert2.text1);
                        alertText2.setText(alert2.text2);
                    }
                }
            }

        });

    }

    // submits to both xml file and sql on website
    @FXML
    public void submit() {
        alertText1.selectAll();
        alertText1.copy();
        String value1 = alertText1.getText();
        alertText2.selectAll();
        alertText2.copy();
        String value2 = alertText2.getText();
        int valueID;
        // send contents to store and sql
        ConfigParameters.Alerts alert1 = store.getAlerts().get(0);
        ConfigParameters.Alerts alert2 = store.getAlerts().get(1);
        String selected = ((RadioButton) setGroup.getSelectedToggle()).getText();
        if(selected.equals(alert1.name)){
            valueID = 1;
        } else {
            valueID = 2;
        }
        if (store.setAlertAttribute(selected, "text1", value1) && store.setAlertAttribute(selected, "text2", value2)) {
            submitStatus.setText("Sucessfully set " + selected + " values");
            
        } else {
            submitStatus.setText("Failed to set " + selected + " values");
        }
        if(!setScreenText(valueID, value1, value2)){
            submitStatus.setText("SQL failed to connect");
        }
    }

    @FXML
    public void activate() {
        // enable and disable in xml(enabled true or false) and sql(active 1 or 0)
        String selected = ((RadioButton) setGroup.getSelectedToggle()).getText();
        String alert1Name = store.getAlerts().get(0).name;
        String alert2Name = store.getAlerts().get(1).name;
        if(selected.equals(alert1Name)){
            // if set 1 is selected enable set 1, disable set 2
            store.setAlertAttribute(alert1Name, "enabled", "true");
            store.setAlertAttribute(alert2Name, "enabled", "false");
            sendEvent(alert1Name + " is now active");
            // set active values in sql
            setActiveSet(1); // int value of set i.e. set 1 = 1
            submitStatus.setText(alert1Name + " is now active");
        } else {
            // else opposite
            store.setAlertAttribute(alert2Name, "enabled", "true");
            store.setAlertAttribute(alert1Name, "enabled", "false");
            sendEvent(alert2Name + " is now active");
            // set active values in sql
            setActiveSet(2); // int value of set i.e. set 1 = 1
            submitStatus.setText(alert2Name + " is now active");
        }
    }

    // TODO preview panel? 
    @FXML
    public void dash() {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }
    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
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
    
    
  
    public String getScreenText() {

        return "";
    }

    public boolean setScreenText(int valueID, String txt1, String txt2) {
        //UPDATE `overlayData` SET `value1` = 'test 1', `value2` = 'test4' WHERE `overlayData`.`value2` = '';
        sqlStatement = ("UPDATE overlayData SET value1 = \'" + txt1 + "\', value2 = \'" + txt2 + "\' WHERE valueID = " + valueID + "");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLOverlay, USER, PASS);
            stmt = con.createStatement();
            stmt.execute(sqlStatement);
            con.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean setActiveSet(int valueID) {
        int otherID = 1;
        if(valueID == 1){
            otherID = 2;
        } 
        sqlStatement = ("UPDATE overlayData SET active = 1 WHERE valueID = " + valueID + "");
        String sqlStatement2 = ("UPDATE overlayData SET active = 0 WHERE valueID = " + otherID + "");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(SQLOverlay, USER, PASS);
            stmt = con.createStatement();
            stmt.execute(sqlStatement);
            stmt.execute(sqlStatement2);
            con.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
