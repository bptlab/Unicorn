/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import org.apache.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the controller for the database access and to get a connection
 * to the EntityManager.
 */
public class Persistor {

	private static EntityManagerFactory entityManagerFactory = getEntityManagerFactory();

	private Persistor() {
		throw new IllegalAccessError("Utility class");
	}

	private static EntityManagerFactory getEntityManagerFactory() {
		Map<String, String> persistenceMap = new HashMap<>();

		if (System.getProperty("db.host") != null && System.getProperty("db.port") != null) {
			persistenceMap = getUpdatedPersistenceMap();
		}
		return Persistence.createEntityManagerFactory(PersistenceUnit.DEVELOPMENT.getName(), persistenceMap);
	}

	/**
	 * Updates the persistence map, if the db.host and db.port are set via parameter
	 * @return - an updated persistence map
	 */
	private static Map<String, String> getUpdatedPersistenceMap() {
		Map<String, String> persistenceMap = new HashMap<>();
		String databaseBaseUrl = System.getProperty("db.host") + ":" + System.getProperty("db.port");
		String url = "jdbc:mariadb://" + databaseBaseUrl + "/eap_development?createDatabaseIfNotExist=true";

		persistenceMap.put("javax.persistence.jdbc.url", url);
		return persistenceMap;
	}

	public static void useTestEnvironment() {
		Persistor.entityManagerFactory.close();
		Persistor.entityManagerFactory = Persistence.createEntityManagerFactory(PersistenceUnit.TEST.getName());
	}

	public static EntityManager getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}
}