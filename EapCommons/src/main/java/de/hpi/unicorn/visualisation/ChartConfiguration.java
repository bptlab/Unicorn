/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.visualisation;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

/**
 * This class saves the configurations for a chart that visualizes event values
 * of an attribute of a certain eventtype. This class is given as an
 * parameterObject to the ChartCreationClass, at the moment either
 * SplatterChartOptions or BarChartOptions.
 */
@Entity
@Table(name = "ChartConfiguration")
public class ChartConfiguration extends Persistable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ID;

	@JoinColumn(name = "EventType")
	@ManyToOne(cascade = CascadeType.PERSIST)
	private EapEventType eventType;

	@Column(name = "attributeName")
	private String attributeName;

	@Column(name = "attributeType")
	@Enumerated(EnumType.STRING)
	private AttributeTypeEnum attributeType;

	@Column(name = "title")
	private String title;

	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	private ChartTypeEnum type;

	@Column(name = "rangeSize")
	private Integer rangeSize = 1;

	/**
	 * Default Constructor for JPA
	 */
	public ChartConfiguration() {
	}

	/**
	 * Creates a new chart configuration. For integer values (therefore the
	 * rangesize).
	 * 
	 * @param eventType
	 * @param attributeName
	 * @param attributeType
	 * @param title
	 * @param type
	 * @param rangeSize
	 */
	public ChartConfiguration(final EapEventType eventType, final String attributeName,
			final AttributeTypeEnum attributeType, final String title, final ChartTypeEnum type, final Integer rangeSize) {
		this.eventType = eventType;
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.title = title;
		this.type = type;
		this.rangeSize = rangeSize;
	}

	/**
	 * Creates a new chart configuration. For string values (therefore without
	 * rangesize).
	 * 
	 * @param eventType
	 * @param attributeName
	 * @param attributeType
	 * @param title
	 * @param type
	 */
	public ChartConfiguration(final EapEventType eventType, final String attributeName,
			final AttributeTypeEnum attributeType, final String title, final ChartTypeEnum type) {
		this.eventType = eventType;
		this.attributeName = attributeName;
		this.attributeType = attributeType;
		this.title = title;
		this.type = type;
	}

	// Getter and Setter

	public Integer getRangeSize() {
		return this.rangeSize;
	}

	public void setRangeSize(final Integer rangeSize) {
		this.rangeSize = rangeSize;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	public EapEventType getEventType() {
		return this.eventType;
	}

	public void setEventType(final EapEventType eventType) {
		this.eventType = eventType;
	}

	public String getAttributeName() {
		return this.attributeName;
	}

	public void setAttributeName(final String attributeName) {
		this.attributeName = attributeName;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public ChartTypeEnum getType() {
		return this.type;
	}

	public void setType(final ChartTypeEnum type) {
		this.type = type;
	}

	public AttributeTypeEnum getAttributeType() {
		return this.attributeType;
	}

	public void setAttributeType(final AttributeTypeEnum attributeType) {
		this.attributeType = attributeType;
	}

	// JPA-Methods

	/**
	 * Finds all chart configurations from database.
	 * 
	 * @return all chart configurations
	 */
	public static List<ChartConfiguration> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t FROM ChartConfiguration t");
		return q.getResultList();
	}

	/**
	 * Finds chart configurations by attribute.
	 * 
	 * @param columnName
	 * @param value
	 * @return chart configurations that matche the attribute condition
	 */
	private static List<ChartConfiguration> findByAttribute(final String columnName, final String value) {
		final Query query = Persistor.getEntityManager()
				.createNativeQuery("SELECT * FROM ChartConfiguration WHERE " + columnName + " = '" + value + "'",
						ChartConfiguration.class);
		return query.getResultList();
	}

	/**
	 * Finds all chart configurations for an event type from database.
	 * 
	 * @param eventType
	 * @return all chart configuration for an event type
	 */
	public static List<ChartConfiguration> findByEventType(final EapEventType eventType) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM ChartConfiguration WHERE EventType = " + eventType.getID(), ChartConfiguration.class);
		return query.getResultList();
	}

	/**
	 * Finds chart configuration by ID from database.
	 * 
	 * @param ID
	 * @return chart configuration
	 */
	public static ChartConfiguration findByID(final int ID) {
		final List<ChartConfiguration> list = ChartConfiguration.findByAttribute("ID", new Integer(ID).toString());
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Deletes all chart configuration from the database.
	 */
	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM ChartConfiguration");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

}
