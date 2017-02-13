/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.user;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.AuthenticatedSession;
import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.main.MainPage;
import de.hpi.unicorn.user.EapUser;

/**
 * A page to render a login form for authenticate users.
 * 
 * @author micha
 */
public class LoginPage extends AbstractEapPage {

	private static final long serialVersionUID = -7896431319431474548L;
	private Form<Void> loginForm;
	private TextField<String> emailInput;
	private PasswordTextField passwordInput;
	private Form<?> logoutForm;

	/**
	 * Constructor for a page to render a login form for authenticate users.
	 */
	public LoginPage() {
		super();
		this.buildMainLayout();
	}

	private void buildMainLayout() {
		this.loginForm = new WarnOnExitForm("loginForm");

		this.emailInput = new TextField<String>("emailInput", Model.of(""));
		this.loginForm.add(this.emailInput);

		this.passwordInput = new PasswordTextField("passwordInput", Model.of(""));
		this.loginForm.add(this.passwordInput);

		this.addLoginButton();
		this.addRegisterLink();

		this.add(this.loginForm);

		this.logoutForm = new Form<Object>("logoutForm");

		this.addLogoutButton();

		this.add(this.logoutForm);

		if (((AuthenticatedSession) Session.get()).getUser() != null) {
			this.loginForm.setVisible(false);
		} else {
			this.logoutForm.setVisible(false);
		}
	}

	private void addRegisterLink() {
		this.loginForm.add(new Link<Object>("registerLink") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				this.setResponsePage(RegisterPage.class);
			};
		});

	}

	private void addLoginButton() {
		final Button applyButton = new Button("loginButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				final String email = LoginPage.this.emailInput.getValue();
				final String password = LoginPage.this.passwordInput.getValue();
				if (LoginPage.this.login(email, password)) {
					this.setResponsePage(LoginPage.class);
				}
			}

		};
		this.loginForm.add(applyButton);
	}

	private void addLogoutButton() {
		final Button applyButton = new BlockingAjaxButton("logoutButton", this.logoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				this.getSession().invalidate();
				this.setResponsePage(MainPage.class);
			}

		};
		this.logoutForm.add(applyButton);
	}

	private boolean login(final String email, final String password) {
		final AuthenticatedSession session = this.getMySession();
		if (session.signIn(email, password)) {
			this.continueToOriginalDestination();
			return true;
		} else {
			if (EapUser.findByMail(email).isEmpty()) {
				this.error("Login failed: User does not exist.");
			} else {
				this.error("Login failed: Wrong password.");
			}
			return false;
		}
	}

	private AuthenticatedSession getMySession() {
		return (AuthenticatedSession) this.getSession();
	}

}
