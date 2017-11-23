/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.CommandParser;
import com.twitchbotx.bot.handlers.LotteryHandler;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class SongLottoController implements Initializable {

    ScreensController myController = new ScreensController();
    LinkedHashMap<String, LotteryHandler.Entrant<Integer, String>> MAP;
    public static LotteryHandler.SongList entries = CommandParser.songs;
    static int c = 0;
    @FXML
    ScrollPane queueList;

    @FXML
    Label statusWindow;

    @FXML
    TextField username;

    @FXML
    TextField songName;

    @FXML
    Label winnerName;

    @FXML
    Label winnerSong;

    @FXML
    public void clearQueue() {
        entries.getMap().clear();
        statusWindow.setText("Lottery has been emptied");
        showQueue();
    }

    @FXML
    public void openLotto() {
        entries.songOpen();
        statusWindow.setText("Lottery has been opened !song [song name] to enter");
    }

    @FXML
    public void closeLotto() {
        entries.songClose();
        statusWindow.setText("Lottery has been closed");
    }

    @FXML
    public void addUser() {
        username.selectAll();
        username.copy();
        String user = username.getText();
        songName.selectAll();
        songName.copy();
        String content = songName.getText();
        entries.addUser(user, "!song " + content);
        statusWindow.setText(user + " has been added with song: " + content);
        username.setText("");
        songName.setText("");
        showQueue();
    }

    @FXML
    public void drawWinner() {
        String winner = entries.drawSong();
        int begIndex = winner.indexOf("song:") + 7;
        int endIndex = winner.length();
        String song = winner.substring(begIndex, endIndex);
        winner = winner.substring(0, winner.indexOf("song:")-1);
        winnerName.setText(winner);
        winnerSong.setText(song);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sendEvent(sdf.format(cal.getTime()) + " song lottery winner: " + winner + " with song: " + song);
        showQueue();
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
    public void showQueue() {

        MAP = entries.getMap();

        MAP.entrySet().forEach((m) -> {
            System.out.println("Current map item: " + m.getKey() + "  current tickets: " + m.getValue().getTicket());
        });
        VBox vB = new VBox();
        vB.setAlignment(Pos.CENTER_LEFT);
        vB.setPrefWidth(225);
        vB.setPrefHeight(318);
        MAP.entrySet().forEach((m) -> {
            vB.getChildren().add(new Label(" " + (c + 1) + ". " + m.getKey() + ": " + m.getValue().getContent()));
            c++;
        });
        c = 0;
        queueList.setContent(vB);
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
        // TODO
        MAP = entries.getMap();
        entries.addUser("Test user 1", "!song song choice 1");
        entries.addUser("Test user 3", "!song song choice 1");
        entries.addUser("Test user 2", "!song song choice 1");
        entries.addUser("Test user 4", "!song song choice 1");
        entries.addUser("Test user 5", "!song snog choice 1");
        entries.addUser("Test user 6", "!song song choice 1");
        entries.addUser("Test user 7", "!song song choice 1");
        entries.addUser("Test user 38", "!song song choice 1");
        MAP.entrySet().forEach((m) -> {
            System.out.println("Current map item: " + m.getKey() + "  current tickets: " + m.getValue().getTicket());
        });
        showQueue();
        statusWindow.setText("Song lottery ready to be opened");
    }

}
