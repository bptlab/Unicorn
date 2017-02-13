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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.tree.ISortableTreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.application.pages.simulator.DurationEntryPanel;
import de.hpi.unicorn.bpmn.decomposition.XORComponent;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.AbstractBPMNGateway;
import de.hpi.unicorn.bpmn.element.BPMNTask;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.simulation.DerivationType;
import de.hpi.unicorn.utils.Tuple;

/**
 * wraps the given tree nodes
 *
 * @param <T>
 */
/**
 * @author micha
 * 
 * @param <T>
 */
public class SimulationTreeTableProvider<T> extends AbstractDataProvider implements
		ISortableTreeProvider<SimulationTreeTableElement<T>, String> {

	private static final long serialVersionUID = 1L;
	private List<SimulationTreeTableElement<T>> treeTableElements;
	private List<SimulationTreeTableElement<T>> treeTableRootElements;
	private final List<SimulationTreeTableElement<T>> selectedTreeTableElements = new ArrayList<SimulationTreeTableElement<T>>();

	public SimulationTreeTableProvider() {
		this.treeTableElements = new ArrayList<SimulationTreeTableElement<T>>();
	}

	/**
	 * constructor
	 * 
	 * @param treeNodes
	 *            root nodes of the tree, child nodes are accessed by this
	 *            component automatically
	 */
	public SimulationTreeTableProvider(final ArrayList<SimulationTreeTableElement<T>> treeNodes) {
		this.treeTableElements = treeNodes;
	}

	@Override
	public void detach() {
	}

	@Override
	public Iterator<? extends SimulationTreeTableElement<T>> getRoots() {
		return this.getRootElements().iterator();
	}

	private List<SimulationTreeTableElement<T>> getRootElements() {
		this.treeTableRootElements = new ArrayList<SimulationTreeTableElement<T>>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getParent() == null) {
				this.treeTableRootElements.add(element);
			}
		}
		return this.treeTableRootElements;
	}

	@Override
	public boolean hasChildren(final SimulationTreeTableElement<T> node) {
		return !node.getChildren().isEmpty();
	}

	@Override
	public Iterator<? extends SimulationTreeTableElement<T>> getChildren(final SimulationTreeTableElement<T> node) {
		return node.getChildren().iterator();
	}

	@Override
	public SimulationTreeTableElementModel<T> model(final SimulationTreeTableElement<T> node) {
		return new SimulationTreeTableElementModel<T>(this.getRootElements(), node);
	}

	@Override
	public ISortState<String> getSortState() {
		return new SingleSortState<String>();
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final SimulationTreeTableElement<T> treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				this.selectedTreeTableElements.add(treeTableElement);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final SimulationTreeTableElement<T> treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				this.selectedTreeTableElements.remove(treeTableElement);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final SimulationTreeTableElement<T> treeTableElement : this.selectedTreeTableElements) {
			if (treeTableElement.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the next free ID for an new element.
	 * 
	 * @return
	 */
	public int getNextID() {
		int highestNumber = 0;
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			highestNumber = element.getID() > highestNumber ? element.getID() : highestNumber;
		}
		return ++highestNumber;
	}

	public List<SimulationTreeTableElement<T>> getTreeTableElements() {
		return this.treeTableElements;
	}

	public void addTreeTableElement(final SimulationTreeTableElement<T> treeTableElement) {
		this.treeTableElements.add(treeTableElement);
		if (!this.selectedTreeTableElements.isEmpty()) {
			final SimulationTreeTableElement<T> parent = this.selectedTreeTableElements.get(0);
			parent.getChildren().add(treeTableElement);
			treeTableElement.setParent(parent);
		}
	}

	public void addTreeTableElementWithParent(final SimulationTreeTableElement<T> treeTableElement,
			final SimulationTreeTableElement<T> parent) {
		this.treeTableElements.add(treeTableElement);
		parent.getChildren().add(treeTableElement);
		treeTableElement.setParent(parent);
	}

	public void setTreeTableElements(final List<SimulationTreeTableElement<T>> treeTableElements) {
		this.treeTableElements = treeTableElements;
	}

	public void deleteSelectedEntries() {
		for (final SimulationTreeTableElement<T> element : this.selectedTreeTableElements) {
			element.remove();
		}
		this.treeTableElements.removeAll(this.selectedTreeTableElements);
		this.selectedTreeTableElements.clear();
	}

	public List<SimulationTreeTableElement<T>> getSelectedTreeTableElements() {
		return this.selectedTreeTableElements;
	}

	public List<SimulationTreeTableElement<T>> getRootTreeTableElements() {
		return this.getRootElements();
	}

	public EventTree<T> getModelAsTree() {
		final EventTree<T> tree = new EventTree<T>();
		for (final SimulationTreeTableElement<T> element : this.treeTableRootElements) {
			this.addElementToTree(null, element, tree);
		}
		return tree;
	}

	private void addElementToTree(final SimulationTreeTableElement<T> parent,
			final SimulationTreeTableElement<T> element, final EventTree<T> tree) {
		if (parent != null) {
			tree.addChild(parent.getContent(), element.getContent());
		} else {
			tree.addChild(null, element.getContent());
		}
		if (element.getContent() instanceof EapEventType) {
			final Map<TypeTreeNode, String> attributeMap = new HashMap<TypeTreeNode, String>();
			for (final SimulationTreeTableElement<T> child : element.getChildren()) {
				attributeMap.put((TypeTreeNode) child.getContent(), child.getInput());
			}
			tree.addChild(element.getContent(), (T) attributeMap);
		} else {
			for (final SimulationTreeTableElement<T> child : element.getChildren()) {
				this.addElementToTree(element, child, tree);
			}
		}
	}

	public String getInputForEntry(final int entryID) {
		return this.getEntry(entryID).getInput();
	}

	public void setInputForEntry(final String input, final int entryID) {
		this.getEntry(entryID).setInput(input);
	}

	public String getDurationForEntry(final int entryID) {
		return this.getEntry(entryID).getDuration();
	}

	public void setDurationForEntry(final String duration, final int entryID) {
		this.getEntry(entryID).setDuration(duration);
	}

	public void setDerivationTypeForEntry(final DerivationType derivationType, final int entryID) {
		this.getEntry(entryID).setDerivationType(derivationType);
	}

	public DerivationType getDerivationTypeForEntry(final int entryID) {
		return this.getEntry(entryID).getDerivationType();
	}

	public void setDerivationForEntry(final String derivation, final int entryID) {
		this.getEntry(entryID).setDerivation(derivation);
	}

	public String getDerivationForEntry(final int entryID) {
		return this.getEntry(entryID).getDerivation();
	}

	public void setProbabilityForEntry(final String probability, final int entryID) {
		this.getEntry(entryID).setProbability(probability);
	}

	public String getProbabilityForEntry(final int entryID) {
		return this.getEntry(entryID).getProbability();
	}

	public boolean editableColumnsVisibleForEntry(final int entryID) {
		// TODO: rename!
		return this.getEntry(entryID).editableColumnsVisible();
	}

	public Map<TypeTreeNode, List<Serializable>> getAttributeValuesFromModel() {
		final Map<TypeTreeNode, List<Serializable>> attributes = new HashMap<TypeTreeNode, List<Serializable>>();
		Boolean alreadyInserted;
		for (final SimulationTreeTableElement<T> treeTableElement : this.treeTableElements) {
			if (treeTableElement.getContent() instanceof TypeTreeNode) {
				alreadyInserted = false;
				for (final TypeTreeNode insertedAttribute : attributes.keySet()) {
					if (insertedAttribute.equals(treeTableElement.getContent())) {
						alreadyInserted = true;
						break;
					}
				}
				if (!alreadyInserted) {
					attributes.put((TypeTreeNode) treeTableElement.getContent(),
							this.getValuesFromInput(treeTableElement));
				}
			}
		}
		return attributes;
	}

	private List<Serializable> getValuesFromInput(final SimulationTreeTableElement<T> attributeElement) {
		final List<Serializable> values = new ArrayList<Serializable>();
		final String valueString = attributeElement.getInput();
		final TypeTreeNode attribute = (TypeTreeNode) attributeElement.getContent();
		if (attribute.getType().equals(AttributeTypeEnum.DATE)) {
			// TODO: datum parsen
		} else if (attribute.getType().equals(AttributeTypeEnum.INTEGER)
				|| attribute.getType().equals(AttributeTypeEnum.FLOAT)) {
			final String[] split = valueString.split(",");
			for (final String element : split) {
				if (element.contains("-")) {
					final String[] subSplit = element.split("-");
					// falsche eingaben abfangen
					if (subSplit.length != 2) {
						// TODO: was tun bei falscher eingabe?
					} else {
						if (attribute.getType().equals(AttributeTypeEnum.INTEGER)) {
							for (Long j = Long.parseLong(subSplit[0].trim()); j <= Long.parseLong(subSplit[1].trim()); j++) {
								values.add(j);
							}
						} else if (attribute.getType().equals(AttributeTypeEnum.FLOAT)) {
							for (Double j = Double.parseDouble(subSplit[0].trim()); j <= Double.parseDouble(subSplit[1]
									.trim()); j++) {
								values.add(j);
							}
						}

					}
				} else {
					if (attribute.getType().equals(AttributeTypeEnum.INTEGER)) {
						values.add(Long.parseLong(element.trim()));
					} else if (attribute.getType().equals(AttributeTypeEnum.FLOAT)) {
						values.add(Double.parseDouble(element.trim()));
					}
				}
			}
		} else {
			final String[] split = valueString.split(",");
			for (final String element : split) {
				values.add(element.trim());
			}
		}
		return values;
	}

	public void updateAllEqualInputFields(final String input, final int entryID) {
		final SimulationTreeTableElement<T> sourceTreeTableElement = this.getEntry(entryID);
		for (final SimulationTreeTableElement<T> treeTableElement : this.treeTableElements) {
			if (treeTableElement.getContent().equals(sourceTreeTableElement.getContent())) {
				treeTableElement.setInput(input);
				final Integer otherEntryId = treeTableElement.getID();
				this.setInputForEntry(input, otherEntryId);
			}
		}

	}

	public void setCorrelationAttributes(final List<TypeTreeNode> correlationAttributes) {
	}

	public void deleteAllEntries() {
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			element.remove();
		}
		this.selectedTreeTableElements.clear();
		this.treeTableElements.clear();
	}

	/**
	 * Checks if an element of the provider has empty input fields.
	 * 
	 * @return
	 */
	public boolean hasEmptyFields() {
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof TypeTreeNode) {
				if (element.getInput() == null || element.getInput().isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	public List<EapEventType> getEventTypes() {
		final List<EapEventType> eventTypes = new ArrayList<EapEventType>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof EapEventType) {
				eventTypes.add((EapEventType) element.getContent());
			}
		}
		return eventTypes;
	}

	public List<SimulationTreeTableElement<T>> getEventTypeElements() {
		final List<SimulationTreeTableElement<T>> eventTypes = new ArrayList<SimulationTreeTableElement<T>>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof EapEventType) {
				eventTypes.add(element);
			}
		}
		return eventTypes;
	}

	public Map<EapEventType, String> getEventTypesWithDuration() {
		final Map<EapEventType, String> eventTypesWithDuration = new HashMap<EapEventType, String>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof EapEventType) {
				eventTypesWithDuration.put((EapEventType) element.getContent(), element.getDuration());
			}
		}
		return eventTypesWithDuration;
	}

	public Map<EapEventType, String> getEventTypesWithDerivation() {
		final Map<EapEventType, String> eventTypesWithDuration = new HashMap<EapEventType, String>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof EapEventType) {
				eventTypesWithDuration.put((EapEventType) element.getContent(), element.getDerivation());
			}
		}
		return eventTypesWithDuration;
	}

	public Map<EapEventType, DerivationType> getEventTypesWithDerivationType() {
		final Map<EapEventType, DerivationType> eventTypesWithDuration = new HashMap<EapEventType, DerivationType>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof EapEventType) {
				eventTypesWithDuration.put((EapEventType) element.getContent(), element.getDerivationType());
			}
		}
		return eventTypesWithDuration;
	}

	public Map<AbstractBPMNElement, String> getBPMNElementWithDuration() {
		final Map<AbstractBPMNElement, String> taskssWithDuration = new HashMap<AbstractBPMNElement, String>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof AbstractBPMNElement) {
				taskssWithDuration.put((AbstractBPMNElement) element.getContent(), element.getDuration());
			}
		}
		return taskssWithDuration;
	}

	@Override
	public SimulationTreeTableElement<T> getEntry(final int entryId) {
		for (final SimulationTreeTableElement<T> treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				return treeTableElement;
			}
		}
		return null;
	}

	public void registerDurationInputAtEntry(final DurationEntryPanel durationEntryPanel, final int entryId) {
		this.getEntry(entryId).setDurationEntryPanel(durationEntryPanel);
	}

	public Map<Object, String> getProbabilityStrings() {
		final Map<Object, String> probabilityStrings = new HashMap<Object, String>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof XORComponent) {
				for (final SimulationTreeTableElement<T> child : element.getChildren()) {
					probabilityStrings.put(child, child.getProbability());
				}
			}
		}
		return probabilityStrings;
	}

	public Map<AbstractBPMNElement, DerivationType> getBPMNElementWithDerivationType() {
		final Map<AbstractBPMNElement, DerivationType> elementsWithDerivation = new HashMap<AbstractBPMNElement, DerivationType>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof AbstractBPMNElement) {
				elementsWithDerivation.put((AbstractBPMNElement) element.getContent(), element.getDerivationType());
			}
		}
		return elementsWithDerivation;
	}

	public Map<AbstractBPMNElement, String> getBPMNElementWithDerivation() {
		final Map<AbstractBPMNElement, String> elementWithDerivation = new HashMap<AbstractBPMNElement, String>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof AbstractBPMNElement) {
				elementWithDerivation.put((AbstractBPMNElement) element.getContent(), element.getDerivation());
			}
		}
		return elementWithDerivation;
	}

	public Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, String>>> getXorSuccessorsWithProbability() {
		final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, String>>> xorSuccessorsProbability = new HashMap<BPMNXORGateway, List<Tuple<AbstractBPMNElement, String>>>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof BPMNXORGateway
					&& ((AbstractBPMNGateway) element.getContent()).isSplitGateway()) {
				final List<Tuple<AbstractBPMNElement, String>> successorList = new ArrayList<Tuple<AbstractBPMNElement, String>>();
				for (final SimulationTreeTableElement<T> xorSuccessorElement : this.treeTableElements) {
					if (((BPMNXORGateway) element.getContent()).getSuccessors().contains(
							xorSuccessorElement.getContent())) {
						final Tuple<AbstractBPMNElement, String> tuple = new Tuple<AbstractBPMNElement, String>(
								(AbstractBPMNElement) xorSuccessorElement.getContent(),
								xorSuccessorElement.getProbability());
						successorList.add(tuple);
					}
				}
				xorSuccessorsProbability.put((BPMNXORGateway) element.getContent(), successorList);
			}
		}
		return xorSuccessorsProbability;
	}

	public List<TypeTreeNode> getAttributes() {
		Boolean isInList;
		final List<TypeTreeNode> attributeList = new ArrayList<TypeTreeNode>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof TypeTreeNode) {
				isInList = false;
				for (final TypeTreeNode attributeInList : attributeList) {
					if (attributeInList.equals(element.getContent())) {
						isInList = true;
						break;
					}
				}
				if (!isInList) {
					attributeList.add((TypeTreeNode) element.getContent());
				}
			}
		}
		return attributeList;
	}

	public List<BPMNTask> getTasks() {
		final List<BPMNTask> tasks = new ArrayList<BPMNTask>();
		for (final SimulationTreeTableElement<T> element : this.treeTableElements) {
			if (element.getContent() instanceof BPMNTask) {
				tasks.add((BPMNTask) element.getContent());
			}
		}
		return tasks;
	}
}
