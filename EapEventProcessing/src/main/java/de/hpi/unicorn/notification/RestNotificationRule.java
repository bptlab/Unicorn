package de.hpi.unicorn.notification;

import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.query.QueryWrapper;
import de.hpi.unicorn.user.EapUser;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONObject;


/**
 * The RestNotificationRule class is responsible for handling automatic subscriptions to
 * Unicorn. These always have to include a callback path, where the notification has to be
 * send to as a POST request.
 */
@Entity
@DiscriminatorValue("R")
public class RestNotificationRule extends NotificationRuleForQuery {

	// Needed for REST Notifications
	private String notificationPath;

	/**
	 * @param query            Query which triggers this notification rule
	 * @param notificationPath path to send request to.
	 */
	public RestNotificationRule(final QueryWrapper query, String notificationPath) {
		this.priority = NotificationMethod.REST;
		this.timestamp = new Date();
		this.uuid = UUID.randomUUID().toString();
		this.query = query;
		this.notificationPath = notificationPath;
		this.user = null;
	}

	/**
	 * Default constructor for JPA
	 */
	public RestNotificationRule() {
		this.priority = NotificationMethod.REST;
		this.timestamp = new Date();
		this.uuid = UUID.randomUUID().toString();
		this.query = new QueryWrapper();
		this.notificationPath = "";
		this.user = null;
	}

	public static RestNotificationRule findByUUID(final String uuid) {
		final Query q = Persistor.getEntityManager().createNativeQuery("SELECT * FROM NotificationRule WHERE UUID = '" + uuid + "'", RestNotificationRule.class);

		if (q.getResultList().isEmpty()) {
			return null;
		} else {
			return (RestNotificationRule) q.getResultList().get(0);
		}
	}

	@Override
	public EapUser getUser() {
		EapUser user = new EapUser();
		user.setID(-1);
		user.setMail("");
		user.setName("");
		return user;
	}

	@Override
	public Persistable getTriggeringEntity() {
		return this.query;
	}

	@Override
	/*
	 * No longer persisting RestNotificationForQuery 
	 */
	public boolean trigger(final Map<Object, Serializable> eventObject) {
		try {
			final JSONObject event = NotificationRuleUtils.toJSON(eventObject);
			//final RestNotificationForQuery notification = new RestNotificationForQuery(event.toString(), this);
			// no longer storing the notifications, as they were causing errors with JPA
			// probably because the entity was configured incorrectly. However, as no one
			// ever looks them up again, we should stop persisting these anyway.
			//notification.save();

			Client client = ClientBuilder.newClient();
			WebTarget target = client.target(this.notificationPath);

			Response response = target.request().post(javax.ws.rs.client.Entity.json(event.toString()));
			return response.getStatus() == 200;
		} catch (UnsupportedJsonTransformation e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String toString() {
		String representation = "Notification for " + this.query;
		representation += " for endpoint " + this.notificationPath;
		return representation;
	}
}
