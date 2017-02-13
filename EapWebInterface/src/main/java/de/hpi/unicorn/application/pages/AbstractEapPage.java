/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.button.dropdown.MenuBookmarkablePageLink;
import de.agilecoders.wicket.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.markup.html.bootstrap.extensions.button.DropDownAutoOpen;
import de.agilecoders.wicket.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.NavbarComponents;
import de.hpi.unicorn.application.AuthenticatedSession;
import de.hpi.unicorn.application.components.NavBarDropDownButton;
import de.hpi.unicorn.application.pages.correlation.CorrelationPage;
import de.hpi.unicorn.application.pages.eventrepository.EventRepository;
import de.hpi.unicorn.application.pages.eventrepository.eventtypeeditor.EventTypeEditor;
import de.hpi.unicorn.application.pages.export.Export;
import de.hpi.unicorn.application.pages.input.AdapterPage;
import de.hpi.unicorn.application.pages.input.FileUploader;
import de.hpi.unicorn.application.pages.input.bpmn.BPMNProcessUpload;
import de.hpi.unicorn.application.pages.input.replayer.ReplayerPage;
import de.hpi.unicorn.application.pages.main.MainPage;
import de.hpi.unicorn.application.pages.monitoring.bpmn.BPMNMonitoringPage;
import de.hpi.unicorn.application.pages.monitoring.eventviews.EventViewPage;
import de.hpi.unicorn.application.pages.monitoring.notification.NotificationPage;
import de.hpi.unicorn.application.pages.monitoring.visualisation.AttributeChartPage;
import de.hpi.unicorn.application.pages.querying.LiveQueryEditor;
import de.hpi.unicorn.application.pages.querying.OnDemandQueryEditor;
import de.hpi.unicorn.application.pages.transformation.TransformationPage;
import de.hpi.unicorn.application.pages.user.LoginPage;
import de.hpi.unicorn.configuration.EapConfiguration;

/**
 * All pages in the web application should be a child class of
 * {@link AbstractEapPage}. This page constructs the Bootstrap Navbar, adds a
 * {@link FeedbackPanel} for status informations and the footer.
 */
public abstract class AbstractEapPage extends WebPage {

    private static final long serialVersionUID = 1L;

    // private String footer =
    // "&copy; 2013-2014 <a href=\"http://bpt.hpi.uni-potsdam.de/\"
    // target=\"_blank\">Business Process Technology group</a> at the Hasso
    // Plattner Institute";
    // private boolean stripTags;

    private String logInString;

    private NotificationPanel feedbackPanel;

    /**
     * Constructor for the {@link AbstractEapPage}. This page constructs the
     * Bootstrap Navbar, adds a feedback panel for status information and the
     * footer.
     */
    @SuppressWarnings("unchecked")
    public AbstractEapPage() {
	// TODO: richtige URL?!
	// add(new FaviconLink("favicon", "favicon.ico"));

	if (((AuthenticatedSession) Session.get()).getUser() != null) {
	    this.logInString = ((AuthenticatedSession) Session.get()).getUser().getName();
	} else {
	    this.logInString = "Sign In";
	}

	final Navbar navbar = new Navbar("eapNavBar");
	navbar.fluid();
	navbar.brandName(Model.of(""));
	// TODO: Remove also in backend
	navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.LEFT,
		new NavbarButton<MainPage>(MainPage.class, Model.of("Home")).setIconType(IconType.home),
		this.newImportDropDownButton(), this.newProcessingDropDownButton(),
		new NavbarButton<EventRepository>(EventRepository.class, Model.of("Event Repository"))
			.setIconType(IconType.book),
		this.newQueriesDropDownButton(),
		 newMonitoringDropDownButton(),
		new NavbarButton<NotificationPage>(NotificationPage.class, Model.of("Notifications"))
			.setIconType(IconType.envelope),
		new NavbarButton<Export>(Export.class, Model.of("Export")).setIconType(IconType.download)) // ,
	// newEventProducerDropDownButton())
	);

	navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.RIGHT,
		new NavbarButton<MainPage>(LoginPage.class, Model.of(this.logInString)).setIconType(IconType.user)));

	this.add(navbar);
	this.addFeedbackPanel();

	// Label footer = new Label("footer", new PropertyModel<String>(this,
	// "footer"));
	// footer.setEscapeModelStrings(false);
	// add(footer);
    }

    private Component newImportDropDownButton() {
	return new NavBarDropDownButton(Model.of("Import"))
		.addButton(new MenuBookmarkablePageLink<FileUploader>(FileUploader.class, Model.of("XML / XLS / XSD"))
			.setIconType(IconType.upload))
		.addButton(new MenuBookmarkablePageLink<BPMNProcessUpload>(BPMNProcessUpload.class, Model.of("BPMN"))
			     .setIconType(IconType.leaf))
		.addButton(new MenuBookmarkablePageLink<ReplayerPage>(ReplayerPage.class, Model.of("Replayer"))
			.setIconType(IconType.play))
		.addButton(new MenuBookmarkablePageLink<AdapterPage>(AdapterPage.class, Model.of("Adapter"))
			.setIconType(IconType.upload))
		.setIconType(IconType.cog).add(new DropDownAutoOpen());

    }

    // private Component newImportDropDownButton() {
    // return new NavBarDropDownButton(Model.of("Import"))
    // .addButton(
    // new MenuBookmarkablePageLink<FileUploader>(
    // FileUploader.class, Model
    // .of("Excel / XML / XSD"))
    // .setIconType(IconType.inbox))
    // .addButton(
    // new MenuBookmarkablePageLink<BPMNProcessUpload>(
    // BPMNProcessUpload.class, Model.of("BPMN"))
    // .setIconType(IconType.leaf))
    // .addButton(
    // new MenuBookmarkablePageLink<TomTomWeatherPage>(
    // TomTomWeatherPage.class, Model
    // .of("Weather (DWD)"))
    // .setIconType(IconType.leaf))
    // .setIconType(IconType.upload).add(new DropDownAutoOpen());
    // }

    private Component newProcessingDropDownButton() {
	return new NavBarDropDownButton(Model.of("Processing"))
		.addButton(new MenuBookmarkablePageLink<EventTypeEditor>(TransformationPage.class,
			Model.of("Event Transformation")).setIconType(IconType.filter))
		.addButton(new MenuBookmarkablePageLink<CorrelationPage>(CorrelationPage.class,
			Model.of("Event Correlation")).setIconType(IconType.random))
		.setIconType(IconType.cog).add(new DropDownAutoOpen());
    }

    private Component newQueriesDropDownButton() {
	final NavBarDropDownButton button = new NavBarDropDownButton(Model.of("Queries"));
	if (EapConfiguration.supportingOnDemandQueries) {
	    button.addButton(
		    new MenuBookmarkablePageLink<OnDemandQueryEditor>(OnDemandQueryEditor.class, Model.of("On-Demand"))
			    .setIconType(IconType.handleft));
	}
	button.addButton(new MenuBookmarkablePageLink<LiveQueryEditor>(LiveQueryEditor.class, Model.of("Live"))
		.setIconType(IconType.time));
	// .addButton(
	// new MenuBookmarkablePageLink<BPMNQueryEditor>(
	// BPMNQueryEditor.class, Model.of("BPMN"))
	// .setIconType(IconType.leaf))
	button.setIconType(IconType.thlarge).add(new DropDownAutoOpen());
	return button;
    }

    // private Component newEventProducerDropDownButton() {
    // return new NavBarDropDownButton(Model.of("Event Producing"))
    // .addButton(
    // new MenuBookmarkablePageLink<SimulationPage>(
    // SimulationPage.class, Model.of("Simulator")))
    // // .addButton(new
    // // MenuBookmarkablePageLink<AdapterPage>(AdapterPage.class,
    // // Model.of("Adapter")))
    // .setIconType(IconType.wrench).add(new DropDownAutoOpen());
    // }

    private Component newMonitoringDropDownButton() {
	return new NavBarDropDownButton(Model.of("Monitoring"))
		.addButton(new MenuBookmarkablePageLink<BPMNMonitoringPage>(BPMNMonitoringPage.class, Model.of("BPMN"))
			.setIconType(IconType.leaf))
		.addButton(new MenuBookmarkablePageLink<AttributeChartPage>(AttributeChartPage.class,
			Model.of("Attribute Charts")))
		.addButton(new MenuBookmarkablePageLink<EventViewPage>(EventViewPage.class, Model.of("Event Views")))
		.setIconType(IconType.camera).add(new DropDownAutoOpen());
    }

    private void addFeedbackPanel() {
	this.feedbackPanel = new NotificationPanel("feedback");
	this.feedbackPanel.setOutputMarkupId(true);
	this.feedbackPanel.setOutputMarkupPlaceholderTag(true);
	this.add(this.feedbackPanel);
    }

    public NotificationPanel getFeedbackPanel() {
	return this.feedbackPanel;
    }

    public void setFeedbackPanel(final NotificationPanel feedbackPanel) {
	this.feedbackPanel = feedbackPanel;
    }

}
