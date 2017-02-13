/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import de.hpi.unicorn.event.EapEvent;

import java.io.Serializable;

import javax.persistence.EntityManager;

/**
 * This is the parent class for all class, which are saved via JPA.
 */
public abstract class Persistable implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Saves the current object to the database.
	 * 
	 * @return
	 */
	public Persistable save() {
		try {
			final EntityManager entityManager = Persistor.getEntityManager();
			entityManager.getTransaction().begin();
			entityManager.persist(this);
			entityManager.getTransaction().commit();
			return this;
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Merges the state of the current object to the database.
	 * 
	 * @return
	 */
	public Persistable merge() {
		try {
			final EntityManager entityManager = Persistor.getEntityManager();
			entityManager.getTransaction().begin();
			entityManager.merge(this);
			entityManager.getTransaction().commit();
			return this;
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Removes this Persistable from the database.
	 * 
	 * @return the removed Persistable
	 */
	public Persistable remove() {
		try {
			final EntityManager entityManager = Persistor.getEntityManager();
			entityManager.getTransaction().begin();
			final Persistable toBeRemoved = entityManager.merge(this);
			entityManager.remove(toBeRemoved);
			entityManager.getTransaction().commit();
			return this;
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Refreshes the state of the current object from the database. Overwrites
	 * changes made to the object, if any.
	 * 
	 * @return
	 */
	public Persistable refresh() {
		try {
			final EntityManager entityManager = Persistor.getEntityManager();
			entityManager.getTransaction().begin();
			entityManager.refresh(this);
			entityManager.getTransaction().commit();
			return this;
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the database ID for the object.
	 * 
	 * @return
	 */
	public abstract int getID();

}
