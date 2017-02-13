/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.user;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.notification.NotificationRule;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.utils.HashUtil;

/**
 * This class represents users of the system. The users can be saved in and
 * restored from the database.
 * 
 * @author micha
 */
@Entity
@Table(name = "EapUser")
public class EapUser extends Persistable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ID;

	@Column(name = "NAME")
	private String name;

	@Column(name = "PASSWORD")
	private String passwordHash;

	@Column(name = "MAIL")
	private String mail;

	/**
	 * Default Constructor for JPA
	 */
	public EapUser() {

	}

	/**
	 * Constructor to create a new user with a name, a password and a mail
	 * address.
	 * 
	 * @param name
	 * @param password
	 * @param mail
	 */
	public EapUser(final String name, final String password, final String mail) {
		this.name = name;
		this.passwordHash = HashUtil.generateHash(password);
		this.mail = mail;
	}

	@Override
	public String toString() {
		return this.name + "(" + this.mail + ")";
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Returns the hashed password for the current user.
	 * 
	 * @return
	 */
	public String getPasswordHash() {
		return this.passwordHash;
	}

	/**
	 * Returns the mail adress for the current user.
	 * 
	 * @return
	 */
	public String getMail() {
		return this.mail;
	}

	/**
	 * Sets a new mail adress for the current user.
	 * 
	 * @param mail
	 */
	public void setMail(final String mail) {
		this.mail = mail;
	}

	/**
	 * Finds all users in the database.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<EapUser> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t FROM EapUser t");
		return q.getResultList();
	}

	/**
	 * Returns all users with the given attribute and the corresponding value
	 * from the database.
	 * 
	 * @param columnName
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<EapUser> findByAttribute(final String columnName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM EapUser WHERE " + columnName + " = '" + value + "'", EapUser.class);
		return query.getResultList();
	}

	/**
	 * Returns the user with the given ID from the database, if any.
	 * 
	 * @param ID
	 * @return
	 */
	public static EapUser findByID(final int ID) {
		final List<EapUser> list = EapUser.findByAttribute("ID", new Integer(ID).toString());
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Returns all users from the database with the given name.
	 * 
	 * @param name
	 * @return
	 */
	public static List<EapUser> findByName(final String name) {
		return EapUser.findByAttribute("NAME", name);
	}

	/**
	 * Returns all users from the database with the given mail.
	 * 
	 * @param mail
	 * @return
	 */
	public static List<EapUser> findByMail(final String mail) {
		return EapUser.findByAttribute("MAIL", mail);
	}

	/**
	 * Deletes this user from the database.
	 * 
	 * @return
	 */
	@Override
	public Persistable remove() {
		// remove notificationrules
		for (final NotificationRule notificationRule : NotificationRule.findByUser(this)) {
			notificationRule.remove();
		}
		return super.remove();
	}

	/**
	 * Deletes all users from the database.
	 */
	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM EapUser");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}