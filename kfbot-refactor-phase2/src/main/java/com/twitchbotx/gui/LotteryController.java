/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.*;
import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.bot.handlers.*;
import java.io.PrintStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
//import javax.annotation.concurrent.GuardedBy;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class LotteryController implements Initializable {

    ScreensController myController = new ScreensController();

    private Datastore store;
    final ConfigParameters configuration = new ConfigParameters();
    private static final TwitchMessenger messenger = guiHandler.messenger;
    private PrintStream out;
    LinkedHashMap<String, LotteryHandler.Entrant<Integer, String>> MAP;
    public static LotteryHandler.Lotto lotto = CommandParser.lotto;
    static int c = 0;

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
    
    private synchronized ScrollPane getPane(){
        return queueList;
    }

    private synchronized void setPane(VBox vB){
        this.queueList.setContent(vB);
    }
    
    @FXML
    public synchronized void showQueue() {
        boolean isEmpty = lotto.getCurr().isEmpty();
        if(!isEmpty){
        MAP = lotto.getMap();
        if(!lotto.getCurr().isEmpty()){
        MAP.entrySet().forEach((m) -> {
            System.out.println("Current map item: " + m.getKey() + "  current tickets: " + m.getValue().getTicket());
        });
        VBox vB = new VBox();
        vB.setAlignment(Pos.CENTER_LEFT);
        vB.setPrefWidth(225);
        vB.setPrefHeight(318);
        MAP.entrySet().forEach((m) -> {
            vB.getChildren().add(new Label(" " + (c + 1) + ". " + m.getKey()));
            c++;
        });
        c = 0;
        //setPane(vB);
        queueList.setContent(vB);
        } else{
            qStatus.setText("Lottery is empty");
        }
        } else {
            VBox vB = new VBox();
            //setPane(vB);
            queueList.setContent(vB);
        }
    }

    @FXML
    public void clearQueue() {
        lotto.lottoClear();
        showQueue();
        qStatus.setText("Lottery queue cleared");
    }

    @FXML
    public void openQueue() {
        //Handle text from keyword box
        //Check sub only button
        //Clear and enable list
        keywordText.selectAll();
        keywordText.copy();
        System.out.println("Keyword set to: " + keywordText.getText());
        String sub = "+a";
        if (!keywordText.getText().isEmpty()) {
            qStatus.setAlignment(Pos.CENTER);
            if (subRadioButton.isSelected()) {
                sub = "+s";
                qStatus.setText("A sub-only lottery has started! Subs can type " + keywordText.getText() + " to enter!");
            } else {
                qStatus.setText("Lottery has started! Type " + keywordText.getText() + " to enter!");
            }
            addUserTextField.clear();
            winnerText.setText("");
            lotto.lottoOpen("!lotto-open " + sub + " " + keywordText.getText());
        } else {
            qStatus.setText("No keyword found to open lottery");
        }

    }

    @FXML
    public void closeQueue() {
        lotto.lottoClose();
        addUserTextField.setText("Lottery closed");
        // send message to user to join bot to channel before using lottery
        qStatus.setText("Lottery currently closed");
    }

    @FXML
    public void addUser() {
        if (lotto.getLottoStatus()) {
            addUserTextField.selectAll();
            addUserTextField.copy();

            if (!addUserTextField.getText().isEmpty()) {
                try {
                    if (lotto.getCurr().contains(addUserTextField.getText()) || addUserTextField.getText().equals("Duplicate entry found")) {
                        addUserTextField.setText("Duplicate entry found");
                    } else {
                        lotto.addUser(addUserTextField.getText(), true);
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

    @FXML
    public void popUser() {
        try {
            boolean isEmpty = lotto.getCurr().isEmpty();
            if (!isEmpty) {
                Collections.shuffle(names);
                winner = lotto.drawLotto();
                winnerText.setAlignment(Pos.CENTER);
                winnerText.setText(winner);
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                sendEvent(sdf.format(cal.getTime()) + " lottery winner: " + winner);
                showQueue();
            } else {
                winnerText.setAlignment(Pos.CENTER);
                sendMessage("Lottery is empty");
                winnerText.setText("Lottery is empty");
                showQueue();
            }
        } catch (NullPointerException n) {
            sendMessage("Lottery is closed");
            winnerText.setText("Lottery is closed");
        }
    }

    private void sendEvent(final String msg) {
        String event = msg;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                guiHandler.bot.getStore().getEventList().addList(event);
            }
        });
    }

    @FXML
    public void goToDashboard() {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    public void songLotto() {
        setDimensions();
        myController.loadScreen(guiHandler.songLottoID, guiHandler.songLottoFile);
        myController.setScreen(guiHandler.songLottoID);
        myController.setId("songLotto");
        myController.show(myController);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        MAP = lotto.getMap();
        try {
            // Create store instance to access lotteryhandler
            names = new ArrayList();
            store = guiHandler.bot.getStore();
            out = store.getBot().getOut();
            out.println("TESTPRIVMSG #");
            qStatus.setText("Lottery ready to use");
        } catch (NullPointerException e) {
            qStatus.setText("Start bot prior to opening Lottery");
        } catch (Exception ex) {
            Logger.getLogger(LotteryController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendMessage(String msg) {
        msg = "/me > " + msg;
        try {
            store.getBot().getOut().println("PRIVMSG #"
                    + store.getConfiguration().joinedChannel
                    + " :"
                    + msg);
        } catch (NullPointerException ne) {
            // send message to user to join bot to channel before using lottery
            qStatus.setText("Bot must connect first");
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
