/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.gui.DashboardController;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author RaxaStudios
 */

/*
** Manages connection and events from Twitch PubSub feed
** Outputs relay changes in stream video (going live/offline)
** as well as whispers received
** To be passed to Discord handler for stream is live notification
** whispers for filter management
 */

 /*
** Per PubSub API documentation
** send PING at least once per 5 minutes
** reconnect if no PONG message is received within 10 seconds
** RECONNECT messages may be received
** bot should reconnect within 30 seconds of message
 */
public class PubSubSubscriptionHandler {

    private static WebSocket connection = null;
    private static String CHANNEL_ID;
    private static String CHANNEL_AUTH_TOKEN;
    private static int pongCounter = 0;
    private final Datastore store;

    private final PrintStream outstream;

    public PubSubSubscriptionHandler(final Datastore store,
            final PrintStream outstream) {
        this.store = store;
        this.outstream = outstream;
        this.CHANNEL_ID = store.getConfiguration().channelID;
        this.CHANNEL_AUTH_TOKEN = store.getConfiguration().pubSubAuthToken;
    }

    public static void connect(final Datastore store,
            final PrintStream outstream) {
        try {
            connection = new WebSocketFactory().createSocket("wss://pubsub-edge.twitch.tv").addListener(new PubSubListener(store, outstream)).connect();

        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> connection.sendText("{ \"type\": \"PING\" }"), 4, 5, TimeUnit.MINUTES);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> checkPongs(store, outstream), 21, 20, TimeUnit.MINUTES);
    }

    private static void checkPongs(final Datastore store,
            final PrintStream outstream) {
        if (pongCounter < 2) {
            System.out.println("Attemping to reconnect to sub PUBSUB");
            connection.sendClose();
            PubSubSubscriptionHandler.connect(store, outstream);
        } else {
            pongCounter = 0;
        }
    }

    private static class PubSubListener extends WebSocketAdapter {

        private final Datastore store;

        private TwitchMessenger messenger;

        private final PrintStream outstream;

        public PubSubListener(final Datastore store,
                final PrintStream outstream) {
            this.store = store;
            this.outstream = outstream;
            this.messenger = new TwitchMessenger(outstream, store.getConfiguration().joinedChannel);
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
            System.out.println("Subs PubSub connected");
            ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
            dataNode.putArray("topics").add("channel-subscribe-events-v1." + CHANNEL_ID);
            dataNode.put("auth_token", CHANNEL_AUTH_TOKEN);
            ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
            rootNode.put("type", "LISTEN");
            rootNode.set("data", dataNode);
            websocket.sendText(rootNode.toString());
            //System.out.println(rootNode.toString());
        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) {
            String enabled = this.store.getConfiguration().sStatus;
            if (enabled.equals("on")) {

                try {
                    JsonNode root = new ObjectMapper().readTree(text);
                    System.out.println(root.toString());
                    if (root.get("type").asText().equalsIgnoreCase("MESSAGE")) {
                        String topic = root.get("data").get("topic").asText();
                        if (topic.equalsIgnoreCase("channel-subscribe-events-v1." + CHANNEL_ID)) {
                            JsonNode messageNode = new ObjectMapper().readTree(root.get("data").get("message").asText());
                            System.out.println(messageNode.toString());
                            String userId = messageNode.get("user_id").asText();
                            int months = messageNode.get("months").asInt();
                            String displayName = messageNode.get("display_name").asText();
                            String subPlan = messageNode.get("sub_plan").asText();
                            int points = 0;
                            int spoopPoints = 0;
                            String subTier = "";
                            if (subPlan.equals("Prime")) {
                                spoopPoints = 5;
                                points = 500;
                                subTier = "Prime";
                            } else if (subPlan.equals("1000")) {
                                spoopPoints = 5;
                                points = 500;
                                subTier = "Tier 1";
                            } else if (subPlan.equals("2000")) {
                                spoopPoints = 10;
                                points = 1000;
                                subTier = "Tier 2";
                            } else if (subPlan.equals("3000")) {
                                spoopPoints = 25;
                                points = 2500;
                                subTier = "Tier 3";
                            }
                            String msg = messageNode.get("sub_message").get("message").asText();
                            if (months < 2) {
                                months = 0;
                            }
                            sendEvent(displayName, msg, subTier, months);
                            if (!msg.contains("#")) {
                                System.out.println("MSG !CONTAIN #: " + msg);
                                messenger.sendMessage("@" + displayName + ", let a mod know what game you'd like to choose for !spoopathon");
                            } else {
                                //addDonationWarItem(msg, points);
                                System.out.println("PRINT ONLY going to subSQL: " + msg + " p: " + spoopPoints);
                                addSubSQLPoints(msg, spoopPoints);
                            }

                            //sub point tracker
                            if (months == 1 || months == 0) {
                                //add 1 to subPoints in sql table kfTimer, new subs only
                                CountHandler addSubPoint = new CountHandler(store, outstream);
                                //addSubPoint.addSubPointTracker();
                            }

                            //keep track of sub points, first 180 new sub points are double points
                            CountHandler getPoints = new CountHandler(store, outstream);
                            int subNum = 0;
                            //getPoints.getSubPoints();
                            if ((months == 1 || months == 0) && subNum < 201) {
                                points *= 2;
                                //getPoints.addSubPoint();
                            }
                            System.out.println("months" + months + " subPlan:" + subPlan + " points: " + points);
                            //send information to CountHandler.java
                            //addPoints(points);

                        }
                    } else if (root.get("type").asText().contains("PONG")) {
                        pongCounter++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) {
            System.out.println("***PubSub Error Message: " + new String(data));
        }

        @Override
        public void onFrame(WebSocket websocket, WebSocketFrame frame) {
            if (!frame.getPayloadText().contains("\"type\": \"PONG\"")) {
                System.out.println("PubSub Frame: " + frame.getPayloadText());
            }
            //Ignore pong responses for now, correct behavior would be to time the ping/pong difference and reconnect if no pong response 10 seconds after ping.
        }

        @Override
        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) {
            System.out.println("PubSub Ping Frame" + frame);
        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) {
            System.out.println("**PubSub Error: " + cause.toString());
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
            System.out.println("PubSub disconnected, by server: " + closedByServer);
        }

        public void addPoints(int points) {
            CountHandler ch = new CountHandler(store, outstream);
            //ch.addPoints("!addPoints " + Integer.toString(points));
        }

        public void addDonationWarItem(String msg, int points) {
            CountHandler dw = new CountHandler(store, outstream);
            //dw.addDonationPoints(msg, points);
        }

        public void addSubSQLPoints(String msg, int points) {
            //parse for #game, send with points to sqlHandler.java
            sqlHandler sql = new sqlHandler(store, outstream);
            sql.gameSearch(msg, points);
        }

        private void sendEvent(String user, String msg, String subTier, int months) {
            String eventMsg;
            String monthFormat;
            String subFormat;
            if (months == 0) {
                monthFormat = "as a new sub!";
            } else {
                monthFormat = "for " + months + " in a row!";
            }
            if (subTier.equals("Prime")) {
                subFormat = " using Twitch Prime";
            } else {
                subFormat = " at " + subTier;
            }
            eventMsg = "Subscriber Event: " + user + " subscribed " + subFormat + " " + monthFormat + " Message: " + msg;
            DashboardController dc = new DashboardController();
            dc.eventObLAdd(eventMsg);
        }
    }
}
