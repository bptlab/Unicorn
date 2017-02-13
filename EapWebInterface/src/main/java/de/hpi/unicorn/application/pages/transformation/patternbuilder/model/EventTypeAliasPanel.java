/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.model;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.application.pages.transformation.AdvancedTransformationRuleEditorPanel;
import de.hpi.unicorn.transformation.element.EventTypeElement;

public class EventTypeAliasPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final EventTypeElement element;
	private final String alias;
	private final Form<Void> layoutForm;
	private final AdvancedTransformationRuleEditorPanel panel;

	public EventTypeAliasPanel(final String id, final EventTypeElement element,
			final AdvancedTransformationRuleEditorPanel panel) {
		super(id);

		this.element = element;
		this.alias = element.getAlias();
		this.panel = panel;

		this.layoutForm = new Form<Void>("layoutForm");
		this.buildEventTypeAliasInput();

		this.add(this.layoutForm);
	}

	private void buildEventTypeAliasInput() {
		final TextField<String> eventTypeAliasInput = new TextField<String>("eventTypeAliasInput",
				new PropertyModel<String>(this, "alias"));
		eventTypeAliasInput.setOutputMarkupId(true);

		final OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = -1427433442511094442L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// TODO: make sure that alias does not already exist somewhere
				EventTypeAliasPanel.this.element.setAlias(EventTypeAliasPanel.this.alias);
				target.add(EventTypeAliasPanel.this.panel.getAttributeTreePanel().getAttributeTreeTable());
			}
		};
		eventTypeAliasInput.add(onChangeAjaxBehavior);

		this.layoutForm.add(eventTypeAliasInput);
	}
}
