/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.tree;

import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

/**
 * tree component for visualization of hierarchical elements nodes are not
 * selectable
 * 
 * @param <T>
 *            the type of nodes to be stored in the tree
 */
public class LabelTree<T> extends NestedTree<T> {

	/**
	 * constructor
	 * 
	 * @param id
	 *            wicket identifier used in the corresponding HTML file
	 * @param provider
	 *            see de.hpi.unicorn.application.components.tree.NestedTreeProvider
	 * @param state
	 *            see
	 *            de.hpi.unicorn.application.components.tree.NestedTreeExpansionModel
	 */
	public LabelTree(final String id, final ITreeProvider<T> provider, final IModel<Set<T>> state) {
		super(id, provider, state);
		this.setTheme();
	}

	@Override
	protected Component newContentComponent(final String id, final IModel<T> model) {
		return new Label(id, model);
	}

	protected void setTheme() {
		this.add(new Behavior() {
			Behavior theme = new HumanTheme();

			@Override
			public void onComponentTag(final Component component, final ComponentTag tag) {
				this.theme.onComponentTag(component, tag);
			}

			@Override
			public void renderHead(final Component component, final IHeaderResponse response) {
				this.theme.renderHead(component, response);
			}
		});
	}

}
