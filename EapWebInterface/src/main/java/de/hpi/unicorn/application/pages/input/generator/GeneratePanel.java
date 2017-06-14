/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator;

import com.google.common.io.Files;
import de.hpi.unicorn.application.pages.export.AJAXDownload;
import de.hpi.unicorn.application.pages.input.generator.attributeInput.AttributeInput;
import de.hpi.unicorn.application.pages.input.generator.validation.DateRangeValidator;
import de.hpi.unicorn.attributeDependency.AttributeDependencyManager;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.importer.json.JsonExporter;
import de.hpi.unicorn.importer.json.JsonImporter;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.validation.validator.RangeValidator;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * {@link Panel}, which allows the generation of events.
 */
public class GeneratePanel extends Panel {

    private static final long serialVersionUID = 1L;
    private GeneratorPage page;
    private GeneratePanel panel;
    private static final Integer DEFAULT_EVENTCOUNT = 10;
    private static final Integer DEFAULT_SCALEFACTOR = 10000;
    private static final Integer MAXIMUM_EVENTCOUNT = 100;
    private static final String AJAX_BEHAVIOR = "change";
    private Integer eventCount = DEFAULT_EVENTCOUNT;
    private Integer scaleFactor = DEFAULT_SCALEFACTOR;
    private String eventTimestamps;
    private Form layoutForm;
    private Form importForm;
    private DropDownChoice<EapEventType> eventTypeDropDown;
    private TextField<Integer> eventCountField;
    private TextField<Integer> scaleFactorField;
    private TextField<String> timestampField;
    private FileUploadField uploadField;
    protected String eventTypeName;
    private EapEventType selectedEventType = new EapEventType("test");
    private ListView<TypeTreeNode> listview;
    private List<AttributeInput> attributeInputs = new ArrayList<>();
    private WebMarkupContainer listContainer;
    private AttributeDependencyManager attributeDependencyManager;
    private final List<EapEventType> eventTypes = EapEventType.findAll();
    private static Logger logger = Logger.getLogger(GeneratePanel.class);

    /**
     * Constructor for the generate panel. The page is initialized in this method,
     * including the event type dropdown and the according list of input fields.
     *
     * @param id initialized ID
     * @param page the page the panel belongs to
     */
    GeneratePanel(String id, final GeneratorPage page) {
        super(id);
        this.page = page;
        this.panel = this;

        layoutForm = new Form("layoutForm") {

            @Override
            public void onSubmit() {
                EventGenerator eventGenerator = new EventGenerator();
                if (eventTimestamps == null) {
                    eventGenerator.generateEvents(eventCount, scaleFactor, selectedEventType, attributeInputs);
                }
                else {
                    eventGenerator.generateEvents(eventCount, scaleFactor, selectedEventType, attributeInputs, eventTimestamps);
                }
                success("Event(s) successfully created");
            }
        };
        this.add(layoutForm);

        importForm = new Form("importForm") {
            @Override
            public void onSubmit() {
                // this is completely handled in the import button that submits the form
            }
        };
        layoutForm.add(importForm);

        if (eventTypes.isEmpty()) {
            selectedEventType = new EapEventType("test");
        } else {
            selectedEventType = eventTypes.get(0);
        }
        attributeDependencyManager = new AttributeDependencyManager(selectedEventType);

        addEventCountField();
        addScaleFactorField();
        addTimestampField();
        addEventTypeDropDown();
        addSubmitButton();
        addExportValuesButton();
        addImportField();
        addImportSubmitButton();
    }

    /**
     * Add field to specify the number of events to generate.
     */
    private void addEventCountField() {
        eventCountField = new TextField<>("eventCountField", new PropertyModel<Integer>(this, "eventCount"));
        eventCountField.setRequired(true);
        eventCountField.add(new RangeValidator<>(1, MAXIMUM_EVENTCOUNT));
        eventCountField.add(new AjaxFormComponentUpdatingBehavior(AJAX_BEHAVIOR) {
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                target.add(eventCountField);
            }
        });
        eventCountField.setOutputMarkupId(true);
        layoutForm.add(eventCountField);
    }

    /**
     * Add field to specify the scale factor to replay with.
     */
    private void addScaleFactorField() {
        scaleFactorField = new TextField<>("scaleFactorField", new PropertyModel<Integer>(this, "scaleFactor"));
        scaleFactorField.setRequired(true);
        scaleFactorField.add(new AjaxFormComponentUpdatingBehavior(AJAX_BEHAVIOR) {
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                target.add(scaleFactorField);
            }
        });
        scaleFactorField.setOutputMarkupId(true);
        layoutForm.add(scaleFactorField);
    }

    /**
     * Add field to specify the timestamp of the events to generate.
     */
    private void addTimestampField() {
        timestampField = new TextField<>("timestampField", new PropertyModel<String>(this, "eventTimestamps"));
        timestampField.setLabel(new Model<>("Timestamp"));
        timestampField.add(new DateRangeValidator());
        timestampField.add(new AjaxFormComponentUpdatingBehavior(AJAX_BEHAVIOR) {
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                target.add(timestampField);
            }
        });
        timestampField.setOutputMarkupId(true);
        layoutForm.add(timestampField);
    }


    /**
     * Adds dropdown with existing event types.
     * When selected, a list is generated that contains the attributes of the selected event types and input fields to fill in values.
     * Every input field gets a validator.
     * Additionally a description is provided so that the user knows the format of input that has to be used.
     */
    private void addEventTypeDropDown() {
        LoadableDetachableModel list =  new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return selectedEventType.getValueTypes();
            }
        };
        listview = new ListView<TypeTreeNode>("listview", list) {
            @Override
            protected void populateItem(ListItem item) {
                final TypeTreeNode attribute = (TypeTreeNode) item.getModelObject();
                final AttributeInput attributeInput = getAttributeInput(attribute);

                item.add(new Label("attribute", attributeInput.getAttributeName()));
                item.add(new Label("attributeType", attributeInput.getAttributeType().getName()));
                final Label attributeInputDescriptionLabel = new Label("attributeInputDescription", getAttributeInputDescription(attributeInput));
                attributeInputDescriptionLabel.setOutputMarkupId(true);
                item.add(attributeInputDescriptionLabel);

                IModel<String> attributeInputModel = new Model<String>() {
                    @Override
                    public String getObject() {
                        int indexOfAttributeInput = attributeInputs.indexOf(attributeInput);
                        return attributeInputs.get(indexOfAttributeInput).getInput();
                    }
                    @Override
                    public void setObject(String inputValue) {
                        int indexOfAttributeInput = attributeInputs.indexOf(attributeInput);
                        attributeInputs.get(indexOfAttributeInput).setInput(inputValue);
                    }
                };

                final TextField<String> inputField = new TextField<>("attributeInput", attributeInputModel);
                inputField.add(attributeInput.getAttributeInputValidator());
                inputField.setLabel(new Model<>(attribute.getName()));
                inputField.add(new AjaxFormComponentUpdatingBehavior(AJAX_BEHAVIOR) {
                    @Override
                    public void onUpdate(AjaxRequestTarget target) {
                        target.add(inputField);
                    }
                });
                inputField.setOutputMarkupId(true);
                item.add(inputField);
                Boolean isDependentAttribute = attributeDependencyManager.isDependentAttributeInAnyDependency(attribute);
                item.add(new Label("attributeInputWarning", "").setVisible(isDependentAttribute));

                DropDownChoice<AttributeInput.ProbabilityDistributionEnum> methodDropDown = new DropDownChoice<>(
                        "attributeInputMethodSelection",
                        new PropertyModel<AttributeInput.ProbabilityDistributionEnum>(attributeInput, "selectedMethod"),
                        attributeInput.getAvailableMethods());
                methodDropDown.add(new AjaxFormComponentUpdatingBehavior(AJAX_BEHAVIOR) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        attributeInputDescriptionLabel.detachModels();
                        attributeInputDescriptionLabel.setDefaultModel(getAttributeInputDescription(attributeInput));
                        inputField.remove(inputField.getValidators().get(0));
                        inputField.add(attributeInput.getAttributeInputValidator());
                        target.add(attributeInputDescriptionLabel);
                        target.add(inputField);
                    }
                });
                methodDropDown.setVisible(attributeInput.hasDifferentMethods());
                item.add(methodDropDown);
            }
        };
        listview.setReuseItems(true);

        listContainer = new WebMarkupContainer("theContainer");
        listContainer.add(listview);
        layoutForm.add(listContainer);
        listContainer.setOutputMarkupId(true);

        eventTypeDropDown = new DropDownChoice<>("eventTypeField",
                new PropertyModel<EapEventType>(this, "selectedEventType"), eventTypes);
        eventTypeDropDown.add(new AjaxFormComponentUpdatingBehavior(AJAX_BEHAVIOR) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (selectedEventType != null) {
                    attributeDependencyManager = new AttributeDependencyManager(selectedEventType);
                    setNewAttributeInputs(selectedEventType);
                    listview.removeAll();
                    target.add(listContainer);
                }
            }
        });
        eventTypeDropDown.setOutputMarkupId(true);
        layoutForm.add(eventTypeDropDown);
    }

    /**
     * Create new attribute input objects for the attributes of the given event type.
     *
     * @param newEventType (EapEventType) the inputs should be created for
     */
    private void setNewAttributeInputs(EapEventType newEventType) {
        attributeInputs.clear();
        for (TypeTreeNode attribute : newEventType.getValueTypes()) {
            attributeInputs.add(AttributeInput.attributeInputFactory(attribute));
        }
    }

    /**
     * Searches for the attribute input object fitting to the given attribute.
     *
     * @param attribute (TypeTreeNode) the attribute input should be for
     * @return AttributeInput fitting to the attribute (might be created newly, in case it couldn't be found in existing inputs.
     */
    private AttributeInput getAttributeInput(TypeTreeNode attribute) {
        for (AttributeInput input : attributeInputs) {
            if (input.getAttribute() == attribute) {
                return input;
            }
        }
        AttributeInput newAttributeInput = AttributeInput.attributeInputFactory(attribute);
        attributeInputs.add(newAttributeInput);
        return  newAttributeInput;
    }

    /**
     * Creates a model used for retrieving the description text for the user input from the properties file.
     *
     * @param attributeInput the model should be created for
     * @return a StringResourceModel fitting the given attributeInput
     */
    private StringResourceModel getAttributeInputDescription(AttributeInput attributeInput) {
        StringResourceModel inputDescriptionModel = new StringResourceModel("description.${type}", this,
                new Model<>(attributeInput.getAttribute()));
        if (attributeInput.hasDifferentMethods()) {
            IModel<AttributeInput> attributeInputIModel = new Model<>(attributeInput);
            inputDescriptionModel = new StringResourceModel("description.${attributeType}.${selectedMethod}", this,
                    attributeInputIModel
            );
        }
        return inputDescriptionModel;
    }

    /**
     * Add a button to submit the form.
     */
    private void addSubmitButton() {
        final Button submitButton = new Button("submitButton");
        layoutForm.add(submitButton);
    }

    /**
     * Add a button to export the input values given via input fields.
     */
    private void addExportValuesButton() {
        final AjaxLink exportButton = new AjaxLink<Void>("exportValuesButton") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                layoutForm.modelChanged();
                final AJAXDownload jsonDownload = new AJAXDownload() {
                    @Override
                    protected IResourceStream getResourceStream() {
                        JSONArray valuesForExport = new JSONArray();
                        for (AttributeInput input : attributeInputs) {
                            valuesForExport.put(input.toJson());
                        }
                        final File json = JsonExporter
                                .generateExportFileWithValues(selectedEventType, valuesForExport, eventCount, scaleFactor, eventTimestamps);
                        if (json == null) {
                            return null;
                        }
                        return new FileResourceStream(new org.apache.wicket.util.file.File(json));
                    }
                    @Override
                    protected String getFileName() {
                        return selectedEventType.getTypeName() + "-values.json";
                    }
                };
                GeneratePanel.this.add(jsonDownload);
                jsonDownload.initiate(target);

                GeneratePanel.this.page.getFeedbackPanel().success("Json created.");
                target.add(GeneratePanel.this.page.getFeedbackPanel());
            }
        };
        layoutForm.add(exportButton);
    }

    /**
     * Add a upload field to upload a file.
      */
    private void addImportField() {
        uploadField = new FileUploadField("importValuesUpload");
        this.importForm.add(uploadField);
    }

    /**
     * Add a button to import values from a file.
     */
    private void addImportSubmitButton() {
        AjaxButton button = new AjaxButton("importValuesButton") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form importForm) {
                final FileUpload uploadedFile = uploadField.getFileUpload();
                if (uploadedFile == null) {
                    GeneratePanel.this.page.getFeedbackPanel().error("File not found.");
                    target.add(GeneratePanel.this.page.getFeedbackPanel());
                    return;
                }
                // make sure provided file is json
                final String fileName = uploadedFile.getClientFileName();
                String fileFormat = fileName.substring(fileName.lastIndexOf('.') + 1);
                if (!"json".equals(fileFormat)) {
                    GeneratePanel.this.page.getFeedbackPanel().error("Please provide a json file.");
                    target.add(GeneratePanel.this.page.getFeedbackPanel());
                    return;
                }
                // generate inputs from file
                File newFile;
                Map<Object, Object> valueMap;
                try {
                    newFile = uploadedFile.writeToTempFile();
                    String fileContent = Files.readFirstLine(newFile, Charset.defaultCharset());
                    valueMap = JsonImporter.generateValuesFromString(fileContent);
                    if (valueMap == null) {
                        GeneratePanel.this.page.getFeedbackPanel().error("Values could not be read.");
                        target.add(GeneratePanel.this.page.getFeedbackPanel());
                        return;
                    }
                } catch (Exception e) {
                    logger.warn("File could not be read", e);
                    GeneratePanel.this.page.getFeedbackPanel().error("File could not be read.");
                    target.add(GeneratePanel.this.page.getFeedbackPanel());
                    return;
                }

                EapEventType newEventType = (EapEventType) valueMap.get("eventType");
                if (selectedEventType.getID() != newEventType.getID()) {
                    GeneratePanel.this.page.getFeedbackPanel().error("Please select the correct event type.");
                    target.add(GeneratePanel.this.page.getFeedbackPanel());
                    return;
                }
                eventTypeDropDown.setModelObject(newEventType);

                eventCountField.setModelObject((int) valueMap.get("eventCount"));
                eventCountField.clearInput();
                target.add(eventCountField);

                scaleFactorField.getModel().setObject((int) valueMap.get("scaleFactor"));
                scaleFactorField.clearInput();
                target.add(scaleFactorField);

                String importedTimeStamp = (String) valueMap.get("timestamp");
                if (!"null".equals(importedTimeStamp)) {
                    timestampField.setModelObject(importedTimeStamp);
                }
                else {
                    timestampField.setModelObject("");
                }
                timestampField.clearInput();
                target.add(timestampField);

                JSONArray values = (JSONArray) valueMap.get("values");
                attributeInputs.clear();
                for (int i = 0; i < values.length(); i++) {
                    try {
                        attributeInputs.add(AttributeInput.fromJson(newEventType, values.getJSONObject(i)));
                    }
                    catch (JSONException e) {
                        logger.warn("Could not import attribute input", e);
                    }
                }
                listview.removeAll();
                listview.modelChanged();
                target.add(listContainer);

                GeneratePanel.this.page.getFeedbackPanel().success("Saved attribute values.");
                target.add(GeneratePanel.this.page.getFeedbackPanel());
            }
        };
        button.setDefaultFormProcessing(false);
        this.importForm.add(button);
    }
}
