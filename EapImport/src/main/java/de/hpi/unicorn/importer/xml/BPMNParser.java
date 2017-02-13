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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.AttachableElement;
import de.hpi.unicorn.bpmn.element.BPMNAndGateway;
import de.hpi.unicorn.bpmn.element.BPMNBoundaryEvent;
import de.hpi.unicorn.bpmn.element.BPMNEndEvent;
import de.hpi.unicorn.bpmn.element.BPMNEventBasedGateway;
import de.hpi.unicorn.bpmn.element.BPMNEventBasedGatewayType;
import de.hpi.unicorn.bpmn.element.BPMNEventType;
import de.hpi.unicorn.bpmn.element.BPMNIntermediateEvent;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNSequenceFlow;
import de.hpi.unicorn.bpmn.element.BPMNStartEvent;
import de.hpi.unicorn.bpmn.element.BPMNSubProcess;
import de.hpi.unicorn.bpmn.element.BPMNTask;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;
import de.hpi.unicorn.event.EapEventType;

/**
 * This class generates a logical BPMN representation from a BPMN-2.0-XML
 * 
 * @author micha
 * 
 */
public class BPMNParser extends AbstractXMLParser {

	private static ArrayList<String> VALID_BPMN_XML_ELEMENTS = new ArrayList<String>(Arrays.asList("startEvent",
			"task", "sendTask", "subProcess", "boundaryEvent", "endEvent", "parallelGateway", "exclusiveGateway",
			"intermediateCatchEvent", "eventBasedGateway"));

	/**
	 * Parses a BPMN-2.0-XML from the given file path to a {@link BPMNProcess}.
	 * 
	 * @param filePath
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public static BPMNProcess generateProcessFromXML(final File file) throws IOException, SAXException {
		final Document doc = AbstractXMLParser.readXMLDocument(file);
		return BPMNParser.generateBPMNProcess(doc);
	}

	/**
	 * Parses a BPMN-2.0-XML from the given file path to a {@link BPMNProcess}.
	 * 
	 * @param filePath
	 * @return
	 * @throws XMLParsingException
	 */
	public static BPMNProcess generateProcessFromXML(final String filePath) throws XMLParsingException {

		Document doc;
		try {
			doc = AbstractXMLParser.readXMLDocument(filePath);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new XMLParsingException("could not read XSD named " + filePath
					+ " with the following error message:\n" + e.getMessage());
		}

		return BPMNParser.generateBPMNProcess(doc);
	}

	/**
	 * Parses a {@link BPMNProcess} from the {@link Document}.
	 * 
	 * @param doc
	 * @return
	 */
	private static BPMNProcess generateBPMNProcess(final Document doc) {
		if (doc != null) {
			final XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new BPMNNameSpaceContext());
			// XPath Query for showing all nodes value
			XPathExpression processElementsExpression = null;
			XPathExpression processExpression = null;
			try {
				processElementsExpression = xpath.compile("//ns:process/*");
				processExpression = xpath.compile("//ns:process");
			} catch (final XPathExpressionException e) {
				e.printStackTrace();
			}

			Object elementsResult = null;
			Object processResult = null;
			try {
				elementsResult = processElementsExpression.evaluate(doc, XPathConstants.NODESET);
				processResult = processExpression.evaluate(doc, XPathConstants.NODESET);
			} catch (final XPathExpressionException e) {
				e.printStackTrace();
			}
			final NodeList processNode = (NodeList) processResult;

			final BPMNProcess process = new BPMNProcess(BPMNParser.extractID(processNode.item(0)), "",
					new ArrayList<MonitoringPoint>());

			final NodeList processElements = (NodeList) elementsResult;
			for (int i = 0; i < processElements.getLength(); i++) {
				process.addBPMNElement(BPMNParser.extractBPMNElement(processElements.item(i)));
			}
			BPMNParser.linkProcessElements(process, processElements);
			return process;
		} else {
			System.err.println("Document was null!");
			return null;
		}
	}

	/**
	 * Extraction of BPMN-Elements from the given {@link Node}. <br>
	 * Supported elements: <br>
	 * <ul>
	 * <li>boundaryEvent</li>
	 * <li>endEvent</li>
	 * <li>eventBasedGateway</li>
	 * <li>exclusiveGateway</li>
	 * <li>intermediateCatchEvent</li>
	 * <li>parallelGateway</li>
	 * <li>sequenceFlow</li>
	 * <li>startEvent</li>
	 * <li>subProcess</li>
	 * <li>task</li>
	 * </ul>
	 * 
	 * @param element
	 * @return
	 */
	private static AbstractBPMNElement extractBPMNElement(final Node element) {
		if (element.getNodeName().equals("startEvent")) {
			return BPMNParser.extractStartEvent(element);
		} else if (element.getNodeName().equals("task") || element.getNodeName().equals("sendTask")) {
			return BPMNParser.extractTask(element);
		} else if (element.getNodeName().equals("subProcess")) {
			return BPMNParser.extractSubProcess(element);
		} else if (element.getNodeName().equals("boundaryEvent")) {
			return BPMNParser.extractBoundaryEvent(element);
		} else if (element.getNodeName().equals("endEvent")) {
			return BPMNParser.extractEndEvent(element);
		} else if (element.getNodeName().equals("exclusiveGateway")) {
			return BPMNParser.extractExclusiveGateway(element);
		} else if (element.getNodeName().equals("parallelGateway")) {
			return BPMNParser.extractParallelGateway(element);
		} else if (element.getNodeName().equals("eventBasedGateway")) {
			return BPMNParser.extractEventBasedGateway(element);
		} else if (element.getNodeName().equals("intermediateCatchEvent")) {
			return BPMNParser.extractIntermediateCatchEvent(element);
		} else if (element.getNodeName().equals("sequenceFlow")) {
			return BPMNParser.extractSequenceFlow(element);
		}
		return null;
	}

	/**
	 * Parses an {@link BPMNEndEvent} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNEndEvent extractEndEvent(final Node element) {
		return new BPMNEndEvent(BPMNParser.extractID(element), BPMNParser.extractName(element),
				BPMNParser.extractMonitoringPoints(element));
	}

	/**
	 * Parses an {@link BPMNBoundaryEvent} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNBoundaryEvent extractBoundaryEvent(final Node element) {
		final BPMNBoundaryEvent boundaryEvent = new BPMNBoundaryEvent(BPMNParser.extractID(element),
				BPMNParser.extractName(element), BPMNParser.extractMonitoringPoints(element),
				BPMNParser.extractEventType(element));
		boundaryEvent.setCancelActivity(BPMNParser.extractCancelActivity(element));
		switch (boundaryEvent.getIntermediateEventType()) {
		case Cancel:
			break;
		case Compensation:
			break;
		case Error:
			break;
		case Link:
			break;
		case Message:
			break;
		case Signal:
			break;
		case Timer:
			BPMNParser.extractEventTimerDefinition(element, boundaryEvent);
			break;
		default:
			break;
		}
		return boundaryEvent;
	}

	/**
	 * Parses an {@link BPMNEventType} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNEventType extractEventType(final Node element) {
		if (!BPMNParser.getChildNodesByNodeName(element, "timerEventDefinition").isEmpty()) {
			return BPMNEventType.Timer;
		} else if (!BPMNParser.getChildNodesByNodeName(element, "errorEventDefinition").isEmpty()) {
			return BPMNEventType.Error;
		} else if (!BPMNParser.getChildNodesByNodeName(element, "messageEventDefinition").isEmpty()) {
			return BPMNEventType.Message;
		}
		return BPMNEventType.Blank;
	}

	/**
	 * Parses the timer definition for the given intermediate event and assigns
	 * the definition to this element.
	 * 
	 * @param element
	 * @param intermediateEvent
	 */
	private static void extractEventTimerDefinition(final Node element, final BPMNIntermediateEvent intermediateEvent) {
		if (!BPMNParser.getChildNodesByNodeName(element, "timerEventDefinition").isEmpty()) {
			final List<Node> timerDefinitions = BPMNParser.getChildNodesByNodeName(element, "timerEventDefinition");
			for (final Node timerDefinition : timerDefinitions) {
				// TimeDuration ermitteln
				if (!BPMNParser.getChildNodesByNodeName(timerDefinition, "timeDuration").isEmpty()) {
					final String timeDuration = BPMNParser.getChildNodesByNodeName(timerDefinition, "timeDuration")
							.get(0).getTextContent();
					if (timeDuration != null) {
						float duration;
						try {
							duration = Float.parseFloat(timeDuration);
						} catch (final NumberFormatException n) {
							System.err.println("Time duration could not be parsed!");
							duration = 0;
						}
						intermediateEvent.setTimeDuration(duration);
					}
				}
			}
		}

	}

	/**
	 * Parses an {@link BPMNIntermediateEvent} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNIntermediateEvent extractIntermediateCatchEvent(final Node element) {
		final BPMNIntermediateEvent intermediateEvent = new BPMNIntermediateEvent(BPMNParser.extractID(element),
				BPMNParser.extractName(element), BPMNParser.extractMonitoringPoints(element),
				BPMNParser.extractIntermediateEventType(element));
		intermediateEvent.setCatchEvent(true);
		if (intermediateEvent.getIntermediateEventType().equals(BPMNEventType.Timer)) {
			BPMNParser.extractEventTimerDefinition(element, intermediateEvent);
		}
		return intermediateEvent;
	}

	/**
	 * Proofs, if the given {@link Node} has an cancelActivity attribute.
	 * 
	 * @param element
	 * @return
	 */
	private static boolean extractCancelActivity(final Node element) {
		if (element.getAttributes().getNamedItem("cancelActivity") != null) {
			final String cancelValue = element.getAttributes().getNamedItem("cancelActivity").getNodeValue();
			final boolean isCancelActivity = (cancelValue.equals("true")) ? true : false;
			return isCancelActivity;
		} else {
			return false;
		}
	}

	/**
	 * Parses an {@link BPMNSequenceFlow} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNSequenceFlow extractSequenceFlow(final Node element) {
		return new BPMNSequenceFlow(BPMNParser.extractID(element), BPMNParser.extractName(element),
				BPMNParser.extractSourceRef(element), BPMNParser.extractTargetRef(element));
	}

	/**
	 * Parses an {@link BPMNSubProcess} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNSubProcess extractSubProcess(final Node element) {
		final BPMNSubProcess subProcess = new BPMNSubProcess(BPMNParser.extractID(element),
				BPMNParser.extractName(element), BPMNParser.extractMonitoringPoints(element));
		final NodeList subProcessElements = element.getChildNodes();
		for (int i = 0; i < subProcessElements.getLength(); i++) {
			subProcess.addBPMNElement(BPMNParser.extractBPMNElement(subProcessElements.item(i)));
		}
		return subProcess;
	}

	/**
	 * Parses an {@link BPMNTask} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNTask extractTask(final Node element) {
		final List<MonitoringPoint> monitoringPoints = BPMNParser.extractMonitoringPoints(element);
		return new BPMNTask(BPMNParser.extractID(element), BPMNParser.extractName(element), monitoringPoints);
	}

	/**
	 * Parses an {@link BPMNAndGateway} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNAndGateway extractParallelGateway(final Node element) {
		final List<MonitoringPoint> monitoringPoints = BPMNParser.extractMonitoringPoints(element);
		return new BPMNAndGateway(BPMNParser.extractID(element), BPMNParser.extractName(element), monitoringPoints);
	}

	/**
	 * Parses an {@link BPMNEventBasedGateway} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNEventBasedGateway extractEventBasedGateway(final Node element) {
		final List<MonitoringPoint> monitoringPoints = BPMNParser.extractMonitoringPoints(element);
		return new BPMNEventBasedGateway(BPMNParser.extractID(element), BPMNParser.extractName(element),
				monitoringPoints, BPMNParser.extractEventGatewayType(element));
	}

	/**
	 * Parses an {@link BPMNXORGateway} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNXORGateway extractExclusiveGateway(final Node element) {
		final List<MonitoringPoint> monitoringPoints = BPMNParser.extractMonitoringPoints(element);
		return new BPMNXORGateway(BPMNParser.extractID(element), BPMNParser.extractName(element), monitoringPoints);
	}

	/**
	 * Parses an {@link BPMNStartEvent} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNStartEvent extractStartEvent(final Node element) {
		// TODO: MessageStartEvent unterstützen
		return new BPMNStartEvent(BPMNParser.extractID(element), BPMNParser.extractName(element),
				BPMNParser.extractMonitoringPoints(element), BPMNParser.extractEventType(element));
	}

	/**
	 * Returns the value of the name attribute for the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static String extractName(final Node element) {
		if (element.getAttributes().getNamedItem("name") != null) {
			return element.getAttributes().getNamedItem("name").getNodeValue();
		} else {
			return "";
		}
	}

	/**
	 * Parses an {@link BPMNEventBasedGatewayType} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNEventBasedGatewayType extractEventGatewayType(final Node element) {
		final String eventGatewayType = element.getAttributes().getNamedItem("eventGatewayType").getNodeValue();
		if (eventGatewayType.equals("Parallel")) {
			return BPMNEventBasedGatewayType.Parallel;
		} else {
			return BPMNEventBasedGatewayType.Exclusive;
		}
	}

	/**
	 * Returns the value of the sourceRef attribute for the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static String extractSourceRef(final Node element) {
		return element.getAttributes().getNamedItem("sourceRef").getNodeValue();
	}

	/**
	 * Returns the value of the targetRef attribute for the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static String extractTargetRef(final Node element) {
		return element.getAttributes().getNamedItem("targetRef").getNodeValue();
	}

	/**
	 * Returns the value of the id attribute for the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static String extractID(final Node element) {
		return element.getAttributes().getNamedItem("id").getNodeValue();
	}

	/**
	 * Parses an {@link BPMNEventType} from the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static BPMNEventType extractIntermediateEventType(final Node element) {
		// TODO: Implement other intermediate event type
		if (!BPMNParser.getChildNodesByNodeName(element, "messageEventDefinition").isEmpty()) {
			return BPMNEventType.Message;
		} else if (!BPMNParser.getChildNodesByNodeName(element, "timerEventDefinition").isEmpty()) {
			return BPMNEventType.Timer;
		}
		return null;
	}

	/**
	 * Parses a list of {@link MonitoringPoint}s for the given {@link Node}.
	 * 
	 * @param element
	 * @return
	 */
	private static List<MonitoringPoint> extractMonitoringPoints(final Node element) {
		final List<MonitoringPoint> monitoringPoints = new ArrayList<MonitoringPoint>();

		final ArrayList<Node> extensionElementNodes = BPMNParser.getChildNodesByNodeName(element, "extensionElements");
		assert (extensionElementNodes.size() < 2);
		if (extensionElementNodes.size() == 0) {
			return monitoringPoints;
		}
		final Node extensionElementNode = extensionElementNodes.get(0);

		// ArrayList<Node> monitoringPointNodes =
		// getChildNodesByNodeName(extensionElementNode, "de.hpi.unicorn:transition");
		final ArrayList<Node> monitoringPointNodes = BPMNParser.getChildNodesByNodeName(extensionElementNode,
				"cep:transition");

		for (final Node actualTransitionNode : monitoringPointNodes) {
			final String monitoringTypeString = actualTransitionNode.getAttributes().getNamedItem("type")
					.getNodeValue();
			for (final MonitoringPointStateTransition actualMonitoringPointType : MonitoringPointStateTransition
					.values()) {
				if (monitoringTypeString.equals(actualMonitoringPointType.toString())) {
					// EapEventType eventType =
					// EapEventType.findByTypeName(actualTransitionNode.getAttributes().getNamedItem("regularExpression").getNodeValue());
					final EapEventType eventType = EapEventType.findByTypeName(actualTransitionNode.getAttributes()
							.getNamedItem("pemp").getNodeValue());
					monitoringPoints.add(new MonitoringPoint(eventType, actualMonitoringPointType, ""));
				}
			}
		}
		return monitoringPoints;
	}

	/**
	 * Creates a successor and predecessor relationship for the given
	 * {@link AbstractBPMNElement} based on the {@link BPMNSequenceFlow}s.
	 * 
	 * @param process
	 * @param element
	 * @param childNodes
	 */
	private static void linkActualElement(final BPMNProcess process, final AbstractBPMNElement element,
			final NodeList childNodes) {
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node actualNode = childNodes.item(i);
			if (actualNode.getNodeType() == 1) {
				if (actualNode.getNodeName().equals("incoming")) {
					final BPMNSequenceFlow sequenceFlow = (BPMNSequenceFlow) process.getBPMNElementById(actualNode
							.getTextContent());
					AbstractBPMNElement.connectElements(process.getBPMNElementById(sequenceFlow.getSourceRef()),
							element);
				} else if (actualNode.getNodeName().equals("outgoing")) {
					final BPMNSequenceFlow sequenceFlow = (BPMNSequenceFlow) process.getBPMNElementById(actualNode
							.getTextContent());
					AbstractBPMNElement.connectElements(element,
							process.getBPMNElementById(sequenceFlow.getTargetRef()));
				}
			}
			if (actualNode.getNodeName().equals("subProcess")) {
				BPMNParser.linkProcessElements(process, actualNode.getChildNodes());
			}
		}
	}

	/**
	 * Creates a successor and predecessor relationship between the parsed
	 * {@link AbstractBPMNElement}s based on the {@link BPMNSequenceFlow}s.
	 * 
	 * @param process
	 * @param processElementNodes
	 */
	private static void linkProcessElements(final BPMNProcess process, final NodeList processElementNodes) {
		for (int i = 0; i < processElementNodes.getLength(); i++) {
			final Node actualNode = processElementNodes.item(i);
			if (actualNode.getNodeType() == 1 && BPMNParser.VALID_BPMN_XML_ELEMENTS.contains(actualNode.getNodeName())) {
				if (actualNode.hasChildNodes()) {
					final AbstractBPMNElement element = process.getBPMNElementById(actualNode.getAttributes()
							.getNamedItem("id").getNodeValue());
					if (element != null) {
						BPMNParser.linkActualElement(process, element, actualNode.getChildNodes());
						if (element instanceof BPMNBoundaryEvent) {
							BPMNParser.attachBoundaryEvent(process, (BPMNBoundaryEvent) element, actualNode);
						} else if (element instanceof BPMNSubProcess) {
							BPMNParser.linkProcessElements((BPMNSubProcess) element, actualNode.getChildNodes());
						}
					}
				}
			}
		}
	}

	/**
	 * Attaches a boundary event to the given {@link Node}.
	 * 
	 * @param process
	 * @param boundaryEvent
	 * @param actualNode
	 */
	private static void attachBoundaryEvent(final BPMNProcess process, final BPMNBoundaryEvent boundaryEvent,
			final Node actualNode) {
		final String attachedToElementID = actualNode.getAttributes().getNamedItem("attachedToRef").getNodeValue();
		final AbstractBPMNElement attachedToElement = process.getBPMNElementById(attachedToElementID);
		boundaryEvent.setAttachedToElement(attachedToElement);
		AbstractBPMNElement.connectElements(attachedToElement, boundaryEvent);
		if (attachedToElement instanceof AttachableElement) {
			final AttachableElement attachedElement = (AttachableElement) attachedToElement;
			attachedElement.setAttachedIntermediateEvent(boundaryEvent);
		}
	}

	/**
	 * Returns all child nodes for a given node with the given name, if any.
	 * 
	 * @param element
	 * @param nodeName
	 * @return
	 */
	private static ArrayList<Node> getChildNodesByNodeName(final Node element, final String nodeName) {
		final ArrayList<Node> resultList = new ArrayList<Node>();
		final NodeList childNodes = element.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node actualChildNode = childNodes.item(i);
			if (actualChildNode.getNodeType() == 1 && actualChildNode.getNodeName().equals(nodeName)) {
				resultList.add(actualChildNode);
			}
		}
		return resultList;
	}
}
