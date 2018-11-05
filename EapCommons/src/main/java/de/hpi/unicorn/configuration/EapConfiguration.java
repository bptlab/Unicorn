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
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Enables access to unicorn.properties file
 *
 * @author tw
 */
public class EapConfiguration {

	public static boolean persistEvents = true;
	public static boolean registerPredefinedEventTypes = false;
	public static boolean registerTransformationRules = false;
	public static boolean supportingOnDemandQueries = false;
	public static int defaultInterval = 900; // in seconds
	public static String nokiaHereAppID = "";
	public static String nokiaHereAppCode = "";
	public static String tflAppId = "";
	public static String tflAppCode = "";
	public static String boschIotUsername = "";
	public static String boschIotPassword = "";
	public static String boschIotApiKey = "";
	public static String goodsTagUri = "";
	public static String goodstagUsername = "";
	public static String goodsTagPassword = "";
	public static String goodsTagDeviceIds = "";
	public static String eMailUser = "";
	public static String eMailPassword = "";
	public static String triplestoreLocation = "";
	public static boolean triplestoreClean = false;
	public static MultipleEventValueHandling[] eventValueHandling = {MultipleEventValueHandling.CROSS};

	/**
	 * Initializes configuration options. Must be called once before properties
	 * are accessed.
	 */
	public static void initialize() {
		Properties props = getProperties();
		if (!Boolean.valueOf(props.getProperty("de.hpi.unicorn.eventhandling.persistEvents", "true"))) {
			persistEvents = false;
		}
		if (Boolean.valueOf(props.getProperty("de.hpi.unicorn.esper.StreamProcessingAdapter.registerPredefinedEventTypes", "true"))) {
			registerPredefinedEventTypes = true;
		}
		if (Boolean.valueOf(props.getProperty("de.hpi.unicorn.esper.StreamProcessingAdapter.registerTransformationRules", "true"))) {
			registerTransformationRules = true;
		}
		if (Boolean.valueOf(props.getProperty("de.hpi.unicorn.esper.supportingOnDemandQueries", "false"))) {
			supportingOnDemandQueries = true;
		}
		defaultInterval = Integer.valueOf(props.getProperty("de.hpi.unicorn.adapter.defaultInterval", "900"));
		nokiaHereAppID = props.getProperty("de.hpi.unicorn.adapter.nokiaHereAppID", "");
		nokiaHereAppCode = props.getProperty("de.hpi.unicorn.adapter.nokiaHereAppCode", "");
		tflAppId = props.getProperty("de.hpi.unicorn.adapter.tflAppId", "");
		tflAppCode = props.getProperty("de.hpi.unicorn.adapter.tflAppCode", "");
		boschIotUsername = props.getProperty("de.hpi.unicorn.adapter.boschIotUsername", "");
		boschIotPassword = props.getProperty("de.hpi.unicorn.adapter.boschIotPassword", "");
		boschIotApiKey = props.getProperty("de.hpi.unicorn.adapter.boschIotApiKey", "");
		goodsTagUri = props.getProperty("de.hpi.unicorn.adapter.goodsTagUri", "");
		goodstagUsername = props.getProperty("de.hpi.unicorn.adapter.goodsTagUsername", "");
		goodsTagPassword = props.getProperty("de.hpi.unicorn.adapter.goodsTagPassword", "");
		goodsTagDeviceIds = props.getProperty("de.hpi.unicorn.adapter.goodsTagDeviceIds", "");
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
	 * Returns properties loaded from unicorn.properties file.
	 *
	 * @return Properties map with properties
	 */
	public static Properties getProperties() {
		Reader reader;
		Properties props;
		System.err.println("Working directory is " + System.getProperty("user.dir"));
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
				System.err.println("Please store it in parent folder of <working directory> (..) or in folder <working directory>/conf");
				return new Properties();
			}
		}
	}
}