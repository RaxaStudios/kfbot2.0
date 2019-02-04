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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class AlertResponseController implements Initializable {

    ScreensController myController = new ScreensController();
    Datastore store;

    @FXML
    RadioButton subEnabled;
    @FXML
    RadioButton subDisabled;
    @FXML
    ToggleGroup Sub;

    @FXML
    RadioButton bitEnabled;
    @FXML
    RadioButton bitDisabled;
    @FXML
    ToggleGroup Bit;

    @FXML
    RadioButton raidEnabled;
    @FXML
    RadioButton raidDisabled;
    @FXML
    ToggleGroup Raid;

    @FXML
    RadioButton screenEnabled;
    @FXML
    RadioButton screenDisabled;
    @FXML
    ToggleGroup Screen;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        store = guiHandler.bot.getStore();
        
        // initialize radio button group/systems
        setRadios();
        addListener(Sub, "subReply");
        addListener(Bit, "bitReply");
        addListener(Raid, "raidReply");
        
        // intializers for subs/bits/raids variables
        minBitEnabled.setText(store.getConfiguration().bitReplyMin);
        usageBitAll.setSelected(true);
        minRaidEnabled.setText(store.getConfiguration().raidReplyMin);
        usageRaidAll.setSelected(true);
    }

    private void setRadios(){
        if (store.getConfiguration().subReply.equals("on")) {
            subEnabled.setSelected(true);
        } else {
            subDisabled.setSelected(true);
        }
        if (store.getConfiguration().bitReply.equals("on")) {
            bitEnabled.setSelected(true);
        } else {
            bitDisabled.setSelected(true);
        }
        if (store.getConfiguration().raidReply.equals("on")) {
            raidEnabled.setSelected(true);
        } else {
            raidDisabled.setSelected(true);
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

    @FXML
    public void goDash() {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    @FXML
    public void goFeature() {
        setDimensions();
        myController.loadScreen(guiHandler.featuresID, guiHandler.featuresFile);
        myController.setScreen(guiHandler.featuresID);
        myController.setId("features");
        myController.show(myController);
    }
    
    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }
    
    
    /**
     * Begin Sub Editing 
     * Non-shared variables
     */
    String globalSubSource = "";

    @FXML
    TextArea messageSub;

    @FXML
    Label submitSubStatus;

    @FXML
    Label messageSubHeader;
    
    @FXML
    Label messageSubSelected;

    @FXML
    public void submitSubChanges() {
        String newText;
        messageSub.selectAll();
        messageSub.copy();
        newText = messageSub.getText();
        if (store.modifyConfiguration(globalSubSource, newText)) {
            submitSubStatus.setText(globalSubSource + " set to " + newText);
        } else {
            submitSubStatus.setText("failed to set new value");
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
    public void buttonSubPress(ActionEvent event) {
        String text = "";
        String header = "";
        String selected = "";
        String source = event.getSource().toString();
        source = source.substring(source.indexOf("'") + 1, source.indexOf("'", source.indexOf("'") + 1));
        if (source.equals("New Non-Prime")) {
            globalSubSource = "subNewNormalReply";
            text = store.getConfiguration().subNewNormalReply;
            header = "Available variables: %user %tier";
            selected = "Selected: New Non-Prime";
        } else if (source.equals("Non-Prime Resub")) {
            globalSubSource = "subNormalReply";
            text = store.getConfiguration().subNormalReply;
            header = "Available variables: %user %months %tier";
            selected = "Selected: Non-Prime Resub";
        } else if (source.equals("Prime New")) {
            globalSubSource = "subNewPrimeReply";
            text = store.getConfiguration().subNewPrimeReply;
            header = "Available variables: %user";
            selected = "Selected: Prime New";
        } else if (source.equals("Prime Resub")) {
            globalSubSource = "subPrimeReply";
            text = store.getConfiguration().subPrimeReply;
            header = "Available variables: %user %months";
            selected = "Selected: Prime Resub";
        } else if (source.equals("Single Gift")) {
            globalSubSource = "subSingleGiftReply";
            text = store.getConfiguration().subSingleGiftReply;
            header = "Available variables: %user %recipient %tier";
            selected = "Selected: Single Gift";
        } else if (source.equals("Mass Gift")) {
            globalSubSource = "subMassGiftReply";
            text = store.getConfiguration().subMassGiftReply;
            header = "Available variables: %user %gifts %tier";
            selected = "Selected: Mass Gift";
        }
        messageSub.setText(text);
        messageSubHeader.setText(header);
        messageSubSelected.setText(selected);
    }
    
    /**
     * Begin Bit Editing
     * Non-shared variables
     */
    String globalBitSource = "";

    @FXML
    RadioButton usageBitSpecific;
    
    @FXML
    RadioButton usageBitAll;
    
    @FXML
    ToggleGroup usageBit;
    
    @FXML
    TextArea messageBit;

    @FXML
    TextField minBitEnabled;

    @FXML
    Label submitBitStatus;

    @FXML
    Label messageBitHeader;

    @FXML
    Label messageBitSelected;

    @FXML
    public void submitBitChanges() {
        String newText;
        messageBit.selectAll();
        messageBit.copy();
        newText = messageBit.getText();
        // TODO fix globalSource for options
        globalBitSource = "bitMessage";
        if (store.modifyConfiguration(globalBitSource, newText)) {
            submitBitStatus.setText(globalBitSource + " set to " + newText);
        } else {
            submitBitStatus.setText("failed to set new value");
        }
    }

    @FXML
    public void submitBitSettings() {
        // set min enabled amount
        minBitEnabled.selectAll();
        minBitEnabled.copy();
        String min = minBitEnabled.getText();
        if (store.modifyConfiguration("bitReplyMin", min)) {
            submitBitStatus.setText("Min bit set to " + min);
        } else {
            submitBitStatus.setText("Failed to set min bit");
        }
        // set usage TODO here, use all for everything for now
        RadioButton toggle = (RadioButton) usageBit.getSelectedToggle();
        if(toggle.getText().equals("Use All")){
            System.out.println("Using msg for all");
        } else if (toggle.getText().equals("Usage Specific")){
            System.out.println("Using specific msgs for amts");
        }
    }

    @FXML
    public void buttonBitPress(ActionEvent event) {
        // TODO allow for options, for now all get same message
        String text = "";
        String header = "";
        String selected = "";
        String source = event.getSource().toString();
        source = source.substring(source.indexOf("'") + 1, source.indexOf("'", source.indexOf("'") + 1));
        if (source.equals("1 - 499")) {
            globalBitSource = "bit1";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: 1 - 499";
        } else if (source.equals("500 - 999")) {
            globalBitSource = "bit2";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: 500 - 999";
        } else if (source.equals("1,000 - 4,999")) {
            globalBitSource = "bit3";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: 1,000 - 4,999";
        } else if (source.equals("5,000 - 9,999")) {
            globalBitSource = "bit4";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: 5,000 - 9,999";
        } else if (source.equals("10,000+")) {
            globalBitSource = "bit5";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: 10,000+";
        } else if (source.equals("All")) {
            globalBitSource = "bitAll";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: All";
        }
        messageBit.setText(text);
        messageBitHeader.setText(header);
        messageBitSelected.setText(selected);
    }
 
    /**
     * Begin Raid Editing
     * No shared variables
     */
    String globalRaidSource = "";

    @FXML
    RadioButton usageRaidSpecific;
    
    @FXML
    RadioButton usageRaidAll;
    
    @FXML
    ToggleGroup usageRaid;
    
    @FXML
    TextArea messageRaid;
    
    @FXML
    TextField minRaidEnabled;

    @FXML
    Label submitRaidStatus;

    @FXML
    Label messageRaidHeader;

    @FXML
    Label messageRaidSelected;

    @FXML
    public void submitRaidChanges() {
        String newText;
        messageRaid.selectAll();
        messageRaid.copy();
        newText = messageRaid.getText();
        // TODO fix globalSource and options in XML
        globalRaidSource = "raidMessage";
        if (store.modifyConfiguration(globalRaidSource, newText)) {
            submitRaidStatus.setText(globalRaidSource + " set to " + newText);
        } else {
            submitRaidStatus.setText("failed to set new value");
        }
    }

    @FXML
    public void submitRaidSettings() {
        // set min enabled amount
        minRaidEnabled.selectAll();
        minRaidEnabled.copy();
        String min = minRaidEnabled.getText();
        if (store.modifyConfiguration("raidReplyMin", min)) {
            submitRaidStatus.setText("Min viewers set to " + min);
        } else {
            submitRaidStatus.setText("Failed to set min viewers");
        }
        // set usage TODO here, use all for everything for now
        RadioButton toggle = (RadioButton) usageRaid.getSelectedToggle();
        if(toggle.getText().equals("Use All")){
            System.out.println("Using msg for all");
        } else if (toggle.getText().equals("Usage Specific")){
            System.out.println("Using specific msgs for amts");
        }
    }
    
    @FXML
    public void buttonRaidPress(ActionEvent event) {
// TODO allow for options, for now all get same message
// TODO allow for %game variable to show what streamer was last playing
        String text = "";
        String header = "";
        String selected = "";
        String source = event.getSource().toString();
        source = source.substring(source.indexOf("'") + 1, source.indexOf("'", source.indexOf("'") + 1));
        if (source.equals("0 - 49")) {
            globalRaidSource = "raid1";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 0 - 49";
        } else if (source.equals("50 - 99")) {
            globalRaidSource = "raid2";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 50 - 99";
        } else if (source.equals("100 - 149")) {
            globalRaidSource = "raid3";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 100 - 149";
        } else if (source.equals("150 - 200")) {
            globalRaidSource = "raid4";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 150 - 200";
        } else if (source.equals("200 - 499")) {
            globalRaidSource = "raid5";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 200 - 499";}
        
            else if (source.equals("500 +")) {
            globalRaidSource = "raid6";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: 500+";
        } 
         else if (source.equals("All")) {
            globalRaidSource = "raidAll";
            text = store.getConfiguration().raidMessage;
            header = "Available variables: %user %viewers";
            selected = "Selected: All";
        }
        messageRaid.setText(text);
        messageRaidHeader.setText(header);
        messageRaidSelected.setText(selected);
    }
}
