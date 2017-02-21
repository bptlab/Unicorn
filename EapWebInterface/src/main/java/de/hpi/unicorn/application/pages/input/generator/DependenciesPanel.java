/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * {@link Panel}, which allows the insertion of dependencies between event type attributes.
 */
public class DependenciesPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private GeneratorPage page;
    private DependenciesPanel panel;

    private Form layoutForm;
    protected String eventTypeName;
    private EapEventType selectedEventType = new EapEventType("test" );
    private TypeTreeNode selectedBaseAttribute = new TypeTreeNode("base attribute");
    private TypeTreeNode selectedDependentAttribute = new TypeTreeNode("dependent attribute");
    private String currentBaseAttributeInput = "";
    private String currentDependentAttributeInput = "";

    private HashMap<String, String> dependeciesInput = new HashMap<>();
    private ListView<String> listview;
    private WebMarkupContainer listContainer;

    /**
     * Constructor for the dependencies panel. The page is initialized in this method,
     * including the event type dropdown and the according event type attributes dropdowns.
     *
     * @param id
     * @param page
     */
    DependenciesPanel(String id, final GeneratorPage page) {
        super(id);
        this.page = page;
        this.panel = this;
        layoutForm = new Form("layoutForm");
        this.add(layoutForm);
        addEventTypeDropDown();
        addDependencyValuesInputs();
        addListOfDependencies();
        addAddDependencyButton();
        addSubmitButton();
   }

    /**
     * Adds dropdown with existing event types.
     * When selected, the two dropdowns containing the attributes of the choosen event type will be updated.
     * Using the two dropdowns the base and dependent attribute can be selected.
     */
    private void addEventTypeDropDown() {
        final List<EapEventType> eventTypes = EapEventType.findAll();

        if(!eventTypes.isEmpty()) {
            selectedEventType = eventTypes.get(0);
            setFirstAttributes();
        }

        final DropDownChoice<TypeTreeNode> baseDropDown = new DropDownChoice<>("baseAttributeField", new PropertyModel<TypeTreeNode>( this,
                "selectedBaseAttribute" ),
                selectedEventType.getValueTypes(), new ChoiceRenderer<>("name", "name"));

        final DropDownChoice<TypeTreeNode> dependentDropDown = new DropDownChoice<>("dependentAttributeField", new PropertyModel<TypeTreeNode>( this,
                "selectedDependentAttribute" ),
                getDependentAttributeChoiceList(), new ChoiceRenderer<>("name", "name"));

        final Label selectedBaseAttributeTypeLabel = new Label("selectedBaseAttributeType", new PropertyModel<String>(this,
                "selectedBaseAttribute.type"));
        final Label selectedDependentAttributeTypeLabel = new Label("selectedDependentAttributeType", new PropertyModel<String>(this,
                "selectedDependentAttribute.type"));

        DropDownChoice<EapEventType> eventTypeDropDown = new DropDownChoice<>("eventTypeField", new PropertyModel<EapEventType>( this, "selectedEventType" ),
                eventTypes);

        selectedBaseAttributeTypeLabel.setOutputMarkupId(true);
        selectedDependentAttributeTypeLabel.setOutputMarkupId(true);

        baseDropDown.setOutputMarkupId(true);
        dependentDropDown.setOutputMarkupId(true);

        baseDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if(selectedEventType != null && selectedEventType.getEventAttributes().size() >= 2) {
                    dependentDropDown.setChoices(getDependentAttributeChoiceList());
                    target.add(dependentDropDown);
                    listview.removeAll();
                    target.add(selectedBaseAttributeTypeLabel);
                    target.add(selectedDependentAttributeTypeLabel);
                    target.add(listContainer);
                }
            }
        });
        dependentDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if(selectedEventType != null) {
                    target.add(selectedBaseAttributeTypeLabel);
                    target.add(selectedDependentAttributeTypeLabel);
                    listview.removeAll();
                    target.add(listContainer);
                }
            }
        });

        eventTypeDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if(selectedEventType != null) {
                    setFirstAttributes();
                    baseDropDown.setChoices(selectedEventType.getValueTypes());
                    dependentDropDown.setChoices(getDependentAttributeChoiceList());
                    target.add(selectedBaseAttributeTypeLabel);
                    target.add(selectedDependentAttributeTypeLabel);
                    target.add(baseDropDown);
                    target.add(dependentDropDown);
                    target.add(selectedBaseAttributeTypeLabel);
                    target.add(selectedDependentAttributeTypeLabel);
                    target.add(listContainer);
                }
            }
        });

        layoutForm.add(eventTypeDropDown);
        layoutForm.add(baseDropDown);
        layoutForm.add(dependentDropDown);
        layoutForm.add(selectedBaseAttributeTypeLabel);
        layoutForm.add(selectedDependentAttributeTypeLabel);

    }

    private void addDependencyValuesInputs() {
        TextField<String> baseAttributeInputField = new TextField<String>("baseAttributeInput", new PropertyModel<String>(this,
                "currentBaseAttributeInput"));
        TextField<String> dependentAttributeInputField = new TextField<String>("dependentAttributeInput", new PropertyModel<String>(this,
                "currentDependentAttributeInput"));
        baseAttributeInputField.setLabel(new Model<String>("currentBaseAttributeInput"));
        baseAttributeInputField.setRequired(true);
        dependentAttributeInputField.setLabel(new Model<String>("currentDependentAttributeInput"));
        dependentAttributeInputField.setRequired(true);
        layoutForm.add(baseAttributeInputField);
        layoutForm.add(dependentAttributeInputField);
    }

    private void addListOfDependencies() {
        LoadableDetachableModel list =  new LoadableDetachableModel()
        {
            @Override
            protected Object load() {
                return new ArrayList<String>(dependeciesInput.keySet());
            }
        };
        listview = new ListView<String>("dependenciesListview", list) {
            @Override
            protected void populateItem(ListItem item) {
                final String key = (String) item.getModelObject();
                item.add(new Label("baseAttributeValue", key));
                item.add(new Label("dependentAttributeValue", dependeciesInput.get(key)));
            }
        };
        listContainer = new WebMarkupContainer("dependenciesContainer");
        listContainer.add(new Label("selectedBaseAttributeLabel", new PropertyModel<String>(this, "selectedBaseAttribute.name")).setOutputMarkupId
                (true));
        listContainer.add(new Label("selectedDependentAttributeLabel", new PropertyModel<String>(this, "selectedDependentAttribute.name"))
                .setOutputMarkupId(true));
        listContainer.add(listview);
        listContainer.setOutputMarkupId(true);
        layoutForm.add(listContainer);
    }

    private void addAddDependencyButton() {
       final AjaxButton addDependencyButton = new AjaxButton("addDependencyButton", this.layoutForm) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form form) {
                if(currentBaseAttributeInput == "" || currentDependentAttributeInput == "") {
                    DependenciesPanel.this.page.getFeedbackPanel().error("You must provide an input!");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
                    return;
                }
                if(dependeciesInput.containsKey(currentBaseAttributeInput)) {
                    DependenciesPanel.this.page.getFeedbackPanel().error("This value is already registered!");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
                    return;
                }
                DependenciesPanel.this.page.getFeedbackPanel().info("Added dependency!");
                target.add(DependenciesPanel.this.page.getFeedbackPanel());
                dependeciesInput.put(currentBaseAttributeInput, currentDependentAttributeInput);
                listview.removeAll();
                target.add(listContainer);
            }
        };
        this.layoutForm.add(addDependencyButton);
    }

    private void addSubmitButton() {
        final AjaxButton submitButton = new AjaxButton("submitButton", this.layoutForm) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form form) {
                    DependenciesPanel.this.page.getFeedbackPanel().success("Submitted.");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
            }
        };
        this.layoutForm.add(submitButton);
    }

    private void setFirstAttributes() {
        if(selectedEventType.getValueTypes().size() >= 2) {
            selectedBaseAttribute = selectedEventType.getValueTypes().get(0);
            selectedDependentAttribute = selectedEventType.getValueTypes().get(1);
        }
    }

    private List<TypeTreeNode> getDependentAttributeChoiceList() {
        List<TypeTreeNode> attributesWithoutBaseAttribute = selectedEventType.getValueTypes();
        try {
            attributesWithoutBaseAttribute.remove(selectedBaseAttribute);
        }
        finally {}
        return attributesWithoutBaseAttribute;
    }
}
