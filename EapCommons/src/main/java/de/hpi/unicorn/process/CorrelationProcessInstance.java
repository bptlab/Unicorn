/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.correlation.CorrelationRule;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.utils.ConversionUtils;
import de.hpi.unicorn.utils.DateUtils;

/**
 * Represents an instance of a process. It is unique per process by the values
 * of the correlation attributes. References the events belonging to the process
 * instance and holds a timer event if correlation over time is enabled.
 */
@Entity
@Table(name = "ProcessInstance")
public class CorrelationProcessInstance extends Persistable implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	private int ID;

	@ManyToMany(cascade = CascadeType.MERGE)
	private List<EapEvent> events;

	// @ManyToOne(cascade=CascadeType.ALL)
	// @JoinColumn(name="MapTreeID")
	// private TransformationTree<String, Serializable>
	// correlationAttributesAndValues;

	@ElementCollection
	@MapKeyColumn(name = "ATTRIBUTE")
	@Column(name = "VALUE")
	@CollectionTable(name = "CorrelationValues", joinColumns = @JoinColumn(name = "PROCESSINSTANCE_ID"))
	// @OneToMany(cascade={CascadeType.PERSIST, CascadeType.REMOVE})
	// @MapKeyColumn(name="attributeExpression")
	protected Map<String, String> correlationAttributesAndValues;

	@OneToOne(cascade = CascadeType.MERGE)
	private EapEvent timerEvent;

	@Column(name = "progress")
	private int progress;

	/**
	 * JPA-default constructor.
	 */
	public CorrelationProcessInstance() {
		this.ID = 0;
		this.events = new ArrayList<EapEvent>();
		this.correlationAttributesAndValues = new HashMap<String, String>();
	}

	/**
	 * Constructor
	 * 
	 * @param correlationAttributesAndValues
	 *            map of attribute keys and values - key must be the expression
	 *            of the attribute (e.g. location for a root attribute,
	 *            vehicle_information.railway for a second level attribute with
	 *            vehicle_information as parent)
	 */
	public CorrelationProcessInstance(final Map<String, Serializable> correlationAttributesAndValues) {
		this();
		this.correlationAttributesAndValues = ConversionUtils
				.getValuesConvertedToString(correlationAttributesAndValues);
	}

	/**
	 * Returns the associated {@link EapEvent}s.
	 * 
	 * @return
	 */
	public List<EapEvent> getEvents() {
		return this.events;
	}

	/**
	 * Sets the associated {@link EapEvent}s.
	 * 
	 * @return
	 */
	public void setEvents(final List<EapEvent> events) {
		this.events = events;
	}

	/**
	 * Adds an {@link EapEvent} to the associated events.
	 * 
	 * @return
	 */
	public boolean addEvent(final EapEvent event) {
		if (!this.events.contains(event)) {
			return this.events.add(event);
		}
		return false;
	}

	/**
	 * Removes an {@link EapEvent} from the associated events.
	 * 
	 * @return
	 */
	public boolean removeEvent(final EapEvent event) {
		return this.events.remove(event);
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	/**
	 * Must not add values to the returned map - use
	 * addCorrelationAttributeAndValue(...) instead!
	 * 
	 * @return
	 */
	public Map<String, Serializable> getCorrelationAttributesAndValues() {
		List<TypeTreeNode> attributes = new ArrayList<TypeTreeNode>();
		if (this.getProcess().getCorrelationAttributes() != null
				&& !this.getProcess().getCorrelationAttributes().isEmpty()) {
			attributes = this.getProcess().getCorrelationAttributes();
		} else { // using correlation rules
			for (final CorrelationRule rule : this.getProcess().getCorrelationRules()) {
				attributes.add(rule.getFirstAttribute());
				attributes.add(rule.getSecondAttribute());
			}
		}
		return ConversionUtils.getValuesConvertedToSerializable(attributes, this.correlationAttributesAndValues);
	}

	public void addCorrelationAttributeAndValue(final String correlationAttribute, final Serializable correlationValue) {
		final Map<String, Serializable> valuesToBeConverted = new HashMap<String, Serializable>();
		valuesToBeConverted.put(correlationAttribute, correlationValue);
		final Map<String, String> valuesToBeAdded = ConversionUtils.getValuesConvertedToString(valuesToBeConverted);
		this.correlationAttributesAndValues.putAll(valuesToBeAdded);
	}

	public void addCorrelationAttributesAndValues(final Map<String, Serializable> correlationAttributesAndValues) {
		final Map<String, String> valuesToBeAdded = ConversionUtils
				.getValuesConvertedToString(correlationAttributesAndValues);
		this.correlationAttributesAndValues.putAll(valuesToBeAdded);
	}

	public void setCorrelationAttributesAndValues(final Map<String, Serializable> correlationAttributesAndValues) {
		this.correlationAttributesAndValues = ConversionUtils
				.getValuesConvertedToString(correlationAttributesAndValues);
	}

	/**
	 * Returns the {@link CorrelationProcess} for this instance of it from the
	 * database.
	 * 
	 * @return
	 */
	public CorrelationProcess getProcess() {
		final List<CorrelationProcess> processes = CorrelationProcess.findByProcessInstanceID(this.ID);
		if (!processes.isEmpty()) {
			return CorrelationProcess.findByProcessInstanceID(this.ID).get(0);
		}
		return null;
	}

	/**
	 * Returns all {@link CorrelationProcessInstance}s from the database.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CorrelationProcessInstance> findAll() {
		final Query q = Persistor.getEntityManager().createNativeQuery("SELECT * FROM ProcessInstance",
				CorrelationProcessInstance.class);
		return q.getResultList();
	}

	/**
	 * Returns all {@link CorrelationProcessInstance}s from the database, which
	 * have the given correlation attribute.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CorrelationProcessInstance> findByCorrelationAttribute(final String correlationAttribute) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM ProcessInstance " + "WHERE ID IN "
						+ "(SELECT PROCESSINSTANCE_ID IN CorrelationValues WHERE " + "ATTRIBUTE = '"
						+ correlationAttribute + "')", CorrelationProcessInstance.class);
		// "SELECT * " +
		// "FROM ProcessInstance " +
		// "WHERE MapTreeID IN (" +
		// "SELECT TransformationMapTree_TransformationMapID " +
		// "FROM TransformationMapTree_TransformationMapTreeRootElements " +
		// "WHERE treeRootElements_ID IN (" +
		// "SELECT ID " +
		// "FROM EventTransformationElement " +
		// "WHERE MapKey = '" + correlationAttribute + "'))",
		// CorrelationProcessInstance.class);
		// "Select Id " +
		// "FROM CorrelationAttributesAndValues " +
		// "WHERE CorrelationAttribute = '" + correlationAttribute + "')",
		// CorrelationProcessInstance.class);
		return query.getResultList();
	}

	/**
	 * Returns all {@link CorrelationProcessInstance}s from the database, which
	 * have the given correlation attribute and associate value.
	 * 
	 * @return
	 */
	public static List<CorrelationProcessInstance> findByCorrelationAttributeAndValue(
			final String correlationAttribute, final Serializable correlationValue) {
		String valueQueryString = new String();
		if (correlationValue instanceof Date) {
			valueQueryString = " AND VALUE = '" + DateUtils.getFormatter().format((Date) correlationValue) + "')";
		} else {
			valueQueryString = " AND VALUE = '" + correlationValue + "')";
		}
		Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM ProcessInstance" + "WHERE ID IN "
						+ "(SELECT PROCESSINSTANCE_ID IN CorrelationValues WHERE " + "ATTRIBUTE = '"
						+ correlationAttribute + "'" + valueQueryString, CorrelationProcessInstance.class);
		// "Select * FROM ProcessInstance " +
		// "WHERE MapTreeID IN (" +
		// "SELECT TransformationMapTree_TransformationMapID " +
		// "FROM TransformationMapTree_TransformationMapTreeRootElements " +
		// "WHERE treeRootElements_ID IN (" +
		// "SELECT ID " +
		// "FROM EventTransformationElement " +
		// "WHERE MapKey = '" + correlationAttribute + "' AND MapValue = '" +
		// correlationValue + "'))", CorrelationProcessInstance.class);
		// "Select Id " +
		// "FROM CorrelationAttributesAndValues " +
		// "WHERE CorrelationAttribute = '" + correlationAttribute + "')",
		// CorrelationProcessInstance.class);
		final List<CorrelationProcessInstance> returnList = new ArrayList<CorrelationProcessInstance>();
		// for (Object instance : query.getResultList()) {
		// CorrelationProcessInstance processInstance =
		// (CorrelationProcessInstance) instance;
		// if
		// (processInstance.getCorrelationAttributesAndValues().get(correlationAttribute).equals(correlationValue))
		// returnList.add(processInstance);
		// }
		return returnList;
	}

	/**
	 * Returns the {@link CorrelationProcessInstance} from the database, which
	 * has the given ID, if any.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static CorrelationProcessInstance findByID(final int ID) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * " + "FROM ProcessInstance " + "WHERE ID = '" + ID + "'",
				CorrelationProcessInstance.class);
		final List<CorrelationProcessInstance> processInstances = query.getResultList();
		if (!processInstances.isEmpty()) {
			return processInstances.get(0);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<CorrelationProcessInstance> findByIDGreaterThan(final int ID) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * " + "FROM ProcessInstance " + "WHERE ID > '" + ID + "'",
				CorrelationProcessInstance.class);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public static List<CorrelationProcessInstance> findByIDLessThan(final int ID) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * " + "FROM ProcessInstance " + "WHERE ID < '" + ID + "'",
				CorrelationProcessInstance.class);
		return query.getResultList();
	}

	/**
	 * Returns all {@link CorrelationProcessInstance}es from the database, which
	 * contain the given {@link EapEvent}.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CorrelationProcessInstance> findByContainedEvent(final EapEvent event) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * " + "FROM ProcessInstance " + "WHERE ID IN (" + "Select processInstances_ID "
						+ "FROM ProcessInstance_Event " + "WHERE events_ID = '" + event.getID() + "')",
				CorrelationProcessInstance.class);
		return query.getResultList();
	}

	/**
	 * Returns all {@link CorrelationProcessInstance}es from the database, which
	 * are associated to the given {@link CorrelationProcess}.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CorrelationProcessInstance> findByProcess(final CorrelationProcess process) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "Select * " + "FROM ProcessInstance " + "WHERE ID IN (" + "Select processInstances_ID "
						+ "FROM Process_ProcessInstance " + "WHERE CorrelationProcess_ID = '" + process.getID() + "')",
				CorrelationProcessInstance.class);
		return query.getResultList();
	}

	/**
	 * Returns all {@link CorrelationProcessInstance}es from the database, which
	 * contain the given {@link EapEventType}.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CorrelationProcessInstance> findByContainedEventType(final EapEventType eventType) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * " + "FROM ProcessInstance " + "WHERE ID IN (" + "SELECT processInstances_ID "
						+ "FROM ProcessInstance_Event " + "WHERE events_ID IN (" + "SELECT ID " + "FROM Event "
						+ "WHERE EVENTTYPE_ID = '" + eventType.getID() + "'))", CorrelationProcessInstance.class);
		return query.getResultList();
	}

	// @Override
	// public int hashCode() {
	// return ID;
	// }
	//
	// @Override
	// public boolean equals(Object o) {
	// if (!(o instanceof CorrelationProcessInstance)) {
	// return false;
	// }
	// CorrelationProcessInstance object = (CorrelationProcessInstance) o;
	// return object.getID() == this.getID();
	// }

	@Override
	public CorrelationProcessInstance save() {
		return (CorrelationProcessInstance) super.save();
	}

	public static boolean save(final ArrayList<CorrelationProcessInstance> processInstances) {
		try {
			final EntityManager entityManager = Persistor.getEntityManager();
			entityManager.getTransaction().begin();
			for (final CorrelationProcessInstance processInstance : processInstances) {
				entityManager.persist(processInstance);
			}
			entityManager.getTransaction().commit();
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes the specified process instance from the database.
	 * 
	 * @return
	 */
	@Override
	public CorrelationProcessInstance remove() {
		final List<EapEvent> events = EapEvent.findByProcessInstance(this);
		for (final EapEvent event : events) {
			event.removeProcessInstance(this);
			event.merge();
		}
		final List<CorrelationProcess> processes = CorrelationProcess.findByProcessInstance(this);
		for (final CorrelationProcess process : processes) {
			process.removeProcessInstance(this);
			process.merge();
		}
		return (CorrelationProcessInstance) super.remove();
	}

	/**
	 * Deletes the specified eventtypes from the database.
	 * 
	 * @return
	 */
	public static boolean remove(final List<CorrelationProcessInstance> processInstances) {
		boolean removed = true;
		final Iterator<CorrelationProcessInstance> iterator = processInstances.iterator();
		while (iterator.hasNext()) {
			removed = iterator.next().remove() != null;
		}
		return removed;
	}

	public static void removeAll() {
		for (final CorrelationProcessInstance actualInstance : CorrelationProcessInstance.findAll()) {
			final List<CorrelationProcess> processes = CorrelationProcess.findByProcessInstance(actualInstance);
			for (final CorrelationProcess process : processes) {
				process.removeProcessInstance(actualInstance);
				process.merge();
			}
			final List<EapEvent> events = EapEvent.findByProcessInstance(actualInstance);
			for (final EapEvent event : events) {
				event.removeProcessInstance(actualInstance);
				event.merge();
			}
		}
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM CorrelationProcessInstance");
			query.executeUpdate();
			entr.commit();
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public EapEvent getTimerEvent() {
		return this.timerEvent;
	}

	public void setTimerEvent(final EapEvent timerEvent) {
		this.timerEvent = timerEvent;
	}

	public int getProgress() {
		return this.progress;
	}

	public void setProgress(int percentage) {
		if (percentage > 100) {
			percentage = 100;
		}
		if (percentage < 0) {
			percentage = 0;
		}
		this.progress = percentage;
		this.merge();
	}

	public void addToProgress(final int percentage) {
		this.progress += percentage;
		if (this.progress > 100) {
			this.progress = 100;
		}
		if (this.progress < 0) {
			this.progress = 0;
		}
		this.merge();
	}

	@Override
	public String toString() {
		return "Process Instance " + this.ID;
	}

}
