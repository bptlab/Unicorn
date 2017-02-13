package de.hpi.unicorn;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class GETLogManager {

	private static final String LOG_MANAGER_URL = "https://www.rd.npcs.portbase.com/LogManager/";

	public static void main(String[] args) {
		// log("HPI", "UNICORN", "ERROR", "", "Test message.");

		getLog("UNICORN", false);
	}

	public static void logError(String type, String message) {
		log("HPI", "UNICORN", "ERROR", type, message);
	}

	/**
	 * Method that sends a 'log-form' to the logManager.
	 * 
	 * @param organisationId
	 * @param correlationId
	 * @param level
	 * @param type
	 * @param message
	 */
	public static void log(String organisationId, String correlationId, String level, String type, String message) {
		HttpClient httpClient = new DefaultHttpClient();

		// When a proxy is needed:
		// HttpHost proxy = new HttpHost("PROXY HOST NAME", proxyPort);
		// httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// proxy);

		try {
			HttpPost postRequest = new HttpPost(LOG_MANAGER_URL + "/log");
			postRequest.addHeader(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));

			List<NameValuePair> form = new ArrayList<NameValuePair>();

			form.add(new BasicNameValuePair("organisationId", organisationId));
			form.add(new BasicNameValuePair("correlationId", correlationId));
			form.add(new BasicNameValuePair("level", level));
			form.add(new BasicNameValuePair("type", type));
			form.add(new BasicNameValuePair("message", message));

			HttpEntity entity = new UrlEncodedFormEntity(form, HTTP.UTF_8);
			postRequest.setEntity(entity);

			HttpResponse response = httpClient.execute(postRequest);

			System.out.println(response.getEntity().getContent().toString());

			// Do what you want to do with response...
			// response.getEntity();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getLog(String id, boolean byOrg) {
		HttpClient httpClient = new DefaultHttpClient();

		// When a proxy is needed:
		// HttpHost proxy = new HttpHost("PROXY HOST NAME", proxyPort);
		// httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// proxy);

		String type = byOrg ? "organisation" : "correlation";

		try {
			HttpGet getRequest = new HttpGet(LOG_MANAGER_URL + "list/" + type + "/" + id);

			HttpResponse response = httpClient.execute(getRequest);

			System.out.println(IOUtils.toString(response.getEntity().getContent(), HTTP.UTF_8));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
