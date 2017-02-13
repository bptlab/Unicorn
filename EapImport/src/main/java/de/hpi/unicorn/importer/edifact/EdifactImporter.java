/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.edifact;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.milyn.Smooks;
import org.milyn.smooks.edi.unedifact.UNEdifactReaderConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.importer.FileUtils;
import de.hpi.unicorn.importer.xml.XMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.importer.xml.XSDParser;

/**
 * adapter for Edifact files
 */
public class EdifactImporter {

	private static Smooks smooks;

	/**
	 * @param args
	 * @throws Exception
	 * @throws XMLParsingException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static EdifactImporter instance = null;

	/**
	 * singleton
	 */
	public static EdifactImporter getInstance() {
		if (EdifactImporter.instance == null) {
			EdifactImporter.instance = new EdifactImporter();
		}
		return EdifactImporter.instance;
	}

	/**
	 * generates Events from Edifact file
	 */
	public List<EapEvent> generateEventFromEdifact(final String filePath) throws XMLParsingException, Exception {
		final StreamResult result = this.convertEdiFileToXML(filePath);
		final Document doc = FileUtils.createDocumentFromResult(result);
		return XMLParser.generateEventsFromDoc(doc);
	}

	/**
	 * convert edifact file located in path to XML
	 */
	public StreamResult convertEdiFileToXML(final String path) throws Exception {
		final StreamSource messageIn = new StreamSource(new File(path));

		final Writer outWriter = new StringWriter();
		final StreamResult result = new StreamResult(outWriter);

		try {
			final String message = FileUtils.getFileContentAsString(path);
			EdifactImporter.smooks = this.getSmooksForEdifactFile(message);
			EdifactImporter.smooks.filterSource(messageIn, result);
		} catch (final Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (EdifactImporter.smooks != null) {
				EdifactImporter.smooks.close();
			}
		}
		return result;
	}

	private Smooks getSmooksForEdifactFile(final String message) throws Exception {
		if (message.contains("COPINO:D:95B:UN:INT10I")) {
			// This file is momentarily located at Dropbox
			// since the path cannot be resolved by smooks if java is started
			// from a different Project than EapImport
			// Move file to server (but then it of course would only be
			// accessible within the HPI or find other solution
			// mh/11.08.14: use Java's resource lookup
			final Smooks smooks_copino = new Smooks(EdifactImporter.class.getResource("/smooks-config-copino.xml")
					.openStream());
			// "http://172.16.64.105/epp/smooks-config-copino.xml");
			// "https://dl.dropboxusercontent.com/u/18481312/SushiResources/smooks-config-copino.xml");
			// This only works within EapImport...
			// Smooks smooks_copino = new Smooks("smooks-config-copino.xml");
			// System.out.println("use modified d95b");
			return smooks_copino;
		}

		if (message.contains(":D:03A")) {
			final Smooks smooks_95b = new Smooks();
			smooks_95b.setReaderConfig(new UNEdifactReaderConfigurator(
					"urn:org.milyn.edi.unedifact:d03a-mapping:1.5-SNAPSHOT"));
			// System.out.println("use do3a");
			return smooks_95b;
		}
		if (message.contains(":D:95B")) {
			final Smooks smooks_95b = new Smooks();
			smooks_95b.setReaderConfig(new UNEdifactReaderConfigurator(
					"urn:org.milyn.edi.unedifact:d95b-mapping:1.5-SNAPSHOT"));
			// System.out.println("use d95b");
			return smooks_95b;
		}
		if (message.contains(":D:00B")) {
			final Smooks smooks_00b = new Smooks();
			smooks_00b.setReaderConfig(new UNEdifactReaderConfigurator(
					"urn:org.milyn.edi.unedifact:d00b-mapping:1.5-SNAPSHOT"));
			// System.out.println("use d00b");
			return smooks_00b;
		}
		throw new Exception("The edifact standard used in this document is not supported.");

	}

	/**
	 * returns Eventtyp for given Edifact document
	 * 
	 */
	public EapEventType getEventTypeForEdifact(final Document doc) throws XMLParsingException {
		/**
		 * <env:interchangeMessage
		 * xmlns:c="urn:org.milyn.edi.unedifact:un:d03a:common"
		 * xmlns:berman="urn:org.milyn.edi.unedifact:un:d03a:berman"> <env:UNH>
		 * <env:messageRefNum>123827613X</env:messageRefNum>
		 * <env:messageIdentifier> <env:id>BERMAN</env:id>
		 * <env:versionNum>D</env:versionNum>
		 * <env:releaseNum>03A</env:releaseNum>
		 */
		String id = null;
		String version = null;
		String release = null;

		if (doc == null) {
			System.err.println("no document given");
		}

		final XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression miIdExpression = null;
		XPathExpression miVersionExpression = null;
		XPathExpression miReleaseExpression = null;
		try {
			miIdExpression = xpath.compile("//*[local-name() = 'interchangeMessage']/" + "*[local-name() = 'UNH']/"
					+ "*[local-name() = 'messageIdentifier']/" + "*[local-name() = 'id']");
			miVersionExpression = xpath.compile("//*[local-name() = 'interchangeMessage']/"
					+ "*[local-name() = 'UNH']/" + "*[local-name() = 'messageIdentifier']/"
					+ "*[local-name() = 'versionNum']");
			miReleaseExpression = xpath.compile("//*[local-name() = 'interchangeMessage']/"
					+ "*[local-name() = 'UNH']/" + "*[local-name() = 'messageIdentifier']/"
					+ "*[local-name() = 'releaseNum']");
		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		}

		try {
			final Object idResult = miIdExpression.evaluate(doc, XPathConstants.NODE);
			id = ((Node) idResult).getTextContent();
			final Object versionResult = miVersionExpression.evaluate(doc, XPathConstants.NODE);
			version = ((Node) versionResult).getTextContent();
			final Object releaseResult = miReleaseExpression.evaluate(doc, XPathConstants.NODE);
			release = ((Node) releaseResult).getTextContent();
		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		} catch (final NullPointerException e1) {
			throw new XMLParsingException("No matching eventtype was found; please upload corresponding XSD");
			// throw new XMLParsingException("no edifact-formatted event"); TODO
		}

		// System.out.println(getFilePathForEdifactXSD(id, version, release));
		final EapEventType eventType = XSDParser.generateEventTypeFromXSD(
				EdifactImporter.getFilePathForEdifactXSD(id, version, release),
				FileUtils.getFileNameWithoutExtension(EdifactImporter.getFilePathForEdifactXSD(id, version, release)));
		return eventType;
	}

	private static String getFilePathForEdifactXSD(final String id, final String version, final String release) {
		final String path = System.getProperty("user.dir") + "/src/main/resources/";

		if (id.equals("BERMAN") && version.equals("D") && release.equals("03A")) {
			return path + "xsd-definitions/berman.xsd";
		}

		if (id.equals("IFTMCS") && version.equals("D") && release.equals("00B")) {
			return path + "xsd-definitions/iftmcs.xsd";
		}

		if (id.equals("COPRAR") && version.equals("D") && release.equals("95B")) {
			return path + "xsd-definitions/coprar.xsd";
		}

		if (id.equals("COARRI") && version.equals("D") && release.equals("95B")) {
			return path + "xsd-definitions/coarri.xsd";
		}

		if (id.equals("COPINO") && version.equals("D") && release.equals("95B")) {
			return path + "xsd-definitions/copino.xsd";
		}

		return null;
	}

	public static void main(final String[] args) {
		try {
			final Smooks smooksForEdifactFile = new EdifactImporter().getSmooksForEdifactFile("COPINO:D:95B:UN:INT10I");
			System.out.println(smooksForEdifactFile);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
