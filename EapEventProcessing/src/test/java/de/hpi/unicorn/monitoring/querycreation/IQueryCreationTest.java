/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.querycreation;

import de.hpi.unicorn.importer.xml.XMLParsingException;

/**
 * This interface should be implemented from classes, which tests the creation
 * of pattern queries.
 * 
 * @author micha
 */
public interface IQueryCreationTest {

	/**
	 * Tests the import of a BPMN process model for the current test.
	 * 
	 * @throws XMLParsingException
	 */
	public void testImport() throws XMLParsingException;

	/**
	 * Tests the creation of queries for the current BPMN process model.
	 * 
	 * @throws RuntimeException
	 * @throws XMLParsingException
	 */
	public void testQueryCreation() throws XMLParsingException, RuntimeException;

}
