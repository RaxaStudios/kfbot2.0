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
public class BitEditController implements Initializable {

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
        // TODO fix globalSource for options
        globalSource = "bitMessage";
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
        if (store.modifyConfiguration("bitReplyMin", min)) {
            submitStatus.setText("Min bit set to " + min);
        } else {
            submitStatus.setText("Failed to set min bit");
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
        Datastore store = guiHandler.bot.getStore();
        String text = "";
        String header = "";
        String selected = "";
        String source = event.getSource().toString();
        source = source.substring(source.indexOf("'") + 1, source.indexOf("'", source.indexOf("'") + 1));
        if (source.equals("1 - 499")) {
            globalSource = "bit1";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: 1 - 499";
        } else if (source.equals("500 - 999")) {
            globalSource = "bit2";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: 500 - 999";
        } else if (source.equals("1,000 - 4,999")) {
            globalSource = "bit3";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: 1,000 - 4,999";
        } else if (source.equals("5,000 - 9,999")) {
            globalSource = "bit4";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: 5,000 - 9,999";
        } else if (source.equals("10,000+")) {
            globalSource = "bit5";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
            selected = "Selected: 10,000+";
        } else if (source.equals("All")) {
            globalSource = "bitAll";
            text = store.getConfiguration().bitMessage;
            header = "Available variables: %user %bits";
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
        //minBitEnabled.setText("100");
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
