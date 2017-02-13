/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.messageQueue;

import java.util.Properties;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQDestination;

import de.hpi.unicorn.configuration.EapConfiguration;

public class JMSProvider {

	static {
		final Properties properties = EapConfiguration.getProperties();
		JMSProvider.HOST = properties.getProperty("de.hpi.unicorn.messageQueue.jmsHost");
		JMSProvider.PORT = properties.getProperty("de.hpi.unicorn.messageQueue.jmsPort");
		JMSProvider.IMPORT_CHANNEL = properties.getProperty("de.hpi.unicorn.messageQueue.jmsImportChannel");
	}

	public static String HOST;
	public static String PORT;
	public static String IMPORT_CHANNEL;

	public static void receiveMessage(final MessageListener listener, final String brokerHost, final String brokerPort,
			final String topic) throws JMSException {
		// Create a ConnectionFactory
		final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(String.format("tcp://%s:%s",
				brokerHost, brokerPort));

		// Create a Connection
		final Connection connection = connectionFactory.createConnection();
		connection.start();

		// Create a Session
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create the destination (Topic or Queue)
		final Destination destination = session.createQueue(topic);

		// register MessageListener
		final MessageConsumer consumer = session.createConsumer(destination);
		consumer.setMessageListener(listener);
	}

	public static void sendMessage(final String brokerHost, final String brokerPort, final String topic,
			final String message) {
		try {
			// Create a ConnectionFactory
			final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(String.format(
					"tcp://%s:%s", brokerHost, brokerPort));

			// Create a Connection
			final Connection connection = connectionFactory.createConnection();
			connection.start();

			// Create a Session
			final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			final Destination destination = session.createQueue(topic);

			// Create a MessageProducer from the Session to the Topic or Queue
			final MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Create a messages
			final TextMessage messageObject = session.createTextMessage(message);

			// Tell the producer to send the message
			producer.send(messageObject);

			// Clean up
			producer.close();
			session.close();
			connection.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Queues are created dynamically, but can must be destroyed explicitly by
	 * calling this method. Queues with active subscribers cannot be destroyed
	 * and result in an exception.
	 * 
	 * @param brokerHost
	 * @param brokerPort
	 * @param topic
	 * @throws JMSException
	 */
	public static void destroyMessageQueue(final String brokerHost, final String brokerPort, final String topic)
			throws JMSException {
		// Create a ConnectionFactory
		final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(String.format("tcp://%s:%s",
				brokerHost, brokerPort));

		// Create a Connection
		final Connection connection = connectionFactory.createConnection();
		connection.start();

		// Create a Session
		final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create the destination (Topic or Queue), close Handle after that
		final Destination destination = session.createQueue(topic);
		session.close();

		// Destroy Empty Queue And Clean Up
		((ActiveMQConnection) connection).destroyDestination((ActiveMQDestination) destination);
		connection.close();
	}
}
