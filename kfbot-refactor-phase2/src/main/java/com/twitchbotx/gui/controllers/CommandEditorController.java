/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.Commands;
import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
//TODO sub tier 3 command system
public class CommandEditorController implements Initializable {

    Datastore store = guiHandler.bot.getStore();
    final ConfigParameters configuration = new ConfigParameters();

    ScreensController myController = new ScreensController();

    MenuItem cM;

    @FXML
    ListView<String> commandList;

    ObservableList<String> commands = FXCollections.observableArrayList();

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
    ToggleGroup repeating;

    @FXML
    Label editValueLabel;

    @FXML
    Label submitStatus;

    @FXML
    Label cmdStatus;

    @FXML
    Label deleteConfirmText;
    private boolean confirmed;

    @FXML
    private void dash(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    @FXML
    private void repeatCommandManager(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.timedID, guiHandler.timedFile);
        myController.setScreen(guiHandler.timedID);
        myController.setId("timed");
        myController.show(myController);
    }

    //method to set the command to be edited/deleted
    @FXML
    private void chooseCommand(ActionEvent event) {
        attributesMenu.setText("Attributes");
        newValueText.clear();
        newValueText.setPrefSize(149, 25);
        newValueText.setPromptText(null);
        int selected = commandList.getSelectionModel().getSelectedIndex();
        commandToEdit.setText(commandList.getItems().get(selected));
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
        RadioButton toggle = (RadioButton) repeating.getSelectedToggle();
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
    public void clearNewCommand(){
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
                commands.add(commandName1);
            }
        }
        commandList.setItems(commands);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        deleteConfirmText.setText("");
        confirmed = false;

        // Set up command list view 
        List<String> cName = new ArrayList<>();
        String[] commandName = new String[this.store.getCommands().size()];
        for (int i = 0; i < this.store.getCommands().size(); i++) {
            final ConfigParameters.Command command = this.store.getCommands().get(i);
            if (!Commands.getInstance().isReservedCommand(command.name)) {
                commandName[i] = command.name;
                cName.add(command.name);
            }
        }
        // alphabetize here
        Collections.sort(cName);
        for (String commandName1 : cName) {
            if (commandName1 != null) {
                System.out.println("Command: " + commandName1);
                commands.add(commandName1);
            }
        }
        
        /*for (String commandName1 : commandName) {
            if (commandName1 != null) {
                //System.out.println("Command: " + commandName1);
                commands.add(commandName1);
            }
        }*/
        
        commandList.setItems(commands);
    }

    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }

}
