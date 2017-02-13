/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application;

import javax.jms.JMSException;

import org.apache.log4j.PropertyConfigurator;
import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import de.agilecoders.wicket.Bootstrap;
import de.agilecoders.wicket.markup.html.themes.metro.MetroTheme;
import de.agilecoders.wicket.settings.BootstrapSettings;
import de.agilecoders.wicket.settings.BootswatchThemeProvider;
import de.agilecoders.wicket.settings.ThemeProvider;
import de.hpi.unicorn.adapter.JmsAdapter;
import de.hpi.unicorn.application.images.ImageReference;
import de.hpi.unicorn.application.pages.main.MainPage;
import de.hpi.unicorn.application.pages.user.LoginPage;
import de.hpi.unicorn.configuration.EapConfiguration;
import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.messageQueue.JMSProvider;

/**
 * The controller for the web application. Most of the initialization is done
 * here.
 * 
 * @author micha
 */
public class UNICORNApplication extends WebApplication {

	private StreamProcessingAdapter epAdapter;
	private BootstrapSettings bootStrapSettings;

	@Override
	public Class<? extends WebPage> getHomePage() {
		return MainPage.class;
	}

	@Override
	public Session newSession(final Request request, final Response response) {
		return new AuthenticatedSession(request);
	}

	@Override
	public void init() {
		super.init();

		this.getMarkupSettings().setStripWicketTags(true);

		this.bootStrapSettings = new BootstrapSettings();
		this.bootStrapSettings.minify(true); // use minimized version of all
		// bootstrap

		final ThemeProvider themeProvider = new BootswatchThemeProvider() {
			{
				this.add(new MetroTheme());
				this.defaultTheme("cerulean");
			}
		};
		this.bootStrapSettings.setThemeProvider(themeProvider);

		Bootstrap.install(this, this.bootStrapSettings);

		this.mountImages();

		EapConfiguration.initialize();
		PropertyConfigurator.configure(EapConfiguration.getProperties());
		this.epAdapter = StreamProcessingAdapter.getInstance();

		// initializeEventTypesForShowCase();

		this.setAuthorizationStrategy();

		// initialize jms event import interface
		try {
			JMSProvider
					.receiveMessage(new JmsAdapter(), JMSProvider.HOST, JMSProvider.PORT, JMSProvider.IMPORT_CHANNEL);
		} catch (final JMSException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initializes the authorization strategy for the web application. Pages,
	 * which implement the {@link AuthenticatedWebPage} interface, are only
	 * accessible for authenticated users.
	 */
	private void setAuthorizationStrategy() {
		this.getSecuritySettings().setAuthorizationStrategy(new IAuthorizationStrategy() {

			@Override
			public boolean isActionAuthorized(final Component component, final Action action) {
				// authorize everything
				return true;
			}

			@Override
			public <T extends IRequestableComponent> boolean isInstantiationAuthorized(final Class<T> componentClass) {
				// Check if the new Page requires authentication
				// (implements the marker interface)
				if (AuthenticatedWebPage.class.isAssignableFrom(componentClass)) {
					// Is user signed in?
					if (((AuthenticatedSession) Session.get()).isSignedIn()) {
						// okay to proceed
						return true;
					}

					// Intercept the request, but remember the target
					// for later.
					// Invoke Component.continueToOriginalDestination()
					// after successful logon to
					// continue with the target remembered.

					throw new RestartResponseAtInterceptPageException(LoginPage.class);
				}

				// okay to proceed
				return true;
			}
		});
	}

	/**
	 * Loads the images for the web application at start-up.
	 */
	private void mountImages() {
		final ResourceReference alignmentImage = new PackageResourceReference(ImageReference.class, "alignment.jpg");
		this.mountResource("/images/alignment", alignmentImage);

		final ResourceReference eventStreamImage = new PackageResourceReference(ImageReference.class, "eventStream.jpg");
		this.mountResource("/images/eventStream", eventStreamImage);

		final ResourceReference groupImage = new PackageResourceReference(ImageReference.class, "group.jpg");
		this.mountResource("/images/group", groupImage);

		final ResourceReference processImage = new PackageResourceReference(ImageReference.class, "process.jpg");
		this.mountResource("/images/process", processImage);
	}

	/**
	 * Gets the adapter for the Esper event processing engine.
	 * 
	 * @return
	 */
	public StreamProcessingAdapter getEp() {
		return this.epAdapter;
	}

	public void setEp(final StreamProcessingAdapter epAdapter) {
		this.epAdapter = epAdapter;
	}
}