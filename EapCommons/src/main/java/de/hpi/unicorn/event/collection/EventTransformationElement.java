/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event.collection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.utils.XMLUtils;

/**
 * 
 * Node for SusiTreeMap. Encapsulate Key/Value pair. Can have childnodes. Knows
 * parent node.
 * 
 * @param <K>
 * @param <V>
 */
@Entity
@Table(name = "EventTransformationElement")
public class EventTransformationElement<K, V> extends Persistable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ID;

	@ManyToOne(cascade = CascadeType.PERSIST)
	private EventTransformationElement<K, V> parent;

	@OneToMany(cascade = CascadeType.PERSIST)
	private List<EventTransformationElement<K, V>> children = new ArrayList<EventTransformationElement<K, V>>();

	@Column(name = "MapKey", length = 255)
	K key;

	@Column(name = "MapValue", length = 15000)
	V value;

	public EventTransformationElement() {
		this.ID = 0;
	}

	public EventTransformationElement(final K key, final V value) {
		this.key = key;
		this.value = value;
	}

	public EventTransformationElement(final EventTransformationElement<K, V> parent, final K key, final V value) {
		this.parent = parent;
		this.key = key;
		this.value = value;
		if (this.parent != null) {
			this.parent.addChild(this);
		}
	}

	/**
	 * use this only if the EventTransformationElement is used as attribute
	 * name/value mapping
	 * 
	 * @return attribute name expression (examples: 'ETA' for first level
	 *         attribute named 'ETA', 'vehicle_information.transport' for second
	 *         level attribute named 'transport' which is child of
	 *         'vehicle_information'
	 */
	public String getAttributeExpression() {
		if (this.isRootElement()) {
			return (String) this.key;
		}
		return this.parent.getAttributeExpression() + "." + (String) this.key;
	}

	/**
	 * converts the tree structure into typed XML nodes
	 * 
	 * @return
	 */
	public Document getNodeWithChildnodes() {
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = domFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		// need document from xml for the xml parser
		final Document doc = builder.newDocument();
		final Element root = doc.createElement(((String) this.key).replaceAll(" ", ""));
		if (this.getChildren().isEmpty()) {
			final V content = this.value;
			// save typed stuff
			if (content instanceof Date) {
				root.setTextContent(XMLUtils.getFormattedDate((Date) content));
			} else if (content instanceof Integer) {
				root.setTextContent(XMLUtils.getXMLInteger((Integer) content));
			} else if (content instanceof Long) {
				root.setTextContent(XMLUtils.getXMLLong((Long) content));
			} else if (content instanceof Double) {
				root.setTextContent(XMLUtils.getXMLDouble((Double) content));
			} else {
				root.setTextContent((String) content);
			}
		}
		doc.appendChild(root);
		for (final EventTransformationElement child : this.getChildren()) {
			final Node importedNode = doc.importNode(child.getNodeWithChildnodes().getFirstChild(), true);
			root.appendChild(importedNode);
		}
		return doc;
	}

	public V getValue() {
		return this.value;
	}

	public K getKey() {
		return this.key;
	}

	public void setKey(final K key) {
		this.key = key;
	}

	public V setValue(final V value) {
		return this.value = value;
	}

	public EventTransformationElement<K, V> getParent() {
		return this.parent;
	}

	public void setParent(final EventTransformationElement<K, V> parent) {
		if (this.parent != null) {
			this.parent.addChild(this);
		}
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	public List<EventTransformationElement<K, V>> getChildren() {
		return this.children;
	}

	public void setChildren(final List<EventTransformationElement<K, V>> children) {
		this.children = children;
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	public boolean isRootElement() {
		return this.parent == null;
	}

	private void addChild(final EventTransformationElement<K, V> childTreeMapElement) {
		if (this.children == null) {
			this.children = new ArrayList<EventTransformationElement<K, V>>();
		}
		this.children.add(childTreeMapElement);
	}

	public void removeChildren() {
		this.children.clear();
	}

	@Override
	public String toString() {
		return "TreeMapElement: [" + this.key + " -> " + this.value + "]";
	}

	public static List<TransformationTree> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t from EventTransformationElement t");
		return q.getResultList();
	}

	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM EventTransformationElement");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	@Override
	public int getID() {
		return this.ID;
	}
}