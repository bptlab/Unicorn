/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.user.EapUser;

/**
 * This class tests the saving, finding and removing of {@link EapUser}.
 * 
 * @author micha
 */
public class UserPersistenceTest implements PersistenceTest {

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	@Override
	public void testStoreAndRetrieve() {
		storeExampleUsers();
		assertTrue("Value should be 2, but was " + EapUser.findAll().size(), EapUser.findAll().size() == 2);
		EapUser.removeAll();
		assertTrue("Value should be 0, but was " + EapUser.findAll().size(), EapUser.findAll().size() == 0);
	}

	@Test
	@Override
	public void testFind() {
		storeExampleUsers();
		assertTrue(EapUser.findAll().size() == 2);
		assertTrue(EapUser.findByName("Tsun").size() == 1);
		EapUser tsun = EapUser.findByName("Tsun").get(0);
		tsun.getMail().equals("tsun@mail.de");

		assertTrue(EapUser.findByMail("micha@mail.de").size() == 1);
		EapUser micha = EapUser.findByMail("micha@mail.de").get(0);
		micha.getName().equals("Micha");
	}

	@Test
	@Override
	public void testRemove() {
		storeExampleUsers();
		List<EapUser> users;
		users = EapUser.findAll();
		assertTrue(users.size() == 2);

		EapUser deletedUser = users.get(0);
		deletedUser.remove();

		users = EapUser.findAll();
		assertTrue(users.size() == 1);

		assertTrue(users.get(0).getID() != deletedUser.getID());
	}

	private void storeExampleUsers() {
		EapUser micha = new EapUser("Micha", "Micha1234", "micha@mail.de");
		micha.save();
		EapUser tsun = new EapUser("Tsun", "Tsun1234", "tsun@mail.de");
		tsun.save();
	}

}
