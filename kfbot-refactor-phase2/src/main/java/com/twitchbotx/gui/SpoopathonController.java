/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.handlers.SpoopathonHandler;
import com.twitchbotx.bot.handlers.sqlHandler;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class SpoopathonController implements Initializable {

    ScreensController myController = new ScreensController();

    private static sqlHandler gameData;
    private static SpoopathonHandler userData;

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
        if (gameData.addPoints("!addPoints " + gameID + " " + points)) {
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
                gameData = new sqlHandler(guiHandler.bot.getStore(), guiHandler.bot.getOut());
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
        gameList.clear();
        userList.clear();
        loadLabel.setVisible(true);
        gameData = new sqlHandler(guiHandler.bot.getStore(), guiHandler.bot.getOut());
        userData = new SpoopathonHandler();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
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

        // set userlist
        SpoopathonHandler.MAP.entrySet().forEach((m) -> {
            userList.add(m.getKey() + " : " + m.getValue());
            System.out.println(i + " " + m.getKey());
            i++;
        });
        addVoteStatus.setText("");
        //System.out.println(userList);
        userListView.setItems(userList);
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
        String user = usernameAddText.getText();
        user = user.toLowerCase();
        votesAddText.selectAll();
        votesAddText.copy();
        int votes = Integer.parseInt(votesAddText.getText());
        if (votes == 0) {
            if (userData.remUser(user)) {
                addVoteStatus.setText("Removed " + user);
            } else {
                addVoteStatus.setText(user + " not found");
            }
        } else if (votes < 0) {
            userData.remVotes(user, votes);
            addVoteStatus.setText("Removed votes");
        } else {
            userData.addVotes(user, votes);
            addVoteStatus.setText("Added points");
        }

        refreshUsers();
    }

    @FXML
    public void refreshUsers() {
        userList.clear();
        SpoopathonHandler.MAP.entrySet().forEach((m) -> {
            userList.add(m.getKey() + " : " + m.getValue());
            // System.out.println(i + " " + m.getKey());
            //i++;
        });

        //System.out.println(userList);
        userListView.setItems(userList);
    }

}
