/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.decomposition;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.AbstractBPMNGateway;

/**
 * This class is a container for {@link AbstractBPMNElement}s and results from a
 * process decomposition with the RPST. A {@link Component} is created for each
 * canonical fragment of the RPST.
 * 
 * @author micha
 */
public class Component extends AbstractBPMNElement {

	private static final long serialVersionUID = 1L;

	protected IPattern type;

	/**
	 * Single entry point to the component, not included in the components
	 * children.
	 */
	protected AbstractBPMNElement entryPoint;

	/**
	 * Single exit point to the component, not included in the components
	 * children.
	 */
	protected AbstractBPMNElement exitPoint;

	/**
	 * Entry element to the component, that belongs to its elements and is the
	 * successor of the entry point.
	 */
	protected AbstractBPMNElement sourceElement;

	/**
	 * Exit element of the component, that belongs to its elements and is the
	 * predecessor of the exit point.
	 */
	protected AbstractBPMNElement sinkElement;

	protected Set<AbstractBPMNElement> children;

	public Component() {
		this.children = new HashSet<AbstractBPMNElement>();
	}

	public Component(final AbstractBPMNElement entryPoint, final AbstractBPMNElement sourceElement) {
		this.entryPoint = entryPoint;
		this.sourceElement = sourceElement;
		this.children = new HashSet<AbstractBPMNElement>(Arrays.asList(sourceElement));
	}

	/**
	 * Creates a new component as a container.
	 * 
	 * @param entryPoint
	 *            - not included
	 * @param sourceElement
	 *            - included
	 * @param exitPoint
	 *            - not included
	 * @param sinkElement
	 *            - included
	 */
	public Component(final AbstractBPMNElement entryPoint, final AbstractBPMNElement sourceElement,
			final AbstractBPMNElement exitPoint, final AbstractBPMNElement sinkElement) {
		this.entryPoint = entryPoint;
		this.sourceElement = sourceElement;
		this.children = new HashSet<AbstractBPMNElement>(Arrays.asList(sourceElement));
		this.exitPoint = exitPoint;
		this.sinkElement = sinkElement;
		this.children.add(sinkElement);
	}

	/**
	 * Returns the single entry point to the component, not included in the
	 * components children.
	 */
	public AbstractBPMNElement getEntryPoint() {
		return this.entryPoint;
	}

	public void setEntryPoint(final AbstractBPMNElement entryPoint) {
		this.entryPoint = entryPoint;
	}

	public AbstractBPMNElement getExitPoint() {
		return this.exitPoint;
	}

	public void setExitPoint(final AbstractBPMNElement exitPoint) {
		this.exitPoint = exitPoint;
	}

	public Set<AbstractBPMNElement> getChildren() {
		return this.children;
	}

	public void setChildren(final Set<AbstractBPMNElement> children) {
		this.children = children;
	}

	public void addChildren(final Collection<AbstractBPMNElement> children) {
		for (final AbstractBPMNElement child : children) {
			this.addChild(child);
		}
	}

	public void addChild(final AbstractBPMNElement child) {
		if (!this.children.contains(child)) {
			this.children.add(child);
		}
	}

	public void removeChild(final AbstractBPMNElement child) {
		this.children.remove(child);
	}

	public void removeChildren(final List<AbstractBPMNElement> innerElements) {
		this.children.removeAll(innerElements);
	}

	public boolean contains(final AbstractBPMNElement element) {
		for (final AbstractBPMNElement child : this.children) {
			if (child.equals(element)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasMonitoringPoints() {
		for (final AbstractBPMNElement child : this.children) {
			if (child != null && child.hasMonitoringPoints()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasMonitoringPointsWithEventType() {
		for (final AbstractBPMNElement child : this.children) {
			if (child.hasMonitoringPointsWithEventType()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the source element of the component, that belongs to its elements
	 * and is the successor of the entry point.
	 */
	public AbstractBPMNElement getSourceElement() {
		return this.sourceElement;
	}

	public void setSourceElement(final AbstractBPMNElement sourceElement) {
		this.sourceElement = sourceElement;
	}

	/**
	 * Returns the sink element of the component, that belongs to its elements
	 * and is the predecessor of the exit point.
	 */
	public AbstractBPMNElement getSinkElement() {
		return this.sinkElement;
	}

	public void setSinkElement(final AbstractBPMNElement sinkElement) {
		this.sinkElement = sinkElement;
	}

	public boolean includesGateways() {
		for (final AbstractBPMNElement child : this.children) {
			if (child instanceof AbstractBPMNGateway) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks, if a component contains gateways, that are not the source or sink
	 * element
	 * 
	 * @return
	 */
	public boolean includesInnerGateways() {
		for (final AbstractBPMNElement child : this.children) {
			if (child instanceof AbstractBPMNGateway
					&& !(child.equals(this.sourceElement) || child.equals(this.sinkElement))) {
				return true;
			}
		}
		return false;
	}

	public IPattern getType() {
		return this.type;
	}

	public void setType(final IPattern type) {
		this.type = type;
	}

}
