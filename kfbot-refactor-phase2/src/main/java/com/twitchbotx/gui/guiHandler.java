/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.*;
import com.twitchbotx.bot.ConfigParameters.Elements;
import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.bot.handlers.CommonUtility;
import com.twitchbotx.bot.handlers.LotteryHandler;
import com.twitchbotx.bot.handlers.SpoopathonHandler;
//import com.twitchbotx.bot.handlers.LotteryHandler;
import eu.mihosoft.scaledfx.ScalableContentPane;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
import java.io.PrintStream;
//import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
//import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
//import javafx.concurrent.Task;
import javax.annotation.concurrent.GuardedBy;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;

/**
 *
 * @author Raxa
 */
public class guiHandler extends Application {

    public Datastore store;
    final ConfigParameters configuration = new ConfigParameters();

    public static TwitchBotX bot;
    public final static List<String> songList = new java.util.ArrayList<>();
    private Scanner scan;

    public static String dashboardID = "dashboard";
    public static String dashboardFile = "Dashboard.fxml";

    //Dashboard button related
    public static String featuresID = "features";
    public static String featuresFile = "FeatureLanding.fxml";
    public static String configurationID = "configuration";
    public static String configurationFile = "Configuration.fxml";
    public static String moderationID = "moderation";
    public static String moderationFile = "Moderation.fxml";
    public static String commandsID = "commands";
    public static String commandsFile = "Commands.fxml";

    // Command page related
    public static String commandEditorID = "commandEditor";
    public static String commandEditorFile = "CommandEditor.fxml";
    public static String timedID = "timed";
    public static String timedFile = "TimedCommands.fxml";

    // Moderation page related
    public static String regexID = "regex";
    public static String regexFile = "Regex.fxml";

    // Feature landing page related
    public static String respEditID = "RespEdit";
    public static String respEditFile = "AlertResponse.fxml";
    public static String eventEditID = "EventEdit";
    public static String eventEditFile = "Events.fxml";
    public static String screenEditID = "ScreenEdit";
    public static String screenEditFile = "ScreenEdit.fxml";
    public static String lotteryID = "lottery";
    public static String lotteryFile = "Lottery.fxml";
    public static String tipFeatureID = "TipFeature";
    public static String tipFeatureFile = "TipFeature.fxml";
    public static String counterID = "Counter";
    public static String counterFile = "Counter.fxml";
    public static String pollID = "Poll";
    public static String pollFile = "PollFeature.fxml";

    // Response page related
    public static String subEditID = "SubEdit";
    public static String subEditFile = "SubEdit.fxml";
    public static String bitEditID = "BitEdit";
    public static String bitEditFile = "BitEdit.fxml";
    public static String raidEditID = "RaidEdit";
    public static String raidEditFile = "RaidEdit.fxml";

    // Event page related
    public static String spoopathonID = "spoopathon";
    public static String spoopathonFile = "Spoopathon.fxml";
    public static String marathonID = "marathon";
    public static String marathonFile = "Marathon.fxml";

    public static Stage stage = new Stage();

    public static String[] spoopGames;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts application Sets up store and configuration Creates instance of
     * the bot but does not read chat until started Sets up ScreenController to
     * handle scene switching, default to dashboard
     */
    @Override
    public void start(Stage primaryStage) {
        //begin bot setup
        try {
            Path xmlFile = Paths.get("");
            Path xmlResolved = xmlFile.resolve("kfbot.xml");
            final Elements elements = configuration.parseConfiguration(xmlResolved.toString());
            store = new XmlDatastore(elements);
            bot = new TwitchBotX(store);
            store.setBot(bot);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        //create songlist based on text content
        try {
            Path location = Paths.get("");
            Path lResolved = location.resolve("jdSongs.txt");
            scan = new Scanner(lResolved);
            String temp = "";
            String numSong = "";
            while (scan.hasNext()) {
                temp = scan.nextLine();
                if (temp.contains(".")) {
                    //System.out.println(temp);
                    int numEndIndex = temp.indexOf(".");
                    numSong = temp.substring(0, numEndIndex);
                    songList.add(temp);
                }
            }

            Platform.runLater(() -> {
                try {
                    guiHandler.bot.getStore().getEventList().addList("Successfully parsed song list from jdSongs.txt");
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("Successfully parsed song list from jdSongs.txt");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error occured trying to create song list");
        }

        //recover and/or create songLottery.map file to hold LinkedHashMap<String, Entrant<Integer, String>>
        //uncomment next 2 lines to create new songLottery.map file
        //LinkedHashMap<String, LotteryHandler.Entrant<Integer, String>> MAP = new LinkedHashMap<>();
        //CommandParser.lotto.writeMap(MAP);
        CommandParser.songs.getMapFromFile();
        CommandParser.lotto.getMapFromFile();
        // attempt to recover spoopathon points map from spoopUser.map
        CommandParser.spoop.getMapFromFile();

        //create and show GUI
        ScreensController container = new ScreensController();
        container.loadScreen(dashboardID, dashboardFile);
        container.setScreen(dashboardID);

        // TODO resizing scale options
        Group root = new Group();
        root.getChildren().addAll(container);
        try {
            Scene scene = new Scene(root);
            scene.getStylesheets().add("stylesheet.css");

            ScalableContentPane scale = new ScalableContentPane();
            scale.setContent(root);

            Scene testScale = new Scene(scale, 600, 400);
            //testScale.getStylesheets().add(stylesheet.getCanonicalPath());
        
        stage.setScene(scene);
        //stage.setScene(testScale);
        String botName = store.getConfiguration().account;
        stage.setTitle(botName);
        stage.getIcons().add(new Image("http://kf.bot.raxastudios.com/kffcLove.png"));
        stage.show();
        stage.setOnCloseRequest(e -> {
            // TODO send error report via ftp
            System.exit(0);
        });
        }catch (Exception ie) {
            ie.printStackTrace();
        }
    }

    public static String nowDateFormatted() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String returnDate = dtf.format(now);
        return returnDate;
    }

    //storage for width/height variables to allow scalable content
    public static class dimensions {

        private int width = 600;

        private int height = 400;

        public int getWidth() {
            return width;
        }

        public void setWidth(int w) {
            width = w;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int h) {
            height = h;
        }
    }
}
