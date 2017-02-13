/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.espertech.esper.client.EPStatement;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.esper.StreamProcessingAdapter;

/**
 * @author micha
 */
public class PatternQuery extends QueryWrapper {

	private static final long serialVersionUID = 1L;
	private PatternQueryType patternQueryType;
	private PatternQueryListener patternQueryListener;
	private List<AbstractBPMNElement> monitoredElements;
	private PatternQuery parentQuery;
	private Set<PatternQuery> childQueries = new HashSet<PatternQuery>();
	private EPStatement epStatement;

	public PatternQuery(final String title, final String queryString, final QueryTypeEnum queryType,
			final PatternQueryType patternQueryType) {
		super(title, queryString, queryType);
		this.patternQueryType = patternQueryType;
	}

	public PatternQuery(final String title, final String queryString, final QueryTypeEnum queryType,
			final PatternQueryType patternQueryType, final List<AbstractBPMNElement> monitoredElements) {
		this(title, queryString, queryType, patternQueryType);
		this.setMonitoredElements(monitoredElements);
	}

	public PatternQueryType getPatternQueryType() {
		return this.patternQueryType;
	}

	public void setPatternQueryType(final PatternQueryType patternQueryType) {
		this.patternQueryType = patternQueryType;
	}

	public PatternQueryListener addToEsper(final StreamProcessingAdapter esper) {
		return esper.addPatternQuery(this);
	}

	public PatternQueryListener updateForEsper(final StreamProcessingAdapter esper) {
		return esper.updatePatternQuery(this);
	}

	public void setListener(final PatternQueryListener patternQueryListener) {
		this.patternQueryListener = patternQueryListener;
	}

	public PatternQueryListener getListener() {
		return this.patternQueryListener;
	}

	public void setMonitoredElements(final List<AbstractBPMNElement> monitoredElements) {
		this.monitoredElements = new ArrayList<AbstractBPMNElement>(monitoredElements);
	}

	/**
	 * Returns a ordered list of the monitored elements of the query.
	 * 
	 * @return
	 */
	public List<AbstractBPMNElement> getMonitoredElements() {
		return this.monitoredElements;
	}

	@Override
	public String toString() {
		return super.title;
	}

	public PatternQuery getParentQuery() {
		return this.parentQuery;
	}

	public void setParentQuery(final PatternQuery parentQuery) {
		this.parentQuery = parentQuery;
	}

	public Set<PatternQuery> getChildQueries() {
		return this.childQueries;
	}

	public boolean hasChildQueries() {
		return !this.childQueries.isEmpty();
	}

	public boolean hasParentQuery() {
		return this.parentQuery != null;
	}

	public void addChildQueries(final PatternQuery childQuery) {
		this.childQueries.add(childQuery);
	}

	public void removeChildQueries(final PatternQuery childQuery) {
		this.childQueries.remove(childQuery);
	}

	public void setChildQueries(final Set<PatternQuery> childQueries) {
		this.childQueries = childQueries;
	}

	public void setEPStatement(final EPStatement epStatement) {
		this.epStatement = epStatement;
	}

	public EPStatement getEPStatement() {
		return this.epStatement;
	}

}
