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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
 * This class is the super class for notification rules. A notification rule
 * saves a situation for that a user wants to be notified.
 */
@Entity
@Table(name = "NotificationRule")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "Disc")
public abstract class NotificationRule extends Persistable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected int ID;

	@Temporal(TemporalType.TIMESTAMP)
	protected Date timestamp = null;

	@ManyToOne
	protected EapUser user;

	@Column(name = "priority")
	@Enumerated(EnumType.STRING)
	protected NotificationMethod priority;

	// Getter and Setter

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
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

	public abstract Persistable getTriggeringEntity();

	// JPA-Methods

	/**
	 * Deletes all notification rules from the database.
	 */
	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM NotificationRule");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * Finds all notification rules from the database.
	 * 
	 * @return all notification rules
	 */
	public static List<NotificationRule> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t FROM NotificationRule t");
		return q.getResultList();
	}

	/**
	 * Find all notification rules for a user from the database.
	 * 
	 * @param user
	 * @return all notification rules for a user
	 */
	public static List<NotificationRule> findByUser(final EapUser user) {
		final Query q = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM NotificationRule WHERE USER_ID = '" + user.getID() + "'", NotificationRule.class);
		return q.getResultList();
	}

	/**
	 * Deletes this notification rule from the database. All connected
	 * notifications are deleted as well.
	 * 
	 * @return
	 */
	@Override
	public Persistable remove() {
		final List<Notification> notifications = Notification.findForNotificationRule(this);
		for (final Notification notification : notifications) {
			notification.remove();
		}
		return super.remove();
	}

}