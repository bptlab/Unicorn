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
import org.apache.wicket.extensions.markup.html.repeater.util.ProviderSubset;
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
public class MultiSelectTreeTable<T, S> extends LabelTreeTable<T, S> {

	private static final long serialVersionUID = 1L;
	protected ProviderSubset<T> selectedElements;
	private final MultiSelectTreeTable<T, S> multiSelectTree;

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
	public MultiSelectTreeTable(final String id, final List<? extends IColumn<T, S>> columns,
			final ITreeProvider<T> provider, final long rowsPerPage, final IModel<Set<T>> state) {
		super(id, columns, provider, rowsPerPage, state);
		this.multiSelectTree = this;
		this.selectedElements = new ProviderSubset<T>(provider, false);
	}

	public int numberOfSelectedElements() {
		return this.selectedElements.size();
	}

	public ProviderSubset<T> getSelectedElements() {
		return this.selectedElements;
	}

	protected boolean isSelected(final T element) {
		return this.selectedElements.contains(element);
	}

	protected void toggle(final T element, final AbstractTree<T> tree, final AjaxRequestTarget target) {
		if (this.isSelected(element)) {
			this.selectedElements.remove(element);
		} else {
			this.selectedElements.add(element);
		}
		tree.updateNode(element, target);
	}

	@Override
	protected Component newContentComponent(final String id, final IModel<T> model) {
		return new TreeLinkLabel<T>(id, this, model) {

			private static final long serialVersionUID = 4384788964095089896L;

			@Override
			protected void onClick(final AjaxRequestTarget target) {
				MultiSelectTreeTable.this.multiSelectTree.toggle(this.getModelObject(),
						MultiSelectTreeTable.this.multiSelectTree, target);
			}

			@Override
			protected boolean isSelected() {
				return MultiSelectTreeTable.this.multiSelectTree.isSelected(this.getModelObject());
			}
		};
	}
}
