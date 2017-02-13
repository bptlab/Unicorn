/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.model;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.pages.transformation.AdvancedTransformationRuleEditorPanel;
import de.hpi.unicorn.transformation.element.PatternOperatorElement;

public class EveryDistinctPatternOperatorPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final Form<Void> layoutForm;
	private final WebMarkupContainer distinctAttributesMarkupContainer;
	private final List<String> distinctAttributes;

	public EveryDistinctPatternOperatorPanel(final String id, final PatternOperatorElement element,
			final AdvancedTransformationRuleEditorPanel panel) {
		super(id);

		this.layoutForm = new Form<Void>("layoutForm");

		this.distinctAttributes = element.getDistinctAttributes();

		final AjaxButton addDistinctAttributeButton = new AjaxButton("addDistinctAttributeButton", this.layoutForm) {
			private static final long serialVersionUID = -118988274912205531L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				EveryDistinctPatternOperatorPanel.this.distinctAttributes.add(new String());
				target.add(EveryDistinctPatternOperatorPanel.this.distinctAttributesMarkupContainer);
			}
		};
		addDistinctAttributeButton.setOutputMarkupId(true);
		this.layoutForm.add(addDistinctAttributeButton);

		final ListView<String> distinctAttributesListView = new ListView<String>("distinctAttributesListView",
				this.distinctAttributes) {
			private static final long serialVersionUID = 4168798264053898499L;

			@Override
			protected void populateItem(final ListItem<String> item) {

				final AttributeExpressionTextField distinctAttributeInput = new AttributeExpressionTextField(
						"distinctAttributeInput", new Model<String>(), panel.getPatternBuilderPanel().getPatternTree());
				final OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {

					private static final long serialVersionUID = 2339672763583311932L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						item.setModelObject(distinctAttributeInput.getModelObject());
					}
				};
				distinctAttributeInput.setModelObject(item.getModelObject());
				distinctAttributeInput.add(onChangeAjaxBehavior);
				distinctAttributeInput.setOutputMarkupId(true);
				item.add(distinctAttributeInput);

				final AjaxButton removeDistinctButton = new AjaxButton("removeDistinctAttributeButton",
						EveryDistinctPatternOperatorPanel.this.layoutForm) {
					private static final long serialVersionUID = -4244320500409194238L;

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
						EveryDistinctPatternOperatorPanel.this.distinctAttributes.remove(item.getModelObject());
						target.add(EveryDistinctPatternOperatorPanel.this.distinctAttributesMarkupContainer);
					}
				};
				item.add(removeDistinctButton);
			}
		};

		this.distinctAttributesMarkupContainer = new WebMarkupContainer("distinctAttributesMarkupContainer");
		this.distinctAttributesMarkupContainer.add(distinctAttributesListView);
		this.distinctAttributesMarkupContainer.setOutputMarkupId(true);
		this.layoutForm.addOrReplace(this.distinctAttributesMarkupContainer);

		this.add(this.layoutForm);
	}
}
