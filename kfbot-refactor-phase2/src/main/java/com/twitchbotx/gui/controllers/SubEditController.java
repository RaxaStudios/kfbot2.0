/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class SubEditController implements Initializable {

    ScreensController myController = new ScreensController();
    String globalSource = "";

    @FXML
    TextArea message;

    @FXML
    Label submitStatus;

    @FXML
    Label messageHeader;
    
    @FXML
    Label messageSelected;

    @FXML
    public void submitChanges() {
        Datastore store = guiHandler.bot.getStore();
        String newText;
        message.selectAll();
        message.copy();
        newText = message.getText();
        if (store.modifyConfiguration(globalSource, newText)) {
            submitStatus.setText(globalSource + " set to " + newText);
        } else {
            submitStatus.setText("failed to set new value");
        }
    }

    /**
     * Take button info and act accordingly
     *
     * @param event
     *
     * send to messageHeader label
     */
    @FXML
    public void buttonPress(ActionEvent event) {
        
        /// TODO anonymous sub gift option?
        Datastore store = guiHandler.bot.getStore();
        String text = "";
        String header = "";
        String selected = "";
        String source = event.getSource().toString();
        source = source.substring(source.indexOf("'") + 1, source.indexOf("'", source.indexOf("'") + 1));
        if (source.equals("New Non-Prime")) {
            globalSource = "subNewNormalReply";
            text = store.getConfiguration().subNewNormalReply;
            header = "Available variables: %user %tier";
            selected = "Selected: New Non-Prime";
        } else if (source.equals("Non-Prime Resub")) {
            globalSource = "subNormalReply";
            text = store.getConfiguration().subNormalReply;
            header = "Available variables: %user %months %tier";
            selected = "Selected: Non-Prime Resub";
        } else if (source.equals("Prime New")) {
            globalSource = "subNewPrimeReply";
            text = store.getConfiguration().subNewPrimeReply;
            header = "Available variables: %user";
            selected = "Selected: Prime New";
        } else if (source.equals("Prime Resub")) {
            globalSource = "subPrimeReply";
            text = store.getConfiguration().subPrimeReply;
            header = "Available variables: %user %months";
            selected = "Selected: Prime Resub";
        } else if (source.equals("Single Gift")) {
            globalSource = "subSingleGiftReply";
            text = store.getConfiguration().subSingleGiftReply;
            header = "Available variables: %user %recipient %tier";
            selected = "Selected: Single Gift";
        } else if (source.equals("Mass Gift")) {
            globalSource = "subMassGiftReply";
            text = store.getConfiguration().subMassGiftReply;
            header = "Available variables: %user %gifts %tier";
            selected = "Selected: Mass Gift";
        }
        message.setText(text);
        messageHeader.setText(header);
        messageSelected.setText(selected);
    }

    @FXML
    public void goDash() {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    // Change to sub editing page
    @FXML
    private void goResponse(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.respEditID, guiHandler.respEditFile);
        myController.setScreen(guiHandler.respEditID);
        myController.setId("RespEdit");
        myController.show(myController);
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }
}
