/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.persistence.annotations.Index;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.event.collection.EventTransformationElement;
import de.hpi.unicorn.event.collection.TransformationTree;
import de.hpi.unicorn.notification.NotificationForEvent;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.utils.ConversionUtils;
import de.hpi.unicorn.utils.DateUtils;
import de.hpi.unicorn.utils.XMLUtils;
import de.hpi.unicorn.visualisation.TimePeriodEnum;

/**
 * Representation of an event
 */
@Entity
@MappedSuperclass
@Table(name = "Event")
public class EapEvent extends Persistable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected int ID;

	@Temporal(TemporalType.TIMESTAMP)
	protected Date timestamp = null;

	@ElementCollection
	@MapKeyColumn(name = "ATTRIBUTE")
	@Column(name = "VALUE", length = 5000)
	@CollectionTable(name = "EventValues", joinColumns = @JoinColumn(name = "EVENT_ID"))
	protected Map<String, String> values;

	@Transient
	protected TransformationTree<String, Serializable> valueTree;

	@ManyToMany(mappedBy = "events")
	List<CorrelationProcessInstance> processInstances;

	@Basic
	protected String semanticEventID;

	@Index
	@ManyToOne
	private EapEventType eventType;

	/**
	 * Default-Constructor for JPA.
	 */
	public EapEvent() {
		this.ID = 0;
		this.eventType = null;
		this.timestamp = null;
		this.values = new HashMap<String, String>();
		this.valueTree = null;
		this.processInstances = new ArrayList<CorrelationProcessInstance>();
	}

	/**
	 * Creates an event with a timestamp
	 * 
	 * @param timestamp
	 */
	private EapEvent(final Date timestamp) {
		this();
		this.timestamp = timestamp;
	}

	/**
	 * Creates an event with event type and timestamp.
	 * 
	 * @param eventType
	 * @param timestamp
	 */
	public EapEvent(final EapEventType eventType, final Date timestamp) {
		this(timestamp);
		this.eventType = eventType;
	}

	/**
	 * Creates an event with timestamp and attributes.
	 * 
	 * @param timestamp
	 * @param values
	 */
	public EapEvent(final Date timestamp, final Map<String, Serializable> values) {
		this(timestamp);
		this.values = ConversionUtils.getValuesConvertedToString(values);
	}

	/**
	 * Creates an event with event type, timestamp and attributes.
	 * 
	 * @param eventType
	 * @param timestamp
	 * @param values
	 */
	public EapEvent(final EapEventType eventType, final Date timestamp, final Map<String, Serializable> values) {
		this(eventType, timestamp);
		this.values = ConversionUtils.getValuesConvertedToString(values);
	}

	public boolean addProcessInstance(final CorrelationProcessInstance processInstance) {
		if (!this.processInstances.contains(processInstance)) {
			return this.processInstances.add(processInstance);
		}
		return false;
	}

	public boolean addAllProcessInstances(final List<CorrelationProcessInstance> processInstances) {
		boolean allInserted = true;
		boolean inserted;
		for (final CorrelationProcessInstance processInstance : processInstances) {
			inserted = this.addProcessInstance(processInstance);
			allInserted = allInserted ? inserted : false;
		}
		return allInserted;
	}

	public boolean removeProcessInstance(final CorrelationProcessInstance processInstance) {
		for (final CorrelationProcessInstance pi : this.processInstances) {
			if (processInstance.getID() == pi.getID()) {
				return this.processInstances.remove(pi);
			}
		}
		return false;
	}

	@Override
	public boolean equals(final Object o) {
		EapEvent e;
		if (o instanceof EapEvent) {
			e = (EapEvent) o;
			if (this.ID == e.getID() && this.timestamp.compareTo(e.getTimestamp()) == 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		final StringBuffer eventText = new StringBuffer();
		eventText.append("Event with ID = " + this.ID);
		eventText.append(", timestamp = " + this.timestamp.toString());
		eventText.append(", event type = " + this.eventType);
		final Map<String, Serializable> values = this.getValues();
		final Iterator<String> valueIterator = values.keySet().iterator();
		while (valueIterator.hasNext()) {
			final String valueKey = valueIterator.next();
			eventText.append(", " + valueKey + "=" + values.get(valueKey));
		}
		return eventText.toString();
	}

	public String shortenedString() {
		final StringBuffer eventText = new StringBuffer();
		eventText.append("Event with ID = " + this.ID);
		eventText.append(", event type = " + this.eventType);
		return eventText.toString();
	}

	public String fullEvent() {
		String eventText = "Event with ID = " + this.ID + ", timestamp = " + this.timestamp.toString();
		eventText = eventText + System.getProperty("line.separator") + this.getValues();
		return eventText;
	}

	public boolean isHierarchical() {
		// return values.isHierarchical();
		for (final String key : this.values.keySet()) {
			if (key.contains(".")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * set the Eventtyp of all given Events to the specified Eventtyp
	 * 
	 * @param events
	 * @param eventType
	 * @return
	 */
	public static List<EapEvent> setEventType(final List<EapEvent> events, final EapEventType eventType) {
		for (final EapEvent event : events) {
			event.setEventType(eventType);
		}
		return events;
	}

	// Getter and Setter

	public Map<String, Serializable> getValues() {
		// because values is of type IndirectMap (JPA)
		// return new HashMap<String, Serializable>(values);
		return ConversionUtils.getValuesConvertedToSerializable(this.eventType, this.values);
	}

	public Map<String, String> getValuesForExport() {
		return this.values;
	}

	public void setValues(final Map<String, Serializable> values) {
		this.values = ConversionUtils.getValuesConvertedToString(values);
	}

	public void setValuesWithoutConversion(final Map<String, String> values) {
		this.values = values;
	}

	public Date getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	public EapEventType getEventType() {
		return this.eventType;
	}

	public void setEventType(final EapEventType eventType) {
		this.eventType = eventType;
	}

	public List<CorrelationProcessInstance> getProcessInstances() {
		// because processInstances is of type IndirectList (JPA)
		return new ArrayList<CorrelationProcessInstance>(this.processInstances);
	}

	public void setProcessInstances(final List<CorrelationProcessInstance> processInstance) {
		this.processInstances = processInstance;
	}

	// JPA-Methods

	/**
	 * Method returns events, where the specified column name has the specified
	 * value.
	 */
	private static List<EapEvent> findByAttribute(final String columnName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM Event " + "WHERE " + columnName + " = '" + value + "'", EapEvent.class);
		return query.getResultList();
	}

	public static List<EapEvent> findByEventTypeAndAttributeExpressionsAndValues(final EapEventType eventType,
			final Map<String, Serializable> attributeExpressionsAndValues) {
		// StringBuffer sb = new StringBuffer();
		// sb.append("" +
		// "SELECT * FROM Event JOIN TransformationMapTree_TransformationMapTreeElements mte "
		// +
		// "ON mte.TransformationMapTree_TransformationMapID=Event.MapTreeID " +
		// "JOIN TransformationMapTree_TransformationMapTreeRootElements mtre "
		// +
		// "ON mtre.TransformationMapTree_TransformationMapID = Event.MapTreeID "
		// +
		// "JOIN EventTransformationElement me " +
		// "ON (me.ID = mtre.treeRootElements_ID OR me.ID = mtre.treeRootElements_ID) "
		// +
		// "WHERE EVENTTYPE_ID = '" + eventType.getID() + "'");
		// Iterator<String> iterator =
		// attributeExpressionsAndValues.keySet().iterator();
		// if (iterator.hasNext()) {
		// sb.append(" AND ");
		// }
		// while (iterator.hasNext()) {
		// String attributeExpression = iterator.next();
		// Serializable value =
		// attributeExpressionsAndValues.get(attributeExpression);
		// if (value instanceof Date) {
		// sb.append("(me.MapKey = '" + attributeExpression +
		// "' AND me.MapValue = {ts '" + (new
		// SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format((Date) value) +
		// "'})");
		// } else {
		// sb.append("(me.MapKey = '" + attributeExpression +
		// "' AND me.MapValue = '" + value + "')");
		// }
		// if (iterator.hasNext()) {
		// sb.append(" OR ");
		// }
		// }
		// Query query =
		// Persistor.getEntityManager().createNativeQuery(sb.toString(),
		// EapEvent.class);
		final StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM Event WHERE EVENTTYPE_ID = '" + eventType.getID() + "'");
		final Iterator<String> iterator = attributeExpressionsAndValues.keySet().iterator();
		while (iterator.hasNext()) {
			final String attributeExpression = iterator.next();
			final Serializable value = attributeExpressionsAndValues.get(attributeExpression);
			sb.append(" AND ID IN (SELECT EVENT_ID FROM EventValues WHERE ATTRIBUTE = '" + attributeExpression + "'");
			if (value instanceof Date) {
				sb.append(" AND VALUE = '" + DateUtils.getFormatter().format((Date) value) + "')");
			} else {
				sb.append(" AND VALUE = '" + value + "')");
			}
		}
		final Query query = Persistor.getEntityManager().createNativeQuery(sb.toString(), EapEvent.class);
		return query.getResultList();
	}

	public static Serializable findValueByEventTypeAndAttributeExpressionsAndValues(final EapEventType eventType,
			final String attributeExpressionOfValue,
			final Map<String, Serializable> attributeExpressionsAndValuesForSearch) {
		// TODO: multiple events can be found - value of first event is returned
		final StringBuffer sb = new StringBuffer();
		sb.append("SELECT v.VALUE FROM Event e INNER JOIN EventValues v ON (e.ID = v.EVENT_ID) "
				+ "WHERE e.EVENTTYPE_ID = '" + eventType.getID() + "' AND v.ATTRIBUTE = '" + attributeExpressionOfValue
				+ "'");

		final Iterator<String> iterator = attributeExpressionsAndValuesForSearch.keySet().iterator();
		while (iterator.hasNext()) {
			final String attributeExpression = iterator.next();
			final Serializable value = attributeExpressionsAndValuesForSearch.get(attributeExpression);
			sb.append(" AND e.ID IN (SELECT EVENT_ID FROM EventValues WHERE ATTRIBUTE = '" + attributeExpression + "'");
			if (value instanceof Date) {
				sb.append(" AND VALUE = '" + DateUtils.getFormatter().format((Date) value) + "')");
			} else {
				sb.append(" AND VALUE = '" + value + "')");
			}
		}

		final Query query = Persistor.getEntityManager().createNativeQuery(sb.toString());
		if (query.getResultList().isEmpty()) {
			return null;
		} else {
			final TypeTreeNode attribute = eventType.getValueTypeTree().getAttributeByExpression(
					attributeExpressionOfValue);
			final String result = query.getResultList().get(0).toString();
			if (attribute.getType() != null) {
				switch (attribute.getType()) {
				case DATE:
					return DateUtils.parseDate(result);
				case FLOAT:
					return Double.parseDouble(result);
				case INTEGER:
					return Long.parseLong(result);
				default:
					return result;
				}
			} else {
				return null;
			}

		}
	}

	public static List<Serializable> findValuesByEventTypeAndAttributeExpression(final EapEventType eventType,
			final String attributeExpression) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT DISTINCT v.VALUE FROM Event e INNER JOIN EventValues v ON (e.ID = v.EVENT_ID) "
						+ "WHERE e.EVENTTYPE_ID = '" + eventType.getID() + "' AND v.ATTRIBUTE = '"
						+ attributeExpression + "'");
		if (query.getResultList().isEmpty()) {
			return null;
		} else {
			final List<String> queryResult = query.getResultList();
			final List<Serializable> result = new ArrayList<Serializable>();
			final TypeTreeNode attribute = eventType.getValueTypeTree().getAttributeByExpression(attributeExpression);
			if (attribute.getType() != null) {
				switch (attribute.getType()) {
				case DATE:
					for (final String value : queryResult) {
						result.add(DateUtils.parseDate(value));
					}
					break;
				case INTEGER:
					for (final String value : queryResult) {
						result.add(Long.parseLong(value));
					}
					break;
				case FLOAT:
					for (final String value : queryResult) {
						result.add(Double.valueOf(value));
					}
					break;
				default:
					for (final String value : queryResult) {
						result.add(value);
					}
					break;
				}
			}
			return result;
		}
	}

	private static List<EapEvent> findByAttributeGreaterThan(final String columnName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM Event " + "WHERE " + columnName + " > '" + value + "'", EapEvent.class);
		return query.getResultList();
	}

	private static List<EapEvent> findByAttributeLessThan(final String columnName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM Event " + "WHERE " + columnName + " < '" + value + "'", EapEvent.class);
		return query.getResultList();
	}

	/**
	 * returns EapEvents which have the specified attribute/value pair
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static List<EapEvent> findByValue(final String key, final Serializable value) {
		final Map<String, Serializable> attributesAndValues = new HashMap<String, Serializable>();
		attributesAndValues.put(key, value);
		return EapEvent.findByValues(attributesAndValues);
	}

	/**
	 * returns EapEvents which have all spezified attribute/value pairs
	 * 
	 * @param eventAttributes
	 * @return
	 */
	public static List<EapEvent> findByValues(final Map<String, Serializable> attributeExpressionsAndValues) {
		final StringBuffer sb = new StringBuffer();
		sb.append("SELECT * FROM Event");
		final Iterator<String> iterator = attributeExpressionsAndValues.keySet().iterator();
		if (iterator.hasNext()) {
			sb.append(" WHERE");
		}
		while (iterator.hasNext()) {
			final String attributeExpression = iterator.next();
			final Serializable value = attributeExpressionsAndValues.get(attributeExpression);
			sb.append(" ID IN (SELECT EVENT_ID FROM EventValues WHERE ATTRIBUTE = '" + attributeExpression + "'");
			if (value instanceof Date) {
				sb.append(" AND VALUE = '" + DateUtils.getFormatter().format((Date) value) + "')");
			} else {
				sb.append(" AND VALUE = '" + value + "')");
			}
			if (iterator.hasNext()) {
				sb.append(" AND");
			}
		}
		final Query query = Persistor.getEntityManager().createNativeQuery(sb.toString(), EapEvent.class);
		return query.getResultList();
	}

	/**
	 * returns EapEvents from the given EapEventtyp
	 * 
	 * @param eventType
	 * @return
	 */
	public static List<EapEvent> findByEventType(final EapEventType eventType) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM Event WHERE EVENTTYPE_ID = '" + eventType.getID() + "'", EapEvent.class);
		return query.getResultList();
	}

	/**
	 * returns EapEvents from the given EapEventtyp and have a timestamp which
	 * lies maximal "period" in the past
	 * 
	 * @param eventType
	 * @param period
	 * @return
	 */
	public static List<EapEvent> findByEventTypeAndTime(final EapEventType eventType, final TimePeriodEnum period) {
		if (period == TimePeriodEnum.INF) {
			return EapEvent.findByEventType(eventType);
		}
		final Date start = period.getStartTime();
		return EapEvent.findBetween(start, new Date(), eventType);
	}

	/**
	 * returns number EapEvents from the given Eventtyp
	 * 
	 * @param eventType
	 * @return
	 */
	public static long getNumberOfEventsByEventType(final EapEventType eventType) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT count(*) FROM Event WHERE EVENTTYPE_ID = '" + eventType.getID() + "'");
		final long value = (Long) query.getSingleResult();

		return value;
	}

	/**
	 * Returns the number of events in the database.
	 * 
	 * @return overall number of EapEvents
	 */
	public static long getNumberOfEvents() {
		final Query query = Persistor.getEntityManager().createNativeQuery("SELECT count(*) FROM Event");
		final long value = (Long) query.getSingleResult();

		return value;
	}

	/**
	 * returns EapEvents which belongs to the specified processInstance
	 * 
	 * @param processInstance
	 * @return
	 */
	public static List<EapEvent> findByProcessInstance(final CorrelationProcessInstance processInstance) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "Select * " + "FROM Event " + "WHERE ID IN (" + "Select events_ID "
						+ "FROM ProcessInstance_Event " + "WHERE processInstances_ID = '" + processInstance.getID()
						+ "')", EapEvent.class);
		return query.getResultList();
	}

	/**
	 * returns EapEvent with the specified ID
	 * 
	 * @param ID
	 * @return
	 */
	public static EapEvent findByID(final int ID) {
		final List<EapEvent> events = EapEvent.findByAttribute("ID", Integer.toString(ID));
		if (!events.isEmpty()) {
			return events.get(0);
		} else {
			return null;
		}
	}

	/**
	 * returns Events which have an ID greater than the given ID
	 * 
	 * @param ID
	 * @return
	 */
	public static List<EapEvent> findByIDGreaterThan(final int ID) {
		return EapEvent.findByAttributeGreaterThan("ID", Integer.toString(ID));
	}

	/**
	 * returns Events which have an ID less than the given ID
	 * 
	 * @param ID
	 * @return
	 */
	public static List<EapEvent> findByIDLessThan(final int ID) {
		return EapEvent.findByAttributeLessThan("ID", Integer.toString(ID));
	}

	/**
	 * returns Events which have a timestamp between the given Dates
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static List<EapEvent> findBetween(final Date startDate, final Date endDate) {
		return EapEvent.findBetween(startDate, endDate, null);
	}

	/**
	 * returns events from the given event type having a timestamp between the
	 * given dates
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<EapEvent> findBetween(final Date startDate, final Date endDate, final EapEventType eventType) {
		Query query;
		if (eventType != null) {
			query = Persistor.getEntityManager().createNativeQuery(
					"SELECT * FROM Event WHERE TIMESTAMP BETWEEN '" + DateUtils.getFormatter().format(startDate)
							+ "' AND '" + DateUtils.getFormatter().format(endDate) + "' AND EVENTTYPE_ID ='"
							+ eventType.getID() + "'", EapEvent.class);
		} else {
			query = Persistor.getEntityManager().createNativeQuery(
					"SELECT * FROM Event WHERE TIMESTAMP BETWEEN '" + DateUtils.getFormatter().format(startDate)
							+ "' AND '" + DateUtils.getFormatter().format(endDate) + "'", EapEvent.class);
		}
		return query.getResultList();
	}

	/**
	 * @return all EapEvents
	 */
	public static List<EapEvent> findAll() {
		final Query q = Persistor.getEntityManager().createNativeQuery("SELECT * FROM Event", EapEvent.class);
		return q.getResultList();
	}

	/**
	 * @return all EapEvents
	 */
	public static List<EapEvent> findAllUI() {
		final Query q = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM Event ORDER BY TIMESTAMP DESC LIMIT 0,10000", EapEvent.class);
		return q.getResultList();
	}

	@Override
	public EapEvent save() {
		return (EapEvent) super.save();
	}

	@Override
	public EapEvent merge() {
		return (EapEvent) super.merge();
	}

	/**
	 * saves the given EapEvents
	 * 
	 * @param events
	 * @return
	 */
	public static List<EapEvent> save(final List<EapEvent> events) {
		try {
			final EntityManager entityManager = Persistor.getEntityManager();
			entityManager.getTransaction().begin();
			for (final EapEvent event : events) {
				entityManager.persist(event);
			}
			entityManager.getTransaction().commit();
			return events;
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public EapEvent remove() {
		// remove from process instances
		for (final CorrelationProcessInstance processInstance : this.getProcessInstances()) {
			processInstance.removeEvent(this);
			if (processInstance.getTimerEvent() != null && processInstance.getTimerEvent().equals(this)) {
				processInstance.setTimerEvent(null);
				if (processInstance.getProcess().getTimeCondition() != null) {
					processInstance.getProcess().getTimeCondition().removeTimerEvent(this);
					processInstance.getProcess().getTimeCondition().merge();
				}
			}
			processInstance.merge();
		}
		// remove notifications for this event
		for (final NotificationForEvent notification : NotificationForEvent.findForEvent(this)) {
			notification.remove();
		}

		return (EapEvent) super.remove();
	}

	/**
	 * Deletes the specified events from the database.
	 * 
	 * @return
	 */
	public static boolean remove(final ArrayList<EapEvent> events) {
		boolean removed = true;
		for (final EapEvent event : events) {
			removed = (event.remove() != null);
		}
		return removed;
	}

	/**
	 * deletes all EapEvents
	 */
	public static void removeAll() {
		for (final EapEvent actualEvent : EapEvent.findAll()) {
			actualEvent.remove();
		}
	}

	public static List<String> findAllEventAttributes() {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT DISTINCT v.ATTRIBUTE FROM Event e INNER JOIN EventValues v ON (e.ID = v.EVENT_ID)");
		return query.getResultList();
	}

	/**
	 * returns distinct values of the given attribute of the given eventtyp
	 */
	public static List<String> findDistinctValuesOfAttributeOfType(final String attributeExpression,
			final EapEventType eventType) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT DISTINCT v.VALUE FROM Event e INNER JOIN EventValues v ON (e.ID = v.EVENT_ID) "
						+ "WHERE e.EVENTTYPE_ID = '" + eventType.getID() + "' AND v.ATTRIBUTE = '"
						+ attributeExpression + "'");
		return query.getResultList();
	}

	/**
	 * return the number of the repetition of the value in the specified
	 * eventtyp and attribute
	 */
	public static long findNumberOfAppearancesByAttributeValue(final String attributeExpression, final String value,
			final EapEventType eventType) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT count(DISTINCT e.ID) FROM Event e INNER JOIN EventValues v ON (e.ID = v.EVENT_ID) "
						+ "WHERE e.EVENTTYPE_ID = '" + eventType.getID() + "' AND " + "v.ATTRIBUTE = '"
						+ attributeExpression + "' AND v.VALUE = '" + value + "'");
		return (long) query.getSingleResult();
	}

	/**
	 * returns minimal value of the given attribute in the specified eventtyp
	 */
	@SuppressWarnings("null")
	public static long getMinOfAttributeValue(final String attribute, final EapEventType eventType) {
		final List<String> values = EapEvent.findDistinctValuesOfAttributeOfType(attribute, eventType);
		if (values.size() > 0) {
			long min = Long.parseLong(values.get(0));
			for (final String value : values) {
				final long actual = Long.parseLong(value);
				if (actual < min) {
					min = actual;
				}
			}
			return min;
		}
		return (Long) null;
	}

	/**
	 * returns maximal value of the given attribute in the specified eventtyp
	 */
	@SuppressWarnings("null")
	public static long getMaxOfAttributeValue(final String attribute, final EapEventType eventType) {
		final List<String> values = EapEvent.findDistinctValuesOfAttributeOfType(attribute, eventType);
		if (values.size() > 0) {
			long max = Long.parseLong(values.get(0));
			for (final String value : values) {
				final long actual = Long.parseLong(value);
				if (actual > max) {
					max = actual;
				}
			}
			return max;
		}
		return (Long) null;
	}

	public Document getTimestampAsNode() {
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = domFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		// need document from xml for the xml parser
		final Document doc = builder.newDocument();
		final Element root = doc.createElement(this.eventType.getTimestampName().replaceAll(" ", ""));
		root.setTextContent(XMLUtils.getFormattedDate(this.timestamp));
		doc.appendChild(root);
		return doc;
	}

	public TransformationTree<String, Serializable> getValueTree() {
		if (this.valueTree == null) {
			this.valueTree = new TransformationTree<String, Serializable>();
			EventTransformationElement<String, Serializable> parentElement, element;
			final List<EventTransformationElement<String, Serializable>> treeRootElements = new ArrayList<EventTransformationElement<String, Serializable>>();
			final List<EventTransformationElement<String, Serializable>> treeElements = new ArrayList<EventTransformationElement<String, Serializable>>();
			for (final String attributeExpression : this.getValues().keySet()) {
				parentElement = null;
				element = null;
				final Serializable value = this.getValues().get(attributeExpression);
				final Iterator<String> hierarchicalOrderIterator = Arrays.asList(attributeExpression.split("\\."))
						.iterator();
				String key = hierarchicalOrderIterator.next();
				for (final EventTransformationElement<String, Serializable> root : treeRootElements) {
					if (root.getKey().equals(key)) {
						parentElement = root;
						break;
					}
				}
				if (parentElement == null) {
					parentElement = new EventTransformationElement<String, Serializable>(key, value);
					treeRootElements.add(parentElement);
					treeElements.add(parentElement);
				}
				while (hierarchicalOrderIterator.hasNext()) {
					element = null;
					parentElement.setValue(null);
					key = hierarchicalOrderIterator.next();
					final List<EventTransformationElement<String, Serializable>> children = parentElement.getChildren();
					for (final EventTransformationElement<String, Serializable> child : children) {
						if (child.getKey().equals(key)) {
							element = child;
							break;
						}
					}
					if (element == null) {
						element = new EventTransformationElement<String, Serializable>(parentElement, key, value);
						treeElements.add(element);
					}
					parentElement = element;
				}
			}
			this.valueTree.setTreeRootElements(treeRootElements);
			this.valueTree.setTreeElements(treeElements);
		}
		return this.valueTree;
	}

	public void setSemanticEventID(final String semanticEventID) {
		this.semanticEventID = semanticEventID;
	}

	public String getSemanticEventID() {
		return this.semanticEventID;
	}
}
