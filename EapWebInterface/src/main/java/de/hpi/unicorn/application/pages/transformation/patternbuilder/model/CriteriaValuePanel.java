/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.model;

import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.transformation.collection.TransformationPatternTree;

public class CriteriaValuePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final Form<Void> layoutForm;
	private final String attributeExpression;
	private final Map<String, String> criteriaAttributesAndValues;
	private final TransformationPatternTree patternTree;

	public CriteriaValuePanel(final String id, final String attributeExpression,
			final Map<String, String> criteriaAttributesAndValues, final TransformationPatternTree patternTree) {
		super(id);

		this.attributeExpression = attributeExpression;
		this.criteriaAttributesAndValues = criteriaAttributesAndValues;
		this.patternTree = patternTree;

		this.layoutForm = new Form<Void>("layoutForm");
		this.buildCriteriaValueInput();

		this.add(this.layoutForm);
	}

	private void buildCriteriaValueInput() {
		final AttributeExpressionTextField criteriaValueInput = new AttributeExpressionTextField("criteriaValueInput",
				new Model<String>(), this.patternTree);
		criteriaValueInput.setOutputMarkupId(true);

		final OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = -5737941362786901904L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (criteriaValueInput.getModelObject() == null || criteriaValueInput.getModelObject().trim().isEmpty()) {
					CriteriaValuePanel.this.criteriaAttributesAndValues
							.remove(CriteriaValuePanel.this.attributeExpression);
				} else {
					CriteriaValuePanel.this.criteriaAttributesAndValues.put(
							CriteriaValuePanel.this.attributeExpression, criteriaValueInput.getModelObject());
				}
			}
		};
		criteriaValueInput.add(onChangeAjaxBehavior);
		criteriaValueInput.setModelObject(this.criteriaAttributesAndValues.get(this.attributeExpression));

		this.layoutForm.add(criteriaValueInput);
	}
}
