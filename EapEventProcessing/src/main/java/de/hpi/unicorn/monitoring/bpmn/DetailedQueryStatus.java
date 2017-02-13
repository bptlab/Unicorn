/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.bpmn;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import de.hpi.unicorn.query.PatternQuery;

public class DetailedQueryStatus implements Serializable {

	private PatternQuery query;
	private QueryStatus queryStatus;
	private Set<ViolationStatus> violationStatus;
	private Date endTime;

	public DetailedQueryStatus(final PatternQuery query, final QueryStatus queryStatus,
			final Set<ViolationStatus> violationStatus) {
		this.query = query;
		this.setQueryStatus(queryStatus);
		this.violationStatus = violationStatus;
	}

	public PatternQuery getQuery() {
		return this.query;
	}

	public void setQuery(final PatternQuery query) {
		this.query = query;
	}

	public QueryStatus getQueryStatus() {
		return this.queryStatus;
	}

	public void setQueryStatus(final QueryStatus queryStatus) {
		this.queryStatus = queryStatus;
		if (queryStatus.equals(QueryStatus.Finished) || queryStatus.equals(QueryStatus.Skipped)) {
			this.endTime = new Date();
		}
	}

	public Set<ViolationStatus> getViolationStatus() {
		return this.violationStatus;
	}

	public void setViolationStatus(final Set<ViolationStatus> violationStatus) {
		this.violationStatus = violationStatus;
	}

	public Date getEndTime() {
		return this.endTime;
	}

	@Override
	public String toString() {
		return this.query.getTitle() + " is " + this.queryStatus + ". Violations: " + this.violationStatus;
	}

}
