/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.notification;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.user.EapUser;

/**
 * This class is the super class for notifications. A notification is created
 * from a @see NotificationRule informing a user about a situation.
 */
@Entity
@Table(name = "Notification")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "Disc")
public abstract class Notification extends Persistable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected int ID;

	@Column(name = "seen", columnDefinition = "INT(1)")
	protected boolean seen = false;

	@Temporal(TemporalType.TIMESTAMP)
	protected Date timestamp = null;

	@ManyToOne
	protected EapUser user;

	@ManyToOne
	protected NotificationRule notificationRule;

	public boolean isSeen() {
		return this.seen;
	}

	// Getter and Setter

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	public NotificationRule getNotificationRule() {
		return this.notificationRule;
	}

	public void setNotificationRule(final NotificationRule notificationRule) {
		this.notificationRule = notificationRule;
	}

	public void setSeen(final boolean seen) {
		this.seen = seen;
		this.merge();
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}

	public EapUser getUser() {
		return this.user;
	}

	public void setUser(final EapUser user) {
		this.user = user;
	}

	public void setSeen() {
		this.seen = true;
		this.merge();
	}

	public abstract String getTriggeringText();

	// JPA-Methods

	/**
	 * Finds all notifications in the database.
	 * 
	 * @return all notifications
	 */
	public static List<Notification> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t FROM Notification t");
		return q.getResultList();
	}

	/**
	 * Finds all notifications for a user
	 * 
	 * @param user
	 * @return all notifications for a user
	 */
	public static List<Notification> findForUser(final EapUser user) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM Notification WHERE USER_ID = '" + user.getID() + "'", Notification.class);
		return query.getResultList();
	}

	/**
	 * Finds unseen notifications for a user
	 * 
	 * @param user
	 * @return unseen notifications for a user
	 */
	public static List<Notification> findUnseenForUser(final EapUser user) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM Notification WHERE USER_ID = '" + user.getID() + "' AND seen = 0", Notification.class);
		return query.getResultList();
	}

	/**
	 * Finds all notifications belonging to a notification rule
	 * 
	 * @param notification
	 *            rule
	 * @return all notifications for a notification rule
	 */
	public static List<Notification> findForNotificationRule(final NotificationRule rule) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM Notification WHERE NOTIFICATIONRULE_ID = '" + rule.getID() + "'", Notification.class);
		return query.getResultList();
	}

	/**
	 * Deletes all notifications from the database.
	 */
	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM Notification");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

}
