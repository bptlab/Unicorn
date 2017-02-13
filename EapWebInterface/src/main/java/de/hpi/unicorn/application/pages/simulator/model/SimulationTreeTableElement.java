/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator.model;

import java.io.Serializable;
import java.util.ArrayList;

import de.hpi.unicorn.application.pages.simulator.DurationEntryPanel;
import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.simulation.DerivationType;

/**
 * representation of a tree node
 * 
 * @param <T>
 *            type of content to be stored
 */
public class SimulationTreeTableElement<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private int ID;
	private T content;
	private SimulationTreeTableElement<T> parent;
	private final ArrayList<SimulationTreeTableElement<T>> children = new ArrayList<SimulationTreeTableElement<T>>();
	private String probability;
	private String input;

	private DerivationType derivationType;
	private String derivation;

	private DurationEntryPanel durationEntryPanel;

	private String duration;

	public SimulationTreeTableElement(final int ID, final T content) {
		this(ID, content, "1");
	}

	/**
	 * creates a root node
	 * 
	 * @param content
	 *            the content to be stored in the new node
	 */
	public SimulationTreeTableElement(final int ID, final T content, final String probability) {
		this.ID = ID;
		this.content = content;
		this.setProbability(probability);
		this.setDerivationType(DerivationType.FIXED);
		this.setDuration("0");
		this.setDerivation("0");
	}

	/**
	 * creates a node and adds it to its parent
	 * 
	 * @param parent
	 * @param content
	 *            the content to be stored in the node
	 */
	public SimulationTreeTableElement(final SimulationTreeTableElement<T> parent, final int ID, final T content,
			final String probability) {
		this(ID, content, probability);
		this.parent = parent;
		this.parent.getChildren().add(this);
	}

	public Integer getID() {
		return this.ID;
	}

	public void setID(final int ID) {
		this.ID = ID;
	}

	public T getContent() {
		return this.content;
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	public SimulationTreeTableElement<T> getParent() {
		return this.parent;
	}

	public ArrayList<SimulationTreeTableElement<T>> getChildren() {
		return this.children;
	}

	@Override
	public String toString() {
		if (this.content == null) {
			return new String();
		}
		return this.content.toString();
	}

	public void remove() {
		if (this.parent != null) {
			this.parent.getChildren().remove(this);
		}
	}

	public void setParent(final SimulationTreeTableElement<T> parent) {
		this.parent = parent;
	}

	public String getInput() {
		return this.input;
	}

	public void setInput(final String input) {
		this.input = input;
	}

	public boolean editableColumnsVisible() {
		return this.content instanceof TypeTreeNode;
	}

	public boolean canHaveSubElements() {
		return this.content instanceof Component;
	}

	public DerivationType getDerivationType() {
		return this.derivationType;
	}

	public void setDerivationType(final DerivationType derivationType) {
		this.derivationType = derivationType;
		if (this.durationEntryPanel != null) {
			this.durationEntryPanel.setDerivationType(derivationType);
		}
	}

	public void setDerivation(final String derivation) {
		this.derivation = derivation;
	}

	public String getDerivation() {
		return this.derivation;
	}

	public void setDurationEntryPanel(final DurationEntryPanel durationEntryPanel) {
		this.durationEntryPanel = durationEntryPanel;
	}

	public String getDuration() {
		return this.duration;
	}

	public void setDuration(final String duration) {
		this.duration = duration;
	}

	public String getProbability() {
		return this.probability;
	}

	public void setProbability(final String probability) {
		this.probability = probability;
	}
}
