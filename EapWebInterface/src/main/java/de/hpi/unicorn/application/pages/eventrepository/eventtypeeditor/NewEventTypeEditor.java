/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.eventtypeeditor;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.tree.AttributeTreeExpansion;
import de.hpi.unicorn.application.components.tree.AttributeTreeExpansionModel;
import de.hpi.unicorn.application.components.tree.AttributeTreeProvider;
import de.hpi.unicorn.application.components.tree.SelectTree;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;

/**
 * This page allows the creation of new {@link EapEventType}s.
 * 
 * @author micha
 */
public class NewEventTypeEditor extends Panel {

	private static final long serialVersionUID = 1L;
	private TextField<String> eventTypeNameInput;
	private TextField<String> timestampNameInput;
	private String eventTypeName = new String();
	private String timestampName;
	private final List<AttributeTypeEnum> attributeTypes = Arrays.asList(AttributeTypeEnum.values());
	private DropDownChoice<AttributeTypeEnum> attributeTypeDropDownChoice;
	private AttributeTypeEnum attributeType;
	private final Form<Void> layoutForm;
	private TextField<String> eventTypeAttributeNameInput;
	private String attributeName;
	private SelectTree<TypeTreeNode> eventTypeTree;
	private AttributeTypeTree eventTypeAttributesTree = new AttributeTypeTree();
	private final AbstractEapPage abstractEapPage;

	/**
	 * Constructor for a page to create new {@link EapEventType}s.
	 * 
	 * @param id
	 * @param abstractEapPage
	 */
	public NewEventTypeEditor(final String id, final AbstractEapPage abstractEapPage) {
		super(id);
		this.abstractEapPage = abstractEapPage;

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);

		this.layoutForm.add(this.buildEventTypeNameInput());
		this.layoutForm.add(this.buildTimestampNameInput());

		this.layoutForm.add(this.buildEventTypeAttributeNameInput());
		this.layoutForm.add(this.buildEventTypeAttributeTypeDropDownChoice());

		this.buildEventTypeAttributeButtons();

		this.renderOrUpdateTree();

		this.addCreateEventTypeButton();
	}

	private void buildEventTypeAttributeButtons() {
		final AjaxButton editEventTypeAttributeButton = new AjaxButton("editEventTypeAttributeButton", this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final TypeTreeNode selectedElement = NewEventTypeEditor.this.eventTypeTree.getSelectedElement();
				if (selectedElement != null) {
					NewEventTypeEditor.this.attributeName = selectedElement.getName();
					NewEventTypeEditor.this.attributeType = selectedElement.getType();

				}
				target.add(NewEventTypeEditor.this.eventTypeAttributeNameInput);
			}
		};

		this.layoutForm.add(editEventTypeAttributeButton);

		final AjaxButton deleteEventTypeAttributeButton = new AjaxButton("deleteEventTypeAttributeButton",
				this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {

				if (NewEventTypeEditor.this.eventTypeTree.getSelectedElement() != null) {
					final TypeTreeNode selectedAttribute = NewEventTypeEditor.this.eventTypeTree.getSelectedElement();
					if (!selectedAttribute.hasParent()) {
						NewEventTypeEditor.this.eventTypeAttributesTree.removeRoot(selectedAttribute);
					}
					selectedAttribute.removeAttribute();
				}
				NewEventTypeEditor.this.renderOrUpdateTree();
				target.add(NewEventTypeEditor.this.eventTypeTree);
				NewEventTypeEditor.this.attributeName = null;
				target.add(NewEventTypeEditor.this.eventTypeAttributeNameInput);
			}

		};

		this.layoutForm.add(deleteEventTypeAttributeButton);

		final AjaxButton addEventTypeAttributeButton = new AjaxButton("addEventTypeAttributeButton", this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {

				if (NewEventTypeEditor.this.attributeName != null) {
					final TypeTreeNode selectedAttribute = NewEventTypeEditor.this.eventTypeTree.getSelectedElement();
					final TypeTreeNode newAttribute = new TypeTreeNode(NewEventTypeEditor.this.attributeName,
							NewEventTypeEditor.this.attributeType);
					if (selectedAttribute == null) {
						if (!NewEventTypeEditor.this.eventTypeAttributesTree.getRoots().contains(newAttribute)) {
							NewEventTypeEditor.this.eventTypeAttributesTree.addRoot(newAttribute);
						}
					} else {
						if (!selectedAttribute.getChildren().contains(newAttribute)) {
							newAttribute.setParent(selectedAttribute);
							NewEventTypeEditor.this.attributeName = null;
						} else {
							NewEventTypeEditor.this.abstractEapPage.getFeedbackPanel().error(
									"Attribute with this name already exists in the selected node!");
							target.add(NewEventTypeEditor.this.abstractEapPage.getFeedbackPanel());
						}
					}
				}
				NewEventTypeEditor.this.renderOrUpdateTree();
				target.add(NewEventTypeEditor.this.eventTypeTree);
				target.add(NewEventTypeEditor.this.eventTypeAttributeNameInput);
			}
		};

		this.layoutForm.add(addEventTypeAttributeButton);
	}

	private void addCreateEventTypeButton() {
		final AjaxButton addEventTypeAttributeButton = new AjaxButton("createEventTypeButton", this.layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				// abfangen, dass Eventtyp nicht angegeben ist
				if (NewEventTypeEditor.this.eventTypeName == null || NewEventTypeEditor.this.eventTypeName.equals("")) {
					NewEventTypeEditor.this.abstractEapPage.getFeedbackPanel().error(
							"Please provide a name for the event type");
				} else if (!EapEventType.getAllTypeNames().contains(NewEventTypeEditor.this.eventTypeName)) { // abfangen,
																												// dass
																												// Eventtyp
																												// mit
																												// dem
					// Namen schon vorhanden ist
					EapEventType eventType;
					// try {
					// abfangen, dass Timestamp nicht angegeben ist
					if (!(NewEventTypeEditor.this.timestampName == null)
							&& !NewEventTypeEditor.this.timestampName.equals("")) {
						if (!NewEventTypeEditor.this.eventTypeAttributesTree.getAttributesAsExpression().contains(
								NewEventTypeEditor.this.timestampName)) {
							// System.out.println(eventTypeAttributesTree);
							eventType = new EapEventType(NewEventTypeEditor.this.eventTypeName,
									NewEventTypeEditor.this.eventTypeAttributesTree,
									NewEventTypeEditor.this.timestampName);
							Broker.getEventAdministrator().importEventType(eventType);
							NewEventTypeEditor.this.eventTypeAttributesTree = new AttributeTypeTree();
							NewEventTypeEditor.this.attributeName = null;
							NewEventTypeEditor.this.eventTypeName = null;
							NewEventTypeEditor.this.timestampName = null;
							NewEventTypeEditor.this.renderOrUpdateTree();
							NewEventTypeEditor.this.abstractEapPage.getFeedbackPanel().success(
									"Event type " + eventType.getTypeName() + " created");
						} else {
							NewEventTypeEditor.this.abstractEapPage.getFeedbackPanel().error(
									"The timestamp should not be equal to one of the attributes in the tree below");
						}
					} else {
						NewEventTypeEditor.this.abstractEapPage.getFeedbackPanel().error(
								"Please provide a name for the timestamp");
					}
					// } catch (RuntimeException e) {
					// abstractEapPage.getFeedbackPanel().error(e.getMessage());
					// }
				} else {
					NewEventTypeEditor.this.abstractEapPage.getFeedbackPanel().error(
							"Event type with this name already exists!");
				}
				target.add(NewEventTypeEditor.this.abstractEapPage.getFeedbackPanel());
				target.add(NewEventTypeEditor.this.eventTypeTree);
				target.add(NewEventTypeEditor.this.eventTypeAttributeNameInput);
				target.add(NewEventTypeEditor.this.eventTypeNameInput);
				target.add(NewEventTypeEditor.this.timestampNameInput);
			}
		};

		this.layoutForm.add(addEventTypeAttributeButton);
	}

	private Component buildEventTypeAttributeTypeDropDownChoice() {
		this.attributeTypeDropDownChoice = new DropDownChoice<AttributeTypeEnum>("attributeTypeDropDownChoice",
				new PropertyModel<AttributeTypeEnum>(this, "attributeType"), this.attributeTypes);
		this.attributeType = this.attributeTypes.get(0);
		this.attributeTypeDropDownChoice.setOutputMarkupId(true);
		return this.attributeTypeDropDownChoice;
	}

	private Component buildEventTypeNameInput() {
		this.eventTypeNameInput = new TextField<String>("eventTypeNameInput", new PropertyModel<String>(this,
				"eventTypeName"));
		this.eventTypeNameInput.setOutputMarkupId(true);
		return this.eventTypeNameInput;
	}

	private Component buildTimestampNameInput() {
		this.timestampNameInput = new TextField<String>("timestampNameInput", new PropertyModel<String>(this,
				"timestampName"));
		this.timestampNameInput.setOutputMarkupId(true);
		return this.timestampNameInput;
	}

	private Component buildEventTypeAttributeNameInput() {
		this.eventTypeAttributeNameInput = new TextField<String>("eventTypeAttributeNameInput",
				new PropertyModel<String>(this, "attributeName"));
		this.eventTypeAttributeNameInput.setOutputMarkupId(true);
		return this.eventTypeAttributeNameInput;
	}

	private void renderOrUpdateTree() {

		this.eventTypeTree = new SelectTree<TypeTreeNode>("eventTypeTree", new AttributeTreeProvider(
				this.eventTypeAttributesTree.getRoots()), new AttributeTreeExpansionModel());
		AttributeTreeExpansion.get().expandAll();
		this.eventTypeTree.setOutputMarkupId(true);
		this.layoutForm.addOrReplace(this.eventTypeTree);
	}

	// private ArrayList<TypeTreeNode> generateNodesOfEventTypeTree() {
	// ArrayList<TypeTreeNode> treeElements = new ArrayList<TypeTreeNode>();
	// EventTreeElement<String> rootElement = new
	// EventTreeElement<String>(eventTypeName);
	// treeElements.add(rootElement);
	// fillTreeLevel(rootElement, eventTypeAttributesTree.getRootElements(),
	// eventTypeAttributesTree);
	// return treeElements;
	// }
	//
	// private void fillTreeLevel(EventTreeElement<String> parent, List<String>
	// children, EventTree<String> eventTypeAttributesTree) {
	// for (String newValue : children) {
	// EventTreeElement<String> newElement = new
	// EventTreeElement<String>(parent, newValue.toString());
	// if (eventTypeAttributesTree.hasChildren(newValue)) {
	// fillTreeLevel(newElement, eventTypeAttributesTree.getChildren(newValue),
	// eventTypeAttributesTree);
	// }
	// }
	// }
}
