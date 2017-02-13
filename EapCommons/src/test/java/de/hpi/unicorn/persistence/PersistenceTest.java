/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import de.hpi.unicorn.persistence.Persistable;

/**
 * This class is an interface for the testing of persistable classes It
 * comprises the saving, finding and removing of {@link Persistable}s.
 * 
 * @author micha
 */
public interface PersistenceTest {

	public void testStoreAndRetrieve();

	public void testFind();

	public void testRemove();

}
