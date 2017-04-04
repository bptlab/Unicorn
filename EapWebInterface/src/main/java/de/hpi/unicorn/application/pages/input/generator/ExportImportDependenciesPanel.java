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
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.importer.json.JsonExporter;
import de.hpi.unicorn.importer.json.JsonImporter;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * {@link Panel}, that allows the import and export of attribute dependencies as json.
 */

public class ExportImportDependenciesPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private GeneratorPage page;
    private Form exportForm;
    private Form importForm;

    private FileUploadField uploadField;
    private EapEventType selectedEventType;

    private static final Logger logger = Logger.getLogger(ExportImportDependenciesPanel.class);

    /**
     * Constructor for the import/export dependencies panel. The page is initialized in this method,
     * including the event type dropdown and export button and the upload for importing.
     *
     * @param id initialized ID
     * @param page the page the panel belongs to
     */
    ExportImportDependenciesPanel(String id, final GeneratorPage page) {
        super(id);
        this.page = page;
        exportForm = new Form("exportForm");
        this.add(exportForm);
        importForm = new Form("importForm") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                final FileUpload uploadedFile = uploadField.getFileUpload();
                if (uploadedFile == null) {
                    this.error("File not found.");
                    return;
                }
                // make sure provided file is json
                final String fileName = uploadedFile.getClientFileName();
                String fileFormat = fileName.substring(fileName.lastIndexOf('.') + 1);
                if (!"json".equals(fileFormat)) {
                    error("Please provide a json file.");
                    return;
                }
                // generate dependencies from file
                File newFile;
                try {
                    newFile = uploadedFile.writeToTempFile();
                    String fileContent = Files.readFirstLine(newFile, Charset.defaultCharset());
                    boolean success = JsonImporter.generateAttributeDependenciesFromString(fileContent);
                    if (!success) {
                        error("Dependencies could not be created. "
                                + "Make sure you have the correct event type stored and the attribute values match their type.");
                        return;
                    }
                } catch (Exception e) {
                    logger.warn("File could not be read.", e);
                    return;
                }
                success("Saved attribute dependencies.");
            }
        };
        importForm.setMultiPart(true);
        this.add(importForm);

        this.addEventTypeDropDown();
        this.addExportButton();
        this.addImportField();
    }

    /**
     * Add a dropdown for existing event types.
     */
    private void addEventTypeDropDown() {
        final List<EapEventType> eventTypes = EapEventType.findAll();

        if (!eventTypes.isEmpty()) {
            selectedEventType = eventTypes.get(0);
        }
        final DropDownChoice<EapEventType> eventTypeDropDown = new DropDownChoice<>("eventTypeField",
                new PropertyModel<EapEventType>(this, "selectedEventType"), eventTypes);
        exportForm.add(eventTypeDropDown);
    }

    /**
     * Add a button for exporting the dependencies of the given event type.
     */
    private void addExportButton() {
            final AjaxButton exportButton = new AjaxButton("exportButton") {
               private static final long serialVersionUID = 1L;
                @Override
                public void onSubmit(final AjaxRequestTarget target, final Form form) {
                    final AJAXDownload jsonDownload = new AJAXDownload() {
                        @Override
                        protected IResourceStream getResourceStream() {
                            final File csv = JsonExporter.generateExportFileWithDependencies(selectedEventType);
                            return new FileResourceStream(new org.apache.wicket.util.file.File(csv));
                        }
                        @Override
                        protected String getFileName() {
                            return selectedEventType.getTypeName() + "-dependencies.json";
                        }
                    };
                    ExportImportDependenciesPanel.this.add(jsonDownload);
                    jsonDownload.initiate(target);
                    ExportImportDependenciesPanel.this.page.getFeedbackPanel().success("Json created.");
                    target.add(ExportImportDependenciesPanel.this.page.getFeedbackPanel());
                }
            };
            exportForm.add(exportButton);
    }

    /**
     * Add a field for importing a selectable file.
     */
    private void addImportField() {
        uploadField = new FileUploadField("uploadField");
        importForm.add(uploadField);
    }
}
