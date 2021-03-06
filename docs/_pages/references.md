---
title: references
permalink: '/references'
---

<script type="text/javascript" src="https://code.jquery.com/jquery-2.1.4.min.js"></script>
<script type="text/javascript" src="https://cdn.rawgit.com/pcooksey/bibtex-js/ef59e62c/src/bibtex_js.js"></script>
<script type="text/javascript" src="{{ site.baseurl }}/assets/js/bibtex-unicorn.js"></script>

<h1>References</h1>

<div class="references">

<h2>Conferences</h2>

<textarea class="reference">
@inproceedings{baumgrass:2014a,
  abstract={Information sources providing real-time status of physical objects have drastically increased in recent times. So far, research in business process monitoring has mainly focused on checking the completion of tasks. However, the availability of real-time information allows for a more detailed tracking of individual business tasks. This paper describes a framework for controlling the safe execution of tasks and signalling possible misbehaviours at runtime. It outlines a real use case on smart logistics and the preliminary results of its application.},
  author={Cabanillas, C. and Di Ciccio, C. and Mendling, J. and Baumgrass, A.},
  booktitle={Business Process Management (BPM)},
  publisher={Springer},
  series={Lecture Notes in Computer Science},
  title={Predictive Task Monitoring for Business Processes},
  type={conference},
  year = {2014}
}
</textarea>

<textarea class="reference">
@conference{herzberg2013event,
  abstract = {The execution of business processes generates a lot of data comprising final process results as well as information about intermediate activities, both communicated as events. These events may not be correlated to the process they origin from, because the correlation information is usually not present in the event but in so-called context data, which exists orthogonally to the corresponding process. However, in the areas of process monitoring and analysis, events need to be correlated to specific process instances. To close the gap between recorded events without process correlation and required events with process correlation, we propose a framework that enriches recorded events with context data to create events correlated to processes, so-called process events.},
  author = {Herzberg, Nico and Meyer, Andreas and Weske, Mathias},
  booktitle = {Enterprise Distributed Object Computing (EDOC)},
  pages = {107--116},
  publisher = {IEEE},
  title = {An Event Processing Platform for Business Process Management},
  type = {conference},
  url = {http://bpt.hpi.uni-potsdam.de/pub/Public/AndreasMeyer/An_Event_Processing_Platform_for_Business_Process_Management.pdf},
  year = {2013}
}
</textarea>

<h2>Workshop</h2>

<textarea class="reference">
@conference{wong2015batch,
  abstract = {Recently, batch activities have been introduced to improve the execution of
  business processes by collectively performing batch activities that belong to
  different process instances. Using traditional techniques to monitor processes
  with batch activities leads to inadequate representation of process instances,
  since monitoring is unaware of batch activities. This paper introduces an
  approach to monitor batch activities, which also takes into account exceptions
  in batch clusters at different levels of abstraction. The concepts and
  techniques introduced are evaluated by a prototypical implementation using
  real-world event data from the logistics domain.},
  author = {Tsun Yin Wong, Susanne Bülow and Mathias Weske},
  booktitle = {CAiSE Workshops},
  publisher = {Springer},
  title = {Monitoring Batch Regions in Business Processes},
  type = {workshop},
  year = {2015}
}
</textarea>

<textarea class="reference">
@conference{buelow2013monitoring,
  abstract = {Business process monitoring enables a fast and specific overview of the process executions in an enterprise. Traditionally, this kind of monitoring requires a coherent event log. Yet, in reality, execution information is often heterogeneous and distributed. In this paper, we present an approach that enables monitoring of business processes with execution data, independently of the structure and source of the event information. We achieve this by implementing an open source event processing platform combining existing techniques from complex event processing and business process management. Event processing includes transformation for abstraction as well as correlation to process instances and BPMN elements. Monitoring rules are automatically created from BPMN models and executed by the platform.},
  author = {Bülow, Susanne and Backmann, Michael and Herzberg, Nico and Hille, Thomas and  Meyer, Andreas and Ulm, Benjamin and Wong, Tsun Yin and Weske, Mathias},
  booktitle = {Business Process Management Workshops},
  pages = {277--290},
  publisher = {Springer},
  title = {Monitoring of Business Processes with Complex Event Processing},
  type = {workshop},
  url = {http://bpt.hpi.uni-potsdam.de/pub/Public/AndreasMeyer/Monitoring_of_Business_Processes_with_Complex_Event_Processing.pdf},
  year = {2013}
}
</textarea>

<textarea class="reference">
@inproceedings{baumgrass:2014b,
  abstract={The execution of business processes generates a lot of data representing happenings (also called events) that may be utilized for process monitoring and analysis. This, however, is not supported by typical BPMS.
  Especially, in manual executing business process environments, i.e. not driven by a BPMS, the correlation of events to processes for monitoring and analysis is not trivial. At design-time, Process Event Monitoring Points (PEMP) are used in process models to specify the locations, where particular events are expected. Therewith, occurring events can be assigned to a process during run-time. In this paper, we introduce an extension to BPMN, which implements this connection between process models and events. We show applicability of this extension by applying it to a logistics scenario taken from an EU project.},
  author={Baumgrass, A. and Herzberg, N. and Meyer, A. and Weske, M.},
  booktitle={Enterprise Modelling and Information Systems Architectures (EMISA)},
  publisher={Gesellschaft fuer Informatik (GI)},
  series={Lecture Notes in Informatics},
  title={BPMN Extension for Business Process Monitoring},
  type={workshop},
  year = {2014}
}
</textarea>

<textarea class="reference">
@inproceedings{metzke:2013,
  abstract={During the execution of business processes, companies generate vast amounts of events, which makes it hard to detect meaningful process information that could be used for process analysis and improvement. Complex event processing (CEP) can help in this matter by providing techniques for continuous analysis of events. The consideration of domain knowledge can increase the performance of reasoning tasks but it is different for each domain and depends on the requirements of these domains. In this paper, an existing approach of combining CEP and ontological knowledge is applied to the domain of logistics. We show the benefits of semantic complex event processing (SCEP) for logistics processes along the specific use case of tracking and tracing goods and processing related events. In particular, we provide a novel domain-specific function that allows to detect meaningful events for a transportation route. For the demonstration, a prototypical implementation of a system enabling SCEP queries is introduced and analyzed in an experiment.},
  author={Metzke, T. and Rogge-Solti, A. and Baumgrass, A. and Mendling, J. and Weske, M},
  booktitle={Service-Oriented Computing - ICSOC 2013 Workshops},
  publisher={Springer Berlin Heidelberg},
  series={Lecture Notes in Computer Science},
  title={Enabling Semantic Complex Event Processing in the Domain of Logistics},
  type={workshop},
  year = {2014}
}
</textarea>

<textarea class="reference">
@inproceedings{backmann:2013,
  abstract={While executing business processes, a variety of events is produced that is valuable for getting insights about the process execution.
  Specifically, these events can be processed by Complex Event Processing (CEP) engines to deliver a base for business process monitoring.
  Mobile, flexible, and distributed business processes challenge existing process monitoring techniques, especially if process execution is partially done manually.
  Thus, it is not trivial to decide where the required business process execution information can be found, how this information can be extracted, and to which point in the process it belongs to.
  Tackling these challenges, we present a model-driven approach to support the automated creation of (CEP) queries for process monitoring.
  For this purpose, we decompose a process model that includes monitoring information into its structural components.
  Those are transformed to (CEP) queries to monitor business process execution based on events.
  For illustration, we show an implementation for BPMN and describe possible applications.},
  author={Backmann, M. and Baumgrass, A. and Herzberg, N. and Meyer, A. and Weske, M},
  booktitle={Service-Oriented Computing - ICSOC 2013 Workshops},
  publisher={Springer Berlin Heidelberg},
  series={Lecture Notes in Computer Science},
  title={Model-driven Event Query Generation for Business Process Monitoring},
  type={workshop},
  year = {2014}
}
</textarea>

<textarea class="reference">
@inproceedings{herzberg:2013,
  abstract={During business process execution, various systems and services produce a
  variety of data, messages, and events that are valuable for gaining insights
  about business processes, e.g., to ensure a business process is executed as
  expected. However, these data, messages, and events usually originate from
  different kinds of sources, each specified by different kinds of description.
  This variety makes it difficult to automate the detection of relevant event
  sources for business process monitoring. In this paper, we present a course of
  actions to automatically associate different event sources to event object types
  required for business process monitoring. 
  In particular, in a three-step approach we determine the similarity of event
  sources to event object types, rank those results, and derive a mapping between
  their attributes. Thus, relevant event sources and their bindings to specified
  event object types of business processes can be identified. The approach is
  implemented and evaluated using schema matching techniques for an use case that
  is aligned with real-world energy processes, data, messages, and events.},
  author={Herzberg, N. and Khovalko, O. and Baumgrass, A. and Weske, M},
  booktitle={Service-Oriented Computing - ICSOC 2013 Workshops},
  publisher={Springer Berlin Heidelberg},
  series={Lecture Notes in Computer Science},
  title={Towards Automating the Detection of Event Sources},
  type={workshop},
  year = {2014}
}
</textarea>

<textarea class="reference">
@inproceedings{cabanillas:2013,
  abstract={Logistics processes have some characteristics which are fundamentally challenging from a business process management perspective. Their execution usually involves multiple parties and information exchanges and has to ensure a certain level of flexibility in order to respond to unexpected events. On the level of monitoring, potential disruptions have to be detected and reactive measures be taken in order to avoid delays and contract penalties. However, current business process management systems do not exactly address these general requirements which call for the integration of techniques from event processing. Unfortunately, activity-based and event-based execution paradigms are not thoroughly in line. In this paper, we untangle conceptual issues in aligning both. We present a set of three challenges in the monitoring of process-oriented complex logistics chains identified based on a real-world scenario consisting of a three-leg intermodal logistics chain for the transportation of goods. Required features that such a monitoring system should provide, as well as related literature referring to these challenges, are also described.},
  author={Cabanillas, C. and Baumgrass, A. and Mendling, J. and Rogetzer, P. and Bellovoda, B},
  booktitle={BPM 2013 Workshops (PALS)},
  publisher={Springer Berlin Heidelberg},
  series={Lecture Notes in Computer Science},
  title={Towards the Enhancement of Business Process Monitoring for Complex Logistics Chains},
  type={workshop},
  year = {2013}
}
</textarea>

<h2>Miscellaneous</h2>

<textarea class="reference">
@techreport{mgzn:2015,
  Author = {Tobias Duerschmid},
  Booktitle = {HPImgzn},
  Institution = {Zeitungsklub des HPI},
  Type = {report},
  Title = {Green European Transportation - Forschungsprojekte am HPI},
  Year = {2015},
  url = {https://hpi.de/fileadmin/user_upload/hpi/dokumente/hpi_mgzn/HPImgzn_Ausgabe16.pdf},
}
</textarea>

<textarea class="reference">
@techreport{dijkman:2014,
  Author = {Baumgrass, A. and Dijkman, R.M. and Grefen, P.W.P.J. and Pourmirza, S. and Voelzer, H. and Weske, M.},
  Booktitle = {BETA Working Paper 461. BETA Research School, Eindhoven, The Netherlands},
  Institution = {BETA Research School, Eindhoven, The Netherlands},
  Number = {BETA Working Paper 461},
  Type = {report},
  Title = {A Software Architecture for a Transportation Control Tower},
  Month = {September},
  Year = {2014}
}
</textarea>

</div>

<div class="bibtex_template" style="display: none;">
<span class="if author" style="font-weight: bold;"> 
  <span class="author"></span>.
</span>
<span style="font-style: italic;">
  <span class="title"></span>,
</span>
<span class="if booktitle">
  <span class="booktitle"></span>,
</span>
<span class="if publisher">
  <span class="publisher"></span>,
</span>
<span class="year"></span>.
<span class="if url">
    <a class="url" target="_blank">[paper]</a>
</span>
<span class="toggle" onclick="toggleMore(this,'.bibtex')">[bibtex]</span>
<span class="if abstract">
  <span class="toggle" onclick="toggleMore(this,'.abstract')">[abstract]</span>
  <span class="abstract" style="display: none;"></span>
  <span class="bibtex" style="display: none;"></span>
</span>
<span class="if !abstract">
  <span class="bibtex" style="display: none;"></span>
</span>
</div>
