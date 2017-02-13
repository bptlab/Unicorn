/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.adapter;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.w3c.dom.Document;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.xml.XMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.utils.XMLUtils;

public class JmsAdapter implements MessageListener {

	@Override
	public void onMessage(final Message message) {
		if (message instanceof TextMessage) {
			try {
				// generate document from xml String
				final Document doc = XMLUtils.stringToDoc(((TextMessage) message).getText());
				if (doc == null) {
					return;
				}
				// generate the Event from the doc via XML Parser
				List<EapEvent> newEvents;
				newEvents = XMLParser.generateEventsFromDoc(doc);
				for (final EapEvent event : newEvents) {
					Broker.getEventImporter().importEvent(event);
				}
			} catch (final JMSException ex) {
				ex.printStackTrace();
			} catch (final XMLParsingException ex) {
				ex.printStackTrace();
			}
		}
	}
}
