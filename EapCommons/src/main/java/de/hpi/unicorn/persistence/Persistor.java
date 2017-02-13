/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * This class is the controller for the database access and to get a connection
 * to the EntityManager.
 */
public class Persistor {

	private static String PERSISTENCE_UNIT_NAME = PersistenceUnit.DEVELOPMENT.getName();
	private static EntityManagerFactory entityManagerFactory = Persistence
			.createEntityManagerFactory(Persistor.PERSISTENCE_UNIT_NAME);

	public static EntityManagerFactory getEntityManagerFactory() {
		return Persistor.entityManagerFactory;
	}

	public static void setEntityManagerFactory(final EntityManagerFactory entityManagerFactory) {
		Persistor.entityManagerFactory = entityManagerFactory;
	}

	public static EntityManager getEntityManager() {
		return Persistor.entityManagerFactory.createEntityManager();
	}

	public static String getPERSISTENCE_UNIT_NAME() {
		return Persistor.PERSISTENCE_UNIT_NAME;
	}

	public static void useDevelopmentEnviroment() {
		Persistor.setPERSISTENCE_UNIT_NAME(PersistenceUnit.DEVELOPMENT.getName());
	}

	public static void useTestEnviroment() {
		Persistor.setPERSISTENCE_UNIT_NAME(PersistenceUnit.TEST.getName());
	}

	public static void setPERSISTENCE_UNIT_NAME(final String persistenceUnitName) {
		Persistor.entityManagerFactory.close();
		Persistor.entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
	}
}