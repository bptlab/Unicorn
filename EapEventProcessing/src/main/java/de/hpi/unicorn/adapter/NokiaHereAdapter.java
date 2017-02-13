/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.adapter;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hpi.unicorn.configuration.EapConfiguration;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.importer.xml.XSDParser;

/**
 * This class is used for the retrieval and parsing of TrafficIncidentEvents
 * from the NokiaHereAPI along a specified Corridor.
 * 
 * @author Jan Selke
 * 
 */

public class NokiaHereAdapter extends EventAdapter {

	private final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ");
	private String query = "";
	private final List<EapEvent> eventsToSend = new ArrayList<EapEvent>();

	public NokiaHereAdapter(final String name) {
		super(name);
	}

	/**
	 * 
	 * 
	 */
	public NokiaHereAdapter(final String name, final int width, final double... coordinates) {
		this(name);
		this.setAreaForCorridor(width, coordinates);

	}

	/**
	 * This method assembles a query for the NokiaHereAPI, combining the correct
	 * area String with the API-Keys.
	 * 
	 * @param area
	 *            specifies the geographical boundaries of the area that is
	 *            relevant for the query of the TrafficEvents. Right now only
	 *            corridor is supported but the implementation also allows for
	 *            different kinds of area specification of the
	 *            NokiaHereTrafficAPI like bounding-box or proximity within the
	 *            area String.
	 * 
	 * @return returns a String that contains the combined HTTP-query with the
	 *         correct area that is used for the API-webcall.
	 */
	private String createQuery(final String area) {

		final String query = "http://traffic.api.here.com/traffic/6.1/incidents.json?app_id="
				+ EapConfiguration.nokiaHereAppID + "&app_code=" + EapConfiguration.nokiaHereAppCode + "&" + area
				+ "&c=en&lg=en&i18n=true&localtime=true";

		return query;
	}

	/**
	 * This method is used to parse the traffic events out of the JSON-Response
	 * from the NokiaHere-API call. The extracted Events are also mapped onto
	 * the Event-types RoadTraffic and NokiaHereTrafficIncident and saved into
	 * the eventsToSend variable for later usage.
	 * 
	 * @param response
	 *            Expects the JSON-response form the NokiaHere-API call which
	 *            can be obtained via the callWebService() method.
	 * @throws JSONException
	 *             auto generated
	 */
	// @Override
	public void parseResponse(final JSONObject response) throws JSONException {

		Date timestamp = new Date();
		try {
			if (response.has("TIMESTAMP")) {
				timestamp = this.df.parse(response.getString("TIMESTAMP"));
			}
		} catch (final ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (response.has("TRAFFIC_ITEMS")) {
			final JSONArray items = response.getJSONObject("TRAFFIC_ITEMS").getJSONArray("TRAFFIC_ITEM");
			for (int i = 0; i < items.length(); i++) {
				final JSONObject item = items.getJSONObject(i);
				final Map<String, Serializable> values = new HashMap<String, Serializable>();

				values.put("TIMESTAMP", timestamp);

				this.setStringValue(item, values, "MID", "mid");
				this.setLongValue(item, values, "TRAFFIC_ITEM_ID", "TRAFFIC_ITEM_ID");
				this.setLongValue(item, values, "ORIGINAL_TRAFFIC_ITEM_ID", "ORIGINAL_TRAFFIC_ITEM_ID");
				this.setStringValue(item, values, "TRAFFIC_ITEM_STATUS_SHORT_DESC", "TRAFFIC_ITEM_STATUS_SHORT_DESC");
				this.setStringValue(item, values, "TRAFFIC_ITEM_TYPE_DESC", "TRAFFIC_ITEM_TYPE_DESC");
				this.setDateValue(item, values, "START_TIME", "START_TIME");
				this.setDateValue(item, values, "END_TIME", "END_TIME");
				this.setDateValue(item, values, "ENTRY_TIME", "ENTRY_TIME");
				if (item.has("CRITICALITY")) {
					final JSONObject subItem = item.getJSONObject("CRITICALITY");
					this.setStringValue(subItem, values, "CRITICALITY.ID", "ID");
					this.setStringValue(subItem, values, "CRITICALITY.DESCRIPTION", "DESCRIPTION");
				}
				this.setStringValue(item, values, "Verified", "Verified");
				this.setLongValue(item, values, "PARENT_ITEM_ID", "PARENT_ITEM_ID");
				if (item.has("ABBREVIATION")) {
					final JSONObject subItem = item.getJSONObject("ABBREVIATION");
					this.setStringValue(subItem, values, "ABBREVIATION.SHORT_DESC", "SHORT_DESC");
					this.setStringValue(subItem, values, "ABBREVIATION.DESCRIPTION", "DESCRIPTION");
				}
				this.setStringValue(item, values, "COMMENTS", "COMMENTS");

				if (item.has("RDS-TMC_LOCATIONS")) {
					final JSONObject subItem1 = item.getJSONObject("RDS-TMC_LOCATIONS");
					if (subItem1.has("RDS-TMC")) {
						final JSONArray subItem2 = subItem1.getJSONArray("RDS-TMC");
						for (int j = 0; j < subItem2.length(); j++) {
							final JSONObject subItem3 = subItem2.getJSONObject(j);
							if (subItem3.has("ORIGIN")) {
								final JSONObject subItem4 = subItem3.getJSONObject("ORIGIN");
								this.setStringValue(subItem4, values,
										"RDS-TMC_LOCATIONS.RDS-TMC.ORIGIN.EBU_COUNTRY_CODE", "EBU_COUNTRY_CODE");
								this.setLongValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ORIGIN.TABLE_ID",
										"TABLE_ID");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ORIGIN.LOCATION_ID",
										"LOCATION_ID");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ORIGIN.LOCATION_DESC",
										"LOCATION_DESC");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ORIGIN.RDS_DIRECTION",
										"RDS_DIRECTION");
							}
							if (subItem3.has("TO")) {
								final JSONObject subItem4 = subItem3.getJSONObject("TO");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.TO.EBU_COUNTRY_CODE",
										"EBU_COUNTRY_CODE");
								this.setLongValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.TO.TABLE_ID", "TABLE_ID");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.TO.LOCATION_ID",
										"LOCATION_ID");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.TO.LOCATION_DESC",
										"LOCATION_DESC");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.TO.RDS_DIRECTION",
										"RDS_DIRECTION");
							}
							this.setStringValue(subItem3, values, "RDS-TMC_LOCATIONS.RDS-TMC.DIRECTION", "DIRECTION");
							if (subItem3.has("ALERTC")) {
								final JSONObject subItem4 = subItem3.getJSONObject("ALERTC");
								this.setLongValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.TRAFFIC_CODE",
										"TRAFFIC_CODE");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.ALERTC_Q",
										"ALERTC_Q");
								this.setStringValue(subItem4, values,
										"RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.ALERTC_Q_BINARY", "ALERTC_Q_BINARY");
								this.setLongValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.QUANTIFIERS",
										"QUANTIFIERS");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.DESCRIPTION",
										"DESCRIPTION");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.NATURE",
										"NATURE");
								this.setStringValue(subItem4, values,
										"RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.ALERTC_DURATION", "ALERTC_DURATION");
								this.setLongValue(subItem4, values,
										"RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.ALERTC_DIRECTION", "ALERTC_DIRECTION");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.URGENCY",
										"URGENCY");
								this.setLongValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.UPDATE_CLASS",
										"UPDATE_CLASS");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.PHRASE_CODE",
										"PHRASE_CODE");
								this.setStringValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.EXTENT",
										"EXTENT");
								this.setLongValue(subItem4, values, "RDS-TMC_LOCATIONS.RDS-TMC.ALERTC.DURATION",
										"DURATION");
							}
							this.setDoubleValue(subItem3, values, "RDS-TMC_LOCATIONS.RDS-TMC.PRIMARY_OFFSET",
									"PRIMARY_OFFSET");
							this.setDoubleValue(subItem3, values, "RDS-TMC_LOCATIONS.RDS-TMC.LENGTH", "LENGTH");

						}
					}
				}

				if (item.has("LOCATION")) {
					final JSONObject subItem1 = item.getJSONObject("LOCATION");
					if (subItem1.has("DEFINED")) {
						final JSONObject subItem2 = subItem1.getJSONObject("DEFINED");
						if (subItem2.has("ORIGIN")) {
							final JSONObject subItem3 = subItem2.getJSONObject("ORIGIN");
							if (subItem3.has("ROADWAY")) {
								final JSONObject subItem4 = subItem3.getJSONObject("ROADWAY");
								if (subItem4.has("DESCRIPTION")) {
									final JSONArray subItem5 = subItem4.getJSONArray("DESCRIPTION");
									this.setStringValue(subItem5.getJSONObject(0), values,
											"LOCATION.DEFINED.ORIGIN.ROADWAY.DESCRIPTION", "value");
								}
								this.setLongValue(subItem4, values, "LOCATION.DEFINED.ORIGIN.ROADWAY.ID", "ID");
							}
							if (subItem3.has("POINT")) {
								final JSONObject subItem4 = subItem3.getJSONObject("POINT");
								if (subItem4.has("DESCRIPTION")) {
									final JSONArray subItem5 = subItem4.getJSONArray("DESCRIPTION");
									this.setStringValue(subItem5.getJSONObject(0), values,
											"LOCATION.DEFINED.ORIGIN.POINT.DESCRIPTION", "value");
								}
								this.setLongValue(subItem4, values, "LOCATION.DEFINED.ORIGIN.POINT.ID", "ID");
							}
							if (subItem3.has("DIRECTION")) {
								final JSONObject subItem4 = subItem3.getJSONObject("DIRECTION");
								if (subItem4.has("DESCRIPTION")) {
									final JSONArray subItem5 = subItem4.getJSONArray("DESCRIPTION");
									this.setStringValue(subItem5.getJSONObject(0), values,
											"LOCATION.DEFINED.ORIGIN.DIRECTION.DESCRIPTION", "value");
								}
								this.setLongValue(subItem4, values, "LOCATION.DEFINED.ORIGIN.DIRECTION.ID", "ID");
							}
							if (subItem3.has("PROXIMITY")) {
								final JSONObject subItem4 = subItem3.getJSONObject("PROXIMITY");
								this.setStringValue(subItem4, values, "LOCATION.DEFINED.ORIGIN.PROXIMITY.ID", "ID");
								this.setStringValue(subItem4, values, "LOCATION.DEFINED.ORIGIN.PROXIMITY.DESCRIPTION",
										"DESCRIPTION");
							}
							if (subItem3.has("BETWEEN")) {
								final JSONObject subItem4 = subItem3.getJSONObject("BETWEEN");
								if (subItem4.has("ROADWAY")) {
									final JSONObject subItem5 = subItem4.getJSONObject("ROADWAY");
									if (subItem5.has("DESCRIPTION")) {
										final JSONArray subItem6 = subItem5.getJSONArray("DESCRIPTION");
										this.setStringValue(subItem6.getJSONObject(0), values,
												"LOCATION.DEFINED.ORIGIN.BETWEEN.ROADWAY.DESCRIPTION", "value");
									}
									this.setLongValue(subItem5, values, "LOCATION.DEFINED.ORIGIN.BETWEEN.ROADWAY.ID",
											"ID");
								}
								if (subItem4.has("POINT")) {
									final JSONObject subItem5 = subItem4.getJSONObject("POINT");
									if (subItem5.has("DESCRIPTION")) {
										final JSONArray subItem6 = subItem5.getJSONArray("DESCRIPTION");
										this.setStringValue(subItem6.getJSONObject(0), values,
												"LOCATION.DEFINED.ORIGIN.BETWEEN.POINT.DESCRIPTION", "value");
									}
									this.setLongValue(subItem5, values, "LOCATION.DEFINED.ORIGIN.BETWEEN.POINT.ID",
											"ID");
								}
							}

						}
						if (subItem2.has("TO")) {
							final JSONObject subItem3 = subItem2.getJSONObject("TO");
							if (subItem3.has("ROADWAY")) {
								final JSONObject subItem4 = subItem3.getJSONObject("ROADWAY");
								if (subItem4.has("DESCRIPTION")) {
									final JSONArray subItem5 = subItem4.getJSONArray("DESCRIPTION");
									this.setStringValue(subItem5.getJSONObject(0), values,
											"LOCATION.DEFINED.TO.ROADWAY.DESCRIPTION", "value");
								}
								this.setLongValue(subItem4, values, "LOCATION.DEFINED.TO.ROADWAY.ID", "ID");
							}
							if (subItem3.has("POINT")) {
								final JSONObject subItem4 = subItem3.getJSONObject("POINT");
								if (subItem4.has("DESCRIPTION")) {
									final JSONArray subItem5 = subItem4.getJSONArray("DESCRIPTION");
									this.setStringValue(subItem5.getJSONObject(0), values,
											"LOCATION.DEFINED.TO.POINT.DESCRIPTION", "value");
								}
								this.setLongValue(subItem4, values, "LOCATION.DEFINED.TO.POINT.ID", "ID");
							}
							if (subItem3.has("DIRECTION")) {
								final JSONObject subItem4 = subItem3.getJSONObject("DIRECTION");
								if (subItem4.has("DESCRIPTION")) {
									final JSONArray subItem5 = subItem4.getJSONArray("DESCRIPTION");
									this.setStringValue(subItem5.getJSONObject(0), values,
											"LOCATION.DEFINED.TO.DIRECTION.DESCRIPTION", "value");
								}
								this.setLongValue(subItem4, values, "LOCATION.DEFINED.TO.DIRECTION.ID", "ID");
							}
							if (subItem3.has("PROXIMITY")) {
								final JSONObject subItem4 = subItem3.getJSONObject("PROXIMITY");
								this.setStringValue(subItem4, values, "LOCATION.DEFINED.TO.PROXIMITY.ID", "ID");
								this.setStringValue(subItem4, values, "LOCATION.DEFINED.TO.PROXIMITY.DESCRIPTION",
										"DESCRIPTION");
							}
							if (subItem3.has("BETWEEN")) {
								final JSONObject subItem4 = subItem3.getJSONObject("BETWEEN");
								if (subItem4.has("ROADWAY")) {
									final JSONObject subItem5 = subItem4.getJSONObject("ROADWAY");
									if (subItem5.has("DESCRIPTION")) {
										final JSONArray subItem6 = subItem5.getJSONArray("DESCRIPTION");
										this.setStringValue(subItem6.getJSONObject(0), values,
												"LOCATION.DEFINED.TO.BETWEEN.ROADWAY.DESCRIPTION", "value");
									}
									this.setLongValue(subItem5, values, "LOCATION.DEFINED.TO.BETWEEN.ROADWAY.ID", "ID");
								}
								if (subItem4.has("POINT")) {
									final JSONObject subItem5 = subItem4.getJSONObject("POINT");
									if (subItem5.has("DESCRIPTION")) {
										final JSONArray subItem6 = subItem5.getJSONArray("DESCRIPTION");
										this.setStringValue(subItem6.getJSONObject(0), values,
												"LOCATION.DEFINED.TO.BETWEEN.POINT.DESCRIPTION", "value");
									}
									this.setLongValue(subItem5, values, "LOCATION.DEFINED.TO.BETWEEN.POINT.ID", "ID");
								}
							}

						}
					}
					if (subItem1.has("INTERSECTION")) {
						final JSONObject subItem2 = subItem1.getJSONObject("INTERSECTION");
						if (subItem2.has("ORIGIN")) {
							final JSONObject subItem3 = subItem2.getJSONObject("ORIGIN");
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.ORIGIN.ID", "ID");
							if (subItem3.has("STREET1")) {
								final JSONObject subItem4 = subItem3.getJSONObject("STREET1");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.ORIGIN.STREET1.ADDRESS1",
										"ADDRESS1");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.ORIGIN.STREET1.ADDRESS2",
										"ADDRESS2");
							}
							if (subItem3.has("STREET2")) {
								final JSONObject subItem4 = subItem3.getJSONObject("STREET2");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.ORIGIN.STREET2.ADDRESS1",
										"ADDRESS1");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.ORIGIN.STREET2.ADDRESS2",
										"ADDRESS2");
							}
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.ORIGIN.CITY", "CITY");
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.ORIGIN.COUNTY", "COUNTY");
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.ORIGIN.STATE", "STATE");
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.ORIGIN.ZIP", "ZIP");
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.ORIGIN.ALIAS", "ALIAS");
							if (subItem3.has("PROXIMITY")) {
								final JSONObject subItem4 = subItem3.getJSONObject("PROXIMITY");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.ORIGIN.PROXIMITY.ID", "ID");
								this.setStringValue(subItem4, values,
										"LOCATION.INTERSECTION.ORIGIN.PROXIMITY.DESCRIPTION", "DESCRIPTION");
							}
						}
						if (subItem2.has("TO")) {
							final JSONObject subItem3 = subItem2.getJSONObject("TO");
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.TO.ID", "ID");
							if (subItem3.has("STREET1")) {
								final JSONObject subItem4 = subItem3.getJSONObject("STREET1");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.TO.STREET1.ADDRESS1",
										"ADDRESS1");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.TO.STREET1.ADDRESS2",
										"ADDRESS2");
							}
							if (subItem3.has("STREET2")) {
								final JSONObject subItem4 = subItem3.getJSONObject("STREET2");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.TO.STREET2.ADDRESS1",
										"ADDRESS1");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.TO.STREET2.ADDRESS2",
										"ADDRESS2");
							}
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.TO.CITY", "CITY");
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.TO.COUNTY", "COUNTY");
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.TO.STATE", "STATE");
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.TO.ZIP", "ZIP");
							this.setStringValue(subItem3, values, "LOCATION.INTERSECTION.TO.ALIAS", "ALIAS");
							if (subItem3.has("PROXIMITY")) {
								final JSONObject subItem4 = subItem3.getJSONObject("PROXIMITY");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.TO.PROXIMITY.ID", "ID");
								this.setStringValue(subItem4, values, "LOCATION.INTERSECTION.TO.PROXIMITY.DESCRIPTION",
										"DESCRIPTION");
							}
						}

					}
					if (subItem1.has("ADDRESS")) {
						final JSONObject subItem2 = subItem1.getJSONObject("ADDRESS");
						if (subItem2.has("ORIGIN")) {
							final JSONObject subItem3 = subItem2.getJSONObject("ORIGIN");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.ORIGIN.ID", "ID");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.ORIGIN.HOUSE_NO", "HOUSE_NO");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.ORIGIN.ADDRESS1", "ADDRESS1");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.ORIGIN.ADDRESS2", "ADDRESS2");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.ORIGIN.CITY", "CITY");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.ORIGIN.STATE", "STATE");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.ORIGIN.COUNTY", "COUNTY");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.ORIGIN.ZIP", "ZIP");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.ORIGIN.ALIAS", "ALIAS");
							if (subItem3.has("PROXIMITY")) {
								final JSONObject subItem4 = subItem2.getJSONObject("PROXIMITY");
								this.setStringValue(subItem4, values, "LOCATION.ADDRESS.ORIGIN.PROXIMITY.ID", "ID");
								this.setStringValue(subItem4, values, "LOCATION.ADDRESS.ORIGIN.PROXIMITY.DESCRIPTION",
										"DESCRIPTION");
							}
						}
						if (subItem2.has("TO")) {
							final JSONObject subItem3 = subItem2.getJSONObject("TO");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.TO.ID", "ID");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.TO.HOUSE_NO", "HOUSE_NO");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.TO.ADDRESS1", "ADDRESS1");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.TO.ADDRESS2", "ADDRESS2");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.TO.CITY", "CITY");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.TO.STATE", "STATE");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.TO.COUNTY", "COUNTY");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.TO.ZIP", "ZIP");
							this.setStringValue(subItem3, values, "LOCATION.ADDRESS.TO.ALIAS", "ALIAS");
							if (subItem3.has("PROXIMITY")) {
								final JSONObject subItem4 = subItem2.getJSONObject("PROXIMITY");
								this.setStringValue(subItem4, values, "LOCATION.ADDRESS.TO.PROXIMITY.ID", "ID");
								this.setStringValue(subItem4, values, "LOCATION.ADDRESS.TO.PROXIMITY.DESCRIPTION",
										"DESCRIPTION");
							}
						}
					}
					if (subItem1.has("UNDEFINED_LOCATION")) {
						final JSONObject subItem2 = subItem1.getJSONObject("UNDEFINED_LOCATION");
						this.setStringValue(subItem2, values, "LOCATION.UNDEFINED_LOCATION.DESCRIPTION", "DESCRIPTION");
					}
					if (subItem1.has("GEOLOC")) {
						final JSONObject subItem2 = subItem1.getJSONObject("GEOLOC");
						if (subItem2.has("ORIGIN")) {
							final JSONObject subItem3 = subItem2.getJSONObject("ORIGIN");
							this.setDoubleValue(subItem3, values, "LOCATION.GEOLOC.ORIGIN.LATITUDE", "LATITUDE");
							this.setDoubleValue(subItem3, values, "LOCATION.GEOLOC.ORIGIN.LONGITUDE", "LONGITUDE");
						}
						if (subItem2.has("TO")) {
							final JSONArray subItem3 = subItem2.getJSONArray("TO");
							this.setDoubleValue(subItem3.getJSONObject(0), values, "LOCATION.GEOLOC.TO.LATITUDE",
									"LATITUDE");
							this.setDoubleValue(subItem3.getJSONObject(0), values, "LOCATION.GEOLOC.TO.LONGITUDE",
									"LONGITUDE");
						}
					}
					if (subItem1.has("POLITICAL_BOUNDARY")) {
						final JSONObject subItem2 = subItem1.getJSONObject("POLITICAL_BOUNDARY");
						if (subItem2.has("METRO_AREA")) {
							final JSONObject subItem3 = subItem2.getJSONObject("METRO_AREA");
							this.setLongValue(subItem3, values, "LOCATION.POLITICAL_BOUNDARY.METRO_AREA.ID", "ID");
						}
						this.setStringValue(subItem2, values, "LOCATION.POLITICAL_BOUNDARY.STATE", "STATE");
						this.setStringValue(subItem2, values, "LOCATION.POLITICAL_BOUNDARY.COUNTY", "COUNTY");
						if (subItem2.has("MUNICIPALITY")) {
							final JSONObject subItem3 = subItem2.getJSONObject("MUNICIPALITY");
							this.setStringValue(subItem3, values, "LOCATION.POLITICAL_BOUNDARY.MUNICIPALITY.NAME",
									"NAME");
							this.setStringValue(subItem3, values, "LOCATION.POLITICAL_BOUNDARY.MUNICIPALITY.ALIAS",
									"ALIAS");
						}

					}
					if (subItem1.has("NAVTECH")) {
						final JSONObject subItem2 = subItem1.getJSONObject("NAVTECH");
						if (subItem2.has("EDGE")) {
							final JSONObject subItem3 = subItem2.getJSONObject("EDGE");
							if (subItem3.has("EDGE_ID")) {
								final JSONArray subItem4 = subItem3.getJSONArray("EDGE_ID");
								// string builder that puts all separate Edge
								// IDs in one string, each separated with a
								// comma
								final StringBuffer sb = new StringBuffer();
								for (int j = 0; j < subItem4.length(); j++) {
									final String subItem5 = subItem4.getString(j);
									sb.append(subItem5);
									if (j < subItem4.length() - 1) {
										sb.append(",");
									}
								}
								values.put("LOCATION.NAVTECH.EDGE.EDGE_ID", sb.toString());
							}
						}
						this.setStringValue(subItem2, values, "LOCATION.NAVTECH.VERSION_ID", "VERSION_UD");
					}
				}
				if (item.has("TRAFFIC_ITEM_DETAIL")) {
					final JSONObject subItem1 = item.getJSONObject("TRAFFIC_ITEM_DETAIL");
					this.setStringValue(subItem1, values, "TRAFFIC_ITEM_DETAIL.ROAD_CLOSED", "ROAD_CLOSED");
					this.setStringValue(subItem1, values, "TRAFFIC_ITEM_DETAIL.DETOUR_DESC", "DETOUR_DESC");
					if (subItem1.has("LANES_BLOCKED")) {
						final JSONObject subItem2 = subItem1.getJSONObject("LANES_BLOCKED");
						if (subItem2.has("LANE")) {
							final JSONObject subItem3 = subItem2.getJSONObject("LANE");
							this.setStringValue(subItem3, values, "TRAFFIC_ITEM_DETAIL.LANES_BLOCKED.LANE.DESCRIPTION",
									"DESCRIPTION");
							this.setLongValue(subItem3, values, "TRAFFIC_ITEM_DETAIL.LANES_BLOCKED.LANE.NUM_BLOCKED",
									"NUM_BLOCKED");
						}
						this.setStringValue(subItem2, values, "TRAFFIC_ITEM_DETAIL.LANES_BLOCKED.ALL_LANES",
								"ALL_LANES");
					}
					this.setStringValue(subItem1, values, "TRAFFIC_ITEM_DETAIL.LANES_CLEAR_DESC", "LANES_CLEAR_DESC");
					if (subItem1.has("EVENT")) {
						final JSONObject subItem2 = subItem1.getJSONObject("EVENT");
						this.setStringValue(subItem2, values, "TRAFFIC_ITEM_DETAIL.EVENT.EVENT_ITEM_CANCELLED",
								"EVENT_ITEM_CANCELLED");
						if (subItem2.has("PLANNED_EVENT")) {
							final JSONObject subItem3 = subItem2.getJSONObject("PLANNED_EVENT");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.EVENT.PLANNED_EVENT.PLANNED_EVENT_TYPE_DESC",
									"PLANNED_EVENT_TYPE_DESC");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.EVENT.PLANNED_EVENT.PLANNED_EVENT_DESC", "PLANNED_EVENT_DESC");
						}
						if (subItem2.has("SCHEDULED_CONSTRUCTION_EVENT")) {
							final JSONObject subItem3 = subItem2.getJSONObject("SCHEDULED_CONSTRUCTION_EVENT");
							this.setStringValue(
									subItem3,
									values,
									"TRAFFIC_ITEM_DETAIL.EVENT.SCHEDULED_CONSTRUCTION_EVENT.SCHEDULED_CONSTRUCTION_TYPE_DESC",
									"SCHEDULED_CONSTRUCTION_TYPE_DESC");
							this.setStringValue(
									subItem3,
									values,
									"TRAFFIC_ITEM_DETAIL.EVENT.SCHEDULED_CONSTRUCTION_EVENT.SCHEDULED_CONSTRUCTION_DETAIL",
									"SCHEDULED_CONSTRUCTION_DETAIL");
						}
					}
					if (subItem1.has("INCIDENT")) {
						final JSONObject subItem2 = subItem1.getJSONObject("INCIDENT");
						this.setStringValue(subItem2, values, "TRAFFIC_ITEM_DETAIL.INCIDENT.RESPONSE_VEHICLES",
								"RESPONSE_VEHICLES");
						if (subItem2.has("ROAD_HAZARD_INCIDENT")) {
							final JSONObject subItem3 = subItem2.getJSONObject("ROAD_HAZARD_INCIDENT");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.ROAD_HAZARD_INCIDENT.ROAD_HAZARD_TYPE_DESC",
									"ROAD_HAZARD_TYPE_DESC");
						}
						if (subItem2.has("TRAVEL_TIMES")) {
							final JSONObject subItem3 = subItem2.getJSONObject("TRAVEL_TIMES");
							if (subItem3.has("LANE_TYPE")) {
								final JSONObject subItem4 = subItem2.getJSONObject("LANE_TYPE");
								if (subItem4.has("TRAVEL_TIME")) {
									subItem2.getJSONObject("TRAVEL_TIME");
								}
								this.setStringValue(subItem4, values,
										"TRAFFIC_ITEM_DETAIL.INCIDENT.TRAVEL_TIMES.LANE_TYPE.TYPE", "TYPE");
							}
						}
						if (subItem2.has("ACCIDENT_INCIDENT")) {
							final JSONObject subItem3 = subItem2.getJSONObject("ACCIDENT_INCIDENT");
							this.setLongValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.ACCIDENT_INCIDENT.CAR_COUNT", "CAR_COUNT");
							this.setLongValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.ACCIDENT_INCIDENT.TRUCK_COUNT", "TRUCK_COUNT");
							this.setLongValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.ACCIDENT_INCIDENT.TRACTOR_TRAILER_COUNT",
									"TRACTOR_TRAILER_COUNT");
							this.setLongValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.ACCIDENT_INCIDENT.MOTORCYCLE_COUNT",
									"MOTORCYCLE_COUNT");
							this.setLongValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.ACCIDENT_INCIDENT.OTHER_VEHICLE_COUNT",
									"OTHER_VEHICLE_COUNT");
						}
						if (subItem2.has("CONGESTION_INCIDENT")) {
							final JSONObject subItem3 = subItem2.getJSONObject("CONGESTION_INCIDENT");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.CONGESTION_INCIDENT.CONGESTION_TYPE_DESC",
									"CONGESTION_TYPE_DESC");
							this.setLongValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.CONGESTION_INCIDENT.CONGESTION_FACTOR",
									"CONGESTION_FACTOR");
						}
						if (subItem2.has("UNSCHEDULED_CONSTRUCTION_INCIDENT")) {
							final JSONObject subItem3 = subItem2.getJSONObject("UNSCHEDULED_CONSTRUCTION_INCIDENT");
							this.setStringValue(
									subItem3,
									values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.UNSCHEDULED_CONSTRUCTION_INCIDENT.UNSCHED_CONST_TYPE_DESC",
									"UNSCHED_CONST_TYPE_DESC");
							this.setStringValue(
									subItem3,
									values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.UNSCHEDULED_CONSTRUCTION_INCIDENT.UNSCHED_CONST_DETAIL",
									"UNSCHED_CONST_DETAIL");
						}
						if (subItem2.has("DISABLED_VEHICLE_INCIDENT")) {
							final JSONObject subItem3 = subItem2.getJSONObject("DISABLED_VEHICLE_INCIDENT");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.DISABLED_VEHICLE_INCIDENT.DISABLED_ITEM_TYPE_DESC",
									"DISABLED_ITEM_TYPE_DESC");
							this.setLongValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.DISABLED_VEHICLE_INCIDENT.INTEGER_OF_VEHICLES",
									"INTEGER_OF_VEHICLES");
						}
						if (subItem2.has("MISCELLANEOUS_INCIDENT")) {
							final JSONObject subItem3 = subItem2.getJSONObject("MISCELLANEOUS_INCIDENT");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.INCIDENT.MISCELLANEOUS_INCIDENT.MISCELLANEOUS_TYPE_DESC",
									"MISCELLANEOUS_TYPE_DESC");
						}

					}
					if (subItem1.has("NEWS_TYPE")) {
						final JSONObject subItem2 = subItem1.getJSONObject("NEWS_TYPE");
						if (subItem2.has("ALERT_NEWS")) {
							final JSONObject subItem3 = subItem2.getJSONObject("ALERT_NEWS");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.ALERT_NEWS.ALERT_TYPE", "ALERT_TYPE");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.ALERT_NEWS.ALERT_TYPE_DESC", "ALERT_TYPE_DESC");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.ALERT_NEWS.ALERT_SOURCE_DESC", "ALERT_SOURCE_DESC");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.ALERT_SOURCE_COMMENTS", "ALERT_SOURCE_COMMENTS");
						}
						if (subItem2.has("MASS_TRANSIT_NEWS")) {
							final JSONObject subItem3 = subItem2.getJSONObject("MASS_TRANSIT_NEWS");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.MASS_TRANSIT_NEWS.DETAIL_DESC", "DETAIL_DESC");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.MASS_TRANSIT_NEWS.LINE_DESC", "LINE_DESC");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.MASS_TRANSIT_NEWS.TYPE_DESC", "TYPE_DESC");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.MASS_TRANSIT_NEWS.DIRECTION_DESC", "DIRECTION_DESC");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.MASS_TRANSIT_NEWS.SERVICE_DESC", "SERVICE_DESC");
						}
						if (subItem2.has("OTHER_NEWS")) {
							final JSONObject subItem3 = subItem2.getJSONObject("OTHER_NEWS");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.OTHER_NEWS.SENSITIVITY_DESC", "SENSITIVITY_DESC");
							this.setStringValue(subItem3, values, "TRAFFIC_ITEM_DETAIL.NEWS_TYPE.OTHER_NEWS.NEWS_DESC",
									"NEWS_DESC");
						}
						if (subItem2.has("WEATHER_NEWS")) {
							final JSONObject subItem3 = subItem2.getJSONObject("WEATHER_NEWS");
							this.setStringValue(subItem3, values,
									"TRAFFIC_ITEM_DETAIL.NEWS_TYPE.WEATHER_NEWS.WEATHER_TYPE_DESC", "WEATHER_TYPE_DESC");
						}
					}
				}

				this.setStringValue(item, values, "TRAFFIC_ITEM_DESCRIPTION_short", "TRAFFIC_ITEM_DESCRIPTION_short");
				this.setStringValue(item, values, "TRAFFIC_ITEM_DESCRIPTION_desc", "TRAFFIC_ITEM_DESCRIPTION_desc");
				this.setStringValue(item, values, "TRAFFIC_ITEM_DESCRIPTION_noexit", "TRAFFIC_ITEM_DESCRIPTION_noexit");

				final EapEvent trafficEvent = new EapEvent(this.getNokiaHereEventtype(), new Date(), values);
				// System.out.println(trafficEvent); //TODO remove
				this.eventsToSend.add(trafficEvent);
			}
		}

		/*
		 * 
		 * BELOW: Create RoadTraffic event task from [Integration Feature #363]
		 * TODO
		 */

		if (response.has("TRAFFIC_ITEMS")) {
			final JSONArray items = response.getJSONObject("TRAFFIC_ITEMS").getJSONArray("TRAFFIC_ITEM");
			for (int i = 0; i < items.length(); i++) {
				final JSONObject item = items.getJSONObject(i);
				final Map<String, Serializable> values = new HashMap<String, Serializable>();

				this.setStringValue(item, values, "idAtProvider", "TRAFFIC_ITEM_ID"); // XXX
				values.put("timestamp", timestamp); // XXX

				if (item.has("RDS-TMC_LOCATIONS")) {
					final JSONObject subItem1 = item.getJSONObject("RDS-TMC_LOCATIONS");
					if (subItem1.has("RDS-TMC")) {
						final JSONArray subItem2 = subItem1.getJSONArray("RDS-TMC");
						// TODO multiple RDS-TMC Events?
						for (int j = 0; j < subItem2.length(); j++) {
							final JSONObject subItem3 = subItem2.getJSONObject(j);
							if (subItem3.has("ALERTC")) {
								final JSONObject subItem4 = subItem3.getJSONObject("ALERTC");
								this.setStringValue(subItem4, values, "type", "TRAFFIC_CODE"); // XXX
								this.setStringValue(subItem4, values, "cause", "DESCRIPTION"); // XXX
							}
							if (subItem3.has("LENGTH")) {
								values.put("length", (long) (subItem3.getDouble("LENGTH") * 1000));
							}
							String roadsAffectedOrigin = null;
							String roadsAffectedTo = null;
							if (subItem3.has("ORIGIN")) {
								final JSONObject subItem4 = subItem3.getJSONObject("ORIGIN");
								roadsAffectedOrigin = subItem4.getString("LOCATION_DESC");
							}
							if (subItem3.has("TO")) {
								final JSONObject subItem4 = subItem3.getJSONObject("TO");
								roadsAffectedTo = subItem4.getString("LOCATION_DESC");
							}
							values.put("roadsAffected", roadsAffectedOrigin + "," + roadsAffectedTo);

						}
					}
				}
				if (item.has("LOCATION")) {
					final JSONObject subItem1 = item.getJSONObject("LOCATION");
					if (subItem1.has("GEOLOC")) {
						final JSONObject subItem2 = subItem1.getJSONObject("GEOLOC");
						if (subItem2.has("ORIGIN")) {
							final JSONObject subItem3 = subItem2.getJSONObject("ORIGIN");
							setDoubleValue(subItem3, values, "startPointLatitude", "LATITUDE");
							setDoubleValue(subItem3, values, "startPointLongitude", "LONGITUDE");
						}
						if (subItem2.has("TO")) {
							final JSONArray subItem3 = subItem2.getJSONArray("TO");
							setDoubleValue(subItem3.getJSONObject(0), values, "endPointLatitude", "LATITUDE");
							setDoubleValue(subItem3.getJSONObject(0), values, "endPointLongitude", "LONGITUDE");
						}
					}
				}

				if (item.has("TRAFFIC_ITEM_DESCRIPTION")) {
					final JSONArray subItem1 = item.getJSONArray("TRAFFIC_ITEM_DESCRIPTION");
					for (int j = 0; j < subItem1.length(); j++) {
						final JSONObject subItem2 = subItem1.getJSONObject(j);
						if (subItem2.has("TYPE")) {
							if (subItem2.getString("TYPE").equals("desc")) {
								this.setStringValue(subItem2, values, "description", "value"); // XXX
							}
						}
					}
				}

				values.put("provider", "Nokia HERE"); // XXX

				// TODO: how to calculate delay for RoadTraffic ???

				// TODO:delay, (magnitude, identifier)

				final EapEvent trafficEvent = new EapEvent(this.getRoadTrafficEventtype(), new Date(), values);
				System.out.println(trafficEvent); // TODO remove
				this.eventsToSend.add(trafficEvent);
			}

		}

	}

	private void setDoubleValue(final JSONObject item, final Map<String, Serializable> values,
			final String eventAttributeName, final String jsonAttributeName) throws JSONException {
		if (item.has(jsonAttributeName)) {
			values.put(eventAttributeName, item.getDouble(jsonAttributeName));
		}
	}

	private void setLongValue(final JSONObject item, final Map<String, Serializable> values,
			final String eventAttributeName, final String jsonAttributeName) throws JSONException {
		if (item.has(jsonAttributeName)) {
			values.put(eventAttributeName, item.getLong(jsonAttributeName));
		}
	}

	private void setStringValue(final JSONObject item, final Map<String, Serializable> values,
			final String eventAttributeName, final String jsonAttributeName) throws JSONException {
		if (item.has(jsonAttributeName)) {
			values.put(eventAttributeName, item.getString(jsonAttributeName));
		}
	}

	private void setDateValue(final JSONObject item, final Map<String, Serializable> values,
			final String eventAttributeName, final String jsonAttributeName) throws JSONException {
		try {
			if (item.has(jsonAttributeName)) {
				String jsonString = item.getString(jsonAttributeName);
				// TODO generic timezones
				if (jsonString.endsWith(" GMT")) {
					jsonString = jsonString.replace(" GMT", "+0100");
				}
				if (jsonString.endsWith(" CET")) {
					jsonString = jsonString.replace(" CET", "+0100");
				}
				if (jsonString.endsWith(" CEST")) {
					jsonString = jsonString.replace(" CEST", "+0200");
				}
				values.put(eventAttributeName, this.df.parse(jsonString));
			}
		} catch (final ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is used to get the EventType for the
	 * NokiaHereTrafficIncident-Events from the respective XSD.
	 * 
	 * @return Returns the NokiaHereTrafficIncident EventType.
	 */
	private EapEventType getNokiaHereEventtype() {
		EapEventType trafficEventType = EapEventType.findByTypeName("NokiaHereTrafficIncident");
		if (trafficEventType == null) {
			try {
				trafficEventType = XSDParser.generateEventTypeFromXSD(
						this.getClass().getResource("/predefinedEventTypes/NokiaHereTrafficIncident.xsd").getPath(),
						"NokiaHereTrafficIncident");
				trafficEventType = Broker.getEventAdministrator().importEventType(trafficEventType);
			} catch (final XMLParsingException e) {
				e.printStackTrace();
			}
		}
		return trafficEventType;
	}

	/**
	 * This method is used to get the EventType for the RoadTraffic-Events from
	 * the respective XSD.
	 * 
	 * @return Returns the RoadTraffic EventType.
	 */
	private EapEventType getRoadTrafficEventtype() {
		EapEventType trafficEventType = EapEventType.findByTypeName("RoadTraffic");
		if (trafficEventType == null) {
			try {
				trafficEventType = XSDParser.generateEventTypeFromXSD(
						this.getClass().getResource("/predefinedEventTypes/RoadTraffic.xsd").getPath(), "RoadTraffic");
				trafficEventType = Broker.getEventAdministrator().importEventType(trafficEventType);
			} catch (final XMLParsingException e) {
				e.printStackTrace();
			}
		}
		return trafficEventType;
	}

	@Override
	public void start(final long interval) {
		if (this.query.isEmpty()) {
			return;
		}
		super.start(interval);
	}

	// @Override
	public void registerEventType() {

	}

	/**
	 * This method is called periodically from the QuartzJob-Scheduler. The
	 * interval can be configured.
	 * 
	 * In every cycle events for the specified Route are being queried and
	 * loaded into the Unicorn-platform.
	 */
	@Override
	public void trigger() {
		try {
			final JSONObject response = this.callWebservice();
			this.parseResponse(response);
			this.sendEventsToUnicorn();
			System.out.println(response.toString());
		} catch (final JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is executing the NokiaHereAPI-call. The http-string from the
	 * query variable is being used.
	 * 
	 * @return JSON-respond containing the traffic events for route specified in
	 *         the query.
	 */
	// @Override
	public JSONObject callWebservice() {

		final HttpClient httpclient = new DefaultHttpClient();
		String responseBody = "";
		JSONObject jsonRespond = null;

		try {
			final HttpGet httpget = new HttpGet(this.query);
			System.out.println("executing request " + httpget.getURI());
			// Create a response handler
			final ResponseHandler<String> responseHandler = new BasicResponseHandler();
			responseBody = httpclient.execute(httpget, responseHandler);
			jsonRespond = new JSONObject(responseBody);
		} catch (final Exception e) {
			System.err.println("ERROR: unexpected NokiaHereAPI respond or no connection possible: " + responseBody);
			e.printStackTrace();
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}

		return jsonRespond;
	}

	/**
	 * This method is used to send the parsed Events that are saved in the
	 * eventsToSend variable to the Unicorn-platform.
	 */
	// @Override
	public void sendEventsToUnicorn() {
		Broker.getEventImporter().importEvents(this.eventsToSend);
		this.eventsToSend.clear();
	}

	/**
	 * This Method is used to build an Area-String that specifies the corridor
	 * for which the traffic events shall be polled. Then the query is being
	 * build and saved to the instance of the NokiaHereAdapter.
	 * 
	 * @param width
	 *            integer value that specifies the width of the corridor in
	 *            meters
	 * @param coordinates
	 *            expects at least 4 double coordinates, latitude and longitude
	 *            for the start- and end-point of the corridor respectively.
	 *            More then 2 coordinate pairs can be specified, then the
	 *            intermediate pairs will be taken as waypoints between start
	 *            and end of the corridor.
	 */
	public void setAreaForCorridor(final int width, final double... coordinates) {

		String area;
		boolean isLatitude = true;
		final StringBuffer sb = new StringBuffer();
		for (final double c : coordinates) {
			sb.append(c);
			if (isLatitude) {
				sb.append(",");
			} else {
				sb.append(";");
			}
			isLatitude = !isLatitude;
		}
		// puts a "," after every latitude(1st coordinate) and ";" after every
		// pair of coordinates for API requestString

		area = "Corridor=" + sb.toString() + width;
		this.query = this.createQuery(area);
	}

	public void setAreaForCorridor(final int width, final List<Double> coordinates) {

		String area;
		boolean isLatitude = true;
		final StringBuffer sb = new StringBuffer();
		for (final double c : coordinates) {
			sb.append(c);
			if (isLatitude) {
				sb.append(",");
			} else {
				sb.append(";");
			}
			isLatitude = !isLatitude;
		}
		// puts a "," after every latitude(1st coordinate) and ";" after every
		// pair of coordinates for API requestString

		area = "Corridor=" + sb.toString() + width;
		this.query = this.createQuery(area);
	}

}
