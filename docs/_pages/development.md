---
title: development
permalink: '/development'
---
# Development

## Installation

The source code of UNICORN can be found at <https://github.com/bptlab/Unicorn>. On github you also find installation and deployment instructions for the platform. 
<!-- The user guide for the platform (in German) is available <a href="%ATTACHURLPATH%/Nutzerhandbuch.pdf">here</a>. -->


## Example Application

You can watch a <a href="http://www.youtube.com/watch?v=doAFKwIEp6w">screencast</a> of UNICORN in action. The examples shown in the screencast and described below can be found in the examples folder of the source code distribution.

Before the example can be run, you need to <a href="http://www.dwd.de/bvbw/appmanager/bvbw/dwdwwwDesktop?_nfpb=true&_pageLabel=dwdwww_spezielle_nutzer&_state=maximized&_windowLabel=T174800248261285831499722&T174800248261285831499722gsbDocumentPath=Navigation%252FOeffentlichkeit%252FHomepage%252FWetter__Ihre__Website%252Fftp-Zugriff__node.html%253F__nnn%253Dtrue">register at the DWD</a> (Deutscher Wetterdienst = German Weather Agency). The account data must be included in the platform as described in the readme in the <a href="https://github.com/BPT-NH/ComplexEventProcessingPlatform">Github</a>.

To examine the features of the platform by using the test data the following steps must be executed:
1. Activate the weather adapter via "Import => Weather/Traffic"
1. Create the following event types via "Event Repository => Event type => Create event type"
    * !ContainerArrival
        * !ContainerID:Integer
        * Pier:String
        * Refrigeration_Status:String
    * !ReadyForDischarge
        * !ContainerID:Integer
        * Pier:String
        * Refrigeration_Status:String
        * !ShipID:String
    * !ContainerDischarged
        * !ContainerID:Integer
    * !StoreChilled
        * !ContainerID:Integer
        * Warehouse:String
    * !StoreUnChilled
        * !ContainerID:Integer
        * Warehouse:String
    * !TruckReady
        * !ContainerID:Integer
        * !TruckID:String
    * !ContainerDelivered
        * !ContainerID:Integer
        * !TruckID:String
        * Price:Integer
1. Create a process "Transportprocess" via "Event Processing => Correlation"
1. Create a simple correlation rule for "Transportprocess" using the above mentioned event types defining the attribute "ContainerID" as correlation attribute
1. Upload external knowledge from the file "ListOfContainersAndShips" via "Import -> Excel/XML/XSD" by creating a new event type
1. Create a transformation rule via "Event Processing => Transformation => Advanced Rule Editor"
    * Build pattern
        1. Add event type "ContainerArrival" and "legend_warnings_CAP"
        1. Add aliases for both event types (A and B)
        1. Add three filter expressions for "legend_Warnings_CAP"  
            1. B.msgType = 'Alert'
            1. B.info.eventCode.value = '52' for wind
            1. B.info.area.areaDesc = 'Hansestadt Hamburg'
        1. Add pattern "EVERY" for "ContainerArrival"
        1. Add pattern "NOT" for "legend_warnings_CAP"
        1. Add pattern "AND" for "NOT" and "EVERY"
    * Select attribute values
        1. Choose event type "ReadyForDischarge" as resulting event type
        1. Choose as "Transformation time" as timestamp
        1. Use attributes of event A as source for attributes "Pier", "Refrigeration_Status" and "ContainerID"
        1. Use external knowledge for attribute "shipID"
            1. Use event type "ListOfContainersAndShips" with desired attribute "ShipID" and save the rule
            1. Check "ContainerID" to match with "A.ContainerID" and save the rule again
            1. Add default value "NO_SHIP_ID_FOUND" and click "save"
        1. Name and save the transformation rule
1. Create a user via "Sign In => Register" and log in
1. Create the following query via "Queries => Live"
    * Name: !OrdersWithRunLongerThan2Days
    * Query: SELECT !A.ContainerID, (B.Timestamp.getTime() - A.Timestamp.getTime() / (1000 * 60) ) AS run FROM PATTERN [every A=ReadyForDischarge -> every B=ContainerDelivered(!B.ContainerID = !A.ContainerID AND B.Timestamp.getTime() - A.Timestamp.getTime() > (1000*60*60*24*2))]
1. Create a query notification for the above query via "Monitoring => Notification"
1. Create an event notification for event type "TruckReady" via "Monitoring => Notification"
1. Upload file "Transportmodel.bpmn" via "Import => BPMN"
1. Create monitoring points by binding the above created event types to the similar named process elements via "Queries => BPMN"
1. Import the Excel files via "Import => Excel/XML/XSD" in the following order: "ReadyForDischarge" => "ContainerDischarged" => "StoreChilled" => "StoreUnChilled" => "TruckReady" => "ContainerDelivered"
1. See the events in the "Event Repository", the process executions in "Monitoring => BPMN", and the notifications at "Monitoring => Notification"
1. Create event views via "Monitoring => Event View"
1. Create attribute charts via "Monitoring => Attribute Charts"

## BPMN Extension
BPMN is a widely accepted standard for process modeling. Although it does not provide native language elements to model node life cycles and PEMPs (see <a href="http://bpt.hpi.uni-potsdam.de/pub/Public/NicoHerzberg/Enriching_Raw_Events_to_Enable_Process_Intelligence.pdf">[1]</a>), it provides its own extension mechanism. We use this extension mechanism to transform models to platform-specific models and code.

In particular, the BPMN extension mechanism allows the definition of a group of attributes and elements which are attached to standard BPMN elements. Thus, it allows us to extend the BPMN to model by node life cycles and PEMPs without contradicting the semantics of any BPMN element and still be compliant with the BPMN standard. Our extension can be attached to a node in a BPMN model. This extension is required to transform a process model to CEP-specific code for event detection and, thus, enable the monitoring of events in distributed systems that are associated with a single business process. 

For the representation of BPMN models, BPMN defines a set of XML Schema documents specifying the interchange format for BPMN models. Thus, to model and exchange BPMN models that include node life cycles and PEMPs, we derive an own XML Schema for our extension. In particular, we developed a <a href="http://code.google.com/p/bpmnx/wiki/getting_started">BPMN+X model</a> to specify our extension. It allows the attachment of a transition element to a !FlowNode. This transition element references a PEMP and defines its type by enumeration. In this way, any state transition of an activity, gateway, or event including its event type can be represented in a BPMN model.

<!--All sources (schema and BPMN+X definition) and an example can be downloaded <a href="%ATTACHURLPATH%/BPMNExtensionForPEMPs.zip">here</a>.-->


## Semantic Extension
Semantic complex event processing (SCEP) querying on event streams allows for the combination of CEP and semantic web technologies. Thus, incoming event streams can be monitored by a SCEP engine and evaluated against defined user queries with the help of ontological background knowledge and CEP capabilities.

The prototype is based on <a href="http://www.python.org/">Python</a> for server-side implementation and <a href="http://de.wikipedia.org/wiki/JavaScript">JavaScript</a> on the client side. The client-server architecture provides a web interface that can be accessed through a common web browser. The SCEP engine includes an RDF Server, namely a <a href="http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSTriple">Virtuoso RDF Triple Store</a> server which holds the ontologies necessary for the use cases of the logistics domain. This SCEP engine can be used as extension of the EPP introduced above.

In logistics, the task to find transportation-related events to a route would require the query designer to know all the relevant transportation plans, the geographical points on that plans, and their order on the route. To avoid the manual evaluation of events, we use ontological knowledge and provide a logistic function that is able to use the location of events and calculate whether they are relevant for given transportation routes or not. 

The screenshot below shows how the prototype works:
On the left the query considering the given route (Route_KarlOslo_0) is given. This queries evaluates if an incoming event is fitting this query. If it fits then the query is colored in green (other reactions are also possible).
In the middle, queries can be typed and registered that are then shown on the left and considered when processing incoming events. Below, the incoming event that was evaluated against the query is shown (Demonstration).
The right part of the figure shows an expert of the route and the event that was processed on the map.

<!--<img src="%ATTACHURLPATH%/query_hit.png" alt="Prototype on an semantic CEP for logistics" title="Prototype on an semantic CEP for logistics" style="weight:600px;"/>-->

<!--All sources and instructions can be downloaded <a href="%ATTACHURLPATH%/SCEPExtensionForEPP.zip">here</a>.-->