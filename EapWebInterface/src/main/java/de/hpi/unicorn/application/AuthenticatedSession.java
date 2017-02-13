/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

import de.hpi.unicorn.user.EapUser;
import de.hpi.unicorn.user.UserProvider;

/**
 * The session class for an authenticated session for the web application.
 * 
 * @author micha
 */
public class AuthenticatedSession extends AuthenticatedWebSession {

	private static final long serialVersionUID = 1L;

	private EapUser user;

	/**
	 * Constructor for an authenticated session for the web application.
	 * 
	 * @param request
	 */
	protected AuthenticatedSession(final Request request) {
		super(request);
	}

	/**
	 * Checks the given username and password and returns a user object if an
	 * user is be identified by email and password.
	 * 
	 * @param email
	 *            email address
	 * @param password
	 *            password
	 * @return true if the user is authenticated successfully
	 */
	@Override
	public final boolean authenticate(final String email, final String password) {
		this.user = UserProvider.findUser(email, password);
		return this.user != null;
	}

	public EapUser getUser() {
		return this.user;
	}

	/**
	 * Sets a new user.
	 * 
	 * @param user
	 * 
	 */
	public void setUser(final EapUser user) {
		this.user = user;
	}

	/**
	 * @see org.apache.wicket.authentication.AuthenticatedWebSession#getRoles()
	 */
	@Override
	public Roles getRoles() {
		return null;
	}
}
