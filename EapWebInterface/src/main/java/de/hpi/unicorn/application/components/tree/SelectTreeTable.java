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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;

/**
 * tree table component for visualization of hierarchical elements nodes are
 * selectable
 * 
 * @param <T>
 *            the type of nodes to be stored in the tree
 * @param <S>
 *            the type of the sort property
 */
public class SelectTreeTable<T, S> extends LabelTreeTable<T, S> {

	private static final long serialVersionUID = 1L;
	private IModel<T> selectedElement;
	private final SelectTreeTable<T, S> selectTree;
	private final ITreeProvider<T> provider;

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
	public SelectTreeTable(final String id, final List<? extends IColumn<T, S>> columns,
			final ITreeProvider<T> provider, final long rowsPerPage, final IModel<Set<T>> state) {
		super(id, columns, provider, rowsPerPage, state);
		this.provider = provider;
		this.selectTree = this;
	}

	public T getSelectedElement() {
		if (this.selectedElement == null) {
			return null;
		}
		return this.selectedElement.getObject();
	}

	protected boolean isSelected(final T element) {
		return this.selectedElement != null && this.selectedElement.equals(this.provider.model(element));
	}

	protected void select(final T element, final AbstractTree<T> tree, final AjaxRequestTarget target) {
		if (this.isSelected(element)) {
			this.selectedElement = null;
		} else {
			if (this.selectedElement != null) {
				tree.updateNode(this.selectedElement.getObject(), target);
				this.selectedElement = null;
			}
			this.selectedElement = this.provider.model(element);
		}
		tree.updateNode(element, target);
	}

	@Override
	protected Component newContentComponent(final String id, final IModel<T> model) {
		return new TreeLinkLabel<T>(id, this, model) {

			private static final long serialVersionUID = 1569507778098604348L;

			@Override
			protected void onClick(final AjaxRequestTarget target) {
				SelectTreeTable.this.selectTree.select(this.getModelObject(), SelectTreeTable.this.selectTree, target);
			}

			@Override
			protected boolean isSelected() {
				return SelectTreeTable.this.selectTree.isSelected(this.getModelObject());
			}
		};
	}
}
