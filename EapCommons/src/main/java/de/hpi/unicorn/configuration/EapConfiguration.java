/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.configuration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Enables access to unicorn.properties file
 * 
 * @author tw
 * 
 */
public class EapConfiguration {

	public static boolean persistEvents = true;
	public static boolean registerPredefinedEventTypes = false;
	public static boolean registerTransformationRules = false;
	public static boolean supportingOnDemandQueries = false;
	public static int defaultInterval = 900; // in seconds
	public static String nokiaHereAppID = "";
	public static String nokiaHereAppCode = "";
	public static String eMailUser = "";
	public static String eMailPassword = "";
	public static String triplestoreLocation = "";
	public static boolean triplestoreClean = false;
	public static MultipleEventValueHandling[] eventValueHandling = { MultipleEventValueHandling.CROSS };

	/**
	 * 
	 * Initializes configuration options. Must be called once before properties
	 * are accessed.
	 */
	public static void initialize() {
		Properties props = getProperties();
		if (!Boolean.valueOf(props.getProperty("de.hpi.unicorn.eventhandling.persistEvents", "true"))) {
			persistEvents = false;
		}
		if (Boolean.valueOf(props.getProperty(
				"de.hpi.unicorn.esper.StreamProcessingAdapter.registerPredefinedEventTypes", "true"))) {
			registerPredefinedEventTypes = true;
		}
		if (Boolean.valueOf(props.getProperty(
				"de.hpi.unicorn.esper.StreamProcessingAdapter.registerTransformationRules", "true"))) {
			registerTransformationRules = true;
		}
		if (Boolean.valueOf(props.getProperty("de.hpi.unicorn.esper.supportingOnDemandQueries", "false"))) {
			supportingOnDemandQueries = true;
		}
		defaultInterval = Integer.valueOf(props.getProperty("de.hpi.unicorn.adapter.defaultInterval", "900"));
		nokiaHereAppID = props.getProperty("de.hpi.unicorn.adapter.nokiaHereAppID", "");
		nokiaHereAppCode = props.getProperty("de.hpi.unicorn.adapter.nokiaHereAppCode", "");
		eMailUser = props.getProperty("de.hpi.unicorn.email.user", "YOUR GMAIL ADDRESS HERE");
		eMailPassword = props.getProperty("de.hpi.unicorn.email.password", "YOUR PASSWORD HERE");
		triplestoreLocation = props.getProperty("de.hpi.unicorn.semantic.Triplestore.location", "./Triplestore");
		if (Boolean.valueOf(props.getProperty("de.hpi.unicorn.semantic.Triplestore.clean", "true"))) {
			triplestoreClean = true;
		}

		String[] mevConfig = props.getProperty("de.hpi.unicorn.importer.xml.multipleEventValues", "DEFAULT").split(",");
		List<MultipleEventValueHandling> mevTemp = new ArrayList<MultipleEventValueHandling>();
		for (String setting : mevConfig) {
			try {
				mevTemp.add(MultipleEventValueHandling.valueOf(setting));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				mevTemp.add(MultipleEventValueHandling.CROSS);
			}
		}
		eventValueHandling = mevTemp.toArray(new MultipleEventValueHandling[0]);
	}

	/**
	 * 
	 * Returns properties loaded from unicorn.properties file.
	 * 
	 * @return Properties map with properties
	 */
	public static Properties getProperties() {
		Reader reader;
		Properties props;
		System.out.println("Working directory is " + System.getProperty("user.dir"));
		try {
			reader = new FileReader(".." + File.separator + "unicorn.properties");
			props = new Properties();
			props.load(reader);
			return props;
		} catch (IOException e) {
			try {
				reader = new FileReader("conf" + File.separator + "unicorn.properties");
				props = new Properties();
				props.load(reader);
				return props;
			} catch (IOException f) {
				System.err.println("ERROR: unicorn.properties not found");
				System.err
						.println("Please store it in parent folder of <working directory> (..) or in folder <working directory>/conf");
				return new Properties();
			}
		}
	}

	/**
	 * 
	 * The main method configures the persistence.xml of the database and the
	 * pom.xml of EapWebInterface.
	 * 
	 * @param args
	 * @throws IOException
	 *             void
	 */
	public static void main(String[] args) throws IOException {
		Properties props = getProperties();
		String content = null;
		PrintWriter writer = null;
		String folder = null;
		Scanner template = null;

		folder = props.getProperty("de.hpi.unicorn.projectFolder") + File.separator + "EapCommons" + File.separator
				+ "src" + File.separator + "main" + File.separator + "resources" + File.separator + "META-INF";
		template = new Scanner(new File(folder + File.separator + "persistence_template.xml"));
		content = template.useDelimiter("\\Z").next();
		template.close();
		content = content.replace("${db.dev.url}", props.getProperty("javax.persistence.jdbc.devUrl"))
				.replace("${db.test.url}", props.getProperty("javax.persistence.jdbc.testUrl"))
				.replaceAll(Pattern.quote("${db.user}"), props.getProperty("javax.persistence.jdbc.user"))
				.replaceAll(Pattern.quote("${db.passw}"), props.getProperty("javax.persistence.jdbc.password"));
				//	TODO: "eclipselink.ddl-generation" with drop-and-create-tables and create-tables;

		writer = new PrintWriter(folder + File.separator + "persistence.xml");
		writer.print(content);
		writer.close();

		folder = props.getProperty("de.hpi.unicorn.projectFolder") + File.separator + "EapWebInterface";
		template = new Scanner(new File(folder + File.separator + "pom-default.xml"));
		content = template.useDelimiter("\\Z").next();
		template.close();
		content = content.replace("${tomcat.server}", props.getProperty("org.apache.tomcat.maven.server"))
				.replace("${tomcat.url}", props.getProperty("org.apache.tomcat.maven.url"))
				.replace("${tomcat.path}", props.getProperty("org.apache.tomcat.maven.path"));

		writer = new PrintWriter(folder + File.separator + "pom.xml");
		writer.print(content);
		writer.close();
	}
}