/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.espertech.esper.client.EPException;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.exception.DuplicatedSchemaException;
import de.hpi.unicorn.exception.EventTypeNotFoundException;
import de.hpi.unicorn.exception.UnparsableException;
import de.hpi.unicorn.exception.UnparsableException.ParseType;
import de.hpi.unicorn.importer.xml.XMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.importer.xml.XSDParser;
import de.hpi.unicorn.notification.NotificationMethod;
import de.hpi.unicorn.notification.NotificationRuleForQuery;
import de.hpi.unicorn.notification.RestNotificationRule;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;
import de.hpi.unicorn.transformation.TransformationRule;
import de.hpi.unicorn.user.EapUser;

/**
 *
 * provides webservice methods registered in
 * src/main/webapp/WEB-INF/services/XMLService/META-INF/services.xml to start
 * webserver run: mvn jetty:run (WSDL is deployed to: <address of
 * tomcat>/EapWebInterface/services/EventProcessingPlatformWebservice?wsdl).
 *
 */
public class EventProcessingPlatformWebservice {

	// Logger
	private static final Logger logger = Logger.getLogger(EventProcessingPlatformWebservice.class);



	/**
	 * import xml-event if eventtyp of event is registered to the EPP
	 *
	 * @throws XMLParsingException
	 * @throws UnparsableException
	 */
	public void importEvents(final String xml) throws UnparsableException, XMLParsingException {
		final long callStart = System.currentTimeMillis();
		// generate document from xml String
		final Document doc = XMLParser.XMLStringToDoc(xml);
		if (doc == null) {
			throw new UnparsableException(ParseType.EVENT);
		}

		// generate the Event from the doc via XML Parser
		List<EapEvent> newEvents = XMLParser.generateEventsFromDoc(doc);
		for (final EapEvent newEvent : newEvents) {
			Broker.getEventImporter().importEvent(newEvent);
		}
		EventProcessingPlatformWebservice.logger.debug("Import of event took "
				+ (double) (System.currentTimeMillis() - callStart) / 1000 + " seconds");
	}

	/**
	 * Registers an event type using an XSD
	 *
	 * @param xsd
	 * @param schemaName
	 * @param timestampName
	 *            if the timestamp is not part of the event type description in
	 *            the XSD file, an additional attribute of type Date with the
	 *            given timestamp name is created
	 * @return
	 * @throws DuplicatedSchemaException
	 * @throws UnparsableException
	 */
	public void registerEventType(final String xsd, final String schemaName, final String timestampName)
			throws DuplicatedSchemaException, UnparsableException {
		// generate input stream from xml for creating the doc
		// test for already existing
		if (EapEventType.findBySchemaName(schemaName) != null) {
			GETLogManager.logError("registerEventType-DuplicatedSchemaException",
					String.format("Duplicated schema name %s. Already on the server.", schemaName));
			throw new DuplicatedSchemaException(schemaName);
		}

		// generate the EventType from the xml string via XML Parser
		EapEventType newEventType = XSDParser.generateEventType(xsd, schemaName, timestampName);
		Broker.getEventAdministrator().importEventType(newEventType);
	}

	/**
	 * Returns all names of event types currently registered on the server.
	 *
	 * @return event type names
	 */
	public List<String> getAllEventTypes() {
		final List<String> eventTypes = new LinkedList<>();
		for (final EapEventType eventType : Broker.getEventAdministrator().getAllEventTypes()) {
			eventTypes.add(eventType.getTypeName());
		}
		return eventTypes;
	}

	/**
	 *
	 * Returns names of all attributes of given event type.
	 *
	 * @param eventTypeName
	 * @return attribute names
	 * @throws EventTypeNotFoundException
	 */
	public List<String> getAttributeNames(final String eventTypeName) throws EventTypeNotFoundException {

		final EapEventType eventType = EapEventType.findBySchemaName(eventTypeName);

		if (eventType == null) {
			GETLogManager.logError("getAttributeNames-EventTypeNotFoundException",
					String.format("Event type '%s' not found.", eventTypeName));
			throw new EventTypeNotFoundException(eventTypeName);
		}

		return eventType.getAttributeExpressions();
	}

	/**
	 * Returns XSD string of event type.
	 *
	 * @param eventTypeName
	 *            name of event type
	 * @return event type as XSD string
	 * @throws EventTypeNotFoundException
	 */
	public String getEventTypeXSD(final String eventTypeName) throws EventTypeNotFoundException {

		final EapEventType eventType = EapEventType.findBySchemaName(eventTypeName);

		if (eventType == null) {
			GETLogManager.logError("getEventTypeXSD-EventTypeNotFoundException",
					String.format("Event type '%s' not found.", eventTypeName));
			throw new EventTypeNotFoundException(eventTypeName);
		}
		return eventType.getXsdString();
	}

	/**
	 * Unregisters an event type
	 *
	 * @param schemaName
	 *            name of event type
	 * @return
	 */
	public boolean unregisterEventType(final String schemaName) {
		final EapEventType eventType = EapEventType.findBySchemaName(schemaName);
		if (eventType != null) {
			Broker.getEventAdministrator().removeEventType(eventType);
			return true;
		}
		return false;
	}

	private EapUser findOrCreateUserByEMail(final String email) {
		EapUser user;
		if (EapUser.findByMail(email).isEmpty()) {
			user = new EapUser(email, "1234", email);
			user.save();
		} else {
			user = EapUser.findByMail(email).get(0);
		}
		return user;
	}

	/**
	 * Registers a new query and opens a subscriber queue with the returned id.
	 *
	 * @param title
	 * @param queryString
	 * @return queue id
	 */
	public String registerQueryForQueue(final String title, final String queryString, final String eMail) {
		try {
			final QueryWrapper query = new QueryWrapper(title, queryString, QueryTypeEnum.LIVE);
			query.addToEsper();
			query.save();

			final EapUser user = this.findOrCreateUserByEMail(eMail);

			final NotificationRuleForQuery notificationRule = new NotificationRuleForQuery(query, user,
					NotificationMethod.QUEUE);
			notificationRule.save();
			return notificationRule.getUuid();
		} catch (final EPException e) {
			GETLogManager.logError("registerQueryForQueue-EPException", e.getMessage());
			return "EPException: " + e.getMessage();
		}
	}

	public String registerQueryForRest(final String queryString, final String notificationPath) {
		try {
			final QueryWrapper query = new QueryWrapper("Automatic", queryString, QueryTypeEnum.LIVE);
			query.addToEsper();
			query.save();

			final RestNotificationRule notificationRule = new RestNotificationRule(query, notificationPath);
			notificationRule.save();
			return notificationRule.getUuid();
		} catch (final EPException e) {
			GETLogManager.logError("registerQueryForRest-EPException", e.getMessage());
			return "EPException: " + e.getMessage();
		}
	}

	/**
	 * Unregisters the query and destroys the corresponding queue.
	 *
	 * @param uuid
	 */
	public boolean unregisterQueryFromQueue(final String uuid) {
		final NotificationRuleForQuery notificationRule = NotificationRuleForQuery.findByUUID(uuid);
		final QueryWrapper query = notificationRule.getQuery();
		notificationRule.remove();
		if (query.getNotificationRulesForQuery().isEmpty()) {
			query.remove();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Unregisters the query from the corresponding REST notification rule.
	 *
	 * @param uuid
	 */
	public boolean unregisterQueryFromRest(final String uuid) {
		final RestNotificationRule notificationRule = RestNotificationRule.findByUUID(uuid);
		final QueryWrapper query = notificationRule.getQuery();
		notificationRule.remove();
		if (query.getNotificationRulesForQuery().isEmpty()) {
			query.remove();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Unregisters all queries of an user by deleting his notification rules.
	 *
	 * @param email
	 *            email of the user
	 * @return number of deleted notification rules
	 */
	public int unregisterQueriesFromQueue(final String email) {
		final List<EapUser> userList = EapUser.findByMail(email);
		if (userList.isEmpty()) {
			return 0;
		}
		final EapUser user = userList.get(0);
		final List<NotificationRuleForQuery> rules = NotificationRuleForQuery.findQueriesByUser(user);
		final int numberOfQueries = rules.size();
		for (final NotificationRuleForQuery notificationRule : rules) {
			final QueryWrapper query = notificationRule.getQuery();
			notificationRule.remove();
			if (query.getNotificationRulesForQuery().isEmpty()) {
				query.remove();
			}
		}
		return numberOfQueries;
	}

	/**
	 * Registers an event aggregation rule - unique by ruleName and
	 * eventTypeName
	 *
	 * @param ruleName
	 *            name of event aggregation rule
	 * @param eventTypeName
	 *            name of event type
	 * @param eventAggregationRule
	 *            event aggregation rule
	 * @return string containing information whether the registration was
	 *         successful or not
	 */
	public String registerEventAggregationRule(final String ruleName, final String eventTypeName,
			final String eventAggregationRule) {
		String message;
		try {
			if (ruleName == null || ruleName.isEmpty()) {
				message = "Registration of event aggregation rule failed - please provide a name for the event aggregation rule.";
				GETLogManager.logError("registerEventAggregationRule-Exception", message);
				return message;
			} else if (eventTypeName == null || eventTypeName.isEmpty()) {
				message = "Registration of event aggregation rule failed - please provide an event type.";
				GETLogManager.logError("registerEventAggregationRule-Exception", message);
				return message;
			} else if (eventAggregationRule == null || eventAggregationRule.isEmpty()) {
				message = "Registration of event aggregation rule failed - please provide an event aggregation rule.";
				GETLogManager.logError("registerEventAggregationRule-Exception", message);
				return message;
			} else {
				final EapEventType eventType = EapEventType.findByTypeName(eventTypeName);
				if (eventType == null) {
					message = "Registration of event aggregation rule failed - event type was not found.";
					GETLogManager.logError("registerEventAggregationRule-Exception", message);
					return message;
				} else if (TransformationRule.findByEventTypeAndTitle(eventTypeName, ruleName) != null) {
					message = "Registration of event aggregation rule failed - a rule with the given name and the given event type exists.";
					GETLogManager.logError("registerEventAggregationRule-Exception", message);
					return message;
				}
				final TransformationRule transformationRule = new TransformationRule(eventType, ruleName,
						eventAggregationRule);
				Broker.getInstance().register(transformationRule);
				transformationRule.save();
				return "Registration of event aggregation rule was successful.";
			}
		} catch (final EPException e) {
			message = "Registration of event aggregation rule failed - " + e.getMessage();
			GETLogManager.logError("registerEventAggregationRule-Exception", message);
			return message;
		}
	}

	/**
	 * Unregisters an event aggregation rule
	 *
	 * @param ruleName
	 *            name of event aggregation rule
	 * @param eventTypeName
	 *            name of event type
	 * @return true if deletion was successful
	 */
	public boolean unregisterEventAggregationRule(final String ruleName, final String eventTypeName) {
		final TransformationRule transformationRule = TransformationRule.findByEventTypeAndTitle(eventTypeName,
				ruleName);
		if (transformationRule == null) {
			return false;
		}
		Broker.getInstance().remove(transformationRule);
		return true;
	}

	public boolean registerEventCorrelationRule(final String process, final String firstEventType,
			final String firstAttribute, final String secondEventType, final String secondAttribute) {
		// TODO: implement it
		String message = "Thanks for using our platform! This feature will be implemented in a future version of this webservice!";
		GETLogManager.logError("registerEventAggregationRule-Exception", message);
		throw new NotImplementedException(message);
	}

}
