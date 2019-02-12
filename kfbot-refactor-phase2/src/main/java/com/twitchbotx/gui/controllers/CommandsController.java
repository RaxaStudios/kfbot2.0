/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.*;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class CommandsController {

    private Datastore store;
    final ConfigParameters configuration = new ConfigParameters();

    ScreensController myController = new ScreensController();

    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }

    @FXML
    private void dash(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    private void sendEvent(String msg) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                guiHandler.bot.getStore().getEventList().addList(msg);
            }
        });
    }

    public void initialize() {
        try {
            store = guiHandler.bot.getStore();

            VBox vB = new VBox();
            vB.setAlignment(Pos.CENTER_LEFT);
            vB.setPrefWidth(197);
            vB.setPrefHeight(323);
            String[] commandName = new String[this.store.getCommands().size()];
            for (int i = 0; i < this.store.getCommands().size(); i++) {
                final ConfigParameters.Command command = this.store.getCommands().get(i);
                commandName[i] = command.name;
            }
            //alphabetize here
            Arrays.sort(commandName);
            for (int i = 0; i < commandName.length; i++) {
                String cI = commandName[i];
                Button cN = new Button(cI);
                cN.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        showCommandInfo(cI);
                    }
                });
                vB.getChildren().add(cN);

            }
            commandList.setContent(vB);
        } catch (Exception ex) {
            System.out.println("Error at initialize viewer");
            ex.printStackTrace();
        }

        // Editor command initialize
        deleteConfirmText.setText("");
        confirmed = false;

        // Set up command list view 
        List<String> cName = new ArrayList<>();
        String[] commandEditingName = new String[this.store.getCommands().size()];
        for (int i = 0; i < this.store.getCommands().size(); i++) {
            final ConfigParameters.Command command = this.store.getCommands().get(i);
            if (!Commands.getInstance().isReservedCommand(command.name)) {
                commandEditingName[i] = command.name;
                cName.add(command.name);
            }
        }
        // alphabetize here
        Collections.sort(cName);
        for (String commandName1 : cName) {
            if (commandName1 != null) {
                //System.out.println("Command: " + commandName1);
                commandsEditing.add(commandName1);
            }
        }

        commandEditingList.setItems(commandsEditing);

        // Repeating command intialize
        try {
            confirmationLabel.setText("");
            VBox vB = new VBox();
            vB.setAlignment(Pos.CENTER_LEFT);
            vB.setPrefWidth(168);
            vB.setPrefHeight(270);
            List<String> commandRepeatName = new ArrayList<>();
            //String[] commandName = new String[this.store.getCommands().size()];
            int index = 0;
            for (int i = 0; i < this.store.getCommands().size(); i++) {
                final ConfigParameters.Command command = this.store.getCommands().get(i);
                if (!command.interval.equals("")) {
                    commandRepeatName.add(command.name);
                    //System.out.println(command.name);
                    index++;
                }
            }
            for (String cI : commandRepeatName) {
                Button cN = new Button(cI);
                cN.setOnAction((ActionEvent e) -> {
                    //System.out.println("Attemping to add: " + cI);
                    showRepeatCommandInfo(cI);
                });
                vB.getChildren().add(cN);
                //System.out.println(cN.getText());
            }
            //System.out.println(commandList.getContent().toString());
            commandRepeatingList.setContent(vB);

        } catch (Exception ex) {
            System.out.println("Error at initialize repeat");
            ex.printStackTrace();
        }
    }

    /**
     * Command info feature starts here
     */
    @FXML
    ScrollPane commandList;
    @FXML
    Pane commandInfo;

    private void showCommandInfo(String command) {
        VBox vB = new VBox();
        vB.setAlignment(Pos.CENTER);
        vB.setPrefWidth(255);
        vB.setPrefHeight(323);
        for (int i = 0; i < this.store.getCommands().size(); i++) {
            final ConfigParameters.Command commandConfig = this.store.getCommands().get(i);
            if (commandConfig.name.equals(command)) {
                Label name = new Label("Name:");
                Label nameD = new Label(commandConfig.name);
                String authLevel = commandConfig.auth;
                if (authLevel.contains("+a")) {
                    authLevel = "All users (+a)";
                } else if (authLevel.contains("+s")) {
                    authLevel = "Sub only (+s)";
                } else if (authLevel.contains("+m")) {
                    authLevel = "Mod only (+m)";
                } else if (authLevel.equals(" ") || authLevel.equals("")) {
                    authLevel = "Broadcaster only";
                } else {
                    authLevel = "User only: " + commandConfig.auth;
                    authLevel = authLevel.replace("+", "");
                }
                Label auth = new Label("Authorization:");
                Label authD = new Label(authLevel);
                Label cooldown = new Label("Cooldown: " + commandConfig.cooldownInSec + " seconds");

                Label disabled = new Label("Disabled: " + commandConfig.disabled);

                Label repeating = new Label("Repeating: " + commandConfig.repeating);
                Label interval = new Label("Interval: " + commandConfig.interval);
                Label initialDelay = new Label("Initial delay: " + commandConfig.initialDelay);
                Label sound = new Label("Sound: " + commandConfig.sound);
                Label message = new Label("Message:");
                Label messageD = new Label(commandConfig.text);
                messageD.setPrefHeight(200);
                messageD.setWrapText(true);
                messageD.setTextAlignment(TextAlignment.CENTER);
                messageD.setFont(Font.font("", FontWeight.BOLD, 12));
                nameD.setFont(Font.font("", FontWeight.BOLD, 12));
                authD.setFont(Font.font("", FontWeight.BOLD, 12));
                vB.getChildren().addAll(name, nameD, auth, authD, disabled, repeating, interval, initialDelay, cooldown, sound, message, messageD);
            };
        }
        commandInfo.getChildren().clear();
        commandInfo.getChildren().add(vB);
    }

    /**
     * Command Edit features start here
     */
    /**
     * ******************************************************************************************************
     */
    MenuItem cM;

    @FXML
    ListView<String> commandEditingList;

    ObservableList<String> commandsEditing = FXCollections.observableArrayList();

    @FXML
    MenuButton attributesMenu;

    @FXML
    TextField commandToEdit;

    @FXML
    Button submitChanges;

    @FXML
    Button deleteCommand;

    @FXML
    TextArea newValueText;

    @FXML
    TextField newCommandName;

    @FXML
    TextField newCommandAuth;

    @FXML
    TextField newCommandCooldown;

    @FXML
    TextField newCommandInitDelay;

    @FXML
    TextField newCommandInterval;

    @FXML
    TextArea newCommandMessage;

    @FXML
    TextField newCommandSound;

    @FXML
    RadioButton repeatYes;

    @FXML
    RadioButton repeatNo;

    @FXML
    ToggleGroup repeatingGroup;

    @FXML
    Label editValueLabel;

    @FXML
    Label submitStatus;

    @FXML
    Label cmdStatus;

    @FXML
    Label deleteConfirmText;
    private boolean confirmed;

    //method to set the command to be edited/deleted
    @FXML
    private void chooseCommand(ActionEvent event) {
        attributesMenu.setText("Attributes");
        newValueText.clear();
        newValueText.setPrefSize(149, 25);
        newValueText.setPromptText(null);
        int selected = commandEditingList.getSelectionModel().getSelectedIndex();
        commandToEdit.setText(commandEditingList.getItems().get(selected));
        confirmed = false;
    }

    @FXML
    private void deleteCommand(ActionEvent event) {
        if (confirmed) {
            String command = "";
            commandToEdit.selectAll();
            commandToEdit.copy();
            command = commandToEdit.getText();
            store.deleteCommand(command);
            deleteConfirmText.setText(command + " deleted");
            refresh();
        } else {
            deleteConfirmText.setText("Are you sure you want delete this command?");
            confirmed = true;
        }
    }

    private void refresh() {
        confirmed = false;

        // Set up command list view 
        String[] commandName = new String[this.store.getCommands().size()];
        for (int i = 0; i < this.store.getCommands().size(); i++) {
            final ConfigParameters.Command command = this.store.getCommands().get(i);
            if (!Commands.getInstance().isReservedCommand(command.name)) {
                commandName[i] = command.name;
            }
        }

        for (String commandName1 : commandName) {
            if (commandName1 != null) {
                commandsEditing.add(commandName1);
            }
        }
        commandEditingList.setItems(commandsEditing);
    }

    @FXML
    private void submitChanges(ActionEvent event) {
        newValueText.selectAll();
        newValueText.copy();
        String change = newValueText.getText();
        commandToEdit.selectAll();
        commandToEdit.copy();
        String cmd = commandToEdit.getText();
        //conversion to xml values
        String attribute = attributesMenu.getText();
        if (!attribute.equals("Message")) {
            switch (attribute) {
                case "Auth":
                    attribute = "auth";
                    break;
                case "Cooldown":
                    attribute = "cooldownInSec";
                    break;
                case "Disabled":
                    attribute = "disabled";
                    break;
                case "Initial Delay":
                    attribute = "initDelay";
                    break;
                case "Interval":
                    attribute = "interval";
                    break;
                case "Repeating":
                    attribute = "repeating";
                    break;
                case "Sound":
                    attribute = "sound";
                    break;
                default:
                    break;
            }
            store.setUserCommandAttribute(cmd, attribute, change, true);
            if (attribute.equals("repeating")) {
                if (change.equals("true")) {
                    //start up the command repeat schedule
                }
                submitStatus.setText("Changes applied, restart bot for changes to take effect.");
            } else {
                submitStatus.setText("Changes saved!");
            }
        } else {
            store.editCommand(cmd, change);
            submitStatus.setText("Changes saved!");
        }
        refresh();
    }

    @FXML
    private void newCommand(ActionEvent event) {
        System.out.println("Creating new command");
        newCommandName.selectAll();
        newCommandName.copy();
        String cmdName = newCommandName.getText();
        if (cmdName.equals("")) {
            cmdStatus.setText("Enter a !command name");
            return;
        }
        newCommandAuth.selectAll();
        newCommandAuth.copy();
        String auth = newCommandAuth.getText();
        newCommandCooldown.selectAll();
        newCommandCooldown.copy();
        String cooldown = newCommandCooldown.getText();
        RadioButton toggle = (RadioButton) repeatingGroup.getSelectedToggle();
        String repeating = toggle.getText();
        if (repeating.equals("Yes")) {
            repeating = "true";
        } else {
            repeating = "false";
        }
        newCommandInitDelay.selectAll();
        newCommandInitDelay.copy();
        String initDelay = newCommandInitDelay.getText();
        newCommandInterval.selectAll();
        newCommandInterval.copy();
        String interval = newCommandInterval.getText();
        newCommandMessage.selectAll();
        newCommandMessage.copy();
        String msg = newCommandMessage.getText();
        if (msg.equals("")) {
            cmdStatus.setText("Command requires a message");
            return;
        }
        newCommandSound.selectAll();
        newCommandSound.copy();
        String sound = newCommandSound.getText();
        boolean added = store.addCommand(cmdName.toLowerCase(), auth, cooldown, repeating, initDelay, interval, sound, msg);
        if (added) {
            cmdStatus.setText("Added command " + cmdName);
        } else {
            cmdStatus.setText("Command " + cmdName + " already exists!");
        }
        refresh();
    }

    @FXML
    public void clearNewCommand() {
        newCommandName.setText("");
        newCommandAuth.setText("");
        newCommandCooldown.setText("");
        newCommandSound.setText("");
        newCommandMessage.setText("");
    }

    @FXML
    private void setAttribute(ActionEvent event) {
        MenuItem choice = (MenuItem) event.getSource();
        String attribute = choice.getText();
        attributesMenu.setText(attribute);
        newValueText.setPrefSize(149, 25);

        //create strings for attribute usage
        String auth = "";
        String sound = "";
        String cooldown = "";
        String disabled = "";
        String initDelay = "";
        String interval = "";
        String repeating = "";
        String msg = "";
        if (!commandToEdit.getText().replaceAll(" ", "").equals("")) {
            for (int i = 0; i < store.getCommands().size(); i++) {
                String command = store.getCommands().get(i).name;
                if (command.equals(commandToEdit.getText())) {
                    auth = store.getCommands().get(i).auth;
                    sound = store.getCommands().get(i).sound;
                    if (sound == null) {
                        sound = "";
                    }
                    cooldown = store.getCommands().get(i).cooldownInSec;
                    if (cooldown == null) {
                        cooldown = "";
                    }
                    disabled = String.valueOf(store.getCommands().get(i).disabled);
                    if (disabled == null) {
                        disabled = "";
                    }
                    initDelay = store.getCommands().get(i).initialDelay;
                    if (initDelay == null) {
                        initDelay = "";
                    }
                    interval = store.getCommands().get(i).interval;
                    if (interval == null) {
                        interval = "";
                    }
                    repeating = store.getCommands().get(i).repeating;
                    if (repeating == null) {
                        repeating = "";
                    }
                    msg = store.getCommands().get(i).text;
                    if (msg == null) {
                        msg = "";
                    }
                }
            }
        }

        //change Prompt Text for options
        switch (attribute) {
            case "Auth":
                if (auth.isEmpty()) {
                    newValueText.clear();
                    newValueText.setPromptText("-s +m -a +username");
                } else {
                    newValueText.setText(auth);
                }
                break;
            case "Cooldown":
                if (cooldown.isEmpty()) {
                    newValueText.clear();
                    newValueText.setPromptText("value in seconds");
                } else {
                    newValueText.setText(cooldown);
                }
                break;
            case "Disabled":
                if (disabled.isEmpty()) {
                    newValueText.clear();
                    newValueText.setPromptText("true/false");
                } else {
                    newValueText.setText(disabled);
                }
                break;
            case "Initial Delay":
                if (initDelay.equals("")) {
                    newValueText.clear();
                    newValueText.setPromptText("value in seconds");
                } else {
                    newValueText.setText(initDelay);
                }
                break;
            case "Interval":
                if (interval.equals("")) {
                    newValueText.clear();
                    newValueText.setPromptText("value in seconds");
                } else {
                    newValueText.setText(interval);
                }
                break;
            case "Repeating":
                if (repeating.equals("")) {
                    newValueText.clear();
                    newValueText.setPromptText("true/false");
                } else {
                    newValueText.setText(repeating);
                }
                break;
            case "Sound":
                if (sound.isEmpty()) {
                    newValueText.clear();
                    newValueText.setPromptText("soundfile.mp3");
                } else {
                    newValueText.setText(sound);
                }
                break;
            case "Message":
                if (msg.isEmpty()) {
                    newValueText.clear();
                    newValueText.setPrefSize(149, 75);
                    newValueText.setPromptText("Message to chat here");
                } else {
                    newValueText.setPrefSize(149, 75);
                    newValueText.setText(msg);
                }
                break;
            default:
                break;
        }

    }

    /**
     * Repeating command features start here
     */
    /**
     * ******************************************************************************************************
     */
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
    ScrollPane commandRepeatingList;

    private void showRepeatCommandInfo(String command) {
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
        showRepeatCommandInfo(commandToSchedule);
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
        showRepeatCommandInfo(commandToStop);
    }

}
