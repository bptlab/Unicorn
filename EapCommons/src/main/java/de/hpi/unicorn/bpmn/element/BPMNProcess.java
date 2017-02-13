/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * This class is a logical representation of a BPMN process.
 * @author micha
 */
/**
 * @author micha
 * 
 */
@Entity
@Table(name = "BPMNProcess")
@Inheritance(strategy = InheritanceType.JOINED)
public class BPMNProcess extends AbstractBPMNElement {

	private static final long serialVersionUID = 1L;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private final List<AbstractBPMNElement> BPMNElements = new ArrayList<AbstractBPMNElement>();

	public BPMNProcess() {
		super();
	}

	public BPMNProcess(final String ID, final String name, final List<MonitoringPoint> monitoringPoints) {
		super(ID, name, monitoringPoints);
	}

	@Override
	public boolean isProcess() {
		return true;
	}

	public void addBPMNElements(final List<AbstractBPMNElement> elements) {
		this.BPMNElements.addAll(elements);
	}

	public void addBPMNElement(final AbstractBPMNElement element) {
		if (element != null && !(this.BPMNElements.contains(element))) {
			this.BPMNElements.add(element);
		}
	}

	public void removeBPMNElements(final Collection<AbstractBPMNElement> elements) {
		this.BPMNElements.removeAll(elements);
	}

	public void removeBPMNElement(final AbstractBPMNElement element) {
		this.BPMNElements.remove(element);
	}

	public List<AbstractBPMNElement> getBPMNElements() {
		return this.BPMNElements;
	}

	public List<AbstractBPMNElement> getBPMNElementsWithOutSequenceFlows() {
		final List<AbstractBPMNElement> elements = new ArrayList<AbstractBPMNElement>();
		for (final AbstractBPMNElement element : this.BPMNElements) {
			if (!(element instanceof BPMNSequenceFlow)) {
				elements.add(element);
			}
		}
		return elements;
	}

	/**
	 * Return all activities({@link BPMNTask}), which are contained in this
	 * process.
	 * 
	 * @return
	 */
	public List<BPMNTask> getAllTasks() {
		final List<BPMNTask> elements = new ArrayList<BPMNTask>();
		for (final AbstractBPMNElement element : this.getBPMNElementsWithOutSequenceFlows()) {
			if (element instanceof BPMNTask) {
				elements.add((BPMNTask) element);
			}
		}
		return elements;
	}

	/**
	 * Returns all {@link Component}s, which are contained in this process.
	 * 
	 * @return
	 */
	public List<Component> getAllComponents() {
		final List<Component> elements = new ArrayList<Component>();
		for (final AbstractBPMNElement element : this.getBPMNElementsWithOutSequenceFlows()) {
			if (element instanceof Component) {
				elements.add((Component) element);
			}
		}
		return elements;
	}

	/**
	 * Returns a list of all BPMN-IDs of the contained elements.
	 * 
	 * @return
	 */
	public ArrayList<String> getBPMNElementIDs() {
		final ArrayList<String> ids = new ArrayList<String>();
		for (final AbstractBPMNElement element : this.BPMNElements) {
			ids.add(element.getId());
		}
		return ids;
	}

	public AbstractBPMNElement getBPMNElementById(final String ID) {
		for (final AbstractBPMNElement element : this.BPMNElements) {
			if (element.getId().equals(ID)) {
				return element;
			}
		}
		return null;
	}

	public AbstractBPMNElement getBPMNElementByName(final String name) {
		for (final AbstractBPMNElement element : this.BPMNElements) {
			if (element.getName().equals(name)) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Returns the start event for a process.
	 * 
	 * @return
	 */
	public BPMNStartEvent getStartEvent() {
		for (final AbstractBPMNElement element : this.BPMNElements) {
			if (element instanceof BPMNStartEvent) {
				return (BPMNStartEvent) element;
			}
		}
		return null;
	}

	/**
	 * Returns all start events for a process.
	 * 
	 * @return
	 */
	public List<BPMNStartEvent> getStartEvents() {
		final List<BPMNStartEvent> startEvents = new ArrayList<BPMNStartEvent>();
		for (final AbstractBPMNElement element : this.BPMNElements) {
			if (element instanceof BPMNStartEvent) {
				startEvents.add((BPMNStartEvent) element);
			}
		}
		return startEvents;
	}

	/**
	 * Returns the end event for a process.
	 * 
	 * @return
	 */
	public BPMNEndEvent getEndEvent() {
		for (final AbstractBPMNElement element : this.BPMNElements) {
			if (element instanceof BPMNEndEvent) {
				return (BPMNEndEvent) element;
			}
		}
		return null;
	}

	/**
	 * Returns all end events for a process.
	 * 
	 * @return
	 */
	public List<BPMNEndEvent> getEndEvents() {
		final List<BPMNEndEvent> endEvents = new ArrayList<BPMNEndEvent>();
		for (final AbstractBPMNElement element : this.BPMNElements) {
			if (element instanceof BPMNEndEvent) {
				endEvents.add((BPMNEndEvent) element);
			}
		}
		return endEvents;
	}

	public AbstractBPMNElement getNextElementFor(final AbstractBPMNElement element) {
		// lookForSequenceFlow
		for (final AbstractBPMNElement flow : this.BPMNElements) {
			if (flow.isSequenceFlow()) {
				final BPMNSequenceFlow seqflow = (BPMNSequenceFlow) flow;
				if (seqflow.getSourceRef().equals(element.getId())) {

					return this.getBPMNElementById(seqflow.getTargetRef());
				}
			}
		}
		// lookForEvents
		return this.getAttachedElementsFor(element);
	}

	private AbstractBPMNElement getAttachedElementsFor(final AbstractBPMNElement element) {
		for (final AbstractBPMNElement el : this.BPMNElements) {
			if (el.isBoundaryEvent()) {
				final BPMNBoundaryEvent event = (BPMNBoundaryEvent) el;
				if (event.getAttachedToElement().getId().equals(element.getId())) {
					return event;
				}
			}
		}
		return null;
	}

	public String printProcess() {
		return this.printProcess(this);
	}

	/**
	 * This method returns a textual representation of all process elements and
	 * their monitoring points.
	 * 
	 * @param process
	 * @return
	 */
	public String printProcess(final BPMNProcess process) {
		final StringBuffer output = new StringBuffer();
		Collection<AbstractBPMNElement> elements = new ArrayList<>();
		if (process.getStartEvent() != null) {
			elements.add(process.getStartEvent());
			elements.addAll(process.getStartEvent().getIndirectSuccessors());
		} else {
			elements = process.getBPMNElementsWithOutSequenceFlows();
		}
		for (final AbstractBPMNElement element : elements) {
			output.append(element.toString());
			output.append(System.getProperty("line.separator"));
			for (final MonitoringPoint monitoringPoint : element.getMonitoringPoints()) {
				output.append("[Monitoring Point] " + monitoringPoint.toString());
				output.append(System.getProperty("line.separator"));
			}
			if (element instanceof BPMNSubProcess) {
				output.append(this.printProcess((BPMNSubProcess) element));
			}
		}
		return output.toString();
	}

	/**
	 * Returns all {@link BPMNProcess}es from the database, which have the
	 * specified ID.
	 * 
	 * @return
	 */
	public static BPMNProcess findByID(final int ID) {
		final List<BPMNProcess> list = BPMNProcess.findByAttribute("ID", new Integer(ID).toString());
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Returns all {@link BPMNProcess}es from the database, which have the
	 * specified name.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<BPMNProcess> findByName(final String name) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * " + "FROM BPMNElement " + "WHERE ID IN (" + "	SELECT RESULT.ID " + "	FROM ("
						+ "		SELECT * " + "		FROM BPMNElement AS SELECTEDBPMNELEMENT " + "		WHERE ID IN ("
						+ "			SELECT ID " + "			FROM BPMNProcess AS SELECTEDBPMNPROCESS)) AS RESULT"
						+ "			WHERE RESULT.NAME ='" + name + "')", BPMNProcess.class);
		return query.getResultList();
	}

	/**
	 * Returns all {@link BPMNProcess}es from the database, which have the
	 * specified attribute and value.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<BPMNProcess> findByAttribute(final String columnName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM BPMNProcess WHERE " + columnName + " = '" + value + "'", BPMNProcess.class);
		return query.getResultList();
	}

	/**
	 * Returns all {@link BPMNProcess}es from the database.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<BPMNProcess> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("select t from BPMNProcess t");
		return q.getResultList();
	}

	/**
	 * Removes all {@link BPMNProcess}es from the database.
	 */
	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM BPMNProcess");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	/**
	 * Returns all split gateways, contained in this {@link BPMNProcess}.
	 * 
	 * @return
	 */
	public List<AbstractBPMNGateway> getAllSplitGateways() {
		final List<AbstractBPMNGateway> elements = new ArrayList<AbstractBPMNGateway>();
		for (final AbstractBPMNElement element : this.getBPMNElementsWithOutSequenceFlows()) {
			if (element instanceof AbstractBPMNGateway && ((AbstractBPMNGateway) element).isSplitGateway()) {
				elements.add((AbstractBPMNGateway) element);
			}
		}
		return elements;
	}

	/**
	 * Returns all joining gateways, contained in this {@link BPMNProcess}.
	 * 
	 * @return
	 */
	public List<AbstractBPMNGateway> getAllJoinGateways() {
		final List<AbstractBPMNGateway> elements = new ArrayList<AbstractBPMNGateway>();
		for (final AbstractBPMNElement element : this.getBPMNElementsWithOutSequenceFlows()) {
			if (element instanceof AbstractBPMNGateway && ((AbstractBPMNGateway) element).isJoinGateway()) {
				elements.add((AbstractBPMNGateway) element);
			}
		}
		return elements;
	}

	@Override
	public Persistable remove() {
		final CorrelationProcess process = CorrelationProcess.findByBPMNProcess(this);
		if (process != null) {
			process.setBpmnProcess(null);
			process.merge();
		}
		return super.remove();
	}

	public boolean hasSubProcesses() {
		for (final AbstractBPMNElement element : this.BPMNElements) {
			if (element instanceof BPMNSubProcess) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the contained {@link BPMNSubProcess}es, if any.
	 * 
	 * @return
	 */
	public List<BPMNSubProcess> getSubProcesses() {
		final List<BPMNSubProcess> subProcesses = new ArrayList<BPMNSubProcess>();
		for (final AbstractBPMNElement element : this.BPMNElements) {
			if (element instanceof BPMNSubProcess) {
				subProcesses.add((BPMNSubProcess) element);
			}
		}
		return subProcesses;
	}

	public List<AbstractBPMNElement> getSubElementsWithMonitoringpoints() {
		final List<AbstractBPMNElement> subElementsWithMonitoringpoints = new ArrayList<AbstractBPMNElement>();
		for (final AbstractBPMNElement subElement : this.BPMNElements) {
			if (subElement.hasMonitoringPoints()) {
				subElementsWithMonitoringpoints.add(subElement);
			}
		}
		return subElementsWithMonitoringpoints;
	}

	public static boolean exists(final String name) {
		return !BPMNProcess.findByName(name).isEmpty();
	}

	/**
	 * Searches in all saved {@link BPMNProcess}es for one, which contains the
	 * given {@link AbstractBPMNElement}.
	 * 
	 * @param bpmnElement
	 * @return
	 */
	public static BPMNProcess findByContainedElement(final AbstractBPMNElement bpmnElement) {
		for (final BPMNProcess process : BPMNProcess.findAll()) {
			for (final AbstractBPMNElement element : process.getBPMNElementsWithOutSequenceFlows()) {
				if (element.getID() == bpmnElement.getID()) {
					return process;
				}
			}
		}
		return null;
	}

}
