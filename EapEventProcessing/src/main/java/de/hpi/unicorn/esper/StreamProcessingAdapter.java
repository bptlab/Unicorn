/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.esper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.xpath.XPathConstants;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.espertech.esper.client.ConfigurationEventTypeXMLDOM;
import com.espertech.esper.client.ConfigurationOperations;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.core.service.EPServiceProviderSPI;

import de.hpi.unicorn.adapter.AdapterManager;
import de.hpi.unicorn.configuration.EapConfiguration;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.exception.UnparsableException;
import de.hpi.unicorn.importer.FileUtils;
import de.hpi.unicorn.importer.xml.XSDParser;
import de.hpi.unicorn.query.LiveQueryListener;
import de.hpi.unicorn.query.PatternQuery;
import de.hpi.unicorn.query.PatternQueryListener;
import de.hpi.unicorn.query.QueryWrapper;
import de.hpi.unicorn.transformation.TransformationListener;
import de.hpi.unicorn.transformation.TransformationRule;
import de.hpi.unicorn.transformation.TransformationRuleLogic;
import de.hpi.unicorn.utils.XMLUtils;

/**
 * This singleton class is an adapter for the Esper event proccesing engine.
 */
@SuppressWarnings("serial")
public class StreamProcessingAdapter implements Serializable {

	private static StreamProcessingAdapter instance = null;
	private ConfigurationOperations esperConfiguration;
	private EPServiceProviderSPI esperServiceProvider;
	private EPRuntime esperRuntime;
	/**
	 * Stores all EPL statements registered with Esper.
	 */
	private final HashMap<String, Object> statementNames = new HashMap<String, Object>();
	private int statementID = 0;

	private AdapterManager adapterManager;

	private static final Logger logger = Logger.getLogger(StreamProcessingAdapter.class);

	/**
	 * Constructor. Called by {@link StreamProcessingAdapter.getInstance()}
	 *
	 * Does the following steps: 1) initializes Esper 2) loads event types, live
	 * queries and transformation rules from the database 3) initializes traffic
	 * and weather adapters 4) loads predefined event types 5) loads predefined
	 * transformation rules
	 */
	private StreamProcessingAdapter() {
		this.initializeEsper();
		// this.loadFromDatabase();
		this.initializeAdapter();
		if (EapConfiguration.registerPredefinedEventTypes) {
			this.registerPredefinedEventTypes();
		}
		if (EapConfiguration.registerTransformationRules) {
			this.registerTransformationRules();
		}
	}

	/**
	 * Lazily creates the singleton instance of {@link StreamProcessingAdapter}.
	 *
	 * @return the singleton instance of {@link StreamProcessingAdapter}
	 */
	public static StreamProcessingAdapter getInstance() {
		if (StreamProcessingAdapter.instance == null) {
			StreamProcessingAdapter.instance = new StreamProcessingAdapter();
		}
		return StreamProcessingAdapter.instance;
	}

	/**
	 * Checks whether an instance of {@link StreamProcessingAdapter} exists.
	 *
	 * @return true, if no instance exists
	 */
	public static boolean instanceIsCleared() {
		return (StreamProcessingAdapter.instance == null);
	}

	/**
	 * Sets an existing instance of {@link StreamProcessingAdapter} to
	 * {@literal NULL}.
	 */
	public static void clearInstance() {
		if (StreamProcessingAdapter.instance != null) {
			// instance.weatherAdapter.deleteQuartzJob();
			// instance.trafficAdapter.deleteQuartzJob();
		}
		StreamProcessingAdapter.instance = null;
	}

	/**
	 * Initializes and configures the Esper part of the platform. Especially,
	 * functions to be used in EPL queries are registered.
	 */
	private void initializeEsper() {
		this.esperServiceProvider = (EPServiceProviderSPI) EPServiceProviderManager
				.getProvider(EPServiceProviderSPI.DEFAULT_ENGINE_URI);
		this.esperServiceProvider.initialize();

		this.esperConfiguration = this.esperServiceProvider.getEPAdministrator().getConfiguration();
		this.esperConfiguration.addPlugInSingleRowFunction("currentDate", "de.hpi.unicorn.esper.EapUtils",
				"currentDate");
		this.esperConfiguration.addPlugInSingleRowFunction("formatDate", "de.hpi.unicorn.esper.EapUtils", "formatDate");
		this.esperConfiguration.addPlugInSingleRowFunction("parseDate", "de.hpi.unicorn.esper.EapUtils", "parseDate");
		this.esperConfiguration.addPlugInSingleRowFunction("getIntersection", "de.hpi.unicorn.esper.EapUtils",
				"getIntersection");
		this.esperConfiguration.addPlugInSingleRowFunction("isIntersectionNotEmpty", "de.hpi.unicorn.esper.EapUtils",
				"isIntersectionNotEmpty");
		this.esperConfiguration.addPlugInSingleRowFunction("integerValueFromEvent", "de.hpi.unicorn.esper.EapUtils",
				"integerValueFromEvent");
		this.esperConfiguration.addPlugInSingleRowFunction("doubleValueFromEvent", "de.hpi.unicorn.esper.EapUtils",
				"doubleValueFromEvent");
		this.esperConfiguration.addPlugInSingleRowFunction("stringValueFromEvent", "de.hpi.unicorn.esper.EapUtils",
				"stringValueFromEvent");
		this.esperConfiguration.addPlugInSingleRowFunction("dateValueFromEvent", "de.hpi.unicorn.esper.EapUtils",
				"dateValueFromEvent");
		this.esperConfiguration.addPlugInSingleRowFunction("sumFromEventList", "de.hpi.unicorn.esper.EapUtils",
				"sumFromEventList");
		this.esperConfiguration.addPlugInSingleRowFunction("isSubstringOf", "de.hpi.unicorn.esper.EapUtils",
				"isSubstringOf");
		this.esperConfiguration.addPlugInSingleRowFunction("distance", "de.hpi.unicorn.utils.GeoUtils", "distance");
		this.esperConfiguration.addPlugInSingleRowFunction("inBetween", "de.hpi.unicorn.utils.GeoUtils", "inBetween");
		this.esperConfiguration.addPlugInSingleRowFunction("deadline",
				"de.hpi.unicorn.application.TransportNodeLookup", "getDeadline");
		this.esperConfiguration.addPlugInSingleRowFunction("legLength",
				"de.hpi.unicorn.application.TransportNodeLookup", "getLegLength");
		this.esperConfiguration.addPlugInSingleRowFunction("getOperator",
				"de.hpi.unicorn.application.TransportNodeLookup", "getOperatorForRoute");
		this.esperConfiguration.addPlugInSingleRowFunction("isFinalNode",
				"de.hpi.unicorn.application.TransportNodeLookup", "isFinalNode");

		this.esperRuntime = this.esperServiceProvider.getEPRuntime();
		// esperRuntime.sendEvent(new
		// TimerControlEvent(TimerControlEvent.ClockType.CLOCK_EXTERNAL));
	}

	public AdapterManager getAdapterManager() {
		return this.adapterManager;
	}

	/**
	 * Loads event types, live queries and transformation rules from the
	 * database and registers them with Esper.
	 */
	private void loadFromDatabase() {
		// 1. Event types
		for (final EapEventType eventType : EapEventType.findAll()) {
			this.addEventType(eventType);
		}
		// 2. (Live) Queries
		for (final QueryWrapper currentQuery : QueryWrapper.getAllLiveQueries()) {
			try {
				this.addLiveQuery(currentQuery);
			} catch (final EPException e) {
				currentQuery.remove();
			}
		}
		// 3. Transformation Rules
		TransformationRuleLogic.getInstance();
		for (final TransformationRule rule : TransformationRule.findAll()) {
			if (!this.statementNames.keySet().contains(TransformationRuleLogic.generateStatementName(rule))) {
				this.addTransformationRule(rule);
				StreamProcessingAdapter.logger.info("Registered transformation rule '" + rule.getTitle()
						+ "' for event type '" + rule.getEventType().getTypeName() + "' from database.");
			} else {
				StreamProcessingAdapter.logger.info("Transformation rule " + rule.getTitle()
						+ " already registered in Esper.");
			}
		}
	}

	/**
	 * Initializes adapters for traffic and weather information if the
	 * corresponding flags are true. A {@link org.quartz.Job} is registered with
	 * the quartz scheduler to be executed regularly. TODO: move configuration
	 * of adapters to config file
	 */
	private void initializeAdapter() {
		// if (activatedWeatherAdapter) {
		// this.weatherAdapter = new WeatherAdapter();
		// this.weatherAdapter.scheduleQuartzJob();
		// }
		// if (activatedTomTomAdapter) {
		// this.trafficAdapter = new TrafficAdapter();
		// this.trafficAdapter.scheduleQuartzJob();
		// }
		this.adapterManager = AdapterManager.getInstance();
	}

	/**
	 * Registers all event types with Esper whose xsd file is stored in the
	 * resource folder of EapWebInterface project (because that project is
	 * deployed).
	 *
	 * TODO: Fail silently, if one event type can not be imported then skip it
	 */
	private void registerPredefinedEventTypes() {

		try {

			// getting all xsd from the folder in EapWebInterface
			final File[] schemasFiles = this.readFolder(this.getClass().getResource("/predefinedEventTypes").getPath());
			for (final File file : schemasFiles) {
				final String schemaName = file.getName().split(".xsd", 2)[0];
				// if not already existing
				if (EapEventType.findBySchemaName(schemaName) == null) {
					final String xsd = FileUtils.getFileContentAsString(file.getAbsolutePath());

					String timestampName = null;
					if (xsd.contains("timestamp")) {
						timestampName = "timestamp";
					} else if (xsd.contains("time")) {
						timestampName = "time";
					} else if (xsd.contains("start")) {
						timestampName = "start";
					}
					// generate the EventType from the xml string via XML Parser
					final EapEventType newEventType = XSDParser.generateEventType(xsd, schemaName, timestampName);
					this.addEventType(newEventType);
					newEventType.save();
					/*
					 * Cannot use Broker as it requires an instance of
					 * StreamProcessingAdapter, which is being created right now
					 */
					// Broker.getEventAdministrator().importEventType(newEventType);

				}
			}
		} catch (final NullPointerException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final UnparsableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void registerTransformationRules() {
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader;
			reader = factory.createXMLStreamReader(this.getClass().getResourceAsStream("/transformationRules.xml"));
			TransformationRule rule = null;
			List<String> incomingTypeNames = new ArrayList<>();
			List<EapEventType> incomingTypes = new ArrayList<>();
			String tag;
			while (reader.hasNext()) {
				int event = reader.next();
				switch (event) {
				case XMLStreamConstants.START_ELEMENT:
					tag = reader.getLocalName();
					if ("transformationRule".equals(tag)) {
						rule = new TransformationRule();
					} else if ("ruleName".equals(tag)) {
						rule.setTitle(reader.getElementText());
					} else if ("rule".equals(tag)) {
						rule.setQuery(reader.getElementText());
					} else if ("incomingEventType".equals(tag)) {
						incomingTypeNames.add(reader.getElementText());
					} else if ("eventType".equals(tag)) {
						String typeName = reader.getElementText();
						rule.setEventType(EapEventType.findByTypeName(typeName));
					}
					break;
				case XMLStreamConstants.END_ELEMENT:
					tag = reader.getLocalName();
					if ("incomingEventTypes".equals(tag)) {
						for (String typeName : incomingTypeNames) {
							EapEventType type;
							if ((type = EapEventType.findByTypeName(typeName)) != null)
								incomingTypes.add(type);
						}
						rule.setEventTypesOfIncomingEvents(incomingTypes);
						incomingTypeNames.clear();
						incomingTypes.clear();
					} else if ("transformationRule".equals(tag)) { // rule
																	// complete
						if (!statementNames.keySet().contains(TransformationRuleLogic.generateStatementName(rule))) {
							// No statement with the same canonical name exists
							addTransformationRule(rule);
							/*
							 * cannot use the Broker to register the rule as it
							 * would trigger the initialization of
							 * StreamProcessAdapter (which we are performing
							 * right now)
							 */
							rule.save();
						}
					}
					break;
				default:
					break;
				}
			}
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private File[] readFolder(final String folder) {
		final File file = new File(folder);
		return file.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name) {
				return !name.toLowerCase().startsWith("._");
			}
		});
	}

	/**
	 * Checks if event type is already registered in Esper.
	 *
	 * @return true, if event type is already registered
	 */
	public boolean isRegistered(final EapEventType eventType) {
		return this.esperConfiguration.isEventTypeExists(eventType.getTypeName());
	}

	/**
	 * returns information of eventtyp from {@link
	 * EventType.getPropertyType(String)}
	 */
	public Class getEventTypeInfo(final EapEventType eventType, final String attribute) {
		final EventType type = this.esperConfiguration.getEventType(eventType.getTypeName());
		if (type.isProperty(attribute) == false) {
			return null;
		}
		return type.getPropertyType(attribute);
	}

	/**
	 * checks if eventyp has attribut in Esper
	 *
	 * @param eventType
	 * @param attribute
	 * @return
	 */
	public Boolean eventTypeHasAttribute(final EapEventType eventType, final String attribute) {
		final EventType type = this.esperConfiguration.getEventType(eventType.getTypeName());
		return type.isProperty(attribute);
	}

	/**
	 * returns attribute names of eventtyp
	 *
	 * @param eventType
	 * @return
	 */
	public String[] getAttributesOfEventType(final EapEventType eventType) {
		final EventType type = this.esperConfiguration.getEventType(eventType.getTypeName());
		return type.getPropertyNames();
	}

	/**
	 * add event
	 *
	 * @param events
	 */
	public void addEvents(final List<EapEvent> events) {
		for (final EapEvent event : events) {
			this.addEvent(event);
		}
	}

	/**
	 * stores a EapEvent in a triplestore converts a EapEvent to a XMLEvent and
	 * sends it to Esper if a semantic part of an transformation rule exists,
	 * its EPL query will be evaluated against the event only if the semantic
	 * query returned a positive result
	 *
	 * @param event
	 */
	public void addEvent(final EapEvent event) {
		final List<EPStatement> statementsWithStoppedListeners = new ArrayList<EPStatement>();
		final long semStart = System.currentTimeMillis();
		final List<TransformationRule> transformationRules = TransformationRule.getTransformationRulesForEvent(event);
		final long semEnd = System.currentTimeMillis();

		StreamProcessingAdapter.logger.debug(String.format("Semantic processing of event took %.2f seconds",
				(double) (semEnd - semStart) / 1000));
		final long xmlStart = System.currentTimeMillis();
		final Node node = XMLUtils.eventToNode(event);
		final long xmlEnd = System.currentTimeMillis();
		StreamProcessingAdapter.logger.debug(String.format("Conversion of event to node took %.2f seconds",
				(double) (xmlEnd - xmlStart) / 1000));
		// XMLUtils.printDocument((Document) node);
		if (node == null) {
			System.err.println("Event was not parseable!");
		}
		// long timeInMilliseconds = event.getTimestamp().getTime();
		// this.esperRuntime.sendEvent(new
		// CurrentTimeEvent(timeInMilliseconds));
		final long esperStart = System.currentTimeMillis();


		/**
		 * THIS DOES THE ACTUAL TRIGGERING STUFF
		 */
		this.esperRuntime.sendEvent(node);



		final long esperEnd = System.currentTimeMillis();
		StreamProcessingAdapter.logger.debug(String.format("Esper processing of event took %.2f seconds",
				(double) (esperEnd - esperStart) / 1000));
		// reactivate Esper statements
		for (final EPStatement statement : statementsWithStoppedListeners) {
			statement.start();
		}
	}

	/**
	 * Creates schema from given event type and registers it with Esper. Creates
	 * "keep-all" window for the created event type and also registers it with
	 * Esper. TODO: the creation of windows should be configurable
	 *
	 * @param eventType
	 */
	public void addEventType(final EapEventType eventType) {
		if (!this.isRegistered(eventType)) {
			final ConfigurationEventTypeXMLDOM dom = this.eventTypeToXMLDom(eventType);
			this.esperConfiguration.addEventType(eventType.getTypeName(), dom);
		}
		if (EapConfiguration.supportingOnDemandQueries) {
			if (this.hasWindow(eventType.getTypeName() + "Window")) {
				return;
			}
			this.esperServiceProvider.getEPAdministrator().createEPL(
					"CREATE WINDOW " + eventType.getTypeName() + "Window.win:keepall() AS " + eventType.getTypeName());
			this.esperServiceProvider.getEPAdministrator().createEPL(
					"INSERT INTO " + eventType.getTypeName() + "Window SELECT * FROM " + eventType.getTypeName());
		}
	}

	/**
	 * Prepares the provided {@link EapEventType} for insertion into Esper
	 * engine which expects a object of type
	 * {@link ConfigurationEventTypeXMLDOM}. Basically this object defines how
	 * to access properties of this type by specifying XPath path statements.
	 *
	 * @param eventType
	 * @return the {@link ConfigurationEventTypeXMLDOM} required by the Esper
	 *         engine
	 */
	private ConfigurationEventTypeXMLDOM eventTypeToXMLDom(final EapEventType eventType) {
		final AttributeTypeTree tree = eventType.getValueTypeTree();
		final ConfigurationEventTypeXMLDOM dom = new ConfigurationEventTypeXMLDOM();
		dom.setRootElementName(eventType.getTypeName());
		dom.addXPathProperty(eventType.getTimestampName(),
				"/" + eventType.getTypeName() + "/" + eventType.getTimestampName(), XPathConstants.STRING,
				"java.util.Date");
		dom.setStartTimestampPropertyName(eventType.getTimestampName());
		dom.addXPathProperty("ProcessInstances", "/" + eventType.getTypeName() + "/ProcessInstances",
				XPathConstants.STRING, "java.util.List");

		for (final TypeTreeNode element : tree.getAttributes()) {
			final AttributeTypeEnum attType = element.getType();
			// System.out.println(element.toString());
			if (attType != null) {
				switch (attType) {
				case DATE:
					dom.addXPathProperty(element.getAttributeExpression(),
							"/" + eventType.getTypeName() + element.getXPath(), XPathConstants.STRING, "java.util.Date");
					break;
				// case DATE :
				// dom.addXPathProperty(element.getAttributeExpression(), "/" +
				// eventType.getTypeName() + element.getXPath(),
				// DatatypeConstants.DATETIME, "java.util.Date"); break;
				case FLOAT:
					dom.addXPathProperty(element.getAttributeExpression(),
							"/" + eventType.getTypeName() + element.getXPath(), XPathConstants.NUMBER);
					break;
				case INTEGER:
					dom.addXPathProperty(element.getAttributeExpression(),
							"/" + eventType.getTypeName() + element.getXPath(), XPathConstants.NUMBER, "long");
					break;
				default:
					dom.addXPathProperty(element.getAttributeExpression(),
							"/" + eventType.getTypeName() + element.getXPath(), XPathConstants.STRING);
				}
			}
		}
		return dom;
	}


	/**
	 * deletes given events from event window
	 *
	 * @param events
	 */
	public void removeEvents(final List<EapEvent> events) {
		for (final EapEvent event : events) {
			this.removeEvent(event);
		}
	}

	/**
	 * delete given event from event window
	 *
	 * @param event
	 */
	public void removeEvent(final EapEvent event) {

		final StringBuffer sb = new StringBuffer();
		if (EapConfiguration.supportingOnDemandQueries) {
			sb.append("DELETE FROM " + event.getEventType().getTypeName() + "Window WHERE ");
			sb.append("(" + event.getEventType().getTimestampName() + ".getTime() = " + event.getTimestamp().getTime()
					+ ")");
			final Iterator<String> iterator = event.getValues().keySet().iterator();
			while (iterator.hasNext()) {
				final String key = iterator.next();
				final Serializable value = event.getValues().get(key);
				if (event.getEventType().getValueTypeTree().getAttributeByExpression(key).getType() == AttributeTypeEnum.STRING) {
					String stringValue = value.toString();
					if (stringValue.contains("\'")) {
						stringValue = stringValue.replaceAll("\'", "\\\\\\\\\'");
					}
					sb.append(" AND (" + key + " = '" + stringValue + "')");
				} else if (event.getEventType().getValueTypeTree().getAttributeByExpression(key).getType() == AttributeTypeEnum.DATE) {
					sb.append(" AND (" + key + ".getTime() = " + ((Date) value).getTime() + ")");
				} else {
					sb.append(" AND (" + key + " = " + value + ")");
				}
			}
			final EPStatementObjectModel statement = this.esperServiceProvider.getEPAdministrator().compileEPL(
					sb.toString());
			this.esperRuntime.executeQuery(statement);
		}
	}

	/**
	 * deletes Eventype from Esper
	 *
	 * @param eventType
	 * @throws Exception
	 */
	public void removeEventType(final EapEventType eventType) {
		// System.out.println(esperConfiguration.getEventTypeNameUsedBy(eventType.getTypeName()));
		final Set<String> names = new HashSet<String>(this.esperConfiguration.getEventTypeNameUsedBy(eventType
				.getTypeName()));
		for (final String statementName : names) {
			final EPStatement statement = this.getStatement(statementName);
			if (statement != null) {
				statement.removeAllListeners();
				statement.destroy();
			}
			final Object ruleOrQuery = this.statementNames.get(statementName);
			if (ruleOrQuery != null) {
				if (ruleOrQuery instanceof QueryWrapper) {
					final QueryWrapper liveQuery = (QueryWrapper) ruleOrQuery;
					liveQuery.remove();
				} else if (ruleOrQuery instanceof TransformationRule) {
					final TransformationRule rule = (TransformationRule) ruleOrQuery;
					Broker.getInstance().remove(rule);
				} else {
					System.err.println("WARNING - Parent of statement '" + statementName
							+ "' is neither transformation rule nor live query.");
				}
				this.statementNames.remove(statementName);
			}
		}
		this.esperConfiguration.removeEventType(eventType.getTypeName(), true);
	}

	/**
	 * registers live query to Esper and starts a listener to it
	 *
	 * @param liveQuery
	 * @return Listener which will get notifications if live-query gets
	 *         triggered
	 */
	public LiveQueryListener addLiveQuery(final QueryWrapper liveQuery) throws EPException {
		final String statementName = ++this.statementID + "_" + liveQuery.getTitle();
		liveQuery.setStatementName(statementName);
		final EPStatement newStatement = this.esperServiceProvider.getEPAdministrator().createEPL(
				liveQuery.getEsperQuery(), statementName);
		final LiveQueryListener listener = new LiveQueryListener(liveQuery);
		newStatement.addListener(listener);
		this.statementNames.put(statementName, liveQuery);
		return listener;
	}

	public PatternQueryListener addPatternQuery(final PatternQuery patternQuery) throws EPException {
		final EPStatement newStatement = this.esperServiceProvider.getEPAdministrator().createEPL(
				patternQuery.getEsperQuery());
		final LiveQueryListener listener = new PatternQueryListener(patternQuery);
		newStatement.addListener(listener);
		patternQuery.setEPStatement(newStatement);
		return (PatternQueryListener) listener;
	}

	public PatternQueryListener updatePatternQuery(final PatternQuery patternQuery) throws EPException {
		// Erstes altes Statement löschen
		this.esperServiceProvider.getEPAdministrator().getStatement(patternQuery.getEPStatement().getName()).destroy();

		final EPStatement newStatement = this.esperServiceProvider.getEPAdministrator().createEPL(
				patternQuery.getEsperQuery());
		newStatement.addListener(patternQuery.getListener());
		patternQuery.setEPStatement(newStatement);
		return patternQuery.getListener();
	}

	/**
	 * removes query from Esper
	 *
	 * @param query
	 * @return
	 */
	public void remove(final QueryWrapper query) {
		final EPStatement statement = this.getStatement(query.getStatementName());
		if (statement != null) {
			statement.removeAllListeners();
			statement.destroy();
		}

		this.statementNames.remove(query.getStatementName());
	}

	public LiveQueryListener getListenerByQuery(final QueryWrapper query) {
		final EPStatement statement = this.getStatement(query.getStatementName());
		if (statement != null) {
			final Iterator<UpdateListener> listeners = statement.getUpdateListeners();
			return (LiveQueryListener) listeners.next();
		}
		return null;
	}

	public EPRuntime getEsperRuntime() {
		return this.esperRuntime;
	}

	public void setEsperRuntime(final EPRuntime esperRuntime) {
		this.esperRuntime = esperRuntime;
	}

	/**
	 * return the names of the current active windows
	 *
	 * @return
	 */
	public String[] getWindowNames() {
		final String[] names = this.esperServiceProvider.getNamedWindowService().getNamedWindows();
		return names;
	}

	/**
	 * checks if window already exists
	 *
	 * @param windowName
	 * @return
	 */
	public boolean hasWindow(final String windowName) {
		return this.esperServiceProvider.getNamedWindowService().isNamedWindow(windowName);
	}

	public EPAdministrator getEsperAdministrator() {
		return this.esperServiceProvider.getEPAdministrator();
	}

	/**
	 * (doc from the delegated method {@link EPAdministrator.getStatement(name)}
	 * ) Returns the statement by the given statement name. Returns null if a
	 * statement of that name has not been created, or if the statement by that
	 * name has been destroyed.
	 *
	 * @param name
	 *            is the statement name to return the statement for
	 * @return statement for the given name, or null if no such started or
	 *         stopped statement exists
	 */
	public EPStatement getStatement(final String name) {
		return this.getEsperAdministrator().getStatement(name);
	}

	/**
	 * Registers a transformation rule with Esper and binds a listener to its
	 * event type. Also stores the statement name.
	 *
	 * Does not check, if the statement already exists. Does not save the rule
	 * into the database.
	 *
	 * @param transformationRule
	 */
	public void addTransformationRule(final TransformationRule transformationRule) {
		final EPStatement newStatement = this.esperServiceProvider.getEPAdministrator().createEPL(
				transformationRule.getEsperQuery(), TransformationRuleLogic.generateStatementName(transformationRule));
		final TransformationListener listener = new TransformationListener(transformationRule.getEventType());
		newStatement.addListener(listener);
		this.statementNames.put(newStatement.getName(), transformationRule);
	}

	/**
	 * Finds {@link EPStatement} for {@link TransformationRule}, removes the
	 * listener and destroys the statement. The statement is also removed from
	 * the map of statments.
	 *
	 * @param transformationRule
	 */
	public void removeTransformationRule(final TransformationRule transformationRule) {
		final EPStatement statement = this.getStatement(TransformationRuleLogic
				.generateStatementName(transformationRule));
		if (statement != null) {
			statement.removeAllListeners();
			statement.destroy();
		}
		this.statementNames.remove(statement);
	}
}
