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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.ProviderSubset;
import org.apache.wicket.model.IModel;

/**
 * tree component for visualization of hierarchical elements multiple can be
 * selected at a time
 * 
 * @param <T>
 *            the type of nodes to be stored in the tree
 */
public class MultiSelectTree<T> extends LabelTree<T> {

	private static final long serialVersionUID = 1L;
	protected ProviderSubset<T> selectedElements;
	private final MultiSelectTree<T> multiSelectTree;

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
	public MultiSelectTree(final String id, final ITreeProvider<T> provider, final IModel<Set<T>> state) {
		super(id, provider, state);
		this.multiSelectTree = this;
		this.selectedElements = new ProviderSubset<T>(provider, false);
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

			private static final long serialVersionUID = 9187088363688868766L;

			@Override
			protected void onClick(final AjaxRequestTarget target) {
				MultiSelectTree.this.multiSelectTree.toggle(this.getModelObject(),
						MultiSelectTree.this.multiSelectTree, target);
			}

			@Override
			protected boolean isSelected() {
				return MultiSelectTree.this.multiSelectTree.isSelected(this.getModelObject());
			}
		};
	}

}
