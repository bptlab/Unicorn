/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.application.pages.input.model.EventAttributeProvider;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

public class AttributeTypeDropDownChoicePanel extends Panel {

	private static final long serialVersionUID = 1L;
	protected AttributeTypeEnum attributeType;

	public AttributeTypeDropDownChoicePanel(final String id, final TypeTreeNode attribute,
			final boolean dropDownChoiceEnabled, final EventAttributeProvider dataProvider) {
		super(id);
		final Form<Void> layoutForm = new Form<Void>("layoutForm");

		final List<AttributeTypeEnum> attributeTypes = new ArrayList<AttributeTypeEnum>();
		if (attribute.getType() != null) {
			switch (attribute.getType()) {
			case DATE:
				attributeTypes.add(AttributeTypeEnum.DATE);
				attributeTypes.add(AttributeTypeEnum.STRING);
				break;
			case FLOAT:
				attributeTypes.add(AttributeTypeEnum.FLOAT);
				attributeTypes.add(AttributeTypeEnum.STRING);
				break;
			case INTEGER:
				attributeTypes.add(AttributeTypeEnum.INTEGER);
				attributeTypes.add(AttributeTypeEnum.FLOAT);
				attributeTypes.add(AttributeTypeEnum.STRING);
				break;
			default:
				attributeTypes.add(AttributeTypeEnum.STRING);
			}
		}

		final DropDownChoice<AttributeTypeEnum> attributeTypeDropDownChoice = new DropDownChoice<AttributeTypeEnum>(
				"attributeTypeDropDownChoice", new PropertyModel<AttributeTypeEnum>(this, "attributeType"),
				attributeTypes);
		attributeTypeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				attribute.setType(AttributeTypeDropDownChoicePanel.this.attributeType);
			}
		});

		attributeTypeDropDownChoice.setEnabled(dropDownChoiceEnabled);

		layoutForm.add(attributeTypeDropDownChoice);
		this.add(layoutForm);
	}
}
