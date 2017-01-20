package de.hpi.unicorn.application.pages.input.replayer;

import com.espertech.esper.client.EventType;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.log4j.Logger;

import javax.swing.event.DocumentEvent;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

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
    private String[] productFamilyID;

    private int eventCount;
    private int replayScaleFactor = 10000;
    private static Random random = new Random();
    private static final Logger logger = Logger.getLogger(EventGenerator.class);

    public EventGenerator() {
        dateOfServiceIntervention = new Date[]{
                new Date(2014 - 1900,11-1,7,12,0,0),
                new Date(2014 - 1900,11-1,8,12,0,0),
                new Date(2014 - 1900,11-1,9,12,0,0),
                new Date(2014 - 1900,11-1,10,12,0,0)};
        dateOfInstallation = new Date[]{
                new Date(2013 - 1900,11-1,7,12,0,0),
                new Date(2013 - 1900,11-1,8,12,0,0),
                new Date(2013 - 1900,11-1,9,12,0,0),
                new Date(2013 - 1900,11-1,10,12,0,0)};
        factoryID = new int[]{8290};
        dateOfProduction = new int[]{303,305,455,209};
        counter = new int[]{1398,39068,22748,9569};
        softwareVersion = new double[]{4.09,4.05};
        feedbackOfInstaller = new String[]{"Elektrode getauscht",
                "Gasarmatur getauscht",
                "Luftansaugrohr zur Gasarmatur montiert",
                "Kabelbaum getauscht"};
        objectID = new int[]{32835142,20228133,18701976,32842891};
        locationOfDeviceID = new int[]{32835144,32832677,5650483,20343255};
        modelID = new long[]{7716010416l,7716010417l,7716010612l,7716010615l};
        modelTitle = new String[]{"Buderus Logamax plus GB172-14,  EG-E",
                "Buderus Logamax plus GB172-20, EG-E",
                "Buderus Logamax plus GB172-24K, G25",
                "Buderus Logamax plus GB172-24, G25"};
        codingPlugID = new int[]{1116,1117,1118,1119};
        codingPlugBusID = new int[]{154};
        codingPlugSoftwareVersion = new double[]{3.0,5.0,9.0,11.0};
        errorID = new String[]{"6A-227","9L-238","4C-224","0Y-276"};
        errorFailureTreeID = new int[]{39534764,39535998,39534450,39533957};
        errorDescription = new String[]{
                "Kein Ionisationsstrom vorhanden",
                "Kein Rückmeldesignal beim Test der Gasarmatur",
                "Sicherheitstemperaturbegrenzer hat ausgelöst",
                "Temperatur am Vorlauffühler ist größer als 95°C"};
        causeID = new int[]{18205831,6525337,7870808,3089362};
        causeDescription = new String[]{
                "Ionisationselektrode blockiert das Ionisationssignal",
                "Gasarmatur defekt",
                "STB hat Unterbrechung",
                "Kondenswassersiphon durch Ablagerungen verstopft"};

        replacementPartID = new long[]{87182243450l,87181070870l,87160134340l, 87072061960l};
        replacementPartName = new String[]{
                "Elektrodensatz",
                "Gasarmatur",
                "Zündkabel",
                "Temperaturbegrenzer"};
        productFamilyID = new String[] {"GB172"};
    }
    public void generateEvents(int eventCount) {
        this.eventCount = eventCount;
        List<EapEvent> events = new ArrayList<EapEvent>();
        for(int j = 0; j < eventCount; j++) {
            Map<String, Serializable> values = new HashMap<String, Serializable>();
            //values.put("dateOfServiceIntervention", getRandom(dateOfServiceIntervention));
            values.put("dateOfInstallation", getRandom(dateOfInstallation));
            values.put("factoryId", getRandom(this.factoryID));
            values.put("dateOfProduction", getRandom(dateOfProduction));
            values.put("counter", getRandom(counter));
            values.put("softwareVersion", getRandom(softwareVersion));
            values.put("feedbackOfInstaller", getRandom(feedbackOfInstaller));
            values.put("objectId", getRandom(objectID));
            values.put("locationOfDeviceId", getRandom(locationOfDeviceID));
            values.put("orderNumber", getRandom(modelID));
            values.put("productName", getRandom(modelTitle));
            values.put("codingPlugId", getRandom(codingPlugID));
            values.put("codingPlugBusId", getRandom(codingPlugBusID));
            values.put("codingPlugSoftwareVersion", getRandom(codingPlugSoftwareVersion));
            values.put("errorId", getRandom(errorID));
            values.put("errorFailureTreeId", getRandom(errorFailureTreeID));
            values.put("errorDescription", getRandom(errorDescription));
            values.put("causeId", getRandom(causeID));
            values.put("causeDescription", getRandom(causeDescription));
            values.put("replacementPartId", getRandom(replacementPartID));
            values.put("replacementPartName", getRandom(replacementPartName));
            values.put("productFamilyId", getRandom(productFamilyID));

            EapEvent event = new EapEvent(EapEventType.findByTypeName("FeedbackData_v4"), getRandom(dateOfServiceIntervention), values);
            events.add(event);
        }
        EventReplayer eventReplayer = new EventReplayer(events, this.replayScaleFactor);
        eventReplayer.replay();
    }

    public void generateEvents(int eventCount, EapEventType eventType, Map<TypeTreeNode, String> attributeSchemas) {
        this.eventCount = eventCount;
        List<EapEvent> events = new ArrayList<EapEvent>();

        for (int j = 0; j < eventCount; j++) {
            Map<String, Serializable> values = new HashMap<String, Serializable>();
            for (Map.Entry<TypeTreeNode, String> attributeSchema : attributeSchemas.entrySet()) {
                switch (attributeSchema.getKey().getType()) {
                    case STRING:
                        values.put(attributeSchema.getKey().getName(), getRandomStringFromInput(attributeSchema.getValue()));
                        break;
                    case INTEGER:
                        values.put(attributeSchema.getKey().getName(), getRandomIntFromInput(attributeSchema.getValue()));
                        break;
                    case FLOAT:
                        values.put(attributeSchema.getKey().getName(), getRandomFloatFromInput(attributeSchema.getValue()));
                        break;
                    case DATE:
                        /* TODO: Implement date handling */
                        values.put(attributeSchema.getKey().getName(), getRandom(dateOfServiceIntervention));
                        break;
                    default:
                        values.put(attributeSchema.getKey().getName(), "UNDEFINED");
                        break;
                }
            }
            EapEvent event = new EapEvent(eventType, getRandom(dateOfServiceIntervention), values);
            events.add(event);
        }
        EventReplayer eventReplayer = new EventReplayer(events, this.replayScaleFactor);
        eventReplayer.replay();
    }

    private static String getRandomStringFromInput(String input) {
        String[] possibleValues = input.split(";");
        return possibleValues[getRandomIndex(possibleValues)];
    }

    private static int getRandomIntFromInput(String input) {
        if(input.contains("-")) {
            int start = Integer.parseInt(input.split("-")[0]);
            int end = Integer.parseInt(input.split("-")[1]);
            return random.nextInt(end - start + 1) + start;
        }
        else {
            String[] possibleValues = input.split(";");
            return Integer.parseInt(possibleValues[getRandomIndex(possibleValues)]);
        }
    }

    private static Float getRandomFloatFromInput(String input) {
        String[] possibleValues = input.split(";");
        return Float.parseFloat(possibleValues[getRandomIndex(possibleValues)]);
    }

    private static int getRandomIndex(Object[] inputArray) {
        return random.nextInt(inputArray.length);
    }

    private static Date getRandom(Date[] examples) {
        int element = random.nextInt(examples.length);
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