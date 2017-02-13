/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.user;

import de.hpi.unicorn.utils.HashUtil;

/**
 * This class manages the creation of new users.
 * 
 * @author micha
 */
public class UserProvider {

	/**
	 * Checks, if the given name is already used for another user.
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isEmailAlreadyInUse(final String email) {
		if (EapUser.findByMail(email).isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Creates a new user with the given name, password and mail, if the user
	 * name is not already in use.
	 * 
	 * @param name
	 * @param password
	 * @param email
	 */
	public static void createUser(final String name, final String password, final String email) {
		if (!UserProvider.isEmailAlreadyInUse(email)) {
			final EapUser user = new EapUser(name, password, email);
			user.save();
		} else {
			throw new RuntimeException("Mail address already in use.");
		}
	}

	public static EapUser findUser(final String email, final String password) {
		if (!EapUser.findByMail(email).isEmpty()) {
			final EapUser user = EapUser.findByMail(email).get(0);
			if (user.getPasswordHash().equals(HashUtil.generateHash(password))) {
				return user;
			}
		}
		return null;
	}
}