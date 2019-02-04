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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class EventsController implements Initializable {

    ScreensController myController = new ScreensController();
    Datastore store;
    
    @FXML
    Label saveText;
    
    @FXML
    TextField subValue;

    @FXML
    TextField bitValue;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        store = guiHandler.bot.getStore();
        
        // set spoopathon point values 
        String value = store.getConfiguration().spoopSubValue;
        System.out.println(value);
        subValue.setText(value);
        value = store.getConfiguration().spoopBitValue;
        System.out.println(value);
        bitValue.setText(value);
    }    
    
    
     @FXML
    private void spoopathon(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.spoopathonID, guiHandler.spoopathonFile);
        myController.setScreen(guiHandler.spoopathonID);
        myController.setId("spoopathon");
        myController.show(myController);
    }

    @FXML
    private void marathon(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.marathonID, guiHandler.marathonFile);
        myController.setScreen(guiHandler.marathonID);
        myController.setId("marathon");
        myController.show(myController);
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
    
    @FXML
    public void goTipFeature() {
        setDimensions();
        myController.loadScreen(guiHandler.tipFeatureID, guiHandler.tipFeatureFile);
        myController.setScreen(guiHandler.tipFeatureID);
        myController.setId("tipFeature");
        myController.show(myController);
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
