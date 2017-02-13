/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event.collection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
 * TreeStructure used for structured key value mapping. Each node can have
 * childes. Its possible to have many root nodes.
 * 
 * @param <K>
 * @param <V>
 */
@Entity
@Table(name = "TransformationTree")
public class TransformationTree<K, V> extends Persistable implements Map<K, V> {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TransformationMapID")
	protected int ID;

	// splitted root elements and hierachical for more convinience
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@JoinTable(name = "TransformationMapTree_TransformationMapTreeElements")
	private List<EventTransformationElement<K, V>> treeElements = new ArrayList<EventTransformationElement<K, V>>();

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@JoinTable(name = "TransformationMapTree_TransformationMapTreeRootElements")
	private List<EventTransformationElement<K, V>> treeRootElements = new ArrayList<EventTransformationElement<K, V>>();

	// JPA cannot save empty objects. this element is carrying
	@Column(name = "Test")
	private final String test = "Test";

	public TransformationTree() {
		this.ID = 0;
	}

	public TransformationTree(final K rootElementKey, final V rootElementValue) {
		this.ID = 0;
		final EventTransformationElement<K, V> element = new EventTransformationElement<K, V>(rootElementKey,
				rootElementValue);
		this.treeElements.add(element);
		this.treeRootElements.add(element);
	}

	public boolean isHierarchical() {
		return (!(this.treeRootElements.size() == this.treeElements.size()));
	}

	public V getValueOfAttribute(final String attribute) {
		for (final EventTransformationElement<K, V> element : this.treeElements) {
			if (element.getKey().equals(attribute)) {
				return element.getValue();
			}
		}
		return null;
	}

	/**
	 * @return Values of the Elements in the first hierarchy
	 */
	public List<V> getRootElementValues() {
		final List<V> rootElementValues = new ArrayList<V>();
		for (final EventTransformationElement<K, V> currentTreeElement : this.treeRootElements) {
			rootElementValues.add(currentTreeElement.getValue());
		}
		return rootElementValues;
	}

	/**
	 * @param treeElementKey
	 * @return value of the partent of the node of the given key
	 */
	public V getParentValue(final K treeElementKey) {
		for (final EventTransformationElement<K, V> currentTreeElement : this.treeElements) {
			if (currentTreeElement.getKey() == treeElementKey) {
				return currentTreeElement.getParent().getValue();
			}
		}
		return null;
	}

	/**
	 * 
	 * @param treeElementKey
	 * @return key of the node of the given key
	 */
	public K getParentKey(final K treeElementKey) {
		for (final EventTransformationElement<K, V> currentTreeElement : this.treeElements) {
			if (currentTreeElement.getKey() == treeElementKey) {
				return currentTreeElement.getParent().getKey();
			}
		}
		return null;
	}

	/**
	 * @param treeElementKey
	 * @return all children keys for a specific element in the tree with the key
	 *         treeElementKey.
	 */
	public List<K> getChildrenKeys(final K treeElementKey) {
		final List<K> childrenKeys = new ArrayList<K>();
		final EventTransformationElement<K, V> currentMapElement = this.findMapElementByKey(treeElementKey);
		for (final EventTransformationElement<K, V> childElement : currentMapElement.getChildren()) {
			childrenKeys.add(childElement.getKey());
		}
		return childrenKeys;
	}

	/**
	 * @param treeElementKey
	 * @return all children values for a specific element in the tree with the
	 *         key treeElementKey.
	 */
	public List<V> getChildrenValues(final K treeElementKey) {
		final List<V> childrenValues = new ArrayList<V>();
		final EventTransformationElement<K, V> currentMapElement = this.findMapElementByKey(treeElementKey);
		for (final EventTransformationElement<K, V> childElement : currentMapElement.getChildren()) {
			childrenValues.add(childElement.getValue());
		}
		return childrenValues;
	}

	/**
	 * checks if the node of the given key has children
	 * 
	 * @param treeElementKey
	 * @return
	 */
	public boolean hasChildren(final K treeElementKey) {
		final EventTransformationElement<K, V> currentMapElement = this.findMapElementByKey(treeElementKey);
		return currentMapElement.hasChildren();
	}

	/**
	 * Adds a child to the specified parent.
	 * 
	 * @param parentKey
	 * @param child
	 */
	public void addChild(final K parentKey, final K childKey, final V childValue) {
		final EventTransformationElement<K, V> parentElement = this.findMapElementByKey(parentKey);
		final EventTransformationElement<K, V> childMapElement = new EventTransformationElement<K, V>(parentElement,
				childKey, childValue);
		// for (EventTransformationElement<K,V> mapElement : treeElements) {
		// if (mapElement.getKey().equals(parentKey)) {
		//
		// }
		// }
		this.treeElements.add(childMapElement);
		if (parentElement == null) {
			this.treeRootElements.add(childMapElement);
		}
	}

	/**
	 * adds root node with the given key and value
	 * 
	 * @param childKey
	 * @param childValue
	 * @return
	 */
	public boolean addRootElement(final K childKey, final V childValue) {
		final EventTransformationElement<K, V> element = new EventTransformationElement<K, V>(childKey, childValue);
		return (this.treeRootElements.add(element) && this.treeElements.add(element));
	}

	/**
	 * Removes all children from the specified map element.
	 * 
	 * @param treeElement
	 * @return
	 */
	public void removeChildren(final K mapElementKey) {
		final EventTransformationElement<K, V> currentMapElement = this.findMapElementByKey(mapElementKey);
		for (final EventTransformationElement<K, V> child : currentMapElement.getChildren()) {
			this.remove(child.getKey());
		}
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
	public boolean containsKey(final Object key) {
		return this.keySet().contains(key);
	}

	@Override
	public boolean containsValue(final Object value) {
		return this.values().contains(value);
	}

	@Override
	public V get(final Object key) {
		final EventTransformationElement<K, V> element = this.findMapElementByKey((K) key);
		if (element != null) {
			return element.getValue();
		} else {
			return null;
		}

	}

	@Override
	public V put(final K key, final V value) {
		this.treeElements.add(new EventTransformationElement<K, V>(key, value));
		this.treeRootElements.add(new EventTransformationElement<K, V>(key, value));
		return value;
	}

	@Override
	public V remove(final Object key) {
		final EventTransformationElement<K, V> removeTreeElement = this.findMapElementByKey((K) key);
		final V removeTreeElementValue = this.removeTreeElement(removeTreeElement);
		return removeTreeElementValue;
	}

	/**
	 * removes the given node from the tree
	 * 
	 * @param removeTreeElement
	 * @return
	 */
	private V removeTreeElement(final EventTransformationElement<K, V> removeTreeElement) {
		V removeTreeElementValue = null;
		if (removeTreeElement != null) {
			removeTreeElementValue = removeTreeElement.getValue();
		}
		if (removeTreeElement != null) {
			if (removeTreeElement.hasChildren()) {
				final List<EventTransformationElement<K, V>> children = new ArrayList<EventTransformationElement<K, V>>(
						removeTreeElement.getChildren());
				for (final EventTransformationElement<K, V> child : children) {
					this.remove(child.getKey());
				}
			}
			if (removeTreeElement.getParent() != null) {
				removeTreeElement.getParent().getChildren().remove(removeTreeElement);
			}
			this.treeElements.remove(removeTreeElement);
			this.treeRootElements.remove(removeTreeElement);
		}
		return removeTreeElementValue;

	}

	@Override
	public void putAll(final Map<? extends K, ? extends V> m) {
		for (final java.util.Map.Entry<? extends K, ? extends V> element : m.entrySet()) {
			this.treeRootElements.add(new EventTransformationElement<K, V>(element.getKey(), element.getValue()));
		}
	}

	@Override
	public void clear() {
		this.treeElements.clear();
		this.treeRootElements.clear();
	}

	@Override
	public Set<K> keySet() {
		final Set<K> keySet = new HashSet<K>();
		for (final EventTransformationElement mapElement : this.treeElements) {
			keySet.add((K) mapElement.getKey());
		}
		return keySet;
	}

	@Override
	public Collection<V> values() {
		final Collection<V> valueCollection = new ArrayList<V>();
		for (final EventTransformationElement mapElement : this.treeElements) {
			valueCollection.add((V) mapElement.getValue());
		}
		return valueCollection;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		final Set<java.util.Map.Entry<K, V>> elementSet = new HashSet<Map.Entry<K, V>>();
		for (final EventTransformationElement<K, V> element : this.treeElements) {
			elementSet.add(new AbstractMap.SimpleEntry<K, V>(element.getKey(), element.getValue()));
		}
		return elementSet;
	}

	/**
	 * return value of the node of the given key
	 * 
	 * @param elementKey
	 * @return
	 */
	public V findElement(final K elementKey) {
		final EventTransformationElement<K, V> element = this.findMapElementByKey(elementKey);
		if (element != null) {
			return element.getValue();
		} else {
			return null;
		}

	}

	/**
	 * return node with the given key
	 * 
	 * @param treeElementKey
	 * @return
	 */
	private EventTransformationElement<K, V> findMapElementByKey(final K treeElementKey) {
		if (treeElementKey == null) {
			return null;
		}
		for (final EventTransformationElement<K, V> currentMapElement : this.treeElements) {
			if (currentMapElement.getKey().equals(treeElementKey)) {
				return currentMapElement;
			}
		}
		return null;
	}

	/**
	 * @return all maptrees
	 */
	public static List<TransformationTree> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t from TransformationTree t");
		return q.getResultList();
	}

	/**
	 * removes all maptrees from DB
	 */
	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM TransformationTree");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	@Override
	public String toString() {
		return this.printMapLevel(this.treeRootElements, 0);
	}

	private String printMapLevel(final List<EventTransformationElement<K, V>> treeElements, final int count) {
		String tree = "";
		for (final EventTransformationElement<K, V> element : treeElements) {
			for (int i = 0; i < count; i++) {
				tree += "\t";
			}
			tree += element.getKey() + " : " + element.getValue() + "\r\n";
			if (element.hasChildren()) {
				tree += this.printMapLevel(element.getChildren(), count + 1);
			}
		}
		return tree;
	}

	/**
	 * deletes all elements except the given
	 * 
	 * @param collection
	 * @return
	 */
	public boolean retainAll(final Collection collection) {
		final boolean retainSuccess = true;
		final List<EventTransformationElement<K, V>> copyTreeList = new ArrayList<EventTransformationElement<K, V>>(
				this.treeElements);
		for (final EventTransformationElement<K, V> element : copyTreeList) {
			if (!collection.contains(element.getValue())) {
				this.remove(element.getValue());
			}
		}
		return retainSuccess;
	}

	/**
	 * use this only if the TransformationTree is used as attribute name/value
	 * mapping
	 * 
	 */
	public void retainAllByAttributeExpression(final ArrayList<String> retainableAttributeExpressions) {
		final List<EventTransformationElement<K, V>> treeElementsToBeRemoved = new ArrayList<EventTransformationElement<K, V>>();
		for (final EventTransformationElement<K, V> element : this.treeElements) {
			if (!retainableAttributeExpressions.contains(element.getAttributeExpression())) {
				treeElementsToBeRemoved.add(element);
			}
		}
		for (final EventTransformationElement<K, V> element : treeElementsToBeRemoved) {
			this.removeTreeElement(element);
		}
	}

	/**
	 * delete all node except those which keys are mentioned
	 * 
	 * @param retainableKeys
	 */
	public void retainAllKeys(final ArrayList<String> retainableKeys) {
		for (final K key : this.keySet()) {
			if (!retainableKeys.contains(key)) {
				this.remove(key);
			}
		}
	}

	public List<EventTransformationElement<K, V>> getTreeRootElements() {
		return this.treeRootElements;
	}

	public void setTreeRootElements(final List<EventTransformationElement<K, V>> treeRootElements) {
		this.treeRootElements = treeRootElements;
	}

	public List<EventTransformationElement<K, V>> getTreeElements() {
		return this.treeElements;
	}

	public void setTreeElements(final List<EventTransformationElement<K, V>> treeElements) {
		this.treeElements = treeElements;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return this.deepClone();
	}

	private TransformationTree<K, V> deepClone() {
		try {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);

			final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			final ObjectInputStream ois = new ObjectInputStream(bais);
			return (TransformationTree<K, V>) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			return null;
		}
	}

	@Override
	public int getID() {
		return this.ID;
	}
}