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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.correlation.CorrelationRule;
import de.hpi.unicorn.correlation.TimeCondition;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

/**
 * This class represents a business process with bounded {@link EapEventType}s
 * and eventually a associated {@link BPMNProcess}. The process knows the
 * correlation rules under which events are assigned to concrete
 * {@link CorrelationProcessInstance}s of this process.
 * 
 * @author micha
 */
@Entity
@Table(name = "Process")
public class CorrelationProcess extends Persistable implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ID;

	@Column(name = "NAME")
	private String name;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinTable(name = "ProcessEventTypes", joinColumns = { @JoinColumn(name = "Id") })
	@JoinColumn(name = "EventTypes")
	private Set<EapEventType> eventTypes = new HashSet<EapEventType>();

	@OneToMany(fetch = FetchType.EAGER)
	private List<CorrelationProcessInstance> processInstances = new ArrayList<CorrelationProcessInstance>();

	@OneToOne(cascade = CascadeType.MERGE)
	private TimeCondition timeCondition;

	@OneToOne(cascade = CascadeType.MERGE)
	private BPMNProcess bpmnProcess;

	@OneToMany(fetch = FetchType.EAGER)
	private List<TypeTreeNode> correlationAttributes = new ArrayList<TypeTreeNode>();

	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "process")
	private Set<CorrelationRule> correlationRules = new HashSet<CorrelationRule>();

	// @OneToOne(cascade = CascadeType.MERGE)
	@Transient
	private EventTree<AbstractBPMNElement> processDecompositionTree = new EventTree<AbstractBPMNElement>();

	/**
	 * Default-Constructor for JPA.
	 */
	public CorrelationProcess() {
		this.name = "";
	}

	public CorrelationProcess(final String name) {
		this.name = name;
	}

	public CorrelationProcess(final String name, final Set<EapEventType> eventTypes) {
		this.name = name;
		this.eventTypes.addAll(eventTypes);
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int ID) {
		this.ID = ID;
	}

	public ArrayList<EapEventType> getEventTypes() {
		return new ArrayList<EapEventType>(this.eventTypes);
	}

	public List<CorrelationProcessInstance> getProcessInstances() {
		return this.processInstances;
	}

	public void setProcessInstances(final List<CorrelationProcessInstance> processInstances) {
		this.processInstances = processInstances;
	}

	public List<TypeTreeNode> getCorrelationAttributes() {
		return this.correlationAttributes;
	}

	public void setCorrelationAttributes(final List<TypeTreeNode> correlationAttributes) {
		this.correlationAttributes = correlationAttributes;
	}

	public boolean isCorrelationWithCorrelationRules() {
		return !this.correlationRules.isEmpty();
	}

	public void addCorrelationAttribute(final TypeTreeNode correlationAttribute) {
		if (!this.correlationAttributes.contains(correlationAttribute)) {
			this.correlationAttributes.add(correlationAttribute);
		}
	}

	public void addCorrelationAttributes(final List<TypeTreeNode> correlationAttributes) {
		for (final TypeTreeNode correlationAttribute : correlationAttributes) {
			this.addCorrelationAttribute(correlationAttribute);
		}
	}

	public void removeCorrelationAttribute(final TypeTreeNode correlationAttribute) {
		this.correlationAttributes.remove(correlationAttribute);
	}

	public Set<CorrelationRule> getCorrelationRules() {
		return this.correlationRules;
	}

	public void setCorrelationRules(final Set<CorrelationRule> correlationRules) {
		this.correlationRules = correlationRules;
	}

	public void addCorrelationRule(final CorrelationRule correlationRule) {
		if (!this.correlationRules.contains(correlationRule)) {
			this.correlationRules.add(correlationRule);
			correlationRule.setProcess(this);
		}
	}

	public void addCorrelationRules(final Set<CorrelationRule> correlationRules) {
		for (final CorrelationRule rule : correlationRules) {
			this.addCorrelationRule(rule);
		}
	}

	public void removeCorrelationRule(final CorrelationRule correlationRule) {
		this.correlationRules.remove(correlationRule);
		correlationRule.setProcess(null);
		correlationRule.remove();
	}

	public boolean addProcessInstance(final CorrelationProcessInstance processInstance) {
		if (!this.processInstances.contains(processInstance)) {
			return this.processInstances.add(processInstance);
		}
		return false;
	}

	public boolean removeProcessInstance(final CorrelationProcessInstance processInstance) {
		for (final CorrelationProcessInstance pi : this.processInstances) {
			if (processInstance.getID() == pi.getID()) {
				return this.processInstances.remove(pi);
			}
		}
		return false;
	}

	public void setEventTypes(final Set<EapEventType> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public boolean addEventType(final EapEventType eventType) {
		if (!this.eventTypes.contains(eventType)) {
			eventType.save();
			this.eventTypes.add(eventType);
			return true;
		}
		return false;
	}

	public boolean removeEventType(final EapEventType eventType) {
		if (!this.eventTypes.remove(eventType)) {
			// EventType als Notfallvariante per Typename und ID suchen und
			// löschen,
			// da durch JPA teilweise unterschiedliche ObjektIDs entstehen
			// können
			for (final EapEventType containedEventType : this.eventTypes) {
				if (containedEventType.getID() == eventType.getID()
						&& containedEventType.getTypeName().equals(eventType.getTypeName())) {
					return this.eventTypes.remove(containedEventType);
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		final String processText = this.name + "(" + this.ID + ")";
		return processText;
	}

	/**
	 * Returns all {@link CorrelationProcess}es from the database.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CorrelationProcess> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t FROM CorrelationProcess t");
		return q.getResultList();
	}

	/**
	 * Returns all {@link CorrelationProcess}es from the database, which have
	 * the given {@link EapEventType}.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CorrelationProcess> findByEventType(final EapEventType eventType) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "Select * " + "FROM Process " + "WHERE ID IN (" + "Select Id " + "FROM ProcessEventTypes "
						+ "WHERE eventTypes_ID = '" + eventType.getID() + "')", CorrelationProcess.class);
		return query.getResultList();
	}

	/**
	 * Returns all {@link CorrelationProcess}es from the database, which have
	 * the given attribute and associated value.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CorrelationProcess> findByAttribute(final String columnName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM Process WHERE " + columnName + " = '" + value + "'", CorrelationProcess.class);
		return query.getResultList();
	}

	/**
	 * Returns all {@link CorrelationProcess}es from the database, which have
	 * the given ID.
	 * 
	 * @return
	 */
	public static CorrelationProcess findByID(final int ID) {
		final List<CorrelationProcess> processes = CorrelationProcess.findByAttribute("ID", Integer.toString(ID));
		if (!processes.isEmpty()) {
			return processes.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Returns all {@link CorrelationProcess}es from the database, which have an
	 * ID greater than the given.
	 * 
	 * @return
	 */
	public static List<CorrelationProcess> findByIDGreaterThan(final int ID) {
		return CorrelationProcess.findByAttributeGreaterThan("ID", Integer.toString(ID));
	}

	/**
	 * Returns all {@link CorrelationProcess}es from the database, which have an
	 * ID less than the given.
	 * 
	 * @return
	 */
	public static List<CorrelationProcess> findByIDLessThan(final int ID) {
		return CorrelationProcess.findByAttributeLessThan("ID", Integer.toString(ID));
	}

	@SuppressWarnings("unchecked")
	private static List<CorrelationProcess> findByAttributeGreaterThan(final String columnName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM Process " + "WHERE " + columnName + " > '" + value + "'", CorrelationProcess.class);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	private static List<CorrelationProcess> findByAttributeLessThan(final String columnName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM Process " + "WHERE " + columnName + " < '" + value + "'", CorrelationProcess.class);
		return query.getResultList();
	}

	public static List<CorrelationProcess> findByName(final String name) {
		return CorrelationProcess.findByAttribute("NAME", name);
	}

	/**
	 * Returns all {@link CorrelationProcess}es from the database, which have
	 * the given {@link CorrelationProcessInstance}.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CorrelationProcess> findByProcessInstance(final CorrelationProcessInstance processInstance) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "Select * " + "FROM Process " + "WHERE ID IN (" + "Select CorrelationProcess_ID "
						+ "FROM Process_ProcessInstance " + "WHERE processInstances_ID = '" + processInstance.getID()
						+ "')", CorrelationProcess.class);
		return query.getResultList();
	}

	/**
	 * Returns all {@link CorrelationProcess}es from the database, which have a
	 * {@link CorrelationProcessInstance} with the given ID.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<CorrelationProcess> findByProcessInstanceID(final int ID) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "Select * " + "FROM Process " + "WHERE ID IN (" + "Select CorrelationProcess_ID "
						+ "FROM Process_ProcessInstance " + "WHERE processInstances_ID = '" + ID + "')",
				CorrelationProcess.class);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public static List<CorrelationProcess> findByTimeCondition(final TimeCondition timeCondition) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "Select * " + "FROM Process " + "WHERE TIMECONDITION_ID = '" + timeCondition.getID() + "'",
				CorrelationProcess.class);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public static CorrelationProcess findByBPMNProcess(final BPMNProcess bpmnProcess) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "Select * " + "FROM Process " + "WHERE BPMNPROCESS_ID = '" + bpmnProcess.getID() + "'",
				CorrelationProcess.class);
		final List<CorrelationProcess> processes = query.getResultList();
		if (!processes.isEmpty()) {
			return processes.get(0);
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<CorrelationProcess> findProcessesByBPMNProcess(final BPMNProcess bpmnProcess) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "Select * " + "FROM Process " + "WHERE BPMNPROCESS_ID = '" + bpmnProcess.getID() + "'",
				CorrelationProcess.class);
		return query.getResultList();
	}

	/**
	 * Saves this process to the database.
	 * 
	 * @return
	 */
	@Override
	public CorrelationProcess save() {
		return (CorrelationProcess) super.save();
	}

	/**
	 * Merges this process to the database.
	 * 
	 * @return
	 */
	@Override
	public CorrelationProcess merge() {
		return (CorrelationProcess) super.merge();
	}

	public static boolean save(final ArrayList<CorrelationProcess> processes) {
		try {
			final EntityManager entityManager = Persistor.getEntityManager();
			entityManager.getTransaction().begin();
			for (final CorrelationProcess process : processes) {
				entityManager.persist(process);
			}
			entityManager.getTransaction().commit();
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes this process from the database.
	 * 
	 * @return
	 */
	@Override
	public CorrelationProcess remove() {
		if (this.timeCondition != null) {
			this.timeCondition.remove();
		}
		final Iterator<CorrelationRule> correlationRuleIterator = this.correlationRules.iterator();
		while (correlationRuleIterator.hasNext()) {
			this.removeCorrelationRule(correlationRuleIterator.next());
		}
		final List<CorrelationProcessInstance> processInstancesToBeRemoved = new ArrayList<CorrelationProcessInstance>();
		processInstancesToBeRemoved.addAll(this.processInstances);
		this.processInstances.clear();
		this.merge();
		final Iterator<CorrelationProcessInstance> processInstanceIterator = processInstancesToBeRemoved.iterator();
		while (processInstanceIterator.hasNext()) {
			final CorrelationProcessInstance processInstance = processInstanceIterator.next();
			processInstance.remove();
		}
		return (CorrelationProcess) super.remove();
	}

	/**
	 * Deletes the specified processes from the database.
	 * 
	 * @return
	 */
	public static boolean remove(final ArrayList<CorrelationProcess> processes) {
		boolean removed = true;
		for (final CorrelationProcess process : processes) {
			removed = (process.remove() != null);
		}
		return removed;
	}

	/**
	 * Deletes all processes from the database.
	 */
	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM Process");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public TimeCondition getTimeCondition() {
		return this.timeCondition;
	}

	public void setTimeCondition(final TimeCondition timeCondition) {
		this.timeCondition = timeCondition;
		if (timeCondition != null) {
			this.timeCondition.setProcess(this);
		}
	}

	public BPMNProcess getBpmnProcess() {
		return this.bpmnProcess;
	}

	public void setBpmnProcess(final BPMNProcess bpmnProcess) {
		this.bpmnProcess = bpmnProcess;
	}

	/**
	 * Searches for the specified process and returns true if it exists.
	 * 
	 * @param name
	 * @return
	 */
	public static boolean exists(final String name) {
		return !CorrelationProcess.findByName(name).isEmpty();
	}

	/**
	 * Returns true, if the process has a correlation rule.
	 * 
	 * @return
	 */
	public boolean hasCorrelation() {
		return !this.correlationAttributes.isEmpty() || !this.correlationRules.isEmpty();
	}

	/**
	 * Returns a decompositional tree of the contained BPMN process, if any.
	 * 
	 * @return
	 */
	public EventTree<AbstractBPMNElement> getProcessDecompositionTree() {
		return this.processDecompositionTree;
	}

	public void setProcessDecompositionTree(final EventTree<AbstractBPMNElement> processDecompositionTree) {
		this.processDecompositionTree = processDecompositionTree;
	}
}
