/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.model;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import de.hpi.unicorn.application.components.tree.TreeTableProvider;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.PatternBuilderPanel;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;
import de.hpi.unicorn.transformation.element.EventTypeElement;
import de.hpi.unicorn.transformation.element.FilterExpressionElement;
import de.hpi.unicorn.transformation.element.FilterExpressionOperatorEnum;

public class EventTypeElementOptionsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final Form<Void> layoutForm;
	private final EventTypeElement element;
	private final TreeTableProvider<Serializable> provider;
	private final TransformationPatternTree tree;
	private final PatternElementTreeTable table;
	private final PatternBuilderPanel panel;

	public EventTypeElementOptionsPanel(final String id, final EventTypeElement element,
			final TransformationPatternTree tree, final TreeTableProvider<Serializable> provider,
			final PatternElementTreeTable table, final PatternBuilderPanel panel) {
		super(id);

		this.element = element;
		this.provider = provider;
		this.tree = tree;
		this.table = table;
		this.panel = panel;

		this.layoutForm = new Form<Void>("layoutForm");
		this.buildAddFilterExpressionButton();
		// buildEventTypeDropDownChoice();
		// buildAddEventTypeElementButton();

		this.add(this.layoutForm);
	}

	private void buildAddFilterExpressionButton() {
		final AjaxButton addFilterExpressionButton = new AjaxButton("addFilterExpressionButton", this.layoutForm) {
			private static final long serialVersionUID = -2611608162033482853L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				final FilterExpressionElement newFilterExpressionElement = new FilterExpressionElement(
						((TreeTableProvider<Serializable>) EventTypeElementOptionsPanel.this.table.getProvider())
								.getNextID(),
						FilterExpressionOperatorEnum.EQUALS);
				newFilterExpressionElement.setParent(EventTypeElementOptionsPanel.this.element);
				EventTypeElementOptionsPanel.this.tree.addElement(newFilterExpressionElement);
				EventTypeElementOptionsPanel.this.provider.setRootElements(EventTypeElementOptionsPanel.this.tree
						.getRoots());
				EventTypeElementOptionsPanel.this.table.getSelectedElements().clear();
				target.add(EventTypeElementOptionsPanel.this.table);
				EventTypeElementOptionsPanel.this.panel.updateOnTreeElementSelection(target);
			}
		};
		this.layoutForm.add(addFilterExpressionButton);
	}

	// private void buildEventTypeDropDownChoice() {
	// List<EapEventType> eventTypes = EapEventType.findAll();
	// eventTypeDropDownChoice = new
	// DropDownChoice<EapEventType>("eventTypeDropDownChoice", new
	// Model<EapEventType>(), eventTypes);
	// eventTypeDropDownChoice.setOutputMarkupId(true);
	// layoutForm.add(eventTypeDropDownChoice);
	// }
	//
	// private void buildAddEventTypeElementButton() {
	// AjaxButton addEventTypeElementButton = new
	// AjaxButton("addEventTypeElementButton", layoutForm) {
	// private static final long serialVersionUID = -2611608162033482853L;
	// @Override
	// public void onSubmit(AjaxRequestTarget target, Form<?> form) {
	// if (eventTypeDropDownChoice.getModelObject() != null) {
	// EventTypeElement newEventTypeElement = new
	// EventTypeElement(((TreeTableProvider<Serializable>)
	// table.getProvider()).getNextID(),
	// eventTypeDropDownChoice.getModelObject());
	// newEventTypeElement.setParent(element);
	// tree.addElement(newEventTypeElement);
	// provider.setRootElements(tree.getRoots());
	// table.getSelectedElements().clear();
	// target.add(table);
	// }
	// }
	// };
	// layoutForm.add(addEventTypeElementButton);
	// }
}
