/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.table.EapProvider;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

/**
 * This class is the provider for event attributes.
 */
public class EventAttributeProvider extends EapProvider<TypeTreeNode> {

	private static final long serialVersionUID = 1L;
	private String timestampName;

	/**
	 * Constructor for providing event attributes.
	 */
	public EventAttributeProvider(final List<TypeTreeNode> attributes) {
		super(attributes);
		this.timestampName = new String();
	}

	public EventAttributeProvider(final List<TypeTreeNode> attributes, final String timestampName) {
		this(attributes);
		this.timestampName = timestampName;
	}

	public EventAttributeProvider(final List<TypeTreeNode> attributes, final List<TypeTreeNode> selectedAttributes) {
		super(attributes, selectedAttributes);
	}

	@Override
	public void detach() {
		// attributes = null;
	}

	@Override
	public IModel<TypeTreeNode> model(final TypeTreeNode attribute) {
		return Model.of(attribute);
	}

	public List<String> getSelectedColumnNames() {
		final ArrayList<String> selectedColumnNames = new ArrayList<String>();
		selectedColumnNames.add(this.timestampName);
		for (final TypeTreeNode attribute : this.entities) {
			if (this.selectedEntities.contains(attribute) && !attribute.getName().equals(this.timestampName)) {
				selectedColumnNames.add(attribute.getName());
			}
		}
		return selectedColumnNames;
	}

	public List<String> getSelectedAttributeExpressions() {
		final ArrayList<String> selectedAttributeExpressions = new ArrayList<String>();
		for (final TypeTreeNode attribute : this.selectedEntities) {
			selectedAttributeExpressions.add(attribute.getAttributeExpression());
		}
		return selectedAttributeExpressions;
	}

	public void setTimestampName(final String timestampName) {
		this.timestampName = timestampName;
	}

}
