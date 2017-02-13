/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @author baumgrass
 * 
 */
public class EnrichUtils {

	/**
	 * Returns the estimated time of arrival (ETA) of a vessel.
	 * 
	 * @return ETA date as string
	 * @throws NoSuchAlgorithmException
	 */
	public static String getVesselETA(String vesselID) throws NoSuchAlgorithmException {
		String eta = "";
		// TODO: use correct one!
		vesselID = "VESSEL_ID";

		// Create a trust manager that does not validate certificate chains
		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
			}

			@Override
			public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		} };

		// Install the all-trusting trust manager
		final SSLContext sslContext = SSLContext.getInstance("SSL");
		try {
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		} catch (final KeyManagementException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Create an ssl socket factory with our all-trusting manager
		final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

		// All set up, we can get a resource through https now:
		URLConnection urlCon = null;
		try {
			urlCon = new URL("https://www.rd.npcs.portbase.com/restlet-nextlogic/api/test/arrival/" + vesselID)
					.openConnection();
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Tell the url connection object to use our socket factory which
		// bypasses security checks
		((HttpsURLConnection) urlCon).setSSLSocketFactory(sslSocketFactory);
		try (InputStream is = urlCon.getInputStream(); JsonReader rdr = Json.createReader(is)) {
			final JsonObject obj = rdr.readObject();
			eta = obj.getString("eta");
			// SimpleDateFormat formatter = new
			// SimpleDateFormat("dd-MM-yyyy' 'HH:mm:ss");
			// Date date = (Date) formatter.parse(eta);

		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return eta;
	}

}
