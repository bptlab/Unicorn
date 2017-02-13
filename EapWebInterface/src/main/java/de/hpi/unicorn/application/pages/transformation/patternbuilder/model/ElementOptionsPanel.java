/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import de.hpi.unicorn.application.components.tree.TreeTableProvider;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.PatternBuilderPanel;
import de.hpi.unicorn.event.collection.EventTreeElement;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;

public class ElementOptionsPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public ElementOptionsPanel(final String id, final EventTreeElement<Serializable> element,
			final PatternBuilderPanel patternBuilderPanel) {
		super(id);

		final Form<Void> layoutForm = new Form<Void>("layoutForm");

		final TransformationPatternTree tree = patternBuilderPanel.getPatternTree();
		final PatternElementTreeTable table = patternBuilderPanel.getPatternTreeTable();
		final TreeTableProvider<Serializable> provider = patternBuilderPanel.getPatternTreeTableProvider();

		final AjaxButton moveUpButton = new AjaxButton("moveUpButton", layoutForm) {
			private static final long serialVersionUID = -3745820767717288739L;
			private List<EventTreeElement<Serializable>> relatedElements = new ArrayList<EventTreeElement<Serializable>>();

			@Override
			public boolean isVisible() {
				if (element.hasParent()) {
					this.relatedElements = element.getParent().getChildren();
				} else {
					return false;
				}
				return this.relatedElements.indexOf(element) > 0;
			}

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				Collections.swap(this.relatedElements, this.relatedElements.indexOf(element) - 1,
						this.relatedElements.indexOf(element));
				element.getParent().setChildren(this.relatedElements);
				target.add(table);
			}
		};
		layoutForm.add(moveUpButton);

		final AjaxButton moveDownButton = new AjaxButton("moveDownButton", layoutForm) {
			private static final long serialVersionUID = -3745820767717288739L;
			private List<EventTreeElement<Serializable>> relatedElements = new ArrayList<EventTreeElement<Serializable>>();

			@Override
			public boolean isVisible() {
				if (element.hasParent()) {
					this.relatedElements = element.getParent().getChildren();
				} else {
					return false;
				}
				return (this.relatedElements.size() > 1)
						&& (this.relatedElements.indexOf(element) < this.relatedElements.size() - 1);
			}

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				Collections.swap(this.relatedElements, this.relatedElements.indexOf(element),
						this.relatedElements.indexOf(element) + 1);
				element.getParent().setChildren(this.relatedElements);
				target.add(table);
			}
		};
		layoutForm.add(moveDownButton);

		final AjaxButton removeElementButton = new AjaxButton("removeElementButton", layoutForm) {
			private static final long serialVersionUID = 5743864457433235849L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				tree.removeElement(element);
				table.getSelectedElements().remove(element);
				if (!element.hasParent()) {
					provider.setRootElements(tree.getRoots());
					// } else {
					// if (!element.getParent().hasChildren()) {
					// TODO: implement intelligent removal of parent elements
					// }
				}
				target.add(table);
				patternBuilderPanel.updateOnTreeElementSelection(target);
			}
		};
		layoutForm.add(removeElementButton);

		this.add(layoutForm);
	}

}
