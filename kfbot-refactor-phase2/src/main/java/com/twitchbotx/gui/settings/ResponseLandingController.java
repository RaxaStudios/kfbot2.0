/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui.settings;

import com.twitchbotx.gui.ScreensController;
import com.twitchbotx.gui.guiHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class ResponseLandingController implements Initializable {

    ScreensController myController = new ScreensController();
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO set the toggle groups for sub, bit, raid
        /* example:
        if (store.getConfiguration().marathonStatus.equals("on")) {
            marEnabled.setSelected(true);
        } else {
            marDisabled.setSelected(true);
        }
        */
    }    
    
   
    
    @FXML
    public void goDash() {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }
    
    @FXML
    public void goSettings() {
        setDimensions();
        myController.loadScreen(guiHandler.configurationID, guiHandler.configurationFile);
        myController.setScreen(guiHandler.configurationID);
        myController.setId("configuration");
        myController.show(myController);
    }
    
     @FXML
    public void goSub(){
        setDimensions();
        myController.loadScreen(guiHandler.subEditID, guiHandler.subEditFile);
        myController.setScreen(guiHandler.subEditID);
        myController.setId("SubEdit");
        myController.show(myController);
    }
    
    @FXML
    public void goBit() {
        setDimensions();
        myController.loadScreen(guiHandler.bitEditID, guiHandler.bitEditFile);
        myController.setScreen(guiHandler.bitEditID);
        myController.setId("BitEdit");
        myController.show(myController);
    }
    
    @FXML
    public void goRaid() {
        setDimensions();
        myController.loadScreen(guiHandler.raidEditID, guiHandler.raidEditFile);
        myController.setScreen(guiHandler.raidEditID);
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
