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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hpi.unicorn.notification.NotificationRuleForQuery;
import de.hpi.unicorn.notification.RestNotificationRule;
import org.apache.xerces.dom.ElementImpl;
import org.w3c.dom.Node;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.event.xml.XMLEventBean;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.monitoring.QueryMonitoringPoint;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.utils.SetUtil;

public class LiveQueryListener implements UpdateListener {

	protected QueryWrapper query;

	public LiveQueryListener(final QueryWrapper liveQuery) {
		this.query = liveQuery;
	}

	@Override
	public void update(final EventBean[] newData, final EventBean[] oldData) {
		Map<Object, Serializable> map = new HashMap<Object, Serializable>();
		final Object eventObject = newData[0].getUnderlying();
		String eventValues = new String();
		if (eventObject instanceof ElementImpl) {
			eventValues = this.convertEventToString((ElementImpl) newData[0].getUnderlying());
			map = this.convertEventToMap((ElementImpl) newData[0].getUnderlying());
		} else if (eventObject instanceof HashMap) {
			map = (Map<Object, Serializable>) eventObject;
			for (final Object value : map.values()) {
				if (value instanceof XMLEventBean) {
					final XMLEventBean bean = (XMLEventBean) value;
					if (bean.getUnderlying() instanceof ElementImpl) {
						eventValues = this.convertEventToString((ElementImpl) bean.getUnderlying());
					}
				} else {
					eventValues = map.toString();
				}
			}
		}
		this.query.addEntryToLog("Event received: " + eventValues);
		// trigger notification
		for (final NotificationRuleForQuery notificationRule : this.query.getNotificationRulesForQuery()) {
			notificationRule.trigger(map);
		}
		for(final RestNotificationRule restNotificationRule : this.query.getRestNotificationRules()) {
			restNotificationRule.trigger(map);
		}

		// trigger monitoringPoints
		final List<CorrelationProcessInstance> instances = this.getInstances(newData);
		for (final QueryMonitoringPoint point : QueryMonitoringPoint.findByQuery(this.query)) {
			for (final CorrelationProcessInstance instance : instances) {
				point.trigger(instance);
			}
		}
	}

	private String convertEventToString(final ElementImpl event) {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("{");
		for (int k = 0; k < event.getChildNodes().getLength(); k++) {
			final Node node = event.getChildNodes().item(k);
			buffer.append(node.getNodeName() + "=" + node.getFirstChild().getNodeValue());
			if (k + 1 < event.getChildNodes().getLength()) {
				buffer.append(", ");
			}
		}
		buffer.append("}");
		return buffer.toString();
	}

	private Map<Object, Serializable> convertEventToMap(final ElementImpl event) {
		final EapEventType eventType = EapEventType.findByTypeName(event.getNodeName());
		if (eventType == null) {
			return new HashMap<Object, Serializable>();
		}
		final HashMap<Object, Serializable> map = new HashMap<Object, Serializable>();
		final AttributeTypeTree attributeTree = eventType.getValueTypeTree();
		for (int k = 0; k < event.getChildNodes().getLength(); k++) {
			final Node node = event.getChildNodes().item(k);

			final TypeTreeNode typeTreeNode = attributeTree.getAttributeByExpression(node.getNodeName());
			if (typeTreeNode != null) {
				final AttributeTypeEnum dataType = typeTreeNode.getType();
				final String tempValue = node.getFirstChild().getNodeValue();
				Serializable value = tempValue;
				if (dataType != null) {
					switch (dataType) {
					case FLOAT:
						value = Double.parseDouble(tempValue);
						break;
					case INTEGER:
						value = Long.parseLong(tempValue);
						break;
					default:
						value = tempValue;
					}
				}
				map.put(node.getNodeName(), value);
			}

		}
		return map;
	}

	private List<CorrelationProcessInstance> getInstances(final EventBean[] newData) {
		final List<CorrelationProcessInstance> instances = new ArrayList<CorrelationProcessInstance>();
		Set<Integer> processInstances = new HashSet<Integer>();
		final List<Set<Integer>> processInstancesList = new ArrayList<Set<Integer>>();
		if (newData[0].getUnderlying() instanceof HashMap) {
			// ProcessInstance fuer Events ermitteln
			final HashMap event = (HashMap) newData[0].getUnderlying();
			for (final Object value : event.values()) {
				if (value instanceof XMLEventBean) {
					final XMLEventBean bean = (XMLEventBean) value;
					if (bean.get("ProcessInstances") != null) {
						processInstancesList.add(new HashSet<Integer>((List<Integer>) bean.get("ProcessInstances")));
					}
				}
			}
			processInstances = SetUtil.intersection(processInstancesList);
		}
		if (newData[0] instanceof XMLEventBean) {
			final XMLEventBean bean = (XMLEventBean) newData[0];
			if (bean.get("ProcessInstances") != null) {
				processInstancesList.add(new HashSet<Integer>((List<Integer>) bean.get("ProcessInstances")));
			}
			processInstances = SetUtil.intersection(processInstancesList);
		}
		for (final int instanceID : processInstances) {
			instances.add(CorrelationProcessInstance.findByID(instanceID));
		}
		return instances;
	}
}