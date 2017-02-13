/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.querying.bpmn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.application.pages.querying.bpmn.model.BPMNTreeTableElement;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;
import de.hpi.unicorn.event.EapEventType;

/**
 * A form with a dropdown to assign monitoring points to BPMN elements on the
 * base of {@link EapEventType}s.
 * 
 * @author micha
 */
public class MonitoringPointField implements Serializable {

	private static final long serialVersionUID = 1L;
	private AjaxButton deleteButton;
	private Component addButton;
	private final List<String> eventTypeNamesList;
	private final BPMNTreeTableElement treeTableElement;
	private final AbstractBPMNElement bpmnElement;

	/**
	 * Constructor for a form with a dropdown to assign monitoring points to
	 * BPMN elements on the base of {@link EapEventType}s.
	 * 
	 * @param treeTableElement
	 */
	public MonitoringPointField(final BPMNTreeTableElement treeTableElement) {
		this.treeTableElement = treeTableElement;
		this.bpmnElement = treeTableElement.getContent();
		this.eventTypeNamesList = new ArrayList<String>();
		for (final EapEventType eventType : EapEventType.findAll()) {
			this.eventTypeNamesList.add(eventType.getTypeName());
		}
	}

	public void addMonitoringField(final Form<Void> monitoringForm,
			final MonitoringPointStateTransition monitoringPointType) {
		final MonitoringPoint monitoringPoint = this.treeTableElement.getMonitoringPoint(monitoringPointType);
		EapEventType monitoringPointEventType = null;
		if (monitoringPoint != null) {
			monitoringPointEventType = monitoringPoint.getEventType();
			this.updateEventTypeNames(monitoringPointType, monitoringPointEventType);
		}
		// Div-Container für Label und Select
		final WebMarkupContainer selectContainer = new WebMarkupContainer(monitoringPointType.getName()
				+ "SelectContainer");
		selectContainer.setOutputMarkupPlaceholderTag(true);
		monitoringForm.add(selectContainer);

		// EnableButton
		this.addButton = new AjaxButton(monitoringPointType.getName() + "Button", monitoringForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				// MonitoringPoint hinzufügen
				selectContainer.setVisible(true);
				MonitoringPointField.this.deleteButton.setVisible(true);
				MonitoringPointField.this.addButton.setVisible(false);
				target.add(selectContainer);
				target.add(MonitoringPointField.this.deleteButton);
				target.add(MonitoringPointField.this.addButton);
			}
		};
		this.addButton.setOutputMarkupPlaceholderTag(true);
		monitoringForm.add(this.addButton);

		// Select
		final DropDownChoice<String> eventTypeSelect;
		if (monitoringPointEventType != null) {
			eventTypeSelect = new DropDownChoice<String>(monitoringPointType.getName() + "Select",
					new PropertyModel<String>(this, monitoringPointType.getName() + "EventTypeName"),
					this.eventTypeNamesList);
		} else {
			eventTypeSelect = new DropDownChoice<String>(monitoringPointType.getName() + "Select", new Model<String>(),
					this.eventTypeNamesList);
		}

		eventTypeSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				final String eventTypeName = eventTypeSelect.getChoices().get(
						Integer.parseInt(eventTypeSelect.getValue()));
				if (eventTypeName != null && !eventTypeName.isEmpty()) {
					final EapEventType selectedEventType = EapEventType.findByTypeName(eventTypeName);
					if (selectedEventType != null) {
						// Da wegen dem ComponentBuilder nicht auf dem
						// originalen BPMN-Process aus der Datenbank gearbeitet
						// wird,
						// muss der noch geholt werden
						final BPMNProcess originalProcess = BPMNProcess
								.findByContainedElement(MonitoringPointField.this.bpmnElement);
						final AbstractBPMNElement originalBPMNElement = originalProcess
								.getBPMNElementById(MonitoringPointField.this.bpmnElement.getId());
						if (originalBPMNElement != null) {
							MonitoringPoint originalMonitoringPoint = originalBPMNElement
									.getMonitoringPointByStateTransitionType(monitoringPointType);
							if (originalMonitoringPoint == null) {
								originalMonitoringPoint = new MonitoringPoint(selectedEventType, monitoringPointType,
										null);
								originalMonitoringPoint.save();
							}
							originalBPMNElement.addMonitoringPoint(originalMonitoringPoint);
							originalBPMNElement.merge();
							MonitoringPointField.this.updateEventTypeNames(monitoringPointType, selectedEventType);
						}

					}
				}
			}
		});
		eventTypeSelect.setOutputMarkupPlaceholderTag(true);
		selectContainer.add(eventTypeSelect);

		// Delete-Button
		this.deleteButton = new AjaxButton(monitoringPointType.getName() + "DeleteButton", monitoringForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				// MonitoringPoint entfernen
				// Da wegen dem ComponentBuilder nicht auf dem originalen
				// BPMN-Process aus der Datenbank gearbeitet wird,
				// muss der noch geholt werden
				final BPMNProcess originalProcess = BPMNProcess
						.findByContainedElement(MonitoringPointField.this.bpmnElement);
				final AbstractBPMNElement originalBPMNElement = originalProcess
						.getBPMNElementById(MonitoringPointField.this.bpmnElement.getId());
				if (originalBPMNElement != null) {
					final MonitoringPoint originalMonitoringPoint = originalBPMNElement
							.getMonitoringPointByStateTransitionType(monitoringPointType);
					originalBPMNElement.removeMonitoringPoint(originalMonitoringPoint);
					originalBPMNElement.merge();
					MonitoringPointField.this.updateEventTypeNames(monitoringPointType, null);
				}
				selectContainer.setVisible(false);
				MonitoringPointField.this.addButton.setVisible(true);
				MonitoringPointField.this.deleteButton.setVisible(false);
				target.add(selectContainer);
				target.add(MonitoringPointField.this.addButton);
				target.add(MonitoringPointField.this.deleteButton);
			}
		};
		this.deleteButton.setOutputMarkupPlaceholderTag(true);
		monitoringForm.add(this.deleteButton);

		if (monitoringPoint != null) {
			this.addButton.setVisible(false);
		} else {
			selectContainer.setVisible(false);
			this.deleteButton.setVisible(false);
		}
	}

	private void updateEventTypeNames(final MonitoringPointStateTransition monitoringPointType,
			final EapEventType monitoringPointEventType) {
		switch (monitoringPointType) {
		case initialize:
			break;
		case begin:
			break;
		case enable:
			break;
		case skip:
			break;
		case terminate:
			break;
		default:
			break;
		}
	}

}
