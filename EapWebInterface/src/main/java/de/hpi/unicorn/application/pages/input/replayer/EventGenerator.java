package de.hpi.unicorn.application.pages.input.replayer;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.EapEvent;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Array;

public class EventGenerator {
    private Date[] dateOfServiceIntervention;
    private Date[] dateOfInstallation;
    private int[] factoryID;
    private int[] dateOfProduction;
    private int[] counter;
    private double[] softwareVersion;
    private String[] feedbackOfInstaller;
    private int[] objectID;
    private int[] locationOfDeviceID;
    private long[] modelID;
    private String[] modelTitle;
    private int[] codingPlugID;
    private int[] codingPlugBusID;
    private double[] codingPlugSoftwareVersion;
    private String[] errorID;
    private int[] errorFailureTreeID;
    private String[] errorDescription;
    private int[] causeID;
    private String[] causeDescription;
    private long[] replacementPartID;
    private String[] replacementPartName;

    private int eventCount;
    private static Random random = new Random();

    public EventGenerator() {
        Date [] dateOfServiceIntervention = {
                new Date(2014,11,7,12,0,0),
                new Date(2014,11,8,12,0,0),
                new Date(2014,11,9,12,0,0),
                new Date(2014,11,10,12,0,0)};
        Date[] dateOfInstallation = {
                new Date(2013,11,7,12,0,0),
                new Date(2013,11,8,12,0,0),
                new Date(2013,11,9,12,0,0),
                new Date(2013,11,10,12,0,0)};
        int[] factoryID = {8290};
        int[] dateOfProduction = {303,305,455,209};
        int[] counter = {1398,39068,22748,9569};
        double[] softwareVersion = {4.09,4.05};
        String[] feedbackOfInstaller = {"Elektrode getauscht",
                "Gasarmatur getauscht",
                "Luftansaugrohr zur Gasarmatur montiert",
                "Kabelbaum getauscht"};
        int[] objecktID = {32835142,20228133,18701976,32842891};
        int[] locationOfDeviceID = {32835144,32832677,5650483,20343255};
        long[] modelID = {7716010416l,7716010417l,7716010612l,7716010615l};
        String[] modelTitle = {"Buderus Logamax plus GB172-14,  EG-E",
                "Buderus Logamax plus GB172-20, EG-E",
                "Buderus Logamax plus GB172-24K, G25",
                "Buderus Logamax plus GB172-24, G25"};
        int[] codingPlugID = {1116,1117,1118,1119};
        int[] codingPlugBusID ={154};
        double[] codingPlugSoftwareVersion ={3.0,5.0,9.0,11.0};
        String[] errorID = {"6A-227","9L-238","4C-224","0Y-276"};
        int[] errorFailureTreeID = {39534764,39535998,39534450,39533957};
        String[] errorDescription = {
                "Kein Ionisationsstrom vorhanden",
                "Kein Rückmeldesignal beim Test der Gasarmatur",
                "Sicherheitstemperaturbegrenzer hat ausgelöst",
                "Temperatur am Vorlauffühler ist größer als 95°C"};
        int[] causeID = {18205831,6525337,7870808,3089362};
        String[] causeDescription = {
                "Ionisationselektrode blockiert das Ionisationssignal",
                "Gasarmatur defekt",
                "STB hat Unterbrechung",
                "Kondenswassersiphon durch Ablagerungen verstopft"};

        long[] replacementPartID = {87182243450l,87181070870l,87160134340l, 87072061960l};
        String[] replacementPartName = {
        "Elektrodensatz",
        "Gasarmatur",
        "Zündkabel",
        "Temperaturbegrenzer"};
    }
    public void generateEvents(int eventCount) {
        this.eventCount = eventCount;
        List<EapEvent> events = new ArrayList<EapEvent>();
        for(int j = 0; j < eventCount; j++) {
            Map<String, Serializable> values = new HashMap<String, Serializable>();
            values.put("dateOfServiceIntervention", getRandom(dateOfServiceIntervention));
            values.put("dateOfInstallation", getRandom(dateOfInstallation));
            values.put("factoryID", getRandom(factoryID));
            values.put("dateOfProduction", getRandom(dateOfProduction));
            values.put("counter", getRandom(counter));
            values.put("softwareVersion", getRandom(softwareVersion));
            values.put("feedbackOfInstaller", getRandom(feedbackOfInstaller));
            values.put("objectID", getRandom(objectID));
            values.put("locationOfDeviceID", getRandom(locationOfDeviceID));
            values.put("modelID", getRandom(modelID));
            values.put("modelTitle", getRandom(modelTitle));
            values.put("codingPlugID", getRandom(codingPlugID));
            values.put("codingPlugBusID", getRandom(codingPlugBusID));
            values.put("codingPlugSoftwareVersion", getRandom(codingPlugSoftwareVersion));
            values.put("errorID", getRandom(errorID));
            values.put("errorFailureTreeID", getRandom(errorFailureTreeID));
            values.put("errorDescription", getRandom(errorDescription));
            values.put("causeID", getRandom(causeID));
            values.put("causeDescription", getRandom(causeDescription));
            values.put("replacementPartID", getRandom(replacementPartID));
            values.put("replacementPartName", getRandom(replacementPartName));

            EapEvent event = new EapEvent(EapEventType.findByTypeName("FeedbackData_v3"), new Date(), values);
            events.add(event);
        }
        EventReplayer eventReplayer = new EventReplayer(events);
        eventReplayer.replay();
    }

    private static Date getRandom(Date[] examples) {
        try {
            int element = random.nextInt(examples.length);
        } catch (Exception e) {

        }
        return examples[element];
    }

    private static String getRandom(String[] examples) {
        int element = random.nextInt(examples.length);
        return examples[element];
    }

    private static int getRandom(int[] examples) {
        int element = random.nextInt(examples.length );
        return examples[element];
    }

    private static long getRandom(long[] examples) {
        int element = random.nextInt(examples.length );
        return examples[element];
    }

    private static double getRandom(double[] examples) {
        int element = random.nextInt(examples.length);
        return examples[element];
    }
}