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

/**
 * model that wraps the expansion state handler of a tree component
 */
public class LabelTreeExpansionModel<T> extends AbstractReadOnlyModel<Set<LabelTreeElement<T>>> {

	private static final long serialVersionUID = 1L;

	@Override
	public Set<LabelTreeElement<T>> getObject() {
		return new LabelTreeExpansion<T>().get();
	}
}
