package de.hpi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.jms.JMSException;

public class EPPServiceClient extends Thread {

	public static final String SCHEMA_FOLDER = "D:/GET/xsd";
	public static final String EVENT_FOLDER = "D:/GET/event";
	public static final String QUERY_FOLDER = "D:/GET/query";
	public static final String EVENT_IMPORT = "Event Import";
	public static final String EVENT_CLIENT = "Event Client";
	private static final String QUEUE_MAIL_ADDRESS = "example@example.com";
	private EventProcessingPlatformWebservicePortTypeProxy service;
	private String schemaFolder;
	private String eventFolder;
	private LinkedList<String> schema;
	private LinkedList<String> events;
	private LinkedList<String> queries;
	private String queryFolder;
	private Integer numberOfEvents;
	private Integer numberOfQueries;
	private boolean uninitialized = true;
	private ImportType importType;
	private String lastEventID;
	public EPPServiceClient() {
		this.schemaFolder = SCHEMA_FOLDER;
		this.eventFolder = EVENT_FOLDER;
		this.queryFolder = QUERY_FOLDER;
		this.numberOfEvents = -1;
		this.numberOfQueries = -1;
		this.importType = ImportType.SOAP;
	}
	public EPPServiceClient(String schemaFolder2, String eventFolder2, String queryFolder2, Integer numberOfQueries, Integer numberOfEvents, ImportType importType) {
		this.schemaFolder = schemaFolder2;
		this.eventFolder = eventFolder2;
		this.queryFolder = queryFolder2;
		this.numberOfEvents = numberOfEvents;
		this.numberOfQueries = numberOfQueries;
		this.importType = importType;
	}

	public static void main(String[] args) throws IOException, JMSException {
		EPPServiceClient client = new EPPServiceClient();
		//String id = client.getService().registerQueryForQueue("title1", "Select addressmatch as address, speed as speedvalue, property as property FROM traceNEW", "test@test.de");
		client.getService().unregisterQueryFromQueue("8c200955-a0c4-4c45-8265-6f904fda1be8");
		//		while (true)
		//			System.out.println(JMSProvider.receiveMessage(null, JMSProvider.DEFAULT_BROKER_HOST, JMSProvider.DEFAULT_BROKER_PORT, "5d355d84-e33b-4ce6-829d-5a67494d6321"));
	}

	public void tearDown() {
		for (String query : getQueries()) {
			try {
				unregisterQueryFromServer(query);
			} catch (RemoteException e) {
				//System.out.println(String.format("Error in Query: %s in Thread: %d", query, Thread.currentThread().getId()));
			}
		}
	}

	public void run() {
		try {
			if (this.uninitialized) {
				throw new ClientUninitializedException("call initialize before run first (and don't forget tearDown either)");
			}
			String id = Stopwatch.start(EVENT_CLIENT);
			importEvents(this.importType);
			Stopwatch.stop(EVENT_CLIENT, id);
		} catch (ClientUninitializedException e) {
			e.printStackTrace();
		}
	}

	public void initialize() throws IOException {
		this.uninitialized = false;
		File[] schemas = readFolder(this.schemaFolder);
		File[] events = readFolder(this.eventFolder);
		File[] queries = readFolder(this.queryFolder);

		// load and create schemas
		for (int i = 0; i < schemas.length; i++) {
			String schema = readFileToString(schemas[i]);
			String name = schemas[i].getName();
			createSchemaOnServer(schema, name.substring(0, name.indexOf(".")));
			getSchema().add(schema);
		}
		//System.out.println(String.format("Thread: %d schemas %s", Thread.currentThread().getId(), getSchema().toString()));

		// load and register queries
		for (int i = 0; i < this.numberOfQueries; i++) {
			int index = wrapIndex(queries, i);
			String query = readFileToString(queries[index]);
			String id = registerQueryOnServer(UUID.randomUUID().toString(), query);
			getQueries().add(id);
		}
		//System.out.println(String.format("Thread: %d queries %s", Thread.currentThread().getId(), getQueries().toString()));

		// load events
		for (int i = 0; i < this.numberOfEvents; i++) {
			int index = wrapIndex(events, i);
			String event = readFileToString(events[index]);
			getEvents().add(event);
		}
	}

	private int wrapIndex(File[] queries2, int i) {
		while (i >= queries2.length) {
			i = i - queries2.length;
		}
		return i;
	}

	private List<String> getQueries() {
		if (this.queries == null) {
			this.queries = new LinkedList<String>();
		}
		return this.queries;
	}

	public void importEvents(ImportType type) {
		for (String event : getEvents()) {
			try {
				switch (type) {
					case SOAP:
						sendEventToServer(event);
						break;
					case JMS:
						sendEventToQueue(event);
						break;
				}
			} catch (RemoteException e) {
				Stopwatch.drop(EVENT_IMPORT, this.lastEventID);
				System.out.println(String.format("Error in Event: %d in Thread: %d", getEvents().indexOf(event), Thread.currentThread().getId()));
			}
		}
	}

	private void sendEventToQueue(String event) {
		this.lastEventID = Stopwatch.start(EVENT_IMPORT);
		JMSProvider.sendMessage(JMSProvider.DEFAULT_BROKER_HOST, JMSProvider.DEFAULT_BROKER_PORT, JMSProvider.ESPER_EVENT_QUEUE, event);
		Stopwatch.stop(EVENT_IMPORT, this.lastEventID);
	}

	public String registerQueryOnServer(String title, String query) throws RemoteException {
		return getService().registerQueryForQueue(title, query, QUEUE_MAIL_ADDRESS);
	}

	public void unregisterQueryFromServer(String id) throws RemoteException {
		getService().unregisterQueryFromQueue(id);
	}

	private String readFileToString(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		StringBuilder string = new StringBuilder();

		while ((line = reader.readLine()) != null) {
			string.append(line);
		}

		reader.close();
		return string.toString();
	}

	private List<String> getSchema() {
		if (this.schema == null) {
			this.schema = new LinkedList<String>();
		}
		return this.schema;
	}

	private List<String> getEvents() {
		if (this.events == null) {
			this.events = new LinkedList<String>();
		}
		return this.events;
	}

	private void createSchemaOnServer(String schema, String name) throws RemoteException {
		getService().registerEventType(schema, name, null);
	}

	public void sendEventToServer(String xml) throws RemoteException {
		this.lastEventID = Stopwatch.start(EVENT_IMPORT);
		getService().importEvents(xml);
		Stopwatch.stop(EVENT_IMPORT, this.lastEventID);
	}

	public File[] readFolder(String folder) {
		File file = new File(folder);
		return file.listFiles();
	}

	public EventProcessingPlatformWebservicePortTypeProxy getService() {
		if (this.service == null) {
			this.service = new EventProcessingPlatformWebservicePortTypeProxy();
		}
		return this.service;
	}

	public enum ImportType {
		SOAP, JMS
	}

	class ClientUninitializedException extends Exception {

		/**
		 *
		 */
		private static final long serialVersionUID = 5137737014634502853L;

		public ClientUninitializedException(String message) {
			super(message);
		}

	}
}