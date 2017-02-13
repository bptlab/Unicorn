/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.jbpt.hypergraph.abs.IGObject;
import org.jbpt.hypergraph.abs.IVertex;

import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;
import de.hpi.unicorn.persistence.Persistable;

/**
 * This class is a logical representation for a BPMN element.
 * 
 * @author micha
 */
@Entity
@Table(name = "BPMNElement")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractBPMNElement extends Persistable implements IVertex {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	protected int ID;

	@Column(name = "BPMN_ID")
	private String BPMN_ID;

	@Column(name = "Name")
	private String name;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinTable(name = "BPMNElement_Predecessors")
	private Set<AbstractBPMNElement> predecessors = new HashSet<AbstractBPMNElement>();

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinTable(name = "BPMNElement_Successors")
	private Set<AbstractBPMNElement> successors = new HashSet<AbstractBPMNElement>();

	@OneToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinTable(name = "BPMNElement_MonitoringPoints")
	private List<MonitoringPoint> monitoringPoints = new ArrayList<MonitoringPoint>();

	public AbstractBPMNElement() {
		this.ID = 0;
		this.name = "";
	}

	public AbstractBPMNElement(final String ID, final String name) {
		this.BPMN_ID = ID;
		this.name = name;
	}

	public AbstractBPMNElement(final String ID, final String name, final List<MonitoringPoint> monitoringPoints) {
		this.BPMN_ID = ID;
		this.name = name;
		if (monitoringPoints != null) {
			this.monitoringPoints = monitoringPoints;
		}
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	public Set<AbstractBPMNElement> getPredecessors() {
		return this.predecessors;
	}

	/**
	 * Returns all elements, that are predecessor of the current element and its
	 * predecessor (all indirect predecessors).
	 * 
	 * @return
	 */
	public Set<AbstractBPMNElement> getIndirectPredecessors() {
		Set<AbstractBPMNElement> elements = new HashSet<AbstractBPMNElement>();
		elements = this.getIndirectPredecessorsWithSelf(this, elements);
		return elements;
	}

	private Set<AbstractBPMNElement> getIndirectPredecessorsWithSelf(final AbstractBPMNElement element,
			final Set<AbstractBPMNElement> elements) {
		if (element instanceof BPMNStartEvent) {
			if (!elements.contains(element)) {
				elements.add(element);
			}
		} else {
			elements.add(element);
			for (final AbstractBPMNElement predecessor : element.getPredecessors()) {
				if (!elements.contains(predecessor)) {
					elements.addAll(this.getIndirectPredecessorsWithSelf(predecessor, elements));
				}
			}
		}
		return elements;
	}

	/**
	 * Returns all elements, that are on a path starting from the current
	 * element (all indirect successors).
	 * 
	 * @return
	 */
	public Set<AbstractBPMNElement> getIndirectSuccessors() {
		final Set<AbstractBPMNElement> elements = new HashSet<AbstractBPMNElement>();
		for (final AbstractBPMNElement element : this.getSuccessors()) {
			elements.addAll(this.getIndirectSuccessors(element, elements));
		}
		return elements;
	}

	/**
	 * Proofs, if the given element is a indirect successor of the current
	 * element.
	 * 
	 * @param element
	 * @return
	 */
	public boolean isIndirectSuccessor(final AbstractBPMNElement element) {
		if (!this.equals(element)) {
			return this.getIndirectSuccessors().contains(element);
		}
		return false;
	}

	private Set<AbstractBPMNElement> getIndirectSuccessors(final AbstractBPMNElement element,
			final Set<AbstractBPMNElement> elements) {
		if (element instanceof BPMNEndEvent) {
			if (!elements.contains(element)) {
				elements.add(element);
			}
		} else {
			elements.add(element);
			for (final AbstractBPMNElement successor : element.getSuccessors()) {
				if (!elements.contains(successor)) {
					elements.add(successor);
					elements.addAll(this.getIndirectSuccessors(successor, elements));
				}
			}
		}
		return elements;
	}

	public void setPredecessor(final Set<AbstractBPMNElement> predecessor) {
		this.predecessors = predecessor;
	}

	/**
	 * Returns the direct successors.
	 * 
	 * @return
	 */
	public Set<AbstractBPMNElement> getSuccessors() {
		return this.successors;
	}

	public void setSuccessors(final Set<AbstractBPMNElement> successors) {
		this.successors = successors;
	}

	/**
	 * Adds an element as a predecessor for the current element.
	 * 
	 * @param element
	 */
	public void addPredecessor(final AbstractBPMNElement element) {
		if (!this.predecessors.contains(element)) {
			this.predecessors.add(element);
		}
	}

	public void removePredecessor(final AbstractBPMNElement element) {
		this.predecessors.remove(element);
	}

	public void removeAllPredecessors() {
		this.predecessors = new HashSet<AbstractBPMNElement>();
	}

	/**
	 * Proofs, if this element has any predecessors.
	 * 
	 * @return
	 */
	public boolean hasPredecessor() {
		return (!this.predecessors.isEmpty());
	}

	/**
	 * Proofs, if this element has any successors.
	 * 
	 * @return
	 */
	public boolean hasSuccessors() {
		return (!this.successors.isEmpty());
	}

	public boolean isSequenceFlow() {
		return false;
	}

	public boolean isBoundaryEvent() {
		return false;
	}

	public boolean isProcess() {
		return false;
	}

	/**
	 * Adds an element as a successor for the current element.
	 * 
	 * @param element
	 */
	public void addSuccessor(final AbstractBPMNElement element) {
		if (!this.successors.contains(element)) {
			this.successors.add(element);
		}
	}

	public void removeSuccessor(final AbstractBPMNElement element) {
		this.successors.remove(element);
	}

	public void removeAllSuccessors() {
		this.successors = new HashSet<AbstractBPMNElement>();
	}

	public String getBPMN_ID() {
		return this.BPMN_ID;
	}

	public void setBPMN_ID(final String id) {
		this.BPMN_ID = id;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int id) {
		this.ID = id;
	}

	public List<MonitoringPoint> getMonitoringPoints() {
		return this.monitoringPoints;
	}

	/**
	 * Returns the monitoring point with the given
	 * {@link MonitoringPointStateTransition} or null.
	 * 
	 * @param transitionType
	 * @return
	 */
	public MonitoringPoint getMonitoringPointByStateTransitionType(final MonitoringPointStateTransition transitionType) {
		for (final MonitoringPoint monitoringPoint : this.monitoringPoints) {
			if (monitoringPoint.getStateTransitionType().equals(transitionType)) {
				return monitoringPoint;
			}
		}
		return null;
	}

	public void addMonitoringPoint(final MonitoringPoint monitoringPoint) {
		if (this.getMonitoringPointByStateTransitionType(monitoringPoint.getStateTransitionType()) != null) {
			this.monitoringPoints.remove(this.getMonitoringPointByStateTransitionType(monitoringPoint
					.getStateTransitionType()));
		}
		this.monitoringPoints.add(monitoringPoint);
	}

	public void removeMonitoringPoint(final MonitoringPoint monitoringPoint) {
		this.monitoringPoints.remove(monitoringPoint);
	}

	public void setMonitoringPoints(final List<MonitoringPoint> monitoringPoints) {
		this.monitoringPoints = monitoringPoints;
	}

	public boolean hasMonitoringPoints() {
		return this.monitoringPoints != null && !this.monitoringPoints.isEmpty();
	}

	/**
	 * Returns true, if an element has monitoring points and if the monitoring
	 * points have an assigned event type.
	 * 
	 * @return
	 */
	public boolean hasMonitoringPointsWithEventType() {
		if (this.hasMonitoringPoints()) {
			boolean monitoringPointWithEventType = false;
			for (final MonitoringPoint monitoringPoint : this.monitoringPoints) {
				if (monitoringPoint.getEventType() != null) {
					monitoringPointWithEventType = true;
				}
			}
			return monitoringPointWithEventType;
		}
		return false;
	}

	@Override
	public String toString() {
		return (this.name.isEmpty()) ? this.getClass().getSimpleName() : this.getClass().getSimpleName() + ": "
				+ this.name;
	}

	@Override
	public AbstractBPMNElement clone() {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			final ObjectInputStream ois = new ObjectInputStream(bais);
			return (AbstractBPMNElement) ois.readObject();
		} catch (final IOException e) {
			return null;
		} catch (final ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public String getId() {
		return this.BPMN_ID;
	}

	@Override
	public void setId(final String id) {
		this.BPMN_ID = id;
	}

	/**
	 * Returns all elements on a path between the given start and end element.
	 * Their must exist a path between these elements.
	 * 
	 * @param startElement
	 *            - excluded
	 * @param endElement
	 *            - excluded
	 * @return
	 */
	public static Set<AbstractBPMNElement> getElementsOnPathBetween(final AbstractBPMNElement startElement,
			final AbstractBPMNElement endElement) {
		final Set<AbstractBPMNElement> elements = new HashSet<AbstractBPMNElement>();
		if (startElement.equals(endElement)) {
			return elements;
		}
		elements.add(startElement);

		for (final AbstractBPMNElement successor : startElement.getSuccessors()) {
			if (successor.equals(endElement)) {
				elements.add(successor);
				return elements;
			}
			if (successor.getIndirectSuccessors().contains(endElement) && !elements.contains(successor)) {
				elements.add(successor);
				elements.addAll(AbstractBPMNElement.getElementsOnPathBetweenWithStartAndEnd(successor, endElement));
			}
		}
		elements.remove(startElement);
		elements.remove(endElement);
		return elements;
	}

	private static Set<AbstractBPMNElement> getElementsOnPathBetweenWithStartAndEnd(
			final AbstractBPMNElement startElement, final AbstractBPMNElement endElement) {
		final Set<AbstractBPMNElement> elements = new HashSet<AbstractBPMNElement>();
		elements.add(startElement);
		for (final AbstractBPMNElement successor : startElement.getSuccessors()) {
			if (successor.equals(endElement)) {
				elements.add(successor);
				return elements;
			}
			if (successor.getIndirectSuccessors().contains(endElement) && !elements.contains(successor)) {
				elements.add(successor);
				elements.addAll(AbstractBPMNElement.getElementsOnPathBetweenWithStartAndEnd(successor, endElement));
			}
		}
		return elements;
	}

	/**
	 * Returns all elements, that belong to the shortest path between the given
	 * start and end element.
	 * 
	 * @param sourceElement
	 * @param destinationElement
	 * @return
	 */
	public static List<AbstractBPMNElement> getShortestPathBetween(final AbstractBPMNElement sourceElement,
			final AbstractBPMNElement destinationElement) {
		final Map<AbstractBPMNElement, Boolean> visitedElements = new HashMap<AbstractBPMNElement, Boolean>();
		final Map<AbstractBPMNElement, AbstractBPMNElement> previousElements = new HashMap<AbstractBPMNElement, AbstractBPMNElement>();

		final List<AbstractBPMNElement> directions = new LinkedList<AbstractBPMNElement>();
		final Queue<AbstractBPMNElement> queue = new LinkedList<AbstractBPMNElement>();
		AbstractBPMNElement current = sourceElement;
		queue.add(current);
		visitedElements.put(current, true);
		while (!queue.isEmpty()) {
			current = queue.remove();
			if (current.equals(destinationElement)) {
				break;
			} else {
				for (final AbstractBPMNElement node : current.getSuccessors()) {
					if (!visitedElements.containsKey(node)) {
						queue.add(node);
						visitedElements.put(node, true);
						previousElements.put(node, current);
					}
				}
			}
		}
		if (!current.equals(destinationElement)) {
			// System.out.println("can't reach destination");
		}
		for (AbstractBPMNElement node = destinationElement; node != null; node = previousElements.get(node)) {
			directions.add(node);
		}
		Collections.reverse(directions);
		return directions;
	}

	@Override
	public int getX() {
		return 0;
	}

	@Override
	public void setX(final int x) {

	}

	@Override
	public int getY() {
		return 0;
	}

	@Override
	public void setY(final int y) {

	}

	@Override
	public int getWidth() {
		return 0;
	}

	@Override
	public void setWidth(final int w) {

	}

	@Override
	public int getHeight() {
		return 0;
	}

	@Override
	public void setHeight(final int h) {

	}

	@Override
	public void setLocation(final int x, final int y) {

	}

	@Override
	public void setSize(final int w, final int h) {

	}

	@Override
	public void setLayout(final int x, final int y, final int w, final int h) {

	}

	@Override
	public Object getTag() {
		return null;
	}

	@Override
	public void setTag(final Object tag) {

	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public void setDescription(final String desc) {

	}

	@Override
	public int compareTo(final IGObject o) {
		return 0;
	}

	@Override
	public String getLabel() {
		return null;
	}

	/**
	 * Connects the two given elements as predecessor and successor.
	 * 
	 * @param predecessor
	 * @param successor
	 */
	public static void connectElements(final AbstractBPMNElement predecessor, final AbstractBPMNElement successor) {
		if (predecessor != null && successor != null) {
			predecessor.addSuccessor(successor);
			successor.addPredecessor(predecessor);
		}
	}

	/**
	 * Disconnects the two given elements as predecessor and successor.
	 * 
	 * @param predecessor
	 * @param successor
	 */
	public static void disconnectElements(final AbstractBPMNElement predecessor, final AbstractBPMNElement successor) {
		if (predecessor != null && successor != null) {
			predecessor.removeSuccessor(successor);
			successor.removePredecessor(predecessor);
		}
	}

	/**
	 * Returns all the elements, that have monitoring points.
	 * 
	 * @param elements
	 * @return
	 */
	public static List<AbstractBPMNElement> getElementsWithMonitoringPoints(final List<AbstractBPMNElement> elements) {
		final Set<AbstractBPMNElement> monitorableElements = new HashSet<AbstractBPMNElement>();
		for (final AbstractBPMNElement element : elements) {
			if (element.hasMonitoringPoints()) {
				monitorableElements.add(element);
			}
		}
		return new ArrayList<AbstractBPMNElement>(monitorableElements);
	}

}
