/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import de.hpi.unicorn.notification.RestNotificationRule;
import org.apache.xerces.dom.ElementImpl;
import org.w3c.dom.Node;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPOnDemandQueryResult;
import com.espertech.esper.client.EPStatementSyntaxException;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.soda.EPStatementObjectModel;

import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.monitoring.QueryMonitoringPoint;
import de.hpi.unicorn.notification.NotificationRuleForQuery;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

/**
 * encapsulate Queries for saving and logging
 */
@Entity
@Table(name = "QueryWrapper")
public class QueryWrapper extends Persistable {

	private static final long serialVersionUID = 7452081036927643770L;
	public static final int maxContentSize = 2097152; // 2Mb

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ID;

	@Temporal(TemporalType.TIMESTAMP)
	Date timestamp = null;

	@Column(name = "TITLE")
	protected String title;

	@Lob
	@Column(name = "QUERY", length = 15000)
	private String esperQuery;

	@Lob
	@Column(name = "SPARQLQUERY", length = 15000)
	private String sparqlQuery;

	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	private QueryTypeEnum type;

	@ElementCollection
	@Column(name = "QueryLogs", length = 15000)
	private final List<String> log;

	@Transient
	private String statementName;

	/**
	 * Default-Constructor for JPA.
	 */
	public QueryWrapper() {
		this.ID = 0;
		this.title = "";
		this.esperQuery = "";
		this.timestamp = new Timestamp(System.currentTimeMillis());
		this.log = new ArrayList<String>();
	}

	public QueryWrapper(final String title, final String queryString, final QueryTypeEnum type) {
		this();
		this.title = title;
		this.type = type;
		this.setQuery(queryString);
	}

	public QueryWrapper(final String title, final String queryString, final QueryTypeEnum type,
			final Timestamp timestamp) {
		this(title, queryString, type);
		this.timestamp = timestamp;
	}

	// Getter and Setter

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getQueryString() {
		final StringBuffer sb = new StringBuffer();
		sb.append(this.esperQuery);
		if (this.sparqlQuery != null) {
			sb.append(" WHERE { " + this.sparqlQuery + " }");
		}
		return sb.toString();
	}

	public void setQuery(final String query) {
		if (query != null) {
			final int beginOfSparqlQuery = query.toUpperCase().indexOf("WHERE {");
			final int endOfSparqlQuery = query.indexOf("}");
			if (beginOfSparqlQuery < 0) {
				this.esperQuery = query;
				this.sparqlQuery = null;
			} else {
				this.esperQuery = query.substring(0, beginOfSparqlQuery);
				this.sparqlQuery = query.substring(beginOfSparqlQuery + 7, endOfSparqlQuery);
			}
		}
	}

	public String getEsperQuery() {
		return this.esperQuery;
	}

	public void setEsperQuery(final String query) {
		this.esperQuery = query;
	}

	public boolean hasSparqlQuery() {
		if (this.sparqlQuery == null || this.sparqlQuery.isEmpty()) {
			return false;
		}
		return true;
	}

	public String getSparqlQuery() {
		return this.sparqlQuery;
	}

	public void setSparqlQuery(final String sparqlQuery) {
		this.sparqlQuery = sparqlQuery;
	}

	public QueryTypeEnum getType() {
		return this.type;
	}

	public boolean isLiveQuery() {
		return this.type == QueryTypeEnum.LIVE;
	}

	public boolean isOnDemandQuery() {
		return this.type == QueryTypeEnum.ONDEMAND;
	}

	public List<String> getLog() {
		return this.log;
	}

	public String getPrintableLog() {
		final StringBuffer logString = new StringBuffer();
		for (final String string : this.log) {
			logString.append(string + System.getProperty("line.separator"));
		}
		return logString.toString();
	}

	public int getNumberOfLogEntries() {
		return this.log.size();
	}

	public void addEntryToLog(final String logentry) {
		this.log.add(logentry);
		this.merge();
	}

	public String getStatementName() {
		if (this.statementName == null) {
			this.statementName = this.ID + "_" + this.title;
		}
		return this.statementName;
	}

	public void setStatementName(final String statementName) {
		this.statementName = statementName;
	}

	@Override
	public String toString() {
		return this.title + " (" + this.ID + ")";
	}

	/**
	 * Executes the query and returns the result. This only works for on-demand
	 * queries. Live queries will return null
	 *
	 * @return
	 */
	public String execute() {
		final StreamProcessingAdapter esper = StreamProcessingAdapter.getInstance();
		if (this.isLiveQuery()) {
			return null;
		}
		EPOnDemandQueryResult result = null;
		// TODO evaluate semantic query part (if existent) for all events
		// only query those events, that passed the semantic test
		final EPStatementObjectModel statement = esper.getEsperAdministrator().compileEPL(this.esperQuery);
		result = esper.getEsperRuntime().executeQuery(statement);

		// print results
		final StringBuffer buffer = new StringBuffer();
		buffer.append("Number of events found: " + result.getArray().length);
		final Iterator<EventBean> i = result.iterator();
		while (i.hasNext()) {
			buffer.append(System.getProperty("line.separator"));
			final EventBean next = i.next();
			if (next.getUnderlying() instanceof ElementImpl) {
				final ElementImpl event = (ElementImpl) next.getUnderlying();
				buffer.append("{");
				for (int k = 0; k < event.getChildNodes().getLength(); k++) {
					final Node node = event.getChildNodes().item(k);
					buffer.append(node.getNodeName() + "=" + node.getFirstChild().getNodeValue());
					if (k + 1 < event.getChildNodes().getLength()) {
						buffer.append(", ");
					}
				}
				buffer.append("}");
			} else {
				buffer.append(next.getUnderlying());
			}
		}
		return buffer.toString();
	}

	/**
	 * register query to esper if the query is a live query
	 *
	 * @return
	 */
	public LiveQueryListener addToEsper() throws EPException {
		return StreamProcessingAdapter.getInstance().addLiveQuery(this);
	}

	/**
	 * checks the syntax of on-demand queries.
	 *
	 * @throws EPStatementSyntaxException
	 */
	public void validate() throws EPStatementSyntaxException {
		StreamProcessingAdapter.getInstance().getEsperRuntime().prepareQuery(this.esperQuery);
	}

	// JPA-Methods

	/**
	 * search query with the title in the database and returns it
	 *
	 * @param title
	 * @return
	 */
	public static QueryWrapper findQueryByTitle(final String title) {
		final EntityManager em = Persistor.getEntityManager();
		final Query query = em.createNativeQuery("SELECT * FROM QueryWrapper WHERE Title = '" + title + "'",
				QueryWrapper.class);
		try {
			return (QueryWrapper) query.getResultList().get(0);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Returns all live queries found in the database.
	 *
	 * @return
	 */
	public static List<QueryWrapper> getAllLiveQueries() {
		final Query dbQuery = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM QueryWrapper WHERE Type='LIVE'", QueryWrapper.class);
		return dbQuery.getResultList();
		// List<QueryWrapper> liveQueryList = new ArrayList<QueryWrapper>();
		// List<QueryWrapper> queryResult = dbQuery.getResultList();
		// for (int i = 0; i < queryResult.size(); i++) {
		// QueryWrapper queryWrapper = queryResult.get(i);
		// if (queryWrapper.isLiveQuery()) {
		// liveQueryList.add(queryWrapper);
		// }
		// }
		// return liveQueryList;
	}

	/**
	 * returns all on-demand queries on the database
	 *
	 * @return
	 */
	public static List<QueryWrapper> getAllOnDemandQueries() {
		final EntityManager em = Persistor.getEntityManager();
		final Query query = em.createNativeQuery("SELECT * FROM QueryWrapper", QueryWrapper.class);
		final List<QueryWrapper> liveQueryList = new ArrayList<QueryWrapper>();
		try {
			for (int i = 0; i < query.getResultList().size(); i++) {
				final QueryWrapper queryWrapper = (QueryWrapper) query.getResultList().get(i);
				if (queryWrapper.isOnDemandQuery()) {
					liveQueryList.add((QueryWrapper) query.getResultList().get(i));
				}
			}
			return liveQueryList;
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * delete query which has the title
	 *
	 * @param title
	 * @return
	 */
	public static QueryWrapper removeQueryWithTitle(final String title) {
		return QueryWrapper.findQueryByTitle(title).remove();
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAllTitlesOfOnDemandQueries() {
		final EntityManager em = Persistor.getEntityManager();
		// System.out.println("select TITLE from QueryWrapper where type = '"+
		// QueryTypeEnum.ONDEMAND +"'");
		final Query query = em.createNativeQuery("select TITLE from QueryWrapper where type = '"
				+ QueryTypeEnum.ONDEMAND + "'");
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAllTitlesOfQueries() {
		final EntityManager em = Persistor.getEntityManager();
		final Query query = em.createNativeQuery("select TITLE from QueryWrapper");
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<NotificationRuleForQuery> getNotificationRulesForQuery() {
		final EntityManager em = Persistor.getEntityManager();
		final Query query = em.createNativeQuery("select * from NotificationRule WHERE Disc = 'Q' AND QUERY_ID = '"
				+ this.getID() + "'", NotificationRuleForQuery.class);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<RestNotificationRule> getRestNotificationRules() {
		final EntityManager em = Persistor.getEntityManager();
		final Query query = em.createNativeQuery("select * from NotificationRule WHERE Disc = 'R' AND QUERY_ID = '"
				+ this.getID() + "'", RestNotificationRule.class);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public static List<String> getAllTitlesOfLiveQueries() {
		final EntityManager em = Persistor.getEntityManager();
		final Query query = em.createNativeQuery("select TITLE from QueryWrapper where type = 'LIVE'");
		return query.getResultList();
	}

	@Override
	public QueryWrapper save() {
		return (QueryWrapper) super.save();
	}

	public static ArrayList<QueryWrapper> save(final ArrayList<QueryWrapper> queries) {
		try {
			final EntityManager entityManager = Persistor.getEntityManager();
			entityManager.getTransaction().begin();
			for (final QueryWrapper query : queries) {
				entityManager.persist(query);
			}
			entityManager.getTransaction().commit();
			return queries;
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean remove(final ArrayList<QueryWrapper> queries) {
		boolean removed = true;
		for (final QueryWrapper query : queries) {
			removed = (query.remove() != null);
		}
		return removed;
	}

	@Override
	public QueryWrapper remove() {
		// remove from Esper
		StreamProcessingAdapter.getInstance().remove(this);
		// remove notification rules
		for (final NotificationRuleForQuery notification : this.getNotificationRulesForQuery()) {
			notification.remove();
		}
		// remove monitoring points
		for (final QueryMonitoringPoint point : QueryMonitoringPoint.findByQuery(this)) {
			point.remove();
		}

		return (QueryWrapper) super.remove();
	}

	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM QueryWrapper");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			// System.out.println(ex.getMessage());
		}
	}

}