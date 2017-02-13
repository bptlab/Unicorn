/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation.collection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.event.collection.EventTreeElement;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.transformation.TransformationRule;
import de.hpi.unicorn.transformation.element.EventTypeElement;
import de.hpi.unicorn.transformation.element.FilterExpressionConnectorElement;
import de.hpi.unicorn.transformation.element.FilterExpressionElement;
import de.hpi.unicorn.transformation.element.PatternOperatorElement;

/**
 * Container object for the pattern elements of a transformation rule. One
 * pattern tree per transformation rule.
 */
@Entity
@Table(name = "TransformationPatternTree")
public class TransformationPatternTree extends Persistable {

	private static final long serialVersionUID = 4641263893746532464L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TransformationPatternTreeID")
	protected int ID;

	@Column(name = "Auxiliary")
	private final String auxiliary = "Auxiliary";

	@OneToOne(mappedBy = "patternTree")
	private TransformationRule transformationRule;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "patternTreeID", referencedColumnName = "TransformationPatternTreeID")
	private final List<EventTreeElement<Serializable>> elements = new ArrayList<EventTreeElement<Serializable>>();

	public TransformationPatternTree() {
		this.ID = 0;
	}

	public TransformationPatternTree(final EventTreeElement<Serializable> element) {
		this();
		assert (element != null);
		this.elements.add(element);
	}

	public TransformationPatternTree(final List<EventTreeElement<Serializable>> elements) {
		this();
		assert (elements != null);
		assert (!elements.isEmpty());
		this.elements.addAll(elements);
	}

	public boolean isEmpty() {
		return this.elements.isEmpty();
	}

	@Override
	public int getID() {
		return this.ID;
	}

	/**
	 * Method to retrieve all elements of the pattern tree.
	 * 
	 * @return list of elements
	 */
	public List<EventTreeElement<Serializable>> getElements() {
		return this.elements;
	}

	/**
	 * Method to retrieve all elements without parents.
	 * 
	 * @return list of root elements in an adequate order
	 */
	public List<EventTreeElement<Serializable>> getRoots() {
		final List<EventTreeElement<Serializable>> rootElements = new ArrayList<EventTreeElement<Serializable>>();
		for (final EventTreeElement<Serializable> currentElement : this.elements) {
			if (!currentElement.hasParent()) {
				rootElements.add(currentElement);
			}
		}
		return rootElements;
	}

	/**
	 * Method to retrieve all elements without children.
	 * 
	 * @return list of root elements in an adequate order
	 */
	public List<EventTreeElement<Serializable>> getLeafs() {
		final List<EventTreeElement<Serializable>> leafElements = new ArrayList<EventTreeElement<Serializable>>();
		for (final EventTreeElement<Serializable> currentElement : this.elements) {
			if (!currentElement.hasChildren()) {
				leafElements.add(currentElement);
			}
		}
		return leafElements;
	}

	/**
	 * Method to retrieve all pattern operator elements of the pattern tree.
	 * 
	 * @return list of pattern operator elements
	 */
	public List<PatternOperatorElement> getPatternOperatorElements() {
		final List<PatternOperatorElement> patternOperatorElements = new ArrayList<PatternOperatorElement>();
		for (final EventTreeElement<Serializable> element : this.elements) {
			if (element instanceof PatternOperatorElement) {
				patternOperatorElements.add((PatternOperatorElement) element);
			}
		}
		return patternOperatorElements;
	}

	/**
	 * Method to retrieve all event type elements of the pattern tree.
	 * 
	 * @return list of event type elements
	 */
	public List<EventTypeElement> getEventTypeElements() {
		final List<EventTypeElement> eventTypeElements = new ArrayList<EventTypeElement>();
		for (final EventTreeElement<Serializable> element : this.elements) {
			if (element instanceof EventTypeElement) {
				eventTypeElements.add((EventTypeElement) element);
			}
		}
		return eventTypeElements;
	}

	/**
	 * Method to retrieve all filter expression elements of the pattern tree.
	 * 
	 * @return list of filter expression elements
	 */
	public List<FilterExpressionElement> getFilterExpressionElements() {
		final List<FilterExpressionElement> filterExpressionElements = new ArrayList<FilterExpressionElement>();
		for (final EventTreeElement<Serializable> element : this.elements) {
			if (element instanceof FilterExpressionElement) {
				filterExpressionElements.add((FilterExpressionElement) element);
			}
		}
		return filterExpressionElements;
	}

	public boolean addElement(final EventTreeElement<Serializable> element) {
		return this.elements.add(element);
	}

	public boolean addElements(final List<EventTreeElement<Serializable>> elements) {
		return elements.addAll(elements);
	}

	/**
	 * Removes the given element from the tree. If an element has parent and
	 * child elements, the child elements will be connected to the parent
	 * elements.
	 * 
	 * @param element
	 *            the element to be removed
	 * @return true if removal was successful
	 */
	public boolean removeElement(final EventTreeElement<Serializable> element) {
		if (element instanceof FilterExpressionElement) {
			final EventTreeElement<Serializable> parentElement = element.getParent();
			parentElement.removeChild(element);
		} else if (element instanceof EventTypeElement) {
			final List<EventTreeElement<Serializable>> allChildrenOfElement = this.getAllChildrenFromElement(element);
			for (final EventTreeElement<Serializable> child : allChildrenOfElement) {
				child.removeElement();
				this.elements.remove(child);
			}
			final List<EventTreeElement<Serializable>> allParentsOfElement = this.getAllParentsFromElement(element);
			for (final EventTreeElement<Serializable> parent : allParentsOfElement) {
				parent.removeElement();
				this.elements.remove(parent);
			}
			element.removeElement();
		} else if (element instanceof PatternOperatorElement || element instanceof FilterExpressionConnectorElement) {
			for (final EventTreeElement<Serializable> childElement : element.getChildren()) {
				if (element.hasParent()) {
					final EventTreeElement<Serializable> parentElement = element.getParent();
					childElement.setParent(parentElement);
					parentElement.removeChild(element);
				} else {
					childElement.setParent(null);
				}
			}
		}
		return this.elements.remove(element);
	}

	/**
	 * Method to retrieve all elements from lower levels that are referenced
	 * directly or indirectly by the given element.
	 * 
	 * @param element
	 *            the element from which all lower level elements shall be found
	 * @return list of elements from lower levels that are referenced directly
	 *         or indirectly by the given element
	 */
	private List<EventTreeElement<Serializable>> getAllChildrenFromElement(final EventTreeElement<Serializable> element) {
		final List<EventTreeElement<Serializable>> allChildren = new ArrayList<EventTreeElement<Serializable>>();
		for (final EventTreeElement<Serializable> child : element.getChildren()) {
			allChildren.add(child);
			if (child.hasChildren()) {
				for (final EventTreeElement<Serializable> childOfChild : child.getChildren()) {
					this.getAllChildrenFromChild(allChildren, childOfChild);
				}
			}
		}
		return allChildren;
	}

	private void getAllChildrenFromChild(final List<EventTreeElement<Serializable>> allChildren,
			final EventTreeElement<Serializable> element) {
		allChildren.add(element);
		if (element.hasChildren()) {
			for (final EventTreeElement<Serializable> child : element.getChildren()) {
				this.getAllChildrenFromChild(allChildren, child);
			}
		}
	}

	/**
	 * Method to retrieve all elements from higher levels that are referenced
	 * directly or indirectly by the given element.
	 * 
	 * @param element
	 *            the element from which all higher shall be found
	 * @return list of elements from higher levels that are referenced directly
	 *         or indirectly by the given element
	 */
	private List<EventTreeElement<Serializable>> getAllParentsFromElement(final EventTreeElement<Serializable> element) {
		final List<EventTreeElement<Serializable>> allParents = new ArrayList<EventTreeElement<Serializable>>();
		if (element.hasParent()) {
			final EventTreeElement<Serializable> parent = element.getParent();
			allParents.add(parent);
			if (parent.hasParent()) {
				this.getAllParentsFromParent(allParents, parent.getParent());
			}
		}
		return allParents;
	}

	private void getAllParentsFromParent(final List<EventTreeElement<Serializable>> allParents,
			final EventTreeElement<Serializable> element) {
		allParents.add(element);
		if (element.hasParent()) {
			this.getAllChildrenFromChild(allParents, element.getParent());
		}
	}

	public static List<TransformationPatternTree> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t FROM TransformationPatternTree t");
		return q.getResultList();
	}

	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM TransformationPatternTree");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	@Override
	public TransformationPatternTree clone() {
		return this.deepClone();
	}

	private TransformationPatternTree deepClone() {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			final ObjectInputStream ois = new ObjectInputStream(bais);
			return (TransformationPatternTree) ois.readObject();
		} catch (final IOException e) {
			return null;
		} catch (final ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public String toString() {
		return this.printTreeLevel(this.getRoots(), 0);
	}

	private String printTreeLevel(final List<EventTreeElement<Serializable>> elements, final int count) {
		String tree = "";
		for (final EventTreeElement<Serializable> element : elements) {
			for (int i = 0; i < count; i++) {
				tree += "\t";
			}
			tree += element + System.getProperty("line.separator");
			if (element.hasChildren()) {
				tree += this.printTreeLevel(element.getChildren(), count + 1);
			}
		}
		return tree;
	}
}
