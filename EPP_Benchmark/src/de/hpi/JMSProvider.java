package de.hpi;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQDestination;

public class JMSProvider {

	public static String DEFAULT_BROKER_HOST = "bpt.hpi.uni-potsdam.de";
	public static String DEFAULT_BROKER_PORT = "61616";
	public static String ESPER_EVENT_QUEUE = "event_import_develop";

	public static void main(String[] args) throws JMSException {
		JMSProvider.sendMessage(DEFAULT_BROKER_HOST, DEFAULT_BROKER_PORT, "blablabla", "test");
		JMSProvider.destroyMessageQueue(DEFAULT_BROKER_HOST, DEFAULT_BROKER_PORT, "blablabla");
	}

	public static String receiveMessage(MessageListener listener, String brokerHost, String brokerPort, String topic) throws JMSException {
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(String.format("tcp://%s:%s", brokerHost, brokerPort));

		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.start();

		// Create a Session
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create the destination (Topic or Queue)
		Destination destination = session.createQueue(topic);

		// register MessageListener
		MessageConsumer consumer = session.createConsumer(destination);
		Message message = consumer.receive();
		if (message instanceof TextMessage) {
			return ((TextMessage) message).getText();
		} else {
			return null;
		}
	}

	public static void sendMessage(String brokerHost, String brokerPort, String topic, String message) {
		try {
			// Create a ConnectionFactory
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(String.format("tcp://%s:%s", brokerHost, brokerPort));

			// Create a Connection
			Connection connection = connectionFactory.createConnection();
			connection.start();

			// Create a Session
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create the destination (Topic or Queue)
			Destination destination = session.createQueue(topic);

			// Create a MessageProducer from the Session to the Topic or Queue
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Create a messages
			TextMessage messageObject = session.createTextMessage(message);

			// Tell the producer to send the message
			producer.send(messageObject);

			// Clean up
			producer.close();
			session.close();
			connection.close();
		} catch (Exception e) {
			//System.out.println("Caught: " + e);
			e.printStackTrace();
		}
	}

	public static void destroyMessageQueue(String brokerHost, String brokerPort, String topic) throws JMSException {
		// Create a ConnectionFactory
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(String.format("tcp://%s:%s", brokerHost, brokerPort));

		// Create a Connection
		Connection connection = connectionFactory.createConnection();
		connection.start();

		// Create a Session
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// Create the destination (Topic or Queue)
		Destination destination = session.createQueue(topic);
		session.close();

		// Destroy Empty Queue
		((ActiveMQConnection) connection).destroyDestination((ActiveMQDestination) destination);
		connection.close();
	}
}
