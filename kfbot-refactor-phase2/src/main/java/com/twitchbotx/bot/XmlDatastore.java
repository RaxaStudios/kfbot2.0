package com.twitchbotx.bot;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class deals with all the interactions with the database.
 *
 * Primarily, it deals with fetching of information, and updating the
 * information.
 */
public final class XmlDatastore implements Datastore {

    // store off the elements
    private final ConfigParameters.Elements elements;

    /**
     * Constructor for the XML datastore
     *
     * @param parameters A list of fields in the XML file
     */
    public XmlDatastore(final ConfigParameters.Elements parameters) {
        this.elements = parameters;
    }

    @Override
    public ConfigParameters.Configuration getConfiguration() {
        final ConfigParameters.Configuration configuration = new ConfigParameters.Configuration();
        configuration.account
                = this.elements.configNode.getElementsByTagName("account").item(0).getTextContent();

        configuration.password
                = this.elements.configNode.getElementsByTagName("password").item(0).getTextContent();

        configuration.clientID
                = this.elements.configNode.getElementsByTagName("clientID").item(0).getTextContent();

        configuration.joinedChannel
                = this.elements.configNode.getElementsByTagName("joinedChannel").item(0).getTextContent();

        configuration.host
                = this.elements.configNode.getElementsByTagName("host").item(0).getTextContent();

        configuration.port
                = Integer.parseInt(this.elements.configNode.getElementsByTagName("port").item(0).getTextContent());

        configuration.pubSub = this.elements.configNode.getElementsByTagName("pubSub").item(0).getTextContent();

        configuration.streamerStatus = this.elements.configNode.getElementsByTagName("streamerStatus").item(0).getTextContent();

        configuration.followage = this.elements.configNode.getElementsByTagName("followage").item(0).getTextContent();

        configuration.youtubeApi = this.elements.configNode.getElementsByTagName("youtubeAPI").item(0).getTextContent();

        configuration.youtubeTitle = this.elements.configNode.getElementsByTagName("youtubeTitle").item(0).getTextContent();

        configuration.onlineCheckTimer = Integer.parseInt(this.elements.configNode.getElementsByTagName("onlineCheckTimer").item(0).getTextContent());

        configuration.recentMessageCacheSize = Integer.parseInt(this.elements.configNode.getElementsByTagName("recentMessageCacheSize").item(0).getTextContent());

        configuration.numCounters = Integer.parseInt(this.elements.configNode.getElementsByTagName("numberOfCounters").item(0).getTextContent());

        configuration.pyramidResponse = this.elements.configNode.getElementsByTagName("pyramidResponse").item(0).getTextContent();

        configuration.sqlURL = this.elements.configNode.getElementsByTagName("sqlURL").item(0).getTextContent();

        configuration.sqlUser = this.elements.configNode.getElementsByTagName("sqlUser").item(0).getTextContent();

        configuration.sqlPass = this.elements.configNode.getElementsByTagName("sqlPass").item(0).getTextContent();

        configuration.channelID = this.elements.configNode.getElementsByTagName("channelID").item(0).getTextContent();

        configuration.botID = this.elements.configNode.getElementsByTagName("botID").item(0).getTextContent();

        configuration.lottoAuth = this.elements.configNode.getElementsByTagName("lottoAuth").item(0).getTextContent();

        configuration.lottoName = this.elements.configNode.getElementsByTagName("lottoName").item(0).getTextContent();

        configuration.pubSubAuthToken = this.elements.configNode.getElementsByTagName("pubSubAuthToken").item(0).getTextContent();

        configuration.botWhisperToken = this.elements.configNode.getElementsByTagName("botWhisperToken").item(0).getTextContent();

        configuration.streamlabsToken = this.elements.configNode.getElementsByTagName("streamlabsToken").item(0).getTextContent();

        return configuration;
    }

    @Override
    public List<ConfigParameters.Command> getCommands() {
        final List<ConfigParameters.Command> commands = new ArrayList<>();

        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element e = (Element) n;

            final ConfigParameters.Command command = new ConfigParameters.Command();
            command.auth = e.getAttribute("auth");
            command.name = e.getAttribute("name");
            command.disabled = Boolean.parseBoolean(e.getAttribute("disabled"));
            command.text = e.getTextContent();
            command.cdUntil = e.getAttribute("cdUntil");
            command.cooldownInSec = e.getAttribute("cooldownInSec");
            command.sound = e.getAttribute("sound");
            commands.add(command);
        }
        return commands;
    }

    @Override
    public List<ConfigParameters.Filter> getFilters() {
        final List<ConfigParameters.Filter> filters = new ArrayList<>();
        for (int i = 0; i < this.elements.filterNodes.getLength(); i++) {
            Node n = this.elements.filterNodes.item(i);
            Element e = (Element) n;

            final ConfigParameters.Filter filter = new ConfigParameters.Filter();
            filter.name = e.getAttribute("name");
            filter.reason = e.getAttribute("reason");
            filter.disabled = Boolean.parseBoolean(e.getAttribute("disabled"));
            filters.add(filter);
        }

        return filters;
    }

    @Override
    public List<ConfigParameters.Counter> getCounters() {
        List<ConfigParameters.Counter> counters = new ArrayList<>();
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element e = (Element) n;

            final ConfigParameters.Counter counter = new ConfigParameters.Counter();
            counter.name = e.getAttribute("name");
            counter.count = Integer.parseInt(e.getAttribute("count"));
            counters.add(counter);
        }

        return counters;
    }

    private final static List<String> lotto = new ArrayList();
    public static boolean qOpen = false;

    @Override
    public void openLottery(String auth, String keyword) {
        qOpen = true;
        modifyConfiguration("lottoAuth", auth);
        modifyConfiguration("lottoName", keyword);
        try {
            lotto.clear();
        } catch (NullPointerException n) {
            System.out.println(n.getMessage());
        }
    }

    @Override
    public boolean getQOpen() {
        return qOpen;
    }

    @Override
    public void openQueue() {
        qOpen = true;
    }

    @Override
    public void closeQueue() {
        qOpen = false;
                com.twitchbotx.bot.CommandParser.lottoOn = false;
    }

    @Override
    public String getLottoName() {
        Node n = this.elements.configNode.getElementsByTagName("lottoName").item(0);
        Element el = (Element) n;
        return el.getTextContent();
    }

    @Override
    public void setupLotto(String auth, String keyword) {
        qOpen = true;
        com.twitchbotx.bot.CommandParser.lottoOn = true;
        try {
            lotto.clear();
        } catch (NullPointerException n) {
            System.out.println(n.getMessage());
        }
        final Node authN = this.elements.configNode.getElementsByTagName("lottoAuth").item(0);
        final Element authE = (Element) authN;
        authE.setTextContent(auth);
        final Node key = this.elements.configNode.getElementsByTagName("lottoName").item(0);
        final Element keyE = (Element) key;
        keyE.setTextContent(keyword);
        commit();
    }

    @Override
    public List<String> lotteryList() {
        //get method for list
        return lotto;
    }

    @Override
    public boolean addLotteryList(String user) {
        //add user to list

        if (!lotto.contains(user)) {
            lotto.add(user);
            return true;
        } else {
            System.out.println("duplicate entry");
            return false;
        }

    }

    @Override
    public String drawLotteryList() {
        //remove name from list

        Collections.shuffle(lotto);
        String winner = lotto.get(0);
        lotto.remove(0);
        return winner;
    }

    @Override
    public void clearLotteryList() {
        //clear list
        lotto.clear();
    }

    @Override
    public void modifyConfiguration(final String node, final String value) {
        final Node n = this.elements.configNode.getElementsByTagName(node).item(0);
        final Element el = (Element) n;
        el.setTextContent(value);
        commit();
    }

    @Override
    public boolean addCommand(final String command, final String text) {
        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            final Node n = this.elements.commandNodes.item(i);
            final Element e = (Element) n;
            if (command.equals(e.getAttribute("name"))) {
                return false;
            }
        }

        Element newNode = this.elements.doc.createElement("command");
        newNode.appendChild(this.elements.doc.createTextNode(text));
        newNode.setAttribute("name", command.toLowerCase());
        newNode.setAttribute("auth", "");
        newNode.setAttribute("repeating", "false");
        newNode.setAttribute("initialDelay", "0");
        newNode.setAttribute("interval", "0");
        newNode.setAttribute("cooldown", "0");
        newNode.setAttribute("cdUntil", "");
        newNode.setAttribute("sound", "");
        newNode.setAttribute("disabled", "false");
        this.elements.commands.appendChild(newNode);
        commit();

        return true;
    }

    @Override
    public boolean editCommand(final String command, final String text) {
        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            final Node n = this.elements.commandNodes.item(i);
            final Element e = (Element) n;
            if (command.equals(e.getAttribute("name"))) {
                e.setTextContent(text);
                commit();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean deleteCommand(final String command) {
        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            final Node n = this.elements.commandNodes.item(i);
            final Element e = (Element) n;
            if (command.equals(e.getAttribute("name"))) {
                this.elements.commands.removeChild(n);
                commit();
                return true;
            }
        }

        return false;
    }

    @Override
    public String listCommand() {
        return "";
    }

    @Override
    public boolean updateCounter(final String name, final int delta) {
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            final Node n = this.elements.counterNodes.item(i);
            final Element e = (Element) n;
            if (name.equals(e.getAttribute("name"))) {
                int value = Integer.parseInt(e.getTextContent()) + delta;
                e.setTextContent(Integer.toString(value));
                commit();

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean setCounter(final String name, final int value) {
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            final Node n = this.elements.counterNodes.item(i);
            final Element e = (Element) n;
            if (name.equals(e.getAttribute("name"))) {
                e.setTextContent(Integer.toString(value));
                commit();

                return true;
            }
        }

        final Element counterNode = this.elements.doc.createElement("counter");
        counterNode.appendChild(this.elements.doc.createTextNode(Integer.toString(value)));
        counterNode.setAttribute("name", name);
        commit();

        return true;
    }

    @Override
    public boolean addCounter(final String name) {
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            final Node n = this.elements.counterNodes.item(i);
            final Element e = (Element) n;
            if (name.equals(e.getAttribute("name"))) {
                return false;
            }
        }

        final Element newNode = this.elements.doc.createElement("counter");
        newNode.appendChild(this.elements.doc.createTextNode("0"));
        newNode.setAttribute("name", name);

        this.elements.counters.appendChild(newNode);
        commit();

        return true;
    }

    @Override
    public boolean deleteCounter(final String counterName) {
        for (int i = 0; i < this.elements.counterNodes.getLength(); i++) {
            final Node n = this.elements.counterNodes.item(i);
            final Element e = (Element) n;
            if (counterName.equals(e.getAttribute("name"))) {
                this.elements.filters.removeChild(n);
                commit();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean addFilter(final ConfigParameters.Filter filter) {
        //TODO index filters and/or separate phrases and filters
        for (int i = 0; i < this.elements.filterNodes.getLength(); i++) {
            final Node n = this.elements.filterNodes.item(i);
            final Element e = (Element) n;
            if (filter.name.equals(e.getAttribute("name"))) {
                return false;
            }
        }

        Element newNode = this.elements.doc.createElement("filter");
        newNode.setAttribute("name", filter.name);
        newNode.setAttribute("reason", filter.reason);
        if (filter.disabled) {
            newNode.setAttribute("disable", "true");
        } else {
            newNode.setAttribute("disable", "false");
        }

        this.elements.filters.appendChild(newNode);
        commit();

        return true;
    }

    @Override
    public boolean deleteFilter(final String filterName) {
        for (int i = 0; i < this.elements.filterNodes.getLength(); i++) {
            final Node n = this.elements.filterNodes.item(i);
            final Element e = (Element) n;
            if (filterName.contentEquals(e.getAttribute("name"))) {
                this.elements.filters.removeChild(n);
                commit();
                return true;
            }
        }

        return false;
    }

    @Override
    public void updateCooldownTimer(final String command, long cooldownUntil) {
        System.out.println(command + " " + cooldownUntil);
        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element el = (Element) n;
            if (command.equals(el.getAttribute("name"))) {
                el.setAttribute("cdUntil", Long.toString(cooldownUntil));
                //TODO need commit()?                
                //commit();
            }
        }
    }

    @Override
    public void commit() {
        try {
            File configFile = new File("kfbot.xml");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(this.elements.doc);
            StreamResult result = new StreamResult(configFile);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean setUserCommandAttribute(final String command,
            final String attribute,
            final String value,
            final boolean allowReservedCmds) {
        if (!allowReservedCmds && Commands.getInstance().isReservedCommand(command)) {
            return false;
        }

        for (int i = 0; i < this.elements.commandNodes.getLength(); i++) {
            Node n = this.elements.commandNodes.item(i);
            Element el = (Element) n;
            if (command.contentEquals(el.getAttribute("name"))) {
                el.setAttribute(attribute, value);
                commit();
                return true;
            }
        }

        return false;
    }
}
