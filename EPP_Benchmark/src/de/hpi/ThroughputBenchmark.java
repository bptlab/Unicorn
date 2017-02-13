package de.hpi;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ThroughputBenchmark {
	
	private static final int NUMBER_OF_CLIENTS = 1;
	private static final int QUERIES = 0;
	public static Integer START_NUMBER_OF_EVENTS = 100; 

	public static void main(String[] args) throws IOException, InterruptedException {
		//for (int query = 0; query < 1; query=query+1) {
			System.out.println(String.format("########################### Events: %d per Client (%d) Queries: %d  #############################", START_NUMBER_OF_EVENTS, NUMBER_OF_CLIENTS, QUERIES));
			Stopwatch.reset();
			List<EPPServiceClient> clients = new LinkedList<EPPServiceClient>();
			for (int clientNumber = 0; clientNumber < NUMBER_OF_CLIENTS; clientNumber++) {
				EPPServiceClient client = new EPPServiceClient(EPPServiceClient.SCHEMA_FOLDER, EPPServiceClient.EVENT_FOLDER, EPPServiceClient.QUERY_FOLDER, QUERIES, START_NUMBER_OF_EVENTS, EPPServiceClient.ImportType.JMS);
				client.initialize();
				clients.add(client);
			}
			System.out.print("Starting clients ...");
			for (Thread client : clients) {
				client.start();
			}
			System.out.print("done\n");
			System.out.print("Wainting for clients ...\n");
			for (Thread client : clients) {
				client.join();
			}
			System.out.print("done\n");
			System.out.print("Tearing down clients ...");
			for (EPPServiceClient client : clients) {
				client.tearDown();
			}
			System.out.print("done\n");
			Stopwatch.print();
			Stopwatch.write(String.format("%dEvents%dQueries%dClients", START_NUMBER_OF_EVENTS, QUERIES, NUMBER_OF_CLIENTS));
		//}
	}
}
