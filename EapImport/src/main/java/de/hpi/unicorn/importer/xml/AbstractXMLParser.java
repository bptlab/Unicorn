/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * This class centralizes methods for the parsing of XML files.
 * 
 * @author micha
 */
public class AbstractXMLParser {

	public static final String CURRENT_TIMESTAMP = "Current timestamp";
	public static final String GENERATED_TIMESTAMP_COLUMN_NAME = "ImportTime";

	/**
	 * Returns a {@link Document} for a XML file from the given file path.
	 * 
	 * @param file
	 * @throws IOException
	 * @throws SAXException
	 */
	protected static Document readXMLDocument(final File file) throws IOException, SAXException {
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = domFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = null;
		doc = builder.parse(file);
		return doc;
	}

	/**
	 * Returns a {@link Document} for a XML file from the given file path.
	 * 
	 * @param filePath
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	protected static Document readXMLDocument(final String filePath) throws ParserConfigurationException, SAXException,
			IOException {
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		builder = domFactory.newDocumentBuilder();
		Document doc = null;
		doc = builder.parse(filePath);
		return doc;
	}

}
