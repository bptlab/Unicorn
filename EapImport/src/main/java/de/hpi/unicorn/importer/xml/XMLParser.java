/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.hpi.unicorn.configuration.EapConfiguration;
import de.hpi.unicorn.configuration.MultipleEventValueHandling;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.importer.FileUtils;
import de.hpi.unicorn.importer.edifact.EdifactImporter;
import de.hpi.unicorn.utils.ConversionUtils;
import de.hpi.unicorn.utils.DateUtils;

/**
 * This class parses events from XML files.
 */
public class XMLParser extends AbstractXMLParser {

	/**
	 * Parses a single event from a XML file from the given file path.
	 * 
	 * @param filePath
	 * @return
	 * @throws XMLParsingException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static List<EapEvent> generateEventsFromXML(final String filePath) throws XMLParsingException {
		Document doc;
		try {
			doc = AbstractXMLParser.readXMLDocument(filePath);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new XMLParsingException("could not read XSD named " + filePath
					+ " with the following error message:\n" + e.getMessage());
		}
		return XMLParser.generateEvents(doc, null);
	}

	/**
	 * Parses a single event from a XML file from the given file path.
	 * 
	 * @param filePath
	 * @return
	 * @throws XMLParsingException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static List<EapEvent> generateEventsFromXML(final File file) throws XMLParsingException, IOException,
			SAXException {
		final Document doc = AbstractXMLParser.readXMLDocument(file);
		return XMLParser.generateEvents(doc, null);
	}

	/**
	 * Parses a single event from a XML file from the given file path under
	 * consideration of the given XSD. The XSD defines the contained nodes and
	 * attributes of the XML file.
	 * 
	 * @param filePath
	 * @return
	 * @throws XMLParsingException
	 */
	public static List<EapEvent> generateEventsFromXML(final String filePath, final String pathToXSD)
			throws XMLParsingException {
		Document doc;
		try {
			doc = AbstractXMLParser.readXMLDocument(filePath);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new XMLParsingException("could not read XSD named " + filePath
					+ " with the following error message:\n" + e.getMessage());
		}
		return XMLParser.generateEvents(doc, pathToXSD);
	}

	/**
	 * Parses a single event from a {@link Document}.
	 * 
	 * @param filePath
	 * @return
	 * @throws XMLParsingException
	 */
	public static List<EapEvent> generateEventsFromDoc(final Document xmlDoc) throws XMLParsingException {
		return XMLParser.generateEvents(xmlDoc, null);
	}

	/**
	 * Parses a single event from a {@link Document} under consideration of the
	 * given XSD. The XSD defines the contained nodes and attributes of the XML
	 * file.
	 * 
	 * @param filePath
	 * @return
	 * @throws XMLParsingException
	 */
	private static List<EapEvent> generateEvents(final Document doc, final String pathToXSD) throws XMLParsingException {
		final XPath xPath = XPathFactory.newInstance().newXPath();
		xPath.setNamespaceContext(new XSDNameSpaceContext());
		// XPath Query for showing all nodes value
		XPathExpression rootElementExpression = null;
		try {
			rootElementExpression = xPath.compile("/./child::*");
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
		assert (rootElementNodes.getLength() == 1);
		final Node actualRootElement = rootElementNodes.item(0);
		EapEventType eventType = null;
		if (pathToXSD == null) {
			final String XSDName = XMLParser.getXSDNameFromNode(actualRootElement);
			if (XSDName == null) {
				// do stuff for getting eventType for EDIFACT
				eventType = EdifactImporter.getInstance().getEventTypeForEdifact(doc);
			} else {
				eventType = EapEventType.findBySchemaName(XSDName);
				if (eventType == null) {
					eventType = EapEventType.findByTypeName(XSDName);
				}
			}
		} else {
			eventType = EapEventType.findBySchemaName(FileUtils.getFileNameWithoutExtension(pathToXSD));
			if (eventType == null) {
				eventType = XSDParser.generateEventTypeFromXSD(pathToXSD,
						FileUtils.getFileNameWithoutExtension(pathToXSD));
			}
		}

		if (eventType == null) {
			throw new XMLParsingException("No matching eventtype was found; please upload corresponding XSD");
		} else {
			Date eventTimestamp;
			// get XPath formatted timestamp name
			final String timestampName = eventType.getTimestampName();

			// retrieve event values
			final List<Map<String, String>> eventValuesList = XMLParser.generateEventTreeFromElement(eventType,
					actualRootElement);

			// if (timestampName == null ||
			// timestampName.equals(AbstractXMLParser.CURRENT_TIMESTAMP) ||
			// timestampName.equals(AbstractXMLParser.GENERATED_TIMESTAMP_COLUMN_NAME)){
			// eventTimestamp = new Date();
			// } else {
			// // retrieve timestamp from XML document
			// try {
			// XPathExpression timeElementExpression = xPath.compile("/" +
			// actualRootElement.getNodeName() + timestampName);
			// Object timeElementsResult = timeElementExpression.evaluate(doc,
			// XPathConstants.NODE);
			// Node timeNode = (Node) timeElementsResult;
			// time = timeNode.getTextContent();
			// System.out.println(time);
			// } catch (XPathExpressionException e) {
			// e.printStackTrace();
			// } catch (NullPointerException e1) {
			// e1.printStackTrace();
			// }
			//
			// eventTimestamp = (DateUtils.parseDate(time) != null) ?
			// DateUtils.parseDate(time) : new Date();
			// }
			// eventValueTree.retainAllByAttributeExpression(attributeExpressions);
			// return new EapEvent(eventType, eventTimestamp, eventValueTree);
			final List<EapEvent> events = new ArrayList<>();
			for (final Map<String, String> eventValues : eventValuesList) {
				if (timestampName == null || timestampName.equals(AbstractXMLParser.CURRENT_TIMESTAMP)
						|| timestampName.equals(AbstractXMLParser.GENERATED_TIMESTAMP_COLUMN_NAME)) {
					eventTimestamp = new Date();
				} else {
					final String time = eventValues.get(eventType.getTimestampName());
					if (time == null) {
						break;
					}
					eventTimestamp = (DateUtils.parseDate(time) != null) ? DateUtils.parseDate(time) : new Date();
				}
				eventValues.keySet().retainAll(eventType.getAttributeExpressionsWithoutTimestampName());
				final EapEvent event = new EapEvent(eventType, eventTimestamp);
				final String nameOfAttributeWithInvalidValue = ConversionUtils.validateEvent(eventType, eventValues);
				if (nameOfAttributeWithInvalidValue != null) {
					throw new XMLParsingException("Event in the XML files does not match to event type: "
							+ "Value type of attribute '" + nameOfAttributeWithInvalidValue + "' in the event "
							+ "does not match to the value type defined in the event type" + "or value for attribute '"
							+ nameOfAttributeWithInvalidValue + "' is missing in the event.");
				}
				event.setValuesWithoutConversion(eventValues);
				events.add(event);
			}
			if (events.size() == 0) {
				throw new XMLParsingException("Events in the XML file do not match to the given event type.");
			}
			return events;
		}
	}

	private static List<Map<String, String>> generateEventTreeFromElement(final EapEventType eventType,
			final Node actualRootElement) throws XMLParsingException {
		// getChildNodesFromEvent(actualRootElement, true);
		return XMLParser.getChildNodesFromEvent(eventType, new ArrayList<Map<String, String>>(),
				new HashMap<String, Integer>(), new String(), actualRootElement);
	}

	// /**
	// * Parses the attributes of the event from the given {@link Node}.
	// * @param actualRootElement
	// */
	// private static TransformationTree<String, Serializable>
	// getChildNodesFromEvent(Node actualRootElement, Boolean shouldBeRoot) {
	// NodeList childNodeList = actualRootElement.getChildNodes();
	// for(int i = 0; i < childNodeList.getLength(); i++){
	// Node childNode = childNodeList.item(i);
	// if (childNode.getNodeType() == Node.ELEMENT_NODE) {
	// // String nodeName = childNode.getNodeName().replace(":", "_");
	// String nodeName =
	// childNode.getNodeName().trim().replaceAll(" +","_").replaceAll("[^a-zA-Z0-9_]+","");
	// String nodeText = null;
	// if(!hasRealChildNodes(childNode)){
	// nodeText = childNode.getTextContent();
	// }
	// if(shouldBeRoot){
	// eventValueTree.addChild(null, nodeName, nodeText);
	// }
	// else{
	// // eventValueTree.addChild(actualRootElement.getNodeName().replace(":",
	// "_"), nodeName, nodeText);
	// eventValueTree.addChild(actualRootElement.getNodeName().trim().replaceAll(" +","_").replaceAll("[^a-zA-Z0-9_]+",""),
	// nodeName, nodeText);
	// }
	// getChildNodesFromEvent(childNode, false);
	// }
	// }
	// return eventValueTree;
	// }

	/**
	 * Parses the attributes of the event from the given {@link Node}.
	 * 
	 * @param actualRootElement
	 * @throws XMLParsingException
	 */
	private static List<Map<String, String>> getChildNodesFromEvent(final EapEventType eventType,
			List<Map<String, String>> eventValuesList, final Map<String, Integer> duplicatedAttributes,
			final String prefix, final Node actualRootElement) throws XMLParsingException {
		if (eventValuesList.isEmpty()) {
			eventValuesList.add(new HashMap<String, String>());
		}
		final NodeList childNodeList = actualRootElement.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); i++) {
			final Node childNode = childNodeList.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				final String nodeName = childNode.getNodeName().trim().replaceAll(" +", "_")
						.replaceAll("[^a-zA-Z0-9_]+", "");
				String nodeText = null;
				if (!XMLParser.hasRealChildNodes(childNode)) {
					nodeText = childNode.getTextContent();
					final TypeTreeNode attribute = eventType.getValueTypeTree().getAttributeByExpression(
							prefix + nodeName);
					if (attribute != null) {
						if (attribute.getType() == AttributeTypeEnum.DATE) {
							nodeText = XMLParser.formatDateNode(nodeText);
						}
						if (EapConfiguration.eventValueHandling[0] == MultipleEventValueHandling.CONCAT) {
							XMLParser.addToOrConcatAttributes(eventType, eventValuesList, duplicatedAttributes, prefix
									+ nodeName, nodeText);
						} else {
							addEventValue(eventType, eventValuesList, duplicatedAttributes, prefix + nodeName,
									nodeText, EapConfiguration.eventValueHandling[0]);
						}
					} else {
						if (eventType.getTimestampName() == null) {
							throw new XMLParsingException(String.format("xml does not match schema %s",
									eventType.getTypeName()));
						}
						if (eventType.getTimestampName().equals(prefix + nodeName)) {
							XMLParser.addToAttributes(eventValuesList, duplicatedAttributes, prefix + nodeName,
									XMLParser.formatDateNode(nodeText));
						}
					}
				}
				XMLParser.getChildNodesFromEvent(eventType, eventValuesList, duplicatedAttributes, prefix + nodeName
						+ ".", childNode);
			}
		}
		return eventValuesList;
	}

	private static void addEventValue(EapEventType eventType, List<Map<String, String>> eventValuesList,
			Map<String, Integer> duplicatedAttributes, String nodeName, String nodeText,
			MultipleEventValueHandling handling) {
		if (handling == MultipleEventValueHandling.CROSS) {
			XMLParser.addToAttributes(eventValuesList, duplicatedAttributes, nodeName, nodeText);
		} else if (handling == MultipleEventValueHandling.FIRST) {
			if (!eventValuesList.get(0).containsKey(nodeName)) {
				XMLParser.addToAttributes(eventValuesList, duplicatedAttributes, nodeName, nodeText);
			}
		} else if (handling == MultipleEventValueHandling.LAST) {
			eventValuesList.get(0).put(nodeName, nodeText);
		}
	}

	private static void addToAttributes(final List<Map<String, String>> eventValuesList,
			final Map<String, Integer> duplicatedAttributes, final String nodeName, final String nodeText) {
		List<Map<String, String>> attributeMapsToChange = eventValuesList;
		// event attribute occurs more than once
		if (eventValuesList.get(0).containsKey(nodeName)) {
			// copy the first attribute map
			int mapsToCopy = 1;
			// there are other attributes that occur multiple times, copy until
			// highest index
			if (!duplicatedAttributes.isEmpty()) {
				mapsToCopy = Collections.max(duplicatedAttributes.values());
			}
			final List<Map<String, String>> attributeMapsToCopy = eventValuesList.subList(0, mapsToCopy);
			final List<Map<String, String>> newAttributeMaps = new ArrayList<Map<String, String>>();
			for (final Map<String, String> attributeMap : attributeMapsToCopy) {
				newAttributeMaps.add(new HashMap<String, String>(attributeMap));
			}
			attributeMapsToChange = newAttributeMaps;
			eventValuesList.addAll(attributeMapsToChange);
			// update number of event attribute maps that need to be copied
			duplicatedAttributes.put(nodeName, eventValuesList.size());
		}
		for (final Map<String, String> eventValues : attributeMapsToChange) {
			eventValues.put(nodeName, nodeText);
		}
	}

	private static void addToOrConcatAttributes(EapEventType eventType, List<Map<String, String>> eventValuesList,
			final Map<String, Integer> duplicatedAttributes, final String nodeName, final String nodeText) {
		List<Map<String, String>> attributeMapsToChange = eventValuesList;
		// event attribute occurs more than once
		if (eventValuesList.get(0).containsKey(nodeName)) {
			if (eventType.getValueTypeTree().getAttributeByExpression(nodeName).getType() != AttributeTypeEnum.STRING) {
				// use MultipleEventValueHandling.DEFAULT behaviour
				// copy the first attribute map
				int mapsToCopy = 1;
				// there are other attributes that occur multiple times, copy
				// until
				// highest index
				if (!duplicatedAttributes.isEmpty()) {
					mapsToCopy = Collections.max(duplicatedAttributes.values());
				}
				final List<Map<String, String>> attributeMapsToCopy = eventValuesList.subList(0, mapsToCopy);
				final List<Map<String, String>> newAttributeMaps = new ArrayList<Map<String, String>>();
				for (final Map<String, String> attributeMap : attributeMapsToCopy) {
					newAttributeMaps.add(new HashMap<String, String>(attributeMap));
				}
				attributeMapsToChange = newAttributeMaps;
				eventValuesList.addAll(attributeMapsToChange);
				// update number of event attribute maps that need to be copied
				duplicatedAttributes.put(nodeName, eventValuesList.size());
			}
		}
		for (final Map<String, String> eventValues : attributeMapsToChange) {
			if (eventValues.containsKey(nodeName)
					&& eventType.getValueTypeTree().getAttributeByExpression(nodeName).getType() == AttributeTypeEnum.STRING) {
				eventValues.put(nodeName, eventValues.get(nodeName) + "," + nodeText);
			} else {
				eventValues.put(nodeName, nodeText);
			}
		}
	}

	/**
	 * Returns true, if this node has child nodes from the Node.ELEMENT_NODE
	 * type.
	 * 
	 * @param node
	 * @return
	 */
	private static boolean hasRealChildNodes(final Node node) {
		boolean hasChildNodes = false;
		final NodeList childNodeList = node.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); i++) {
			final Node childNode = childNodeList.item(i);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				hasChildNodes = true;
			}
		}
		return hasChildNodes;
	}

	/**
	 * Returns the XSD name for a given node.
	 * 
	 * @param element
	 * @return
	 */
	private static String getXSDNameFromNode(final Node element) {
		final Node xsdAttribute = element.getAttributes().getNamedItem("xsi:noNamespaceSchemaLocation");
		if (xsdAttribute == null) {
			System.err.println("no xsd stated in xml");
			return null;
		}
		;
		final String filePath = xsdAttribute.getNodeValue();
		final int begin = filePath.lastIndexOf("/");
		final int end = filePath.lastIndexOf(".");
		return filePath.substring(begin + 1, end);
	}

	/**
	 * Returns the first child with the given name from a given node.
	 * 
	 * @param name
	 * @param parentNode
	 * @return
	 */
	public static Node getFirstChildWithNameFromNode(final String name, final Node parentNode) {
		final NodeList list = parentNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(name)) {
				return list.item(i);
			}
		}
		return null;
	}

	/**
	 * Returns the last child with the given name from a given node.
	 * 
	 * @param name
	 * @param parentNode
	 * @return
	 */
	public static Node getLastChildWithNameFromNode(final String name, final Node parentNode) {
		final NodeList list = parentNode.getChildNodes();
		Node namedNode = null;
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(name)) {
				namedNode = list.item(i);
			}
		}
		return namedNode;
	}

	/**
	 * Returns all childs with the given name from a given node.
	 * 
	 * @param name
	 * @param parentNode
	 * @return
	 */
	public static List<Node> getAllChildWithNameFromNode(final String name, final Node parentNode) {
		final List<Node> resultList = new ArrayList<Node>();
		final NodeList list = parentNode.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeName().equals(name)) {
				resultList.add(list.item(i));
			}
		}
		return resultList;
	}

	public static Document XMLStringToDoc(final String xml) {
		InputStream xsdInputStream;
		try {
			xsdInputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = domFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		// need document from xml for the xml parser
		Document doc = null;
		// System.out.println("received EventType: \n" + xml);
		try {
			doc = builder.parse(xsdInputStream);
		} catch (final SAXException e) {
			e.printStackTrace();
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}

		return doc;
	}

	private static String formatDateNode(final String nodeText) {
		final Date nodeTextAsDate = (DateUtils.parseDate(nodeText) != null) ? DateUtils.parseDate(nodeText)
				: new Date();
		final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		return formatter.format(nodeTextAsDate);
	}

}
