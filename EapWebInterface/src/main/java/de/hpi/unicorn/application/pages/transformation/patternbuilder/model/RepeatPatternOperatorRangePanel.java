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
import de.hpi.unicorn.transformation.element.PatternOperatorElement;
import de.hpi.unicorn.transformation.element.RangeElement;

public class RepeatPatternOperatorRangePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final Form<Void> layoutForm;
	private final int matchCount;
	private final TextField<Integer> matchCountInput;

	public RepeatPatternOperatorRangePanel(final String id, final PatternOperatorElement element,
			final AdvancedTransformationRuleEditorPanel panel) {
		super(id);

		this.layoutForm = new Form<Void>("layoutForm");

		final RangeElement rangeElement = element.getRangeElement();

		this.matchCount = rangeElement.getLeftEndpoint();

		this.matchCountInput = new TextField<Integer>("matchCountInput", new PropertyModel<Integer>(this, "matchCount"));
		final OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = 2251803290291534439L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				rangeElement.setLeftEndpoint(RepeatPatternOperatorRangePanel.this.matchCount);
				target.add(panel.getAttributeTreePanel().getAttributeTreeTable());
			}
		};
		this.matchCountInput.add(onChangeAjaxBehavior);
		this.matchCountInput.setOutputMarkupId(true);
		this.layoutForm.add(this.matchCountInput);

		this.add(this.layoutForm);
	}
}
