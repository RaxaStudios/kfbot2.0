package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.ConfigParameters;
import com.twitchbotx.bot.Datastore;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.logging.Logger;

public final class FilterHandler {

    private static final Logger LOGGER = Logger.getLogger(FilterHandler.class.getSimpleName());

    private final Datastore store;

    public FilterHandler(final Datastore store) {
        this.store = store;
    }

    
    //TODO create system to allow for indexing, timeout changes, phrase parsing, reason parsing
    
    /**
     * This method returns a list of known filters back to the user.
     *
     * @param msg
     * The original message from the user.
     *
     * @param user
     * The username to reply to
     *
     * @return
     * A message for what to reply to the user
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
            return ".w " + user + " Current filters: [" + sb.toString() + "]";
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }
        return ".w " + user + " No filters found.";
    }

    /**
     * This method attempts to add a filter for the user and returns a message back to the user on the add filter status.
     *
     * @param msg
     * The original message from the user.
     *
     * @param user
     * The username to reply to
     *
     * @return
     * A message for what to reply to the user
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
            if(added) {
                return ".w " + user + " Filter added.";
            }

        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }

        return ".w " + user + " Filter could not be added.";
    }

    /**
     * This method attempts to remove a filter for the user and returns a message back to the user on the remove filter status.
     *
     * @param msg
     * The original message from the user.
     *
     * @param user
     * The username to reply to
     *
     * @return
     * A message for what to reply to the user
     */
    public String deleteFilter(String msg, String user) {
        try {
            final String filterName = CommonUtility.getInputParameter("!filter-delete", msg, true);
            final boolean deleted = store.deleteFilter(filterName);

            if(deleted) {
                return ".w " + user + " Filter deleted.";
            }
            return ".w " + user + " Filter not found.";
        } catch (IllegalArgumentException e) {
            LOGGER.info(e.toString());
        }

        return ".w " + user + " Filter could not be deleted.";
    }
}
