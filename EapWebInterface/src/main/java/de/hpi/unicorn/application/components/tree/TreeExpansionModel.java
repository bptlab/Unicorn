/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.tree;

import java.util.Set;

import org.apache.wicket.model.AbstractReadOnlyModel;

import de.hpi.unicorn.event.collection.EventTreeElement;

/**
 * model that wraps the expansion state handler of a tree component
 */
public class TreeExpansionModel<T> extends AbstractReadOnlyModel<Set<EventTreeElement<T>>> {

	private static final long serialVersionUID = 1L;

	@Override
	public Set<EventTreeElement<T>> getObject() {
		return TreeExpansion.get();
	}
}
