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
			persistenceMap.putAll(getUpdatedPersistenceMapWithHost());
		}
		if (System.getProperty("db.user") != null && System.getProperty("db.passw") != null) {
			persistenceMap.putAll(getUpdatedPersistenceMapWithUser());
		}
		return Persistence.createEntityManagerFactory(PersistenceUnit.DEVELOPMENT.getName(), persistenceMap);
	}

	/**
	 * Updates the persistence map, if the db.host and db.port are set via parameter
	 * @return - an updated persistence map
	 */
	private static Map<String, String> getUpdatedPersistenceMapWithHost() {
		Map<String, String> persistenceMap = new HashMap<>();
		String databaseBaseUrl = System.getProperty("db.host") + ":" + System.getProperty("db.port");
		String databaseName = System.getProperty("db.dev.name", "eap_development");
		String url = "jdbc:mariadb://" + databaseBaseUrl + "/" + databaseName + "?createDatabaseIfNotExist=true";

		persistenceMap.put("javax.persistence.jdbc.url", url);
		return persistenceMap;
	}

	private static Map<String, String> getUpdatedPersistenceMapWithUser() {
		Map<String, String> persistenceMap = new HashMap<>();
		persistenceMap.put("javax.persistence.jdbc.user", System.getProperty("db.user"));
		persistenceMap.put("javax.persistence.jdbc.password", System.getProperty("db.passw"));
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
