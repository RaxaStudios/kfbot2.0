/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.*;
import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.bot.handlers.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class LotteryController implements Initializable {

    //TODO change this version to LotteryHandler display
    ScreensController myController = new ScreensController();

    private Datastore store;
    final ConfigParameters configuration = new ConfigParameters();
    private TwitchMessenger messenger = guiHandler.messenger;
    private PrintStream out = guiHandler.out;

    String winner = "";

    private List<String> names;

    @FXML
    private TextField addUserTextField;

    @FXML
    private TextField keywordText;

    @FXML
    private RadioButton subRadioButton;

    @FXML
    ScrollPane queueList;

    @FXML
    Pane winnerPane;

    @FXML
    Label winnerText;

    @FXML
    Label qStatus;

    public void showQueue() {
        //try {
        /* final ConfigParameters.Elements elements = configuration.parseConfiguration("./kfbot.xml");
            store = new XmlDatastore(elements);*/

        List<String> entrants = this.store.lotteryList();
        String[] nameArray = entrants.toArray(new String[entrants.size()]);
        System.out.println(entrants);
        VBox vB = new VBox();
        vB.setAlignment(Pos.CENTER_LEFT);
        vB.setPrefWidth(225);
        vB.setPrefHeight(318);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameArray.length; i++) {
            vB.getChildren().add(new Label(" " + (i + 1) + ". " + nameArray[i]));
            sb.append(nameArray[i]);
        }
        queueList.setContent(vB);
    } //catch (ParserConfigurationException | SAXException | IOException e) {
    //System.out.println("Error parsing XML file in LotteryController");
    //}
    //}

    public void clearQueue() {
        this.store.clearLotteryList();
        showQueue();
        System.out.println("Queue cleared");
    }

    public void openQueue() {
        //Handle text from keyword box
        //Check sub only button
        //Clear and enable list
        keywordText.selectAll();
        keywordText.copy();
        //store.modifyConfiguration("lottoName", keywordText.getText());
        System.out.println("Keyword set to: " + keywordText.getText());
        String sub = "+a";
        //store.clearLotteryList();
        if (!keywordText.getText().isEmpty()) {
            qStatus.setAlignment(Pos.CENTER);
            if (subRadioButton.isSelected()) {
                sub = "+s";
                messenger.sendMessage("A sub-only lottery has started! Subs can type " + keywordText.getText() + " to enter!");
                qStatus.setText("A sub-only lottery has started! Subs can type " + keywordText.getText() + " to enter!");
            } else {
                messenger.sendMessage("Lottery has started! Type " + keywordText.getText() + " to enter!");
                qStatus.setText("Lottery has started! Type " + keywordText.getText() + " to enter!");
            }
            store.setupLotto(sub, keywordText.getText());
        }

    }

    public void closeQueue() {
        store.closeQueue();
        addUserTextField.setText("Lottery closed");
    }

    public void addUser() {
        if (store.getQOpen()) {
            addUserTextField.selectAll();
            addUserTextField.copy();

            if (!addUserTextField.getText().isEmpty()) {
                try {
                    if (store.lotteryList().contains(addUserTextField.getText()) || addUserTextField.getText().equals("Duplicate entry found")) {
                        addUserTextField.setText("Duplicate entry found");
                    } else {
                        store.addLotteryList(addUserTextField.getText());
                        showQueue();
                    }
                } catch (NullPointerException n) {
                    addUserTextField.setText("Lottery is closed");
                }
            } else {
                addUserTextField.setText("Empty add field");
            }
        } else {
            addUserTextField.setText("Lottery is closed");
        }
    }

    public void popUser() {
        try {
            boolean isEmpty = store.lotteryList().isEmpty();
            if (!isEmpty) {
                Collections.shuffle(names);
                winner = this.store.drawLotteryList();
                winnerText.setAlignment(Pos.CENTER);
                winnerText.setText(winner);
                showQueue();
            } else {
                winnerText.setAlignment(Pos.CENTER);
                winnerText.setText("Lottery empty");
                System.out.println("Lottery is empty");
            }
        } catch (NullPointerException n) {
            winnerText.setText("Lottery is closed");
        }
    }

    public void goToDashboard() {
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
        try {
            // Create store instance to access lotteryhandler
            names = new ArrayList();
            store = guiHandler.store;
            out = guiHandler.out;
        } catch (Exception ex) {
            Logger.getLogger(LotteryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
