/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.xml;

import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.exception.UnparsableException;
import de.hpi.unicorn.exception.UnparsableException.ParseType;
import de.hpi.unicorn.importer.FileUtils;

/**
 * This class parses XSD files, which describes the schema of XML files.
 */
public class XSDParser extends AbstractXMLParser {

	private static AttributeTypeTree eventTree;

	/**
	 * Creates a new {@link EapEventType} with the given name from a XSD from
	 * the given file path.
	 * 
	 * @param filePath
	 * @param eventTypeName
	 * @return
	 * @throws XMLParsingException
	 */
	public static EapEventType generateEventTypeFromXSD(final String filePath, final String eventTypeName)
			throws XMLParsingException {

		Document doc;
		String xsd;
		try {
			xsd = FileUtils.getFileContentAsString(filePath);
			doc = XMLParser.XMLStringToDoc(xsd);
			// doc = readXMLDocument(filePath);
		} catch (final IOException e) {
			throw new XMLParsingException("Could not read XSD named " + filePath + " with error:\n" + e.getMessage());
		}
		if (doc == null) {
			throw new XMLParsingException("Could not read XSD: " + filePath);
		}
		final EapEventType eventType = XSDParser.generateEventType(doc, eventTypeName);
		eventType.setXsdString(xsd);
		return eventType;
	}

	/**
	 * Creates a new {@link EapEventType} with the given name from a XSD from
	 * the given document.
	 * 
	 * @param doc
	 * @param schemaName
	 * @return
	 */
	public static EapEventType generateEventType(final Document doc, final String schemaName) {
		final XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(new XSDNameSpaceContext());
		// XPath Query for showing all nodes value
		XPathExpression rootElementExpression = null;
		try {
			rootElementExpression = xpath.compile("//xs:schema/child::xs:element");
		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		}

		Object rootElementsResult = null;
		try {
			rootElementsResult = rootElementExpression.evaluate(doc, XPathConstants.NODESET);

		} catch (final XPathExpressionException e) {
			e.printStackTrace();
		}
		final NodeList rootElementNodes = (NodeList) rootElementsResult;
		// Jedes Root-Element wird ein EventType
		// Im Root-Element nach weiteren Unterelementen suchen
		if (!(rootElementNodes.getLength() == 1)) {
			System.err.println("Number of root elements is wrong, should be 1 but was " + rootElementNodes.getLength());
		}
		final Node actualRootElement = rootElementNodes.item(0);
		XSDParser.eventTree = new AttributeTypeTree();
		// String eventTypeName =
		// actualRootElement.getAttributes().getNamedItem("name").getNodeValue();
		XSDParser.addChildElementsFromElement(actualRootElement, null, null);

		return new EapEventType(schemaName, XSDParser.eventTree, null, schemaName);
	}

	/**
	 * Creates a new {@link EapEventType} with the given name from a XSD from
	 * the given document.
	 * 
	 * @param xsd
	 * @param schemaName
	 * @param timestampName
	 * @return
	 * @throws UnparsableException
	 */
	public static EapEventType generateEventType(final String xsd, final String schemaName, final String timestampName)
			throws UnparsableException {
		final Document doc = XMLParser.XMLStringToDoc(xsd);
		if (doc == null) {
			throw new UnparsableException(ParseType.EVENT_TYPE);
		}
		final EapEventType eventType = XSDParser.generateEventType(doc, schemaName);
		final TypeTreeNode timestampAttribute = eventType.getValueTypeTree().getAttributeByExpression(timestampName);
		if (timestampAttribute != null) {
			timestampAttribute.removeAttribute();
		}
		if (timestampName != null && !timestampName.isEmpty()) {
			eventType.setTimestampName(timestampName);
		}
		eventType.setXsdString(xsd);
		return eventType;
	}

	/**
	 * Creates a {@link AttributeTypeTree} for the given nodes.
	 * 
	 * @param actualRootElement
	 * @param realRootElement
	 * @param realRootAttribute
	 * @return
	 */
	private static AttributeTypeTree addChildElementsFromElement(final Node actualRootElement,
			final Node realRootElement, final TypeTreeNode realRootAttribute) {
		final NodeList childNodeList = actualRootElement.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); i++) {
			final Node childNode = childNodeList.item(i);
			if (childNode.getNodeType() == 1) {
				if (childNode.getNodeName().equals("xs:complexType") || childNode.getNodeName().equals("xs:sequence")) {
					XSDParser.addChildElementsFromElement(childNode, realRootElement, realRootAttribute);
				} else if (childNode.getNodeName().equals("xs:element")) {
					AttributeTypeEnum attributeType = null;
					String xsElementType = null;
					if (childNode.getAttributes().getNamedItem("type") != null) {
						xsElementType = childNode.getAttributes().getNamedItem("type").getNodeValue();
					}
					if (xsElementType == null) {
						// chooses String if no 'type' attribute in xs:element
						// found
						attributeType = AttributeTypeEnum.STRING;
					} else {
						if (xsElementType.toLowerCase().equals("xs:date")
								|| xsElementType.toLowerCase().equals("xs:datetime")
								|| xsElementType.toLowerCase().equals("xs:time")) {
							attributeType = AttributeTypeEnum.DATE;
						} else if (xsElementType.toLowerCase().equals("xs:byte")
								|| xsElementType.toLowerCase().equals("xs:decimal")
								|| xsElementType.toLowerCase().equals("xs:int")
								|| xsElementType.toLowerCase().equals("xs:integer")
								|| xsElementType.toLowerCase().equals("xs:long")) {
							attributeType = AttributeTypeEnum.INTEGER;
						} else if (xsElementType.toLowerCase().equals("xs:double")
								|| xsElementType.toLowerCase().equals("xs:float")) {
							attributeType = AttributeTypeEnum.FLOAT;
						} else {
							attributeType = AttributeTypeEnum.STRING;
						}
					}
					TypeTreeNode newAttribute;
					final String attributeName = childNode.getAttributes().getNamedItem("name").getNodeValue().trim()
							.replaceAll(" +", "_").replaceAll("[^a-zA-Z0-9_]+", "");
					if (realRootElement == null) {
						newAttribute = new TypeTreeNode(attributeName, attributeType);
						XSDParser.eventTree.addRoot(newAttribute);
					} else {
						newAttribute = new TypeTreeNode(realRootAttribute, attributeName, attributeType);
					}
					XSDParser.addChildElementsFromElement(childNode, childNode, newAttribute);
				}
			}
		}
		return XSDParser.eventTree;
	}

}
