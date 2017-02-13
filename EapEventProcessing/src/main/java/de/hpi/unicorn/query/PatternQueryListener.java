/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.xml.XMLEventBean;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.esper.EapUtils;
import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.monitoring.bpmn.BPMNQueryMonitor;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.utils.SetUtil;

/**
 * A listener for {@link PatternQuery}.
 * 
 * @author micha
 */
public class PatternQueryListener extends LiveQueryListener implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<EapEventType> loopBreakEventTypes;
	private boolean isLoopQueryListener = false;
	private final List<Integer> alreadyTriggeredProcessInstances = new ArrayList<Integer>();
	private AbstractBPMNElement catchingElement = null;
	private String timerTriggerEventTypeName = new String();
	private float timeDuration = 0;
	private EapEventType timerEventType;

	public PatternQueryListener(final QueryWrapper patternQuery) {
		super(patternQuery);
	}

	public PatternQueryListener(final QueryWrapper patternQuery, final List<EapEventType> loopBreakEventTypes) {
		super(patternQuery);
		this.loopBreakEventTypes = loopBreakEventTypes;
		this.isLoopQueryListener = true;
	}

	@Override
	public void update(final EventBean[] newData, final EventBean[] oldData) {
		if (newData[0].getUnderlying() instanceof HashMap) {
			// ProcessInstance für PatternEvent ermitteln
			final List<Set<Integer>> processInstancesList = new ArrayList<Set<Integer>>();
			final HashMap patternEvent = (HashMap) newData[0].getUnderlying();
			for (final Object value : patternEvent.values()) {
				if (value instanceof XMLEventBean) {
					final XMLEventBean bean = (XMLEventBean) value;
					if (bean.get("ProcessInstances") != null) {
						processInstancesList.add(new HashSet<Integer>((List<Integer>) bean.get("ProcessInstances")));
					}
				}
			}
			System.err.println(this.query.getTitle() + ": " + processInstancesList);
			final Set<Integer> processInstances = SetUtil.intersection(processInstancesList);
			if (!processInstances.isEmpty()) {
				/*
				 * TODO: processInstances.iterator().next() ist nicht
				 * ausreichend, hier würde nur die erste ProcessInstance
				 * berücksichtigt werden, auch wenn das Event zu mehreren
				 * ProcessInstanzen gehört
				 */
				BPMNQueryMonitor.getInstance().setQueryFinishedForProcessInstance((PatternQuery) this.query,
						CorrelationProcessInstance.findByID(processInstances.iterator().next()));
				// Neues Event erzeugen und an Esper schicken
				final EapEvent event = new EapEvent(EapEventType.findByTypeName(this.query.getTitle()), new Date());
				for (final int processInstanceID : processInstances) {
					event.addProcessInstance(CorrelationProcessInstance.findByID(processInstanceID));
				}
				if (event.getEventType() != null) {
					StreamProcessingAdapter.getInstance().addEvent(event);
				}
				// Gefangene Events nochmal abschicken, die in anderer Query
				// wieder gebraucht werden
				if (!EapUtils.isIntersectionNotEmpty(this.alreadyTriggeredProcessInstances, new ArrayList<Integer>(
						processInstances))) { /*
											 * Wurde Event schon zum zweiten Mal
											 * abgeschickt ?
											 */
					final EapEvent lastEvent = this.getLastEvent(newData);
					if (lastEvent != null) {
						// Schleife wurde getriggert
						if (this.isLoopQueryListener) {
							this.resendLastEvent(lastEvent);
						}
						// Catching-Event wurde getriggert
						if (this.catchingElement != null
								&& lastEvent.getEventType().getTypeName().equals(this.catchingElement.getName())) {
							this.resendLastEvent(lastEvent);
						}
						// Timer-Event wurde getriggert
						if (!this.timerTriggerEventTypeName.isEmpty()
								&& lastEvent.getEventType().getTypeName().equals(this.timerTriggerEventTypeName)) {
							final TimerListener timerListener = new TimerListener(lastEvent, this.timerEventType,
									this.timeDuration);
							timerListener.start();
						}
					}
				}
			}
		}
		// System.out.println("Event received for query " + query.getTitle() +
		// ": " + newData[0].getUnderlying());
	}

	private EapEvent getLastEvent(final EventBean[] newData) {
		EapEvent event = null;
		if (newData[0].getUnderlying() instanceof HashMap) {
			final HashMap patternEvent = (HashMap) newData[0].getUnderlying();
			for (final Object value : patternEvent.values()) {
				if (value instanceof XMLEventBean) {
					final XMLEventBean bean = (XMLEventBean) value;
					final EapEventType eventType = EapEventType.findByTypeName(bean.getEventType().getName());

					final Map<String, Serializable> values = new HashMap<String, Serializable>();
					for (final TypeTreeNode valueType : eventType.getValueTypes()) {
						values.put(valueType.getName(), (Serializable) bean.get(valueType.getName()));
					}

					event = new EapEvent(eventType, new Date(), values);
					final List<Integer> processInstanceIDs = (List<Integer>) bean.get("ProcessInstances");
					this.alreadyTriggeredProcessInstances.addAll(processInstanceIDs);
					for (final Integer processInstanceID : processInstanceIDs) {
						event.addProcessInstance(CorrelationProcessInstance.findByID(processInstanceID));
					}
				}
			}
		}
		return event;
	}

	private void resendLastEvent(final EapEvent event) {
		// TODO: Nicht speichern, Event nur zu Esper senden
		StreamProcessingAdapter.getInstance().addEvent(event);
	}

	public List<EapEventType> getLoopBreakEventTypes() {
		return this.loopBreakEventTypes;
	}

	public void setLoopBreakEventTypes(final List<EapEventType> loopBreakEventTypes) {
		this.loopBreakEventTypes = loopBreakEventTypes;
		this.isLoopQueryListener = true;
	}

	public boolean isLoopQueryListener() {
		return this.isLoopQueryListener;
	}

	public void setLoopQueryListener(final boolean isLoopQueryListener) {
		this.isLoopQueryListener = isLoopQueryListener;
	}

	public AbstractBPMNElement getCatchingElement() {
		return this.catchingElement;
	}

	public void setCatchingElement(final AbstractBPMNElement catchingElement) {
		this.catchingElement = catchingElement;
	}

	/**
	 * Adds a timerTrigger, that should be the name of the eventtype, that
	 * triggers the timer, to this query. If the listener gets such a
	 * timerTrigger, it starts a timer thread, that fires the timerEventType
	 * after the specified timeDuration to Esper.
	 * 
	 * @param timerTrigger
	 * @param timerEventType
	 * @param timeDuration
	 */
	public void setTimer(final String timerTriggerEventTypeName, final EapEventType timerEventType,
			final float timeDuration) {
		this.timerTriggerEventTypeName = timerTriggerEventTypeName;
		this.timerEventType = timerEventType;
		this.timeDuration = timeDuration;
	}

}