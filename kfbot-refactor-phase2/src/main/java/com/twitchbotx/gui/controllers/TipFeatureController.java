/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.controllers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class TipFeatureController implements Initializable {

    ScreensController myController = new ScreensController();
    Datastore store;

    @FXML
    ProgressBar pBar;

    @FXML
    ColorPicker bColor;

    @FXML
    ColorPicker pColor;

    @FXML
    ColorPicker tColor;

    //TODO: add ability to change incremential values
    // ie per sub = 1 versus dollar value
    //TODO: intialize all values from sql on open
    // hex values, preview bar, enabled/disabled, text values
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        pBar.setProgress(0.50);
        pBar.setStyle("-fx-accent: blue");
        bColor.setValue(Color.web("#ffffff"));
        pColor.setValue(Color.BLUE);
        tColor.setValue(Color.web("#ffccff"));
        System.out.println("value of tColor: " + tColor.getValue().toString().substring(2, 8) + " hashvalue: " + tColor.getPromptText());
    }

    @FXML
    public void dash() {
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

    // color value setters
    // need to reset all css values on each change
    @FXML
    public void setColors() {
        //send to sql - _kfOverlay -> tipTracker -> bgColor

        //update local bar
        pBar.setStyle("-fx-background-color: #" + bColor.getValue().toString().substring(2, 8));
        pBar.setStyle("-fx-color: #" + tColor.getValue().toString().substring(2, 8));
        pBar.setStyle("-fx-accent: #" + pColor.getValue().toString().substring(2, 8));
    }

    //enabled disabled value setters
    @FXML
    public void submitBitEnabled() {

    }

    @FXML
    public void submitSubEnabled() {

    }

    @FXML
    public void submitTipEnabled() {

    }

    /**
     * Capture and set all values values: heading(optional), text
     * inside(optional), begin/total amount, description(optional)
     */
    @FXML
    public void submitValues() {

    }

    /**
     * Reset all local and sql values to defaults
     */
    @FXML
    public void resetValues() {

    }

}
