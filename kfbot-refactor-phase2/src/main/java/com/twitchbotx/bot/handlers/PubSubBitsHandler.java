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
 * @author Raxa
 */
public class PubSubBitsHandler {

    private static WebSocket connection = null;
    private static String CHANNEL_ID;
    private static String CHANNEL_AUTH_TOKEN;
    private final Datastore store;
    private static int pongCounter = 0;
    private final PrintStream outstream;

    public PubSubBitsHandler(final Datastore store,
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
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> connection.sendText("{ \"type\": \"PING\" }"), 3, 5, TimeUnit.MINUTES);
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> checkPongs(store, outstream), 22, 20, TimeUnit.MINUTES);
    }

    private static void checkPongs(final Datastore store,
            final PrintStream outstream) {
        if (pongCounter < 2) {
            connection.sendClose();
            System.out.println("Attemping to reconnect to bits PUBSUB");
            PubSubBitsHandler.connect(store, outstream);
        } else {
            pongCounter = 0;
        }
    }

    private static class PubSubListener extends WebSocketAdapter {

        private final Datastore store;
        private final PrintStream outstream;
        private TwitchMessenger messenger;

        public PubSubListener(final Datastore store, final PrintStream outstream) {
            this.store = store;
            this.outstream = outstream;
            this.messenger = new TwitchMessenger(outstream, store.getConfiguration().joinedChannel);
        }

        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) {
            System.out.println("Bits PubSub connected");
            ObjectNode dataNode = JsonNodeFactory.instance.objectNode();
            dataNode.putArray("topics").add("channel-bits-events-v1." + CHANNEL_ID);
            dataNode.put("auth_token", CHANNEL_AUTH_TOKEN);
            ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
            rootNode.put("type", "LISTEN");
            rootNode.set("data", dataNode);
            websocket.sendText(rootNode.toString());
            //System.out.println(rootNode.toString());
        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) {
            //TODO change to synchronized get method in store            
            String enabled = this.store.getConfiguration().spoopathonStatus;
            String marathonEnabled = this.store.getConfiguration().marathonStatus;
            if (enabled.equals("on")) {
                String incoming = text;
                try {
                    JsonNode root = new ObjectMapper().readTree(incoming);
                    if (root.get("type").asText().equalsIgnoreCase("MESSAGE")) {
                        String topic = root.get("data").get("topic").toString();;
                        topic = topic.replaceAll("\"", "");
                        if (topic.equalsIgnoreCase("channel-bits-events-v1." + CHANNEL_ID)) {
                            JsonNode messageNode = new ObjectMapper().readTree(root.get("data").get("message").asText());
                            int bits = messageNode.get("data").get("bits_used").asInt();
                            double bits1 = messageNode.get("data").get("bits_used").asDouble();
                            String userName = messageNode.get("data").get("user_name").asText();
                            String msg = messageNode.get("data").get("chat_message").asText();
                            //send information to CountHandler.java
                            //bits are added 1 point per cent
                            sendEvent(userName, msg, bits);
                            //bits1 = bits1 / 100;
                            int b = (int) ((bits1) / 100);
                            //addPoints(bits);
                            if (!msg.contains("#") && bits > 99) {
                                messenger.sendMessage("@" + userName + ", let a mod know what game you'd like to choose for !spoopathon");
                            } else {
                                addSubSQLPoints(msg, b);
                            }
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
                //System.out.println("PubSub Frame: " + frame.getPayloadText());
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

        public void addPoints(int bits) {
            System.out.println(bits + " BITS ADDPOINTS");
            CountHandler ch = new CountHandler(store, outstream);
            //ch.addPoints("!addPoints " + Integer.toString(bits));
        }

        public void addSubSQLPoints(String msg, int points) {
            //parse for #game, send with points to sqlHandler.java
            sqlHandler sql = new sqlHandler(store, outstream);
            sql.gameSearch(msg, points);
        }

        private void sendEvent(String user, String msg, int bits) {
            String eventMsg = "Bit Event: " + user + " cheered " + bits + " bits. Message: " + msg;
            DashboardController dc = new DashboardController();
            dc.eventObLAdd(eventMsg);
        }
    }
}
