/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.bot.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twitchbotx.bot.Datastore;
import com.twitchbotx.bot.client.TwitchMessenger;
import com.twitchbotx.gui.DashboardController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 *
 * @author Raxa
 */
public class DonationHandler implements Runnable {

    private static String accessToken;
    private final Datastore store;
    private final PrintStream outstream;
    private final TwitchMessenger messenger;
    private String temp = "";
    private int tempCount = 0;

    public DonationHandler(final Datastore store, final PrintStream outstream) {
        this.store = store;
        this.outstream = outstream;
        this.messenger = new TwitchMessenger(outstream, store.getConfiguration().joinedChannel);
        this.accessToken = store.getConfiguration().streamlabsToken;
    }

    public void run() {
        try {
            String limit = "1";
            URL url = new URL("https://streamlabs.com/api/v1.0/donations?access_token=" + accessToken + "&limit=" + limit + "&currency=USD");
            HttpURLConnection t = (HttpURLConnection) url.openConnection();
            t.setRequestMethod("GET");
            t.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.85 Safari/537.36");
            BufferedReader read = new BufferedReader(new InputStreamReader(t.getInputStream()));
            JsonNode data = new ObjectMapper().readTree(read.readLine());

            String donation = data.get("data").toString();
            //System.out.println("donation:  " + donation);
            donation = donation.replace("[", "");
            donation = donation.replace("]", "");
            JsonNode low = new ObjectMapper().readTree(donation);
            if (tempCount == 0) {
                temp = low.get("donation_id").asText();
                tempCount++;
                //for testing 
                temp = low.get("donation_id").asText();
                double amount = low.get("amount").asDouble();
                DecimalFormat f = new DecimalFormat("##.00");

                String outAmount = (String) f.format(amount);
                int am = low.get("amount").asInt();
                outAmount = outAmount.replaceAll("[^0-9]", "");
                System.out.println("DONATIONID:" + temp);
                System.out.println("Donation amount: $" + am + " formatted for marathon:" + outAmount);
                String username = low.get("name").asText();
                System.out.println("From: " + username);
                String msg = low.get("message").asText();
                System.out.println("Message: " + msg);
            }

            if (temp.equals("") || !temp.equals(low.get("donation_id").asText())) {
                //convert dollar amount to point value
                //1 cent = 1 point i.e. $9.50 donation = 950 points
                temp = low.get("donation_id").asText();
                double amount = low.get("amount").asDouble();
                DecimalFormat f = new DecimalFormat("##.00");

                String outAmount = (String) f.format(amount);
                int am = low.get("amount").asInt();
                outAmount = outAmount.replaceAll("[^0-9]", "");
                System.out.println("DONATIONID:" + temp);
                System.out.println("Donation amount: $" + am + " formatted for marathon:" + outAmount);
                String username = low.get("name").asText();
                System.out.println("From: " + username);
                String msg = low.get("message").asText();
                System.out.println("Message: " + msg);

                //check for enabled status of marathon and spoopathon
                String spoopEnabled = this.store.getConfiguration().spoopathonStatus;
                String marathonEnabled = this.store.getConfiguration().marathonStatus;
                if (marathonEnabled.equals("on")) {
                    MarathonHandler mh = new MarathonHandler(store, outstream);
                    //sends w/out decimal ie $10.00 = 10
                    mh.addDollars(am);
                    sendEvent(username, msg, am);
                }
                if (spoopEnabled.equals("on")) {
                    if (!msg.contains("#")) {
                        messenger.sendMessage(".w Raxa " + username + " donated $" + am + " for spoopathon with no #, message: \"" + msg + "\"");
                    } else {
                        addSubSQLPoints(msg, am);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addSubSQLPoints(String msg, int points) {
        //parse for #game, send with points to sqlHandler.java
        sqlHandler sql = new sqlHandler(store, outstream);
        sql.gameSearch(msg, points);
    }

    private void sendEvent(String user, String msg, int amount) {
        String eventMsg = "Donation Event: " + user + " donated $" + amount + " with message: " + msg;
        DashboardController dc = new DashboardController();
        dc.eventObLAdd(eventMsg);
    }
}
