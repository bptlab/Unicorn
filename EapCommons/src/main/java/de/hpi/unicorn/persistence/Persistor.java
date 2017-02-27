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

	private static EntityManagerFactory entityManagerFactory = getEntityManagerFactory(false);

	private Persistor() {
		throw new IllegalAccessError("Utility class");
	}

	private static EntityManagerFactory getEntityManagerFactory(boolean testMode) {
		String databaseBaseUrl = System.getProperty("db.host") + ":" + System.getProperty("db.port");
		Map<String, String> persistenceMap = new HashMap<>();
		String database;
		String url;

		if (databaseBaseUrl.length() > 1) {
			if (testMode) {
				database = "eap_testing";
			} else {
				database = "eap_development";
			}

			url = "jdbc:mariadb://" + databaseBaseUrl + "/" + database + "?createDatabaseIfNotExist=true";
			persistenceMap.put("javax.persistence.jdbc.url", url);
		}
		return Persistence.createEntityManagerFactory("default", persistenceMap);
	}

	public static void useTestEnvironment() {
		entityManagerFactory.close();
		entityManagerFactory = getEntityManagerFactory(true);
	}

	public static EntityManager getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}
}