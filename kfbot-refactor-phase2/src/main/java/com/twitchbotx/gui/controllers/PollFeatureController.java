/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.CommandParser;
import com.twitchbotx.bot.handlers.PollHandler;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
public class PollFeatureController implements Initializable {

    ScreensController myController = new ScreensController();

    private String winner = "";
    private int highest = 0;
    private boolean tie = false;
    private StringBuilder wB = new StringBuilder();
    private static LinkedHashMap<String, Integer> optionMap = new LinkedHashMap<>();
    private static PollHandler pHandler = CommandParser.pHandler;
    private List<String> options = new ArrayList<>();

    @FXML
    ListView pollList;
    public static ObservableList<String> optionList = FXCollections.observableArrayList();

    @FXML
    Label winningNumber;

    @FXML
    Label winningText;
    
    @FXML
    Label intervalError;

    // TODO timer option
    // @FXML
    // TextField time;
    @FXML
    TextField option1;

    @FXML
    TextField option2;

    @FXML
    TextField option3;

    @FXML
    TextField option4;

    @FXML
    TextField intervalText;

    public PollFeatureController() {
        //dummy constructor
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        intervalError.setText("");
        showStats();
        int i = 1;
        for (String s : optionList) {
            s = s.substring(0, s.lastIndexOf(":"));
            switch (i) {
                case 1:
                    option1.setText(s);
                    i++;
                    break;
                case 2:
                    option2.setText(s);
                    i++;
                    break;
                case 3:
                    option3.setText(s);
                    i++;
                    break;
                case 4:
                    option4.setText(s);
                    i++;
                    break;
                default:
                    break;
            }
        }
    }

    @FXML
    public void showStats() {
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    System.out.println("show");
                    optionMap = pHandler.getMap();
                    optionList.clear();
                    optionMap.entrySet().forEach((m) -> {
                        optionList.add(m.getKey() + ": " + m.getValue());
                        System.out.println("adding: " + m.getKey() + ": " + m.getValue());
                    });
                }
            });
            pollList.setItems(optionList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void reset() {
        optionList.clear();
        option1.setText("");
        option2.setText("");
        option3.setText("");
        option4.setText("");
        winningText.setText("");
        winningNumber.setText("");
    }

    @FXML
    private void start() {
        String txt;
        int interval;
        winningText.setText("");
        winningNumber.setText("");
        options.clear();
        option1.selectAll();
        option1.copy();
        txt = option1.getText();
        if (!txt.replaceAll(" ", "").equals("")) {
            options.add(txt);
            System.out.println("Added: " + txt);
        }
        option2.selectAll();
        option2.copy();
        txt = option2.getText();
        if (!txt.replaceAll(" ", "").equals("")) {
            options.add(txt);
            System.out.println("Added: " + txt);
        }
        option3.selectAll();
        option3.copy();
        txt = option3.getText();
        if (!txt.replaceAll(" ", "").equals("")) {
            options.add(txt);
            System.out.println("Added: " + txt);
        }
        option4.selectAll();
        option4.copy();
        txt = option4.getText();
        if (!txt.replaceAll(" ", "").equals("")) {
            options.add(txt);
            System.out.println("Added: " + txt);
        }
        intervalText.selectAll();
        intervalText.copy();
        try {
            interval = Integer.parseInt(intervalText.getText());
            pHandler.startPoll(options, interval);
            showStats();
        } catch (Exception e) {
            e.printStackTrace();
            intervalError.setText("Interval not valid");
        }
    }

    @FXML
    private void end() {
        pHandler.endPoll();
        showStats();
    }

    @FXML
    private void drawWinner() {
        pHandler.drawWinner();
        getResults();
        showStats();
    }

    public void getResults() {
        wB.append("There has been a tie between: ");
        highest = 0;
        winner = "";
        optionMap.entrySet().forEach((m) -> {
            if (m.getValue() > highest) {
                highest = m.getValue();
                winner = m.getKey();
                tie = false;
                highest = m.getValue();
                winner = m.getKey();
                wB.append(m.getKey() + " and ");
            } else if (m.getValue() == highest) {
                tie = true;
                wB.append(m.getKey() + " and ");
            }
        });
        wB.delete(wB.lastIndexOf("and ") - 1, wB.length());
        wB.append(" at " + highest + " votes each!");
        System.out.println(wB.toString());
        if (tie) {
            winningText.setText("A tie has occurred");
            winningNumber.setText(String.valueOf(highest));
        } else {
            winningText.setText(winner);
            winningNumber.setText(String.valueOf(highest));
        }
    }

    @FXML
    public void goDash() {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
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
