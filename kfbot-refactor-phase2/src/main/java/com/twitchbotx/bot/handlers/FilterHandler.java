package com.twitchbotx.bot.handlers;


import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Logger;

public final class FilterHandler {

    private static final Logger LOGGER = Logger.getLogger(FilterHandler.class.getSimpleName());

    private final Datastore store;

    private List<ModerationListener> listeners = new ArrayList<>();

    public FilterHandler(final Datastore store) {
        this.store = store;
    }

    public void addListener(ModerationListener toAdd) {
        listeners.add(toAdd);
    }

    public void flagUpdate() {
        for (ModerationListener ml : listeners) {
            ml.needUpdate();
        }
    }

    //TODO create system to allow for indexing, timeout changes, phrase parsing, reason parsing
    /**
     * This method returns a list of known filters back to the user.
     *
     * @param msg The original message from the user.
     *
     * @param user The username to reply to
     *
     * @return A message for what to reply to the user
     */
    public String getAllFilters(final String msg, final String user) {
        try {
            String[] filters = new String[store.getFilters().size()];
            for (int i = 0; i < store.getFilters().size(); i++) {
                final ConfigParameters.Filter filter = store.getFilters().get(i);
                filters[i] = filter.name;
            }
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < filters.length; j++) {
                if (j > 0) {
                    sb.append("], [");
                }
                sb.append(filters[j]);
            }
            return "";
            //".w " + user + " Current filters: [" + sb.toString() + "]";
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }
        return ".w " + user + " No filters found.";
    }

    /**
     * This method attempts to add a filter for the user and returns a message
     * back to the user on the add filter status.
     *
     * @param msg The original message from the user.
     *
     * @param user The username to reply to
     *
     * @return A message for what to reply to the user
     */
    public String addFilter(String msg, String user) {
        try {
            final String parameters = CommonUtility.getInputParameter("!filter-add", msg, true);
            final int separator = parameters.indexOf(" ");
            final String filterName = parameters.substring(0, separator);
            final String reason = parameters.substring(separator + 1);

            final ConfigParameters.Filter filter = new ConfigParameters.Filter();
            filter.name = filterName;
            filter.reason = reason;
            filter.enabled = true;
            filter.seconds = "600";
            boolean added = store.addFilter(filter);
            if (added) {
                flagUpdate();
                return "Filter added.";
            }

        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }

        return "Filter could not be added.";
    }

    /**
     * This method attempts to remove a filter for the user and returns a
     * message back to the user on the remove filter status.
     *
     * @param msg The original message from the user.
     *
     * @param user The username to reply to
     *
     * @return A message for what to reply to the user
     */
    public String deleteFilter(String msg, String user) {
        try {
            final String filterName = CommonUtility.getInputParameter("!filter-delete", msg, true);
            final boolean deleted = store.deleteFilter(filterName);

            if (deleted) {
                flagUpdate();
                return "Filter deleted.";
            }
            return "Filter not found.";
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }

        return "Filter could not be deleted.";
    }

    /**
     * regex section
     *
     * @param msg message incoming from admin level account takes in 'content'
     * for 'name' add to end of chat regex filter, del param for delete bool
     * @return the message to send back to chat
     */
    public String addRegex(String msg) {
        try {
            //expecting !regex-add [name] [content] [seconds] [reason] 
            String input = CommonUtility.getInputParameter("!regex-add", msg, true);
            int nameBegin = input.indexOf("[") + 1;
            int nameEnd = input.indexOf("]");
            String name = input.substring(nameBegin, nameEnd);
            int contentBegin = input.indexOf("[", nameEnd) + 1;
            int contentEnd = input.indexOf("]", contentBegin);
            String content = input.substring(contentBegin, contentEnd);
            int secondBegin = input.indexOf("[", contentEnd) + 1;
            int secondEnd = input.indexOf("]", secondBegin);
            String seconds = input.substring(secondBegin, secondEnd);
            int reasonBegin = input.indexOf("[", secondEnd) + 1;
            int reasonEnd = input.indexOf("]", reasonBegin);
            String reason = input.substring(reasonBegin, reasonEnd);
            final ConfigParameters.FilterRegex regex = new ConfigParameters.FilterRegex();
            regex.name = name;
            regex.content = content;
            regex.seconds = seconds;
            regex.reason = reason;
            regex.enabled = true;
            if (store.addRegex(regex)) {
                flagUpdate();
                return "Added " + name;
            }
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
            return "Failed to add, syntax: !regex-add [name] [content] [seconds] [reason]";
        }
        return "Duplicate name or error detected";
    }

    public String editRegex(String msg) {
        //expecting !regex-edit [name] [attribute] [new value]
        String name;
        String attribute;
        String newValue;
        int nameBegin = msg.indexOf("[") + 1;
        int nameEnd = msg.indexOf("]");
        name = msg.substring(nameBegin, nameEnd);
        int contentBegin = msg.indexOf("[", nameEnd) + 1;
        int contentEnd = msg.indexOf("]", contentBegin);
        attribute = msg.substring(contentBegin, contentEnd);
        int secondBegin = msg.indexOf("[", contentEnd) + 1;
        int secondEnd = msg.indexOf("]", secondBegin);
        newValue = msg.substring(secondBegin, secondEnd);

        final ConfigParameters.FilterRegex regex = new ConfigParameters.FilterRegex();
        regex.name = name;
        if (attribute.equalsIgnoreCase("content")) {
            regex.content = newValue;
        } else if (attribute.equalsIgnoreCase("seconds")) {
            regex.seconds = newValue;
        } else if (attribute.equalsIgnoreCase("reason")) {
            regex.reason = newValue;
        } else if (attribute.equalsIgnoreCase("enabled")) {
            regex.enabled = Boolean.parseBoolean(newValue);
        }

        if (store.updateRegex(regex, attribute)) {
            flagUpdate();
            return "Updated regex";
        }
        return "Failed to update";
    }

    public String delRegex(String msg) {
        //expecting !regex-del [name]
        String input = CommonUtility.getInputParameter("!regex-del", msg, true);
        input = input.substring(input.indexOf("[") + 1, input.indexOf("]"));
        if (store.deleteRegex(input)) {
            flagUpdate();
            return "Deleted regex";
        }
        return "Failed to delete";
    }

}
