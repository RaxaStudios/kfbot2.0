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
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        pBar.setProgress(0.50);
        System.out.println("value of tColor: " + tColor.getValue() + " hashvalue: " + tColor.getValue().hashCode());
    }    
    
    
    
    // TODO preview panel? 
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
}
