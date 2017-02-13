/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.query.QueryWrapper;

/**
 * Connects a query and a process to enable monitoring without BPMN-model. This
 * monitoring point saves a percentage that represents the percent of how far a
 * process instance is when the query is triggered.
 */
@Entity
@Table(name = "QueryMonitoringPoint")
public class QueryMonitoringPoint extends Persistable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	protected int ID;

	@ManyToOne
	private CorrelationProcess process;

	@ManyToOne
	private QueryWrapper query;

	@Column(name = "percentage")
	private int percentage;

	@Column(name = "isAbsolute")
	private boolean isAbsolute;

	/**
	 * JPA-default constructor
	 */
	public QueryMonitoringPoint() {
	}

	/**
	 * Creates a new query monitoring point.
	 * 
	 * @param process
	 * @param query
	 * @param percentage
	 * @param isAbsolute
	 */
	public QueryMonitoringPoint(final CorrelationProcess process, final QueryWrapper query, final int percentage,
			final boolean isAbsolute) {
		this.process = process;
		this.query = query;
		this.percentage = percentage;
		this.isAbsolute = isAbsolute;
	}

	/**
	 * This method is called when a query is triggered. It updates the progress
	 * of the process instance.
	 * 
	 * @param instance
	 */
	public void trigger(final CorrelationProcessInstance instance) {
		if (this.isAbsolute) {
			instance.setProgress(this.percentage);
			// System.out.println(instance + " was updated to" + percentage +
			// "%");
		} else {
			instance.addToProgress(this.percentage);
			// System.out.println(instance + " was updated with " + percentage +
			// "%");
		}

	}

	// Getter and Setter

	public CorrelationProcess getProcess() {
		return this.process;
	}

	public void setProcess(final CorrelationProcess process) {
		this.process = process;
	}

	public QueryWrapper getQuery() {
		return this.query;
	}

	public void setQuery(final QueryWrapper query) {
		this.query = query;
	}

	public int getPercentage() {
		return this.percentage;
	}

	public void setPercentage(final int percentage) {
		this.percentage = percentage;
	}

	public boolean isAbsolute() {
		return this.isAbsolute;
	}

	public void setAbsolute(final boolean isAbsolute) {
		this.isAbsolute = isAbsolute;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	// JPA-Methods

	/**
	 * Returns all {@link QueryMonitoringPoint}s from the database.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<QueryMonitoringPoint> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("select t from QueryMonitoringPoint t");
		return q.getResultList();
	}

	/**
	 * Deletes all query monitoring points from the database.
	 */
	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM QueryMonitoringPoint");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			// System.out.println(ex.getMessage());
		}
	}

	/**
	 * Finds a query monitoring point with a certain ID.
	 * 
	 * @param ID
	 * @return
	 */
	public static QueryMonitoringPoint findByID(final int ID) {
		return Persistor.getEntityManager().find(QueryMonitoringPoint.class, ID);
	}

	/**
	 * Finds query monitoring points that are connected with a certain query.
	 * 
	 * @param query
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<QueryMonitoringPoint> findByQuery(final QueryWrapper query) {
		final Query q = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM QueryMonitoringPoint WHERE QUERY_ID = '" + query.getID() + "'",
				QueryMonitoringPoint.class);
		return q.getResultList();
	}

}
