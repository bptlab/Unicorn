/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.io.IClusterable;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.tree.AttributeTreeExpansion;
import de.hpi.unicorn.application.components.tree.AttributeTreeExpansionModel;
import de.hpi.unicorn.application.components.tree.AttributeTreeProvider;
import de.hpi.unicorn.application.components.tree.MultiSelectTree;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.main.MainPage;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.FileUtils;
import de.hpi.unicorn.importer.xml.AbstractXMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.importer.xml.XSDParser;

public class XSDEventTypeCreator extends AbstractEapPage {

	private final TextField<String> eventTypeNameTextField;
	private final String filePath;
	private EapEventType importedEventType;
	private final AttributeTypeTree eventTypeAttributesTree;
	private final List<String> leafPathes = new ArrayList<String>();
	private final DropDownChoice<String> timestampDropDownChoice;
	private String timestampXPath;
	private MultiSelectTree<TypeTreeNode> tree;
	private final TextFieldDefaultValues textFieldDefaultValues;
	private final Form<Void> layoutForm;
	private final AbstractEapPage xsdEventTypeCreator;
	private TypeTreeNode timestamp;
	private final AjaxCheckBox importTimeCheckBox;
	private Boolean eventTypeUsingImportTime = false;

	@SuppressWarnings("serial")
	public XSDEventTypeCreator(final PageParameters parameters) throws XMLParsingException {

		super();
		this.xsdEventTypeCreator = this;

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);

		this.filePath = parameters.get("filePath").toString();
		final String schemaName = FileUtils.getFileNameWithoutExtension(this.filePath);
		try {
			this.importedEventType = XSDParser.generateEventTypeFromXSD(this.filePath, schemaName);
		} catch (final XMLParsingException e1) {
			throw new XMLParsingException(e1.getMessage());
		} catch (final RuntimeException e2) {
			this.getFeedbackPanel().error(e2.getMessage());
		}

		this.eventTypeAttributesTree = this.importedEventType.getValueTypeTree();
		// timestamp must be a root attribute
		for (final TypeTreeNode element : this.eventTypeAttributesTree.getLeafAttributes()) {
			if (element.getType() == AttributeTypeEnum.DATE) {
				this.leafPathes.add(element.getXPath());
			}
		}
		if (this.leafPathes.isEmpty()) {
			this.eventTypeUsingImportTime = true;
		} else {
			this.timestampXPath = this.leafPathes.get(0);
		}
		this.textFieldDefaultValues = new TextFieldDefaultValues();
		this.setDefaultModel(new CompoundPropertyModel<TextFieldDefaultValues>(this.textFieldDefaultValues));
		this.eventTypeNameTextField = new TextField<String>("eventTypeNameTextField");
		this.layoutForm.add(this.eventTypeNameTextField);
		this.importTimeCheckBox = new AjaxCheckBox("importTimeCheckBox", new PropertyModel<Boolean>(this,
				"eventTypeUsingImportTime")) {
			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (XSDEventTypeCreator.this.eventTypeUsingImportTime) {
					XSDEventTypeCreator.this.tree.getSelectedElements().remove(XSDEventTypeCreator.this.timestamp);
					XSDEventTypeCreator.this.timestampDropDownChoice.setEnabled(false);
				} else {
					XSDEventTypeCreator.this.tree.getSelectedElements().add(XSDEventTypeCreator.this.timestamp);
					XSDEventTypeCreator.this.timestampDropDownChoice.setEnabled(true);
				}
				target.add(XSDEventTypeCreator.this.tree);
				target.add(XSDEventTypeCreator.this.timestampDropDownChoice);
			}
		};
		this.timestampDropDownChoice = new DropDownChoice<String>("timestampDropDownChoice", new PropertyModel<String>(
				this, "timestampXPath"), this.leafPathes);
		this.timestampDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				XSDEventTypeCreator.this.tree.getSelectedElements().remove(XSDEventTypeCreator.this.timestamp);
				if (XSDEventTypeCreator.this.timestamp.hasParent()) {
					XSDEventTypeCreator.this.deselectTreeElement(XSDEventTypeCreator.this.timestamp.getParent());
				}

				XSDEventTypeCreator.this.timestamp = XSDEventTypeCreator.this.eventTypeAttributesTree
						.getAttributeByXPath(XSDEventTypeCreator.this.timestampXPath);

				XSDEventTypeCreator.this.tree.getSelectedElements().add(XSDEventTypeCreator.this.timestamp);
				if (XSDEventTypeCreator.this.timestamp.hasParent()) {
					XSDEventTypeCreator.this.selectTreeElement(XSDEventTypeCreator.this.timestamp.getParent());
				}
				target.add(XSDEventTypeCreator.this.tree);
			}
		});
		if (this.leafPathes.isEmpty()) {
			this.importTimeCheckBox.setEnabled(false);
			this.timestampDropDownChoice.setEnabled(false);
		}
		this.importTimeCheckBox.setOutputMarkupId(true);
		this.timestampDropDownChoice.setOutputMarkupId(true);
		this.layoutForm.add(this.importTimeCheckBox);
		this.layoutForm.add(this.timestampDropDownChoice);
		this.renderTree();
		this.buildButtons();
	}

	private void selectTreeElement(final TypeTreeNode element) {
		this.tree.getSelectedElements().add(element);
		if (element.hasParent()) {
			this.selectTreeElement(element);
		}
	}

	private void deselectTreeElement(final TypeTreeNode element) {
		final ArrayList<TypeTreeNode> children = element.getChildren();
		boolean hasChildren = false;
		for (final TypeTreeNode child : children) {
			if (this.tree.getSelectedElements().contains(child)) {
				hasChildren = true;
			}
		}
		if (!hasChildren) {
			this.tree.getSelectedElements().remove(element);
			if (element.hasParent()) {
				this.deselectTreeElement(element.getParent());
			}
		}
	}

	protected void renderTree() {
		this.tree = new MultiSelectTree<TypeTreeNode>("eventTypeTree", new AttributeTreeProvider(
				this.eventTypeAttributesTree.getRoots()), new AttributeTreeExpansionModel()) {

			@Override
			protected void toggle(final TypeTreeNode element, final AbstractTree<TypeTreeNode> tree,
					final AjaxRequestTarget target) {
				if (XSDEventTypeCreator.this.eventTypeUsingImportTime
						|| !element.equals(XSDEventTypeCreator.this.timestamp)) {
					if (element.getType() != null) {
						super.toggle(element, tree, target);
					} else {
						final ArrayList<TypeTreeNode> children = element.getChildren();
						boolean hasChildren = false;
						for (final TypeTreeNode child : children) {
							if (this.selectedElements.contains(child)) {
								hasChildren = true;
							}
						}
						if (hasChildren) {
							this.selectedElements.add(element);
						} else {
							this.selectedElements.remove(element);
						}
						tree.updateNode(element, target);
					}
					if (element.hasParent()) {
						this.toggle(element.getParent(), tree, target);
					}
				}
			}
		};
		if (!this.leafPathes.isEmpty()) {
			this.timestamp = this.eventTypeAttributesTree.getAttributeByXPath(this.leafPathes.get(0));
			this.tree.getSelectedElements().add(this.timestamp);
		}
		AttributeTreeExpansion.get().expandAll();
		this.tree.setOutputMarkupId(true);
		this.layoutForm.add(this.tree);
	}

	private void buildButtons() {

		final AjaxButton selectAllButton = new AjaxButton("selectAllButton") {
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				XSDEventTypeCreator.this.tree.getSelectedElements().addAll(
						XSDEventTypeCreator.this.eventTypeAttributesTree.getAttributes());
				target.add(XSDEventTypeCreator.this.tree);
			}
		};
		this.layoutForm.add(selectAllButton);

		final AjaxButton unselectAllButton = new AjaxButton("unselectAllButton") {
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				XSDEventTypeCreator.this.tree.getSelectedElements().clear();
				if (!XSDEventTypeCreator.this.eventTypeUsingImportTime) {
					XSDEventTypeCreator.this.tree.getSelectedElements().add(XSDEventTypeCreator.this.timestamp);
				}
				target.add(XSDEventTypeCreator.this.tree);
			}
		};
		this.layoutForm.add(unselectAllButton);

		final Button confirmButton = new Button("confirmButton") {
			@Override
			public void onSubmit() {
				final Set<TypeTreeNode> selectedNodes = XSDEventTypeCreator.this.tree.getSelectedElements();
				XSDEventTypeCreator.this.eventTypeAttributesTree.retainAllAttributes(selectedNodes);
				try {
					XSDEventTypeCreator.this.importedEventType
							.setTypeName(XSDEventTypeCreator.this.eventTypeNameTextField.getModelObject());
					if (XSDEventTypeCreator.this.eventTypeUsingImportTime) {
						XSDEventTypeCreator.this.importedEventType
								.setTimestampName(AbstractXMLParser.GENERATED_TIMESTAMP_COLUMN_NAME);
					} else {
						final String timestampName = XSDEventTypeCreator.this.timestamp.getAttributeExpression();
						XSDEventTypeCreator.this.importedEventType.setTimestampName(timestampName);
						if (!XSDEventTypeCreator.this.timestamp.hasParent()) {
							XSDEventTypeCreator.this.eventTypeAttributesTree
									.removeRoot(XSDEventTypeCreator.this.timestamp);
							XSDEventTypeCreator.this.importedEventType
									.setValueTypeTree(XSDEventTypeCreator.this.eventTypeAttributesTree);
						}
						XSDEventTypeCreator.this.timestamp.removeAttribute();
					}
					if (EapEventType.findByTypeName(XSDEventTypeCreator.this.importedEventType.getTypeName()) != null) {
						XSDEventTypeCreator.this.xsdEventTypeCreator.getFeedbackPanel().error(
								"Event type " + XSDEventTypeCreator.this.importedEventType.getTypeName()
										+ " already exists");
						return;
					}
					// System.out.println(importedEventType);
					Broker.getEventAdministrator().importEventType(XSDEventTypeCreator.this.importedEventType);
					final PageParameters pageParameters = new PageParameters();
					pageParameters.add("successFeedback",
							"Event type " + XSDEventTypeCreator.this.importedEventType.getTypeName()
									+ " has been created");
					this.setResponsePage(MainPage.class, pageParameters);
				} catch (final RuntimeException e) {
					e.printStackTrace();
					XSDEventTypeCreator.this.xsdEventTypeCreator.getFeedbackPanel().error(
							"Event type has not been created. See output console for more details.");
				}
			}
		};
		this.layoutForm.add(confirmButton);
	}

	private class TextFieldDefaultValues implements IClusterable {

		private static final long serialVersionUID = 1L;

		public String eventTypeNameTextField = FileUtils.getFileNameWithoutExtension(XSDEventTypeCreator.this.filePath);

		@Override
		public String toString() {
			return "eventTypeNameTextField = '" + this.eventTypeNameTextField + "'";
		}
	}
}
