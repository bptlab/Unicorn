package de.hpi.unicorn.notification;

import java.util.Date;

/**
 *
 */
public class RestNotificationForQuery extends NotificationForQuery {

    public RestNotificationForQuery(final String log, final RestNotificationRule rule) {
        this.timestamp = new Date();
        this.user = null;
        this.log = log;
        this.notificationRule = rule;
    }

    @Override
    public String toString() {
        final RestNotificationRule notificationQueryType = (RestNotificationRule) this.notificationRule;
        return notificationQueryType.getQuery() + " was triggered on " + this.timestamp + " : " + this.log;
    }
}
