/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

/**
 * @author micha
 * 
 * @param <T>
 */
@Entity
@Table(name = "EventTree")
public class EventTree<T> extends Persistable implements Collection<T> {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "EventTreeID")
	protected int ID;

	// essentiell, da sonst keine (leeren) EventTrees gespeichert werden können
	@Column(name = "Auxiliary")
	private final String auxiliary = "Auxiliary";

	@OneToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name = "EventTree_EventTreeElements")
	private List<EventTreeElement<T>> treeElements = new ArrayList<EventTreeElement<T>>();

	@OneToMany(cascade = CascadeType.PERSIST)
	@JoinTable(name = "EventTree_EventTreeRootElements")
	private List<EventTreeElement<T>> treeRootElements = new ArrayList<EventTreeElement<T>>();

	public EventTree() {
		this.ID = 0;
	}

	public EventTree(final T rootElementValue) {
		assert (rootElementValue != null);
		final EventTreeElement<T> element = new EventTreeElement<T>(null, rootElementValue);
		this.treeElements = new ArrayList<EventTreeElement<T>>();
		this.treeRootElements = new ArrayList<EventTreeElement<T>>();
		this.treeElements.add(element);
		this.treeRootElements.add(element);
	}

	public T getParent(final T treeElementValue) {
		for (final EventTreeElement<T> currentTreeElement : this.treeElements) {
			if (currentTreeElement.getValue() != null && currentTreeElement.getValue() == treeElementValue
					&& currentTreeElement.getParent() != null) {
				return currentTreeElement.getParent().getValue();
			}
		}
		return null;
	}

	/**
	 * Returns all parents in the tree for the given element.
	 * 
	 * @param element
	 * @return
	 */
	public Set<T> getIndirectParents(final T element) {
		final Set<T> parentValues = new HashSet<T>();
		final EventTreeElement<T> currentTreeElement = this.findTreeElementByValue(element);
		if (currentTreeElement != null) {
			parentValues.addAll(this.getIndirectParents(currentTreeElement));
		}
		return parentValues;
	}

	private Set<T> getIndirectParents(final EventTreeElement<T> currentTreeElement) {
		final Set<T> parentValues = new HashSet<T>();
		if (currentTreeElement.getParent() != null) {
			parentValues.add(currentTreeElement.getParent().getValue());
			parentValues.addAll(this.getIndirectParents(currentTreeElement.getParent()));
		}
		return parentValues;
	}

	/**
	 * Returns all elements from the tree, which contain exactly these children.
	 * 
	 * @param element
	 * @return
	 */
	public Set<T> getIndirectParents(final Collection<T> children) {
		final Set<T> parentValues = new HashSet<T>();
		final Set<EventTreeElement<T>> parents = new HashSet<EventTreeElement<T>>();
		for (final EventTreeElement<T> treeElement : this.treeElements) {
			if (treeElement.getChildValues().containsAll(children)
					&& children.containsAll(treeElement.getChildValues())) {
				parents.add(treeElement);
				parentValues.add(treeElement.getValue());
			}
		}
		for (final EventTreeElement<T> parent : parents) {
			parentValues.addAll(this.getIndirectParents(parent));
		}
		return parentValues;
	}

	/**
	 * Returns all descendants for the given element.
	 * 
	 * @param element
	 * @return
	 */
	public Set<T> getIndirectChildren(final T element) {
		final Set<T> childrenValues = new HashSet<T>();
		final Set<EventTreeElement<T>> children = new HashSet<EventTreeElement<T>>();
		final EventTreeElement<T> currentTreeElement = this.findTreeElementByValue(element);
		if (currentTreeElement != null) {
			children.addAll(this.getIndirectChildren(currentTreeElement));
		}
		for (final EventTreeElement<T> child : children) {
			childrenValues.add(child.getValue());
		}
		return childrenValues;
	}

	private Set<EventTreeElement<T>> getIndirectChildren(final EventTreeElement<T> currentTreeElement) {
		final Set<EventTreeElement<T>> children = new HashSet<EventTreeElement<T>>();
		for (final EventTreeElement<T> childElement : currentTreeElement.getChildren()) {
			children.add(childElement);
			if (childElement.hasChildren()) {
				children.addAll(this.getIndirectChildren(childElement));
			}
		}
		return children;
	}

	public List<T> getChildren(final T element) {
		final List<T> childrenValues = new ArrayList<T>();
		final EventTreeElement<T> currentTreeElement = this.findTreeElementByValue(element);
		if (currentTreeElement != null) {
			for (final EventTreeElement<T> childElement : currentTreeElement.getChildren()) {
				childrenValues.add(childElement.getValue());
			}
		}
		return childrenValues;
	}

	/**
	 * Returns a list of BPMN elements, which are parents for the specified
	 * elements.
	 * 
	 * @param elements
	 * @return
	 */
	public List<T> getParents(final Collection<T> elements) {
		final List<T> parentValues = new ArrayList<T>();
		for (final EventTreeElement<T> treeElement : this.treeElements) {
			final List<T> childrenOfElement = this.getChildren(treeElement.getValue());
			if (childrenOfElement.containsAll(elements) && elements.containsAll(childrenOfElement)) {
				parentValues.add(treeElement.getValue());
			}
		}
		return parentValues;
	}

	/**
	 * Returns the values for all elements of the tree that are leaves (elements
	 * that have no children).
	 * 
	 * @return
	 */
	public List<T> getValuesOfLeaves() {
		final List<T> leafValues = new ArrayList<T>();
		for (final EventTreeElement<T> element : this.treeElements) {
			if (!element.hasChildren()) {
				leafValues.add(element.getValue());
			}
		}
		return leafValues;
	}

	/**
	 * Returns all leaf elements descending from the specified element.
	 * 
	 * @param element
	 * @return
	 */
	public Set<T> getLeafs(final T element) {
		final Set<T> leaveValues = new HashSet<T>();
		final EventTreeElement<T> currentTreeElement = this.findTreeElementByValue(element);
		if (currentTreeElement != null) {
			for (final EventTreeElement<T> treeElement : this.getIndirectChildren(currentTreeElement)) {
				if (!treeElement.hasChildren()) {
					leaveValues.add(treeElement.getValue());
				}
			}
		}
		return leaveValues;
	}

	/**
	 * Returns all elements of the tree, that have no children.
	 * 
	 * @return
	 */
	public Set<T> getLeafElements() {
		final Set<T> leaves = new HashSet<T>();
		for (final EventTreeElement<T> element : this.getLeafs()) {
			leaves.add(element.getValue());
		}
		return leaves;
	}

	/**
	 * Returns all tree elements, that have no children.
	 * 
	 * @return
	 */
	private Set<EventTreeElement<T>> getLeafs() {
		final Set<EventTreeElement<T>> leaves = new HashSet<EventTreeElement<T>>();
		for (final EventTreeElement<T> element : this.treeElements) {
			if (!element.hasChildren()) {
				leaves.add(element);
			}
		}
		return leaves;
	}

	public boolean isInLeaves(final T treeElement) {
		final Set<EventTreeElement<T>> leaves = this.getLeafs();
		for (final EventTreeElement<T> element : leaves) {
			if (element.getValue().equals(treeElement)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasChildren(final T treeElement) {
		final EventTreeElement<T> currentTreeElement = this.findTreeElementByValue(treeElement);
		if (currentTreeElement != null) {
			return currentTreeElement.hasChildren();
		} else {
			return false;
		}
	}

	/**
	 * Adds a child to the specified parent.
	 * 
	 * @param parent
	 * @param child
	 */
	public void addChild(final T parent, final T child) {
		final EventTreeElement<T> treeElement = this.findTreeElementByValue(parent);
		final EventTreeElement<T> childTreeElement = new EventTreeElement<T>(treeElement, child);
		this.treeElements.add(childTreeElement);
		if (parent == null) {
			this.treeRootElements.add(childTreeElement);
		}
	}

	// public void addChild(T parent, T child, AttributeTypeEnum type) {
	//
	// EventTreeElement<T> treeElement = findTreeElementByValue(parent);
	// TypeTreeNode<T> childTreeElement = new TypeTreeNode<T>(treeElement,
	// child, type);
	// treeElements.add(childTreeElement);
	// if(parent == null){
	// treeRootElements.add(childTreeElement);
	// }
	// }

	/**
	 * Returns true if the parent contains the specified child.
	 * 
	 * @param parent
	 * @param child
	 */
	public boolean containsChild(final T parent, final T child) {
		return this.getChildren(parent).contains(child);
	}

	public boolean addRootElement(final T rootElement) {
		final EventTreeElement<T> element = new EventTreeElement<T>(rootElement);
		this.treeElements.add(element);
		return this.treeRootElements.add(element);
	}

	//
	// public boolean addRootElement(T rootElement, AttributeTypeEnum type){
	// TypeTreeNode<T> element = new TypeTreeNode<T>(null, rootElement, type);
	// treeElements.add(element);
	// return treeRootElements.add(element);
	// }

	/**
	 * Removes all children from the specified tree element.
	 * 
	 * @param treeElement
	 * @return
	 */
	public void removeChildren(final T treeElement) {
		final EventTreeElement<T> currentTreeElement = this.findTreeElementByValue(treeElement);
		final List<EventTreeElement<T>> children = currentTreeElement.getChildren();
		this.treeElements.removeAll(children);
		currentTreeElement.removeChildren();
	}

	@Override
	public int size() {
		return this.treeElements.size();
	}

	@Override
	public boolean isEmpty() {
		return this.treeElements.isEmpty();
	}

	@Override
	public boolean contains(final Object o) {
		try {
			for (final T element : this.getElements()) {
				if (element.equals(o)) {
					return true;
				}
			}
			return false;
		} catch (final ClassCastException c) {
			return false;
		}
	}

	public List<EventTreeElement<T>> getTreeElements() {
		return this.treeElements;
	}

	@Override
	public Iterator<T> iterator() {
		return this.getTreeElementValues(this.treeElements).iterator();
	}

	@Override
	public Object[] toArray() {
		return this.treeElements.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		throw new RuntimeException("Was soll das denn zurückgeben!?");
	}

	@Override
	public boolean add(final T e) {
		return this.addRootElement(e);
	}

	@Override
	public boolean remove(final Object removeElement) {
		// TODO: assert(removeElement instanceOf T);
		final EventTreeElement<T> removeTreeElement = this.findTreeElementByValue((T) removeElement);
		return this.remove(removeTreeElement);
	}

	public boolean remove(final EventTreeElement<T> removeTreeElement) {
		if (removeTreeElement != null) {
			if (removeTreeElement.hasChildren()) {
				final List<EventTreeElement<T>> children = new ArrayList<EventTreeElement<T>>(
						removeTreeElement.getChildren());
				for (final EventTreeElement<T> child : children) {
					this.remove(child);
				}
			}
			if (!(removeTreeElement.getParent() == null)) {
				removeTreeElement.getParent().getChildren().remove(removeTreeElement);
			}
			return (this.treeElements.remove(removeTreeElement) && this.treeRootElements.remove(removeTreeElement));
		}
		return false;
	}

	/**
	 * @author tsun
	 * 
	 * @param parent
	 *            Will be removed as well if it has no childs.
	 * @param child
	 */
	public boolean removeChild(final T parent, final T child) {
		EventTreeElement<T> parentElement;
		parentElement = this.findTreeRootElementByValue(parent);
		if (parentElement == null) {
			parentElement = this.findTreeElementByValue(parent);
		}
		if (parentElement != null) {
			if (parentElement.hasChildren()) {
				final List<EventTreeElement<T>> children = new ArrayList<EventTreeElement<T>>(
						parentElement.getChildren());
				for (final EventTreeElement<T> childTreeElement : children) {
					if (childTreeElement.getValue().equals(child)) {
						this.remove(childTreeElement);
						if (!parentElement.hasChildren()) {
							this.remove(parentElement);
						}
						return (this.treeElements.remove(childTreeElement) && this.treeElements.remove(parentElement) && this.treeRootElements
								.remove(parentElement));
					}
				}
			}
		}
		return false;
	}

	/**
	 * @author tsun
	 * 
	 * @param parent
	 *            Will not be removed in any case.
	 * @param child
	 */
	public boolean removeChildOnly(final T parent, final T child) {
		EventTreeElement<T> parentElement;
		parentElement = this.findTreeRootElementByValue(parent);
		if (parentElement == null) {
			parentElement = this.findTreeElementByValue(parent);
		}
		if (parentElement != null) {
			if (parentElement.hasChildren()) {
				final List<EventTreeElement<T>> children = new ArrayList<EventTreeElement<T>>(
						parentElement.getChildren());
				for (final EventTreeElement<T> childTreeElement : children) {
					if (childTreeElement.getValue().equals(child)) {
						this.remove(childTreeElement);
						return (this.treeElements.remove(childTreeElement) && this.treeElements.remove(parentElement) && this.treeRootElements
								.remove(parentElement));
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return this.getTreeElementValues(this.treeElements).containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends T> collection) {
		boolean success = true;
		for (final T element : collection) {
			success = success ? (this.addRootElement(element)) : false;
		}
		return success;
	}

	@Override
	public boolean removeAll(final Collection<?> collection) {
		boolean removeSuccess = true;
		for (final Object element : collection) {
			final boolean elementRemoveSuccess = collection.remove(element);
			removeSuccess = removeSuccess ? elementRemoveSuccess : false;
		}
		return removeSuccess;
	}

	@Override
	public boolean retainAll(final Collection<?> collection) {
		final boolean retainSuccess = true;
		// TODO: assert(collection instanceof T);
		final List<EventTreeElement<T>> copyTreeList = new ArrayList<EventTreeElement<T>>(this.treeElements);
		for (final EventTreeElement<T> element : copyTreeList) {
			if (!collection.contains(element.getValue())) {
				this.remove(element.getValue());
			}
		}
		return retainSuccess;
	}

	@Override
	public void clear() {
		this.treeElements.clear();
		this.treeRootElements.clear();
	}

	private ArrayList<T> getTreeElementValues(final List<EventTreeElement<T>> treeElements) {
		final ArrayList<T> valueElements = new ArrayList<T>();
		for (final EventTreeElement<T> element : treeElements) {
			valueElements.add(element.getValue());
		}
		return valueElements;
	}

	private EventTreeElement<T> findTreeElementByValue(final T treeElementValue) {
		if (treeElementValue == null) {
			return null;
		}
		for (final EventTreeElement<T> currentTreeElement : this.treeElements) {
			if (currentTreeElement.getValue() != null && currentTreeElement.getValue().equals(treeElementValue)) {
				return currentTreeElement;
			}
		}
		return null;
	}

	public boolean isHierarchical() {
		return (!this.treeRootElements.containsAll(this.treeElements));
	}

	private EventTreeElement<T> findTreeRootElementByValue(final T treeElementValue) {
		if (treeElementValue == null) {
			return null;
		}
		for (final EventTreeElement<T> currentTreeElement : this.treeRootElements) {
			if (currentTreeElement.getValue().equals(treeElementValue)) {
				return currentTreeElement;
			}
		}
		return null;
	}

	public List<T> getRootElements() {
		return this.getTreeElementValues(this.treeRootElements);
	}

	public T findElement(final T value) {
		final EventTreeElement<T> element = this.findTreeElementByValue(value);
		if (element != null) {
			return this.findTreeElementByValue(value).getValue();
		} else {
			return null;
		}
	}

	public static List<EventTree> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("select t from EventTree t");
		return q.getResultList();
	}

	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM EventTree");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public ArrayList<T> getElements() {
		return this.getTreeElementValues(this.treeElements);
	}

	/**
	 * Returns the depth of an element from the tree. If the element is not
	 * contained, -1 will be returned.
	 * 
	 * @param element
	 * @return
	 */
	public int getElementDepth(final T element) {
		int depth = 0;
		final EventTreeElement<T> treeElement = this.findTreeElementByValue(element);
		if (treeElement == null) {
			return -1;
		}
		EventTreeElement<T> parent = treeElement.getParent();
		while (parent != null) {
			parent = parent.getParent();
			depth++;
		}
		return depth;
	}

	@Override
	public String toString() {
		return this.printTreeLevel(this.treeRootElements, 0);
	}

	private String printTreeLevel(final List<EventTreeElement<T>> treeElements, final int count) {
		String tree = "";
		for (final EventTreeElement<T> element : treeElements) {
			for (int i = 0; i < count; i++) {
				tree += "\t";
			}
			tree += element.getValue() + System.getProperty("line.separator");
			if (element.hasChildren()) {
				tree += this.printTreeLevel(element.getChildren(), count + 1);
			}
		}
		return tree;
	}

	public Boolean retainAllLeafs(final Collection<?> collection) {
		final boolean retainSuccess = true;
		// TODO: assert(collection instanceof T);
		final List<EventTreeElement<T>> copyTreeList = new ArrayList<EventTreeElement<T>>(this.getLeafs());
		for (final EventTreeElement<T> element : copyTreeList) {
			if (!collection.contains(element.getValue())) {
				this.remove(element.getValue());
			}
		}
		return retainSuccess;
	}

	public boolean containsRootElement(final T eventTypeName) {
		for (final EventTreeElement<T> element : this.treeRootElements) {
			if (element.getValue().equals(eventTypeName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if a node with the given value exists in the children of a parent
	 * node.
	 * 
	 * @param parent
	 * @param child
	 *            the node that shall be in the children of the parent
	 * 
	 * @return true if the node with the given value exists in the children of
	 *         the given parent node
	 */
	public boolean isInChildrenOfNode(final T parent, final T child) {
		if (parent != null) {
			for (final T attribute : this.getElements()) {
				if (attribute.equals(parent)) {
					return this.getChildren(attribute).contains(child);
				}
			}
			return false;
		} else {
			return this.getRootElements().contains(child);
		}
	}

	@Override
	public int getID() {
		return this.ID;
	}

}
