/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.TimerManagement;
import com.twitchbotx.bot.XmlDatastore;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class TimedCommandsController implements Initializable {

    ScreensController myController = new ScreensController();
    private Datastore store;
    final ConfigParameters configuration = new ConfigParameters();
    final TimerManagement timers = new TimerManagement();

    @FXML
    Label commandMessage;

    @FXML
    Label commandNameText;

    @FXML
    Label commandInterval;

    @FXML
    Label commandInitDelay;

    @FXML
    Label commandRepeating;

    @FXML
    Label confirmationLabel;

    @FXML
    ScrollPane commandList;

    @FXML
    private void dash(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    @FXML
    private void editCommands(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.commandEditorID, guiHandler.commandEditorFile);
        myController.setScreen(guiHandler.commandEditorID);
        myController.setId("commandEditor");
        myController.show(myController);
    }

    private void showCommandInfo(String command) {
        for (int i = 0; i < this.store.getCommands().size(); i++) {
            final ConfigParameters.Command commandConfig = this.store.getCommands().get(i);
            if (commandConfig.name.equals(command)) {
                commandNameText.setText(commandConfig.name);
                commandMessage.setText(commandConfig.text);
                commandInterval.setText(commandConfig.interval);
                commandInitDelay.setText(commandConfig.initialDelay);
                commandRepeating.setText(commandConfig.repeating);
            }
        }
    }

    /**
     * Send and submit command name to TimerManagement
     */
    @FXML
    public void startCommand() {
        String commandToSchedule;
        if (commandRepeating.getText().equals("false")) {
            commandToSchedule = commandNameText.getText();
            if (timers.schedule(commandToSchedule)) {
                //set repeating value to true
                store.setUserCommandAttribute(commandToSchedule, "repeating", "true", true);
                sendEvent("Started repeating command: " + commandToSchedule);
                confirmationLabel.setText("Command scheduled");
            } else {
                confirmationLabel.setText("Set true");
                store.setUserCommandAttribute(commandToSchedule, "repeating", "true", true);
            }
        } else {
            confirmationLabel.setText("Already true");
            commandToSchedule = commandNameText.getText();
        }
        showCommandInfo(commandToSchedule);
    }

    /**
     * Send command name to TimerManagement to find then stop task
     */
    @FXML
    public void stopCommand() {
        String commandToStop;
        if (commandRepeating.getText().equals("true")) {
            commandToStop = commandNameText.getText();
            if (timers.stop(commandToStop)) {
                //set repeating value to false
                store.setUserCommandAttribute(commandToStop, "repeating", "false", true);
                sendEvent("Stopped repeating command: " + commandToStop);
                confirmationLabel.setText("Command stopped");
                commandToStop = commandNameText.getText();
            } else {
                confirmationLabel.setText("Failed stopping, see dash");
                store.setUserCommandAttribute(commandToStop, "repeating", "false", true);
                commandToStop = commandNameText.getText();
            }
        } else {
            confirmationLabel.setText("Already false");
            commandToStop = commandNameText.getText();
        }
        showCommandInfo(commandToStop);
    }

    private void sendEvent(String msg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                guiHandler.bot.getStore().getEventList().addList(msg);
            }
        });
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            try {
                final ConfigParameters.Elements elements = configuration.parseConfiguration("./kfbot.xml");
                store = new XmlDatastore(elements);
            } catch (Exception e) {
                e.printStackTrace();
            }
            confirmationLabel.setText("");
            VBox vB = new VBox();
            vB.setAlignment(Pos.CENTER_LEFT);
            vB.setPrefWidth(168);
            vB.setPrefHeight(270);
            List<String> commandName = new ArrayList<>();
            //String[] commandName = new String[this.store.getCommands().size()];
            int index = 0;
            for (int i = 0; i < this.store.getCommands().size(); i++) {
                final ConfigParameters.Command command = this.store.getCommands().get(i);
                if (!command.interval.equals("")) {
                    commandName.add(command.name);
                    System.out.println(command.name);
                    index++;
                }
            }
            for (String cI : commandName) {
                Button cN = new Button(cI);
                cN.setOnAction((ActionEvent e) -> {
                    showCommandInfo(cI);
                });
                vB.getChildren().add(cN);
                System.out.println(cN.getText());
            }
            //System.out.println(commandList.getContent().toString());
            commandList.setContent(vB);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }
}
