/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.user;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.user.UserProvider;

/**
 * A page to render a register form to register new users.
 * 
 * @author micha
 */
public class RegisterPage extends AbstractEapPage {

	private static final long serialVersionUID = -7896431319431474548L;
	private Form<Void> layoutForm;
	private TextField<String> mailInput;
	private TextField<String> nameInput;
	private PasswordTextField passwordInput;
	private PasswordTextField repeatPasswordInput;

	/**
	 * Constructor for a page to render a register form to register new users.
	 */
	public RegisterPage() {
		super();
		this.buildMainLayout();
	}

	private void buildMainLayout() {
		this.layoutForm = new WarnOnExitForm("layoutForm");

		this.mailInput = new TextField<String>("mailInput", Model.of(""));
		this.layoutForm.add(this.mailInput);

		this.nameInput = new TextField<String>("nameInput", Model.of(""));
		this.layoutForm.add(this.nameInput);

		this.passwordInput = new PasswordTextField("passwordInput", Model.of(""));
		this.layoutForm.add(this.passwordInput);

		this.repeatPasswordInput = new PasswordTextField("repeatPasswordInput", Model.of(""));
		this.layoutForm.add(this.repeatPasswordInput);

		this.addRegisterButton();

		this.add(this.layoutForm);
	}

	private void addRegisterButton() {
		final Button applyButton = new Button("registerButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				final String userName = RegisterPage.this.nameInput.getValue();
				final String mail = RegisterPage.this.mailInput.getValue();
				final String password = RegisterPage.this.passwordInput.getValue();
				final String repeatPassword = RegisterPage.this.repeatPasswordInput.getValue();
				if (mail.isEmpty()) {
					this.error("Please provide a mail adress.");
					return;
				}
				if (password.isEmpty()) {
					this.error("Please provide a password.");
					return;
				}
				if (repeatPassword.isEmpty()) {
					this.error("Please provide the repeated password.");
					return;
				}
				if (userName.isEmpty()) {
					this.error("Please provide a user name.");
					return;
				}
				if (!password.equals(repeatPassword)) {
					this.error("Password and repeated password are not equal.");
					return;
				}
				if (UserProvider.isEmailAlreadyInUse(mail)) {
					this.error("Mail address is already taken.");
					return;
				}
				UserProvider.createUser(userName, repeatPassword, mail);
				this.success("User " + userName + " has been created!");
			}
		};
		this.layoutForm.add(applyButton);
	}

}
