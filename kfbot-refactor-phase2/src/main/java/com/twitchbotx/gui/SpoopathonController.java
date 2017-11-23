/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.handlers.sqlHandler;
import java.net.URL;
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

    @FXML
    Label loadLabel;

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
    ListView gameListView;
    public static ObservableList<String> gameList = FXCollections.observableArrayList();

    @FXML
    private void dash(ActionEvent event) {
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
        // TODO
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
}
