package de.hpi.unicorn.application.pages.input.generator;

import com.google.common.io.Files;
import de.hpi.unicorn.application.pages.export.AJAXDownload;
import de.hpi.unicorn.application.pages.export.Export;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.importer.FileUtils;
import de.hpi.unicorn.importer.json.JsonExporter;
import de.hpi.unicorn.importer.json.JsonImporter;
import de.hpi.unicorn.importer.xml.XMLExporter;
import de.hpi.unicorn.utils.TempFolderUtil;
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
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;


public class ExportImportDependenciesPanel extends Panel {
    private static final long serialVersionUID = 1L;
    private GeneratorPage page;
    private ExportImportDependenciesPanel panel;
    private Form exportForm;
    private Form importForm;

    private DropDownChoice<EapEventType> eventTypeDropDown;
    private FileUploadField uploadField;
    private EapEventType selectedEventType;
    private static Logger logger = Logger.getLogger(ExportImportDependenciesPanel.class);

    /**
     * Constructor for the dependencies panel. The page is initialized in this method,
     * including the event type dropdown and the according event type attributes dropdowns.
     *
     * @param id
     * @param page
     */
    ExportImportDependenciesPanel (String id, final GeneratorPage page) {
        super(id);
        this.page = page;
        this.panel = this;
        exportForm = new Form("exportForm");
        this.add(exportForm);
        importForm = new Form("importForm") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit() {
                final FileUpload uploadedFile = uploadField.getFileUpload();
                if (uploadedFile != null) {
                    final String uploadFolder = TempFolderUtil.getFolder();
                    final String fileName = uploadedFile.getClientFileName();
                    final File newFile = new File(uploadFolder + System.getProperty("file.separator") + fileName);

                    if (newFile.exists()) {
                        newFile.delete();
                    }

                    try {
                        newFile.createNewFile();
                        uploadedFile.writeTo(newFile);
                    } catch (final IOException e) {
                        try {
                            throw new IllegalStateException("Error: File could not be saved under " + newFile.getCanonicalPath() + ".");
                        } catch (final IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                    //if (uploadedFile.getContentType().equals("application/json")) {

                        try {
                            String fileContent = Files.readFirstLine(newFile, Charset.defaultCharset());
                            JsonImporter.generateAttributeDependencyFromString(fileContent);
                        } catch (Exception e) {
                        }
                    //}
                } else {
                    this.error("File not found.");
                }
            }
        };
        importForm.setMultiPart(true);
        this.add(importForm);

        this.addEventTypeDropDownExport();
        this.addExportButton();
        this.addEventTypeDropDownImport();
        this.addImportField();
    }

    private void addEventTypeDropDownExport() {
        final List<EapEventType> eventTypes = EapEventType.findAll();

        if(!eventTypes.isEmpty()) {
            selectedEventType = eventTypes.get(0);
        }

        eventTypeDropDown = new DropDownChoice<>("eventTypeFieldExport", new PropertyModel<EapEventType>( this, "selectedEventType" ),
                eventTypes);
        exportForm.add(eventTypeDropDown);
    }

    private void addExportButton() {
            final AjaxButton exportButton = new AjaxButton("exportButton") {
               private static final long serialVersionUID = 1L;

                @Override
                public void onSubmit(final AjaxRequestTarget target, final Form form) {
                    final JsonExporter jsonExporter = new JsonExporter();
                    final AJAXDownload jsonDownload = new AJAXDownload() {

                        @Override
                        protected IResourceStream getResourceStream() {
                            final File csv = jsonExporter.generateExportFileWithDependencies(selectedEventType);
                            return new FileResourceStream(new org.apache.wicket.util.file.File(csv));
                        }

                        @Override
                        protected String getFileName() {
                            return selectedEventType.getTypeName() + ".json";
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

    private void addEventTypeDropDownImport() {
        final List<EapEventType> eventTypes = EapEventType.findAll();

        if(!eventTypes.isEmpty()) {
            selectedEventType = eventTypes.get(0);
        }

        eventTypeDropDown = new DropDownChoice<>("eventTypeFieldImport", new PropertyModel<EapEventType>( this, "selectedEventType" ),
                eventTypes);
        importForm.add(eventTypeDropDown);
    }

    private void addImportField() {
        uploadField = new FileUploadField("uploadField");
        importForm.add(uploadField);
    }
}
