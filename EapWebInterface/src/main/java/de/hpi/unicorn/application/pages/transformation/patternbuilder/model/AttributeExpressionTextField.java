/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.model.IModel;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.event.collection.EventTreeElement;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;
import de.hpi.unicorn.transformation.element.EventTypeElement;

public class AttributeExpressionTextField extends AutoCompleteTextField<String> {

	private static final long serialVersionUID = -565229104833806474L;
	private final TransformationPatternTree patternTree;

	public AttributeExpressionTextField(final String id, final IModel<String> model,
			final TransformationPatternTree patternTree) {
		super(id, model);
		this.patternTree = patternTree;
	}

	@Override
	protected Iterator<String> getChoices(final String input) {
		return this.generateExpressionSuggestions(input);
	}

	public Iterator<String> generateExpressionSuggestions(final String input) {
		// int lastWhitespaceOfInput = input.lastIndexOf(" ");
		String partialInput;
		// if (lastWhitespaceOfInput < 0) {
		partialInput = input;
		// } else {
		// partialInput = input.substring(lastWhitespaceOfInput + 1);
		// }

		final Set<String> matchedExpressions = new HashSet<String>();
		for (final EventTreeElement<Serializable> element : this.patternTree.getElements()) {
			if (element instanceof EventTypeElement) {
				final EventTypeElement eventTypeElement = (EventTypeElement) element;
				if (eventTypeElement.hasAlias()) {
					final EapEventType eventType = ((EapEventType) eventTypeElement.getValue());
					Set<String> expressionsToAdd = new HashSet<String>();
					expressionsToAdd.add(eventTypeElement.getAlias() + "." + "Timestamp");
					expressionsToAdd.add(eventTypeElement.getAlias() + "." + "Timestamp.getTime()");
					for (final String expressionWithAlias : expressionsToAdd) {
						if (partialInput == null || partialInput.isEmpty()
								|| expressionWithAlias.toUpperCase().startsWith(partialInput.toUpperCase())) {
							matchedExpressions.add(expressionWithAlias);
							if (matchedExpressions.size() >= 10) {
								break;
							}
						}
					}
					for (final TypeTreeNode attribute : eventType.getValueTypes()) {
						expressionsToAdd = new HashSet<String>();
						expressionsToAdd.add(eventTypeElement.getAlias() + "." + attribute.getAttributeExpression());
						if (attribute.getType() == AttributeTypeEnum.DATE) {
							expressionsToAdd.add(eventTypeElement.getAlias() + "." + attribute.getAttributeExpression()
									+ ".getTime()");
						}
						for (final String expressionWithAlias : expressionsToAdd) {
							if (partialInput == null || partialInput.isEmpty()
									|| expressionWithAlias.toUpperCase().startsWith(partialInput.toUpperCase())) {
								matchedExpressions.add(expressionWithAlias);
								if (matchedExpressions.size() >= 10) {
									break;
								}
							}
						}
					}
				}
			}
		}
		return matchedExpressions.iterator();
	}

}
