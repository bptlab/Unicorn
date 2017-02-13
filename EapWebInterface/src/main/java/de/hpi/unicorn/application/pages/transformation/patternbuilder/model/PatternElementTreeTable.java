/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;

import de.hpi.unicorn.application.components.tree.MultiSelectTreeTable;
import de.hpi.unicorn.application.components.tree.TreeLinkLabel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.PatternBuilderPanel;
import de.hpi.unicorn.event.collection.EventTreeElement;

/**
 * tree table component for visualization of hierarchical elements nodes are
 * selectable
 * 
 * @param <T>
 *            the type of nodes to be stored in the tree
 * @param <S>
 *            the type of the sort property
 */
public class PatternElementTreeTable extends MultiSelectTreeTable<EventTreeElement<Serializable>, String> {

	private static final long serialVersionUID = 1L;
	private final PatternElementTreeTable patternElementTree;
	private final PatternBuilderPanel patternBuilderPanel;

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
	public PatternElementTreeTable(final String id,
			final List<? extends IColumn<EventTreeElement<Serializable>, String>> columns,
			final ITreeProvider<EventTreeElement<Serializable>> provider, final long rowsPerPage,
			final IModel<Set<EventTreeElement<Serializable>>> state, final PatternBuilderPanel patternBuilderPanel) {
		super(id, columns, provider, rowsPerPage, state);
		this.patternElementTree = this;
		this.patternBuilderPanel = patternBuilderPanel;
	}

	@Override
	protected void toggle(final EventTreeElement<Serializable> element,
			final AbstractTree<EventTreeElement<Serializable>> tree, final AjaxRequestTarget target) {
		if (this.isSelected(element)) {
			this.selectedElements.remove(element);
		} else if (this.selectedElements.size() <= 1) {
			EventTreeElement<Serializable> selectedElement;
			if (!this.selectedElements.isEmpty()) {
				selectedElement = this.selectedElements.iterator().next();
				if (selectedElement.getLevel() != element.getLevel()
						|| ((selectedElement.hasParent() && element.hasParent()) && !selectedElement.getParent()
								.equals(element.getParent()))) {
					this.selectedElements.remove(selectedElement);
					tree.updateNode(selectedElement, target);
				}
			}
			this.selectedElements.add(element);
		}
		tree.updateNode(element, target);
	}

	@Override
	protected Component newContentComponent(final String id, final IModel<EventTreeElement<Serializable>> model) {
		return new TreeLinkLabel<EventTreeElement<Serializable>>(id, this, model) {

			private static final long serialVersionUID = 4384788964095089896L;

			@Override
			protected void onClick(final AjaxRequestTarget target) {
				PatternElementTreeTable.this.patternElementTree.toggle(this.getModelObject(),
						PatternElementTreeTable.this.patternElementTree, target);
				PatternElementTreeTable.this.patternBuilderPanel.updateOnTreeElementSelection(target);
			}

			@Override
			protected boolean isSelected() {
				return PatternElementTreeTable.this.patternElementTree.isSelected(this.getModelObject());
			}
		};
	}
}
