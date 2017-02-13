/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.xml.importer;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNSubProcess;
import de.hpi.unicorn.importer.xml.BPMNParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;

/**
 * This class tests the import of BPMN processes and subprocesses from XML.
 * 
 * @author micha
 */
public class BPMNParserTest {

	private static String filePath = System.getProperty("user.dir") + "/src/test/resources/bpmn/Kinomodell.bpmn20.xml";
	private static String complexfilePath = System.getProperty("user.dir")
			+ "/src/test/resources/bpmn/complexProcess.bpmn20.xml";
	private static String subProcessfilePath = System.getProperty("user.dir")
			+ "/src/test/resources/bpmn/Automontage_TwoTerminal.bpmn20.xml";

	@Test
	public void testXPathParsing() throws ParserConfigurationException, SAXException, IOException,
			XPathExpressionException {
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(false);
		final DocumentBuilder builder = domFactory.newDocumentBuilder();
		final Document doc = builder.parse(BPMNParserTest.filePath);
		final XPath xpath = XPathFactory.newInstance().newXPath();

		final XPathExpression expr = xpath.compile("//process/*");

		final Object result = expr.evaluate(doc, XPathConstants.NODESET);
		final NodeList nodes = (NodeList) result;
		this.printNodes(nodes, 1);
	}

	private void printNodes(final NodeList nodes, final int level) {
		for (int i = 0; i < nodes.getLength(); i++) {
			final Node actualNode = nodes.item(i);
			if (actualNode.getNodeType() == 1) {
				// System.out.println(actualNode.getNodeName() + " " + level);
			}
			if (actualNode.hasChildNodes()) {
				this.printNodes(actualNode.getChildNodes(), level + 1);
			}
		}
	}

	@Test
	public void testComplexProcess() throws XPathExpressionException, ParserConfigurationException, SAXException,
			IOException, XMLParsingException {
		final BPMNProcess BPMNProcess = BPMNParser.generateProcessFromXML(BPMNParserTest.complexfilePath);
		Assert.assertNotNull(BPMNProcess);
		Assert.assertTrue(BPMNProcess.getBPMNElementsWithOutSequenceFlows().size() == 21);
		Assert.assertTrue(BPMNProcess.getStartEvent().getId().equals("sid-EC585815-8EAC-411C-89C2-553ACA85CF5A"));
	}

	@Test
	public void testSubProcessImport() throws XMLParsingException {
		final BPMNProcess BPMNProcess = BPMNParser.generateProcessFromXML(BPMNParserTest.subProcessfilePath);
		Assert.assertNotNull(BPMNProcess);
		Assert.assertTrue(BPMNProcess.getBPMNElementsWithOutSequenceFlows().size() == 7);
		Assert.assertTrue(BPMNProcess.hasSubProcesses());
		final BPMNSubProcess subProcess = BPMNProcess.getSubProcesses().get(0);
		Assert.assertNotNull(subProcess);
		Assert.assertFalse(subProcess.getStartEvent().getSuccessors().isEmpty());
	}

}
