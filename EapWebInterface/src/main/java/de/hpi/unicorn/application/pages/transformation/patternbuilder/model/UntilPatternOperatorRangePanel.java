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
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.transformation.element.PatternOperatorElement;
import de.hpi.unicorn.transformation.element.RangeElement;

public class UntilPatternOperatorRangePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private Form<Void> layoutForm;
	private int leftEndpoint;
	private AjaxCheckBox leftEndpointEnabledCheckBox;
	private TextField<Integer> leftEndpointInput;
	private int rightEndpoint;
	private TextField<Integer> rightEndpointInput;
	private AjaxCheckBox rightEndpointEnabledCheckBox;

	public UntilPatternOperatorRangePanel(final String id, final PatternOperatorElement element) {
		super(id);

		this.layoutForm = new Form<Void>("layoutForm");

		final RangeElement rangeElement = element.getRangeElement();

		this.leftEndpoint = rangeElement.getLeftEndpoint();

		this.leftEndpointInput = new TextField<Integer>("leftEndpointInput", new PropertyModel<Integer>(this,
				"leftEndpoint")) {
			private static final long serialVersionUID = -3575218222042227551L;

			@Override
			public boolean isEnabled() {
				return UntilPatternOperatorRangePanel.this.leftEndpointEnabledCheckBox.getModelObject();
			}
		};
		OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = 8789007504544472059L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				rangeElement.setLeftEndpoint(UntilPatternOperatorRangePanel.this.leftEndpoint);
			}
		};
		this.leftEndpointInput.add(onChangeAjaxBehavior);
		this.leftEndpointInput.setOutputMarkupId(true);
		this.layoutForm.add(this.leftEndpointInput);

		this.leftEndpointEnabledCheckBox = new AjaxCheckBox("leftEndpointEnabledCheckbox", Model.of(rangeElement
				.getLeftEndpoint() != -1)) {
			private static final long serialVersionUID = -8207035371422899809L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (UntilPatternOperatorRangePanel.this.leftEndpointEnabledCheckBox.getModelObject()) {
					UntilPatternOperatorRangePanel.this.leftEndpoint = 0;
				} else {
					UntilPatternOperatorRangePanel.this.leftEndpoint = -1;
				}
				rangeElement.setLeftEndpoint(UntilPatternOperatorRangePanel.this.leftEndpoint);
				target.add(UntilPatternOperatorRangePanel.this.leftEndpointInput);
			}
		};
		this.leftEndpointEnabledCheckBox.setOutputMarkupId(true);
		this.layoutForm.add(this.leftEndpointEnabledCheckBox);

		this.rightEndpoint = rangeElement.getRightEndpoint();

		this.rightEndpointInput = new TextField<Integer>("rightEndpointInput", new PropertyModel<Integer>(this,
				"rightEndpoint")) {
			private static final long serialVersionUID = 4121692531784473397L;

			@Override
			public boolean isEnabled() {
				return UntilPatternOperatorRangePanel.this.rightEndpointEnabledCheckBox.getModelObject();
			}
		};
		onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = 68845840865685483L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				rangeElement.setRightEndpoint(UntilPatternOperatorRangePanel.this.rightEndpoint);
			}
		};
		this.rightEndpointInput.add(onChangeAjaxBehavior);
		this.rightEndpointInput.setOutputMarkupId(true);
		this.layoutForm.add(this.rightEndpointInput);

		this.rightEndpointEnabledCheckBox = new AjaxCheckBox("rightEndpointEnabledCheckbox", Model.of(rangeElement
				.getLeftEndpoint() != -1)) {
			private static final long serialVersionUID = -7937834776333473869L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (UntilPatternOperatorRangePanel.this.rightEndpointEnabledCheckBox.getModelObject()) {
					UntilPatternOperatorRangePanel.this.rightEndpoint = 1;
				} else {
					UntilPatternOperatorRangePanel.this.rightEndpoint = -1;
				}
				rangeElement.setRightEndpoint(UntilPatternOperatorRangePanel.this.rightEndpoint);
				target.add(UntilPatternOperatorRangePanel.this.rightEndpointInput);
			}
		};
		this.rightEndpointEnabledCheckBox.setOutputMarkupId(true);
		this.layoutForm.add(this.rightEndpointEnabledCheckBox);

		this.add(this.layoutForm);
	}
}
