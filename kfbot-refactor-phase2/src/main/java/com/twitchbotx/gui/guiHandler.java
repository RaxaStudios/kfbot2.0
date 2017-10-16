/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.*;
import com.twitchbotx.bot.handlers.*;
import com.twitchbotx.bot.ConfigParameters.Elements;
import com.twitchbotx.bot.client.TwitchMessenger;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 *
 * @author Raxa
 */
public class guiHandler extends Application {

    //TODO
    public static Datastore store;
    final ConfigParameters configuration = new ConfigParameters();
    public static TwitchMessenger messenger;
    public static PrintStream out;
    public static BufferedReader in;
    public static TwitchBotX bot;
    public static Socket socket;

    //refine event viewer mod actions, command updates, etc
    public static String dashboardID = "dashboard";
    public static String dashboardFile = "Dashboard.fxml";
    //public InputStream dashIn = this.getClass().getClassLoader().getResourceAsStream(dashboardFile);
    //add/edit/delete/cooldown/auth etc view all scrollable
    public static String commandsID = "commands";
    public static String commandsFile = "Commands.fxml";
    public static String commandEditorID = "commandEditor";
    public static String commandEditorFile = "CommandEditor.fxml";
    //configuration -> account settings, joined channel
    public static String configurationID = "configuration";
    public static String configurationFile = "Configuration.fxml";
    //moderation -> filters, phrases, toggles, spam
    public static String moderationID = "moderation";
    public static String moderationFile = "Moderation.fxml";
    //queue, lottery, raffle display users + order, scrollable
    public static String lotteryID = "lottery";
    public static String lotteryFile = "Lottery.fxml";
    //spoopathon system
    public static String spoopathonID = "spoopathon";
    public static String spoopathonFile = "Spoopathon.fxml";
    //marathon system
    public static String marathonID = "marathon";
    public static String marathonFile = "Marathon.fxml";

    public static Stage stage = new Stage();

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
        try {
            Path xmlFile = Paths.get("");
            Path xmlResolved = xmlFile.resolve("kfbot.xml");
            System.out.println("kfbot.xml file found at path: " + xmlResolved.toString());
            final Elements elements = configuration.parseConfiguration(xmlResolved.toString());
            store = new XmlDatastore(elements);
            socket = new Socket(store.getConfiguration().host, store.getConfiguration().port);
            out = new PrintStream(socket.getOutputStream());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("PASS " + store.getConfiguration().password);
            out.println("NICK " + store.getConfiguration().account);
            out.println("JOIN #" + store.getConfiguration().joinedChannel);
            messenger = new TwitchMessenger(out, store.getConfiguration().joinedChannel);
            bot = new TwitchBotX();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ScreensController container = new ScreensController();
        container.loadScreen(dashboardID, dashboardFile);
        container.setScreen(dashboardID);

        Group root = new Group();
        root.getChildren().addAll(container);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> System.exit(0));
    }
}
