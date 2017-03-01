package de.hpi.unicorn.application.pages.input.generator;

import de.hpi.unicorn.application.pages.export.AJAXDownload;
import de.hpi.unicorn.application.pages.export.Export;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.importer.json.JsonExporter;
import de.hpi.unicorn.importer.xml.XMLExporter;
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
    private Logger logger = Logger.getLogger(ExportImportDependenciesPanel.class);

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

        this.addEventTypeDropDownExport();
        this.addExportButton();
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
}
