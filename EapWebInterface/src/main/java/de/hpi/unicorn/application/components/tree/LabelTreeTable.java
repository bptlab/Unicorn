/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.tree;

import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.TableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * tree table component for visualization of hierarchical elements nodes are not
 * selectable
 * 
 * @param <T>
 *            the type of nodes to be stored in the tree
 * @param <S>
 *            the type of the sort property
 */
public class LabelTreeTable<T, S> extends TableTree<T, S> {

	private static final long serialVersionUID = 1L;

	/**
	 * constructor
	 * 
	 * @param id
	 *            wicket identifier used in the corresponding HTML file
	 * @param columns
	 *            list of IColumn objects
	 * @param provider
	 *            provider see
	 *            de.hpi.unicorn.application.components.tree.NestedTreeProvider
	 * @param rowsPerPage
	 *            number of rows per page
	 * @param state
	 *            state see
	 *            de.hpi.unicorn.application.components.tree.NestedTreeExpansionModel
	 */
	public LabelTreeTable(final String id, final List<? extends IColumn<T, S>> columns,
			final ITreeProvider<T> provider, final long rowsPerPage, final IModel<Set<T>> state) {
		super(id, columns, provider, rowsPerPage, state);
		this.getTable().add(new AttributeAppender("class", Model.of("table table-striped")));
		this.setTheme();
	}

	@Override
	protected Component newContentComponent(final String id, final IModel<T> model) {
		return new Label(id, model);
	}

	@Override
	protected Item<T> newRowItem(final String id, final int index, final IModel<T> model) {
		return new Item<T>(id, index, model);
	}

	protected void setTheme() {
		this.add(new Behavior() {

			private static final long serialVersionUID = 1L;

			Behavior theme = new HumanTheme();

			@Override
			public void onComponentTag(final Component component, final ComponentTag tag) {
				super.onComponentTag(component, tag);
				this.theme.onComponentTag(component, tag);
			}

			@Override
			public void renderHead(final Component component, final IHeaderResponse response) {
				this.theme.renderHead(component, response);
			}
		});
	}

}
