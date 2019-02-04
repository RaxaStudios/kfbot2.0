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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class RaidEditController implements Initializable {

    ScreensController myController = new ScreensController();
    String globalSource = "";

    @FXML
    RadioButton usageSpecific;
    
    @FXML
    RadioButton usageAll;
    
    @FXML
    ToggleGroup usage;
    
    @FXML
    TextArea message;
    
    @FXML
    TextField minEnabled;

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
        // TODO fix globalSource and options in XML
        globalSource = "raidMessage";
        if (store.modifyConfiguration(globalSource, newText)) {
            submitStatus.setText(globalSource + " set to " + newText);
        } else {
            submitStatus.setText("failed to set new value");
        }
    }

    @FXML
    public void submitSettings() {
        Datastore store = guiHandler.bot.getStore();
        // set min enabled amount
        minEnabled.selectAll();
        minEnabled.copy();
        String min = minEnabled.getText();
        if (store.modifyConfiguration("raidReplyMin", min)) {
            submitStatus.setText("Min viewers set to " + min);
        } else {
            submitStatus.setText("Failed to set min viewers");
        }
        // set usage TODO here, use all for everything for now
        RadioButton toggle = (RadioButton) usage.getSelectedToggle();
        if(toggle.getText().equals("Use All")){
            System.out.println("Using msg for all");
        } else if (toggle.getText().equals("Usage Specific")){
            System.out.println("Using specific msgs for amts");
        }
    }
    
    @FXML
    public void buttonPress(ActionEvent event) {
// TODO allow for options, for now all get same message
// TODO allow for %game variable to show what streamer was last playing
        Datastore store = guiHandler.bot.getStore();
        String text = "";
        String header = "";
        String selected = "";
        String source = event.getSource().toString();
        source = source.substring(source.indexOf("'") + 1, source.indexOf("'", source.indexOf("'") + 1));
        if (source.equals("0 - 49")) {
            globalSource = "raid1";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 0 - 49";
        } else if (source.equals("50 - 99")) {
            globalSource = "raid2";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 50 - 99";
        } else if (source.equals("100 - 149")) {
            globalSource = "raid3";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 100 - 149";
        } else if (source.equals("150 - 200")) {
            globalSource = "raid4";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 150 - 200";
        } else if (source.equals("200 - 499")) {
            globalSource = "raid5";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 200 - 499";}
        
            else if (source.equals("500 +")) {
            globalSource = "raid6";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 500+";
        } 
         else if (source.equals("All")) {
            globalSource = "raidAll";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: All";
        }
        message.setText(text);
        messageHeader.setText(header);
        messageSelected.setText(selected);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO setup individual systems and allow for min enabled
        minEnabled.setText("1");
        Datastore store = guiHandler.bot.getStore();
        usageAll.setSelected(true);
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

    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }
}
