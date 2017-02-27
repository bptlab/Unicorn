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

import org.log4j.Logger;

/**
 * This class is the controller for the database access and to get a connection
 * to the EntityManager.
 */
public class Persistor {

	private static Logger logger = Logger.getLogger(Persistor.class);

	private static EntityManagerFactory entityManagerFactory;

	public Persistor() {
		entityManagerFactory = getEntityManagerFactory(false);
	}

	public Persistor(boolean testMode) {
		entityManagerFactory = getEntityManagerFactory(testMode);
	}

	public static EntityManagerFactory getEntityManagerFactory(boolean testMode) {
		String databaseBaseUrl = System.getProperty("db.host") + ":" + System.getProperty("db.port");
		Map<String, String> persistenceMap = new HashMap<String, String>();

		logger.info(databaseBaseUrl);
		logger.info(testMode);

		if (databaseBaseUrl.length() > 1) {
			if (testMode) {
				database = "eap_testing";
			} else {
				database = "eap_development";
			}

			String url = "jdbc:mariadb://" + databaseBaseUrl + "/" + database + "?createDatabaseIfNotExist=true";
			persistenceMap.put("javax.persistence.jdbc.url", url);
		}
		return Persistance.createEntityManagerFactory("default", persistenceMap);
	}

	public static void setMode(boolean testMode) {
		entityManagerFactory.close();
		entityManagerFactory = getEntityManagerFactory(testMode);
	}

	public static void useDevelopmentEnviroment() {
		setMode(false);
	}

	public static void useTestEnviroment() {
		setMode(true);
	}

	public getEntityManager() {
		return entityManagerFactory.createEntityManager();
	}
}