/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.handlers.SpoopathonHandler;
import com.twitchbotx.bot.handlers.sqlHandler;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class SpoopathonController implements Initializable {

    ScreensController myController = new ScreensController();
    Datastore store;
    private static sqlHandler gameData;
    private final static SpoopathonHandler userData = new SpoopathonHandler();
    int unspent = 0;

    int i = 1;

    @FXML
    Label loadLabel;

    @FXML
    Label addPointsStatus;

    @FXML
    Label editGameStatus;

    @FXML
    Label addGameStatus;

    @FXML
    Label addVoteStatus;

    @FXML
    Label unspentPoints;
    
    @FXML
    Label saveText;
    
    @FXML
    TextField subValue;

    @FXML
    TextField bitValue;

    @FXML
    TextField newGame;

    @FXML
    TextField newPoints;

    @FXML
    TextField addPointsGameID;

    @FXML
    TextField addPointsPoints;

    @FXML
    TextField editGameID;

    @FXML
    TextField editPoints;

    @FXML
    TextField newGameID;

    @FXML
    TextField usernameAddText;

    @FXML
    TextField votesAddText;

    @FXML
    ToggleGroup Spoop;

    @FXML
    RadioButton spoopEnabled;

    @FXML
    RadioButton spoopDisabled;

    @FXML
    ListView gameListView;
    public static ObservableList<String> gameList = FXCollections.observableArrayList();

    @FXML
    ListView userListView;
    public static ObservableList<String> userList = FXCollections.observableArrayList();

    @FXML
    private void addPoints(ActionEvent event) {
        addPointsGameID.copy();
        addPointsGameID.selectAll();
        String gameID = addPointsGameID.getText();
        addPointsPoints.copy();
        addPointsPoints.selectAll();
        String points = addPointsPoints.getText();
        if (gameData.addPoints("!s-addPoints " + gameID + " " + points)) {
            refreshList();
            addPointsStatus.setText(points + " points added to " + gameID);
        } else {
            addPointsStatus.setText("Error adding points, check connection and enabled setting");
        }
    }

    @FXML
    private void refreshList() {
        gameList.clear();
        loadLabel.setVisible(true);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                gameData = new sqlHandler(guiHandler.bot.getStore());
                try {
                    for (int i = 0; i < gameData.getSize(); i++) {
                        gameList.add(gameData.getData(i + 1));
                    }
                } catch (IllegalStateException ie) {
                    //ignore
                }
                loadLabel.setVisible(false);
            }
        });
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(SpoopathonController.class.getName()).log(Level.SEVERE, null, ex);
        }
        gameListView.setItems(gameList);
    }

    @FXML
    private void deleteGame(ActionEvent event) {
        //send command to sqlHandler
        //!s-game-delete[gameID]
        editGameID.selectAll();
        editGameID.copy();
        String gameID = editGameID.getText();
        if (gameData.deleteGame("!s-game-delete " + gameID)) {
            //update a status label event sent from sqlHandler
            editGameStatus.setText(gameID + " deleted");
            refreshList();
        } else {
            //error
            editGameStatus.setText("Error occured trying to delete " + gameID);
        }
    }

    @FXML
    private void submitGameInfo(ActionEvent event) {
        //send command to sqlHandler
        //!s-set-points [id] [amount]
        editGameID.selectAll();
        editGameID.copy();
        String id = editGameID.getText();
        editPoints.selectAll();
        editPoints.copy();
        String amount = editPoints.getText();
        String msg = "!s-set-points " + id + " " + amount;
        if (gameData.setPoints(msg)) {
            editGameStatus.setText(id + " set to " + amount);
            refreshList();
        } else {
            editGameStatus.setText("Error occurred setting points check connection");
        }
    }

    @FXML
    private void addNewGame(ActionEvent event) {
        //send command to sqlHandler
        //!s-game-add [id] (auto set to 0 points)
        newGameID.selectAll();
        newGameID.copy();
        String id = newGameID.getText();
        String msg = "!s-game-add " + id;
        if (gameData.addGame(msg)) {
            addGameStatus.setText(id + " created and set to 0 points");
            refreshList();
        } else {
            addGameStatus.setText("Error occured trying to add new game");
        }
    }

    @FXML
    private void selectGame(ActionEvent event) {
        int selected = gameListView.getSelectionModel().getSelectedIndex();
        String gText = gameListView.getItems().get(selected).toString();
        //list format= #1 AB: 1234
        //parse down to AB as gameID
        gText = gText.substring(gText.indexOf(" ") + 1, gText.indexOf(":"));
        addPointsGameID.setText(gText);
        addPointsStatus.setText("");
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
        gameList.clear();
        userList.clear();
        loadLabel.setVisible(true);
        // TODO set on load, need faster way to do this
        // set userlist and total unspent points
        unspent = 0;
        SpoopathonHandler.MAP.entrySet().forEach((m) -> {
            userList.add(m.getKey() + " : " + m.getValue());
            System.out.println(i + " " + m.getKey());
            unspent = unspent + m.getValue();
            i++;
        });
        unspentPoints.setText("Total Unspent Points: " + String.valueOf(unspent));
        addVoteStatus.setText("");
        //System.out.println(userList);
        userListView.setItems(userList);

        // initialize enable/disable radio buttons and listeners
        setRadios();
        addListener(Spoop, "sStatus");
    }

    private void setRadios() {
        if (store.getConfiguration().spoopathonStatus.equals("on")) {
            Spoop.selectToggle(spoopEnabled);
        } else {
            Spoop.selectToggle(spoopDisabled);
        }
    }

    /**
     * adds a listener to the radio button toggle group instant change in on/off
     * status for group
     *
     * @param ToggleGroup to listen to
     * @param String config name associated with XML value
     */
    private void addListener(ToggleGroup group, String name) {
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            public void changed(ObservableValue<? extends Toggle> ob,
                    Toggle o, Toggle n) {
                
                RadioButton rb = (RadioButton) group.getSelectedToggle();
                System.out.println("Test print");
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

    // begin user votes commands
    @FXML
    public void addVotes() {
        addVoteStatus.setText("");
        //grab username and votes to add
        usernameAddText.selectAll();
        usernameAddText.copy();
        String userUpper = usernameAddText.getText();
        userUpper = userUpper.replace(" ", "");
        String userLower = userUpper.toLowerCase();
        votesAddText.selectAll();
        votesAddText.copy();
        int votes = Integer.parseInt(votesAddText.getText());
        System.out.println("user spoop: " + userUpper + " votes: " + votes);
        if (votes == 0) {
            if (userData.remUser(userLower)) {
                addVoteStatus.setText("Removed " + userUpper);
            } else {
                addVoteStatus.setText(userUpper + " not found");
            }
        } else if (votes < 0) {
            userData.remVotes(userUpper, votes, false);
            addVoteStatus.setText("Removed votes");
        } else {
            userData.addVotes(userUpper, userLower, votes, false);
            addVoteStatus.setText("Added points");
        }

        refreshUsers();
    }

    @FXML
    public void refreshUsers() {
        userList.clear();
        unspent = 0;
        SpoopathonHandler.MAP.entrySet().forEach((m) -> {
            userList.add(m.getKey() + " : " + m.getValue());
            unspent = unspent + m.getValue();
            // System.out.println(i + " " + m.getKey());
            //i++;
        });
        unspentPoints.setText("Total Unspent Points: " + String.valueOf(unspent));
        //System.out.println(userList);
        userListView.setItems(userList);
    }

    @FXML
    public void clearUsers() {
        userList.clear();
        userListView.setItems(userList);
        SpoopathonHandler.clearMap();
    }

    
    
    // spoopathon value settings
    @FXML
    public void submitSpoop() {
        subValue.selectAll();
        subValue.copy();
        String subPoint = subValue.getText();
        bitValue.selectAll();
        bitValue.copy();
        String bitPoint = bitValue.getText();
        if (subPoint.equals("") || subPoint.equals(null) || bitValue.equals("") || bitValue.equals(null)) {
            saveText.setText("Values blank");
        } else {
            store.modifyConfiguration("spoopSubValue", subPoint);
            store.modifyConfiguration("spoopBitValue", bitPoint);
            saveText.setText("Values updated");
        }
    }
    
}
