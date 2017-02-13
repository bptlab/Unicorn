/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.correlation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.Collapsible;
import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.process.modal.ProcessEditorModal;
import de.hpi.unicorn.correlation.AttributeCorrelator;
import de.hpi.unicorn.correlation.CorrelationRule;
import de.hpi.unicorn.correlation.RuleCorrelator;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;

@SuppressWarnings("serial")
public class CorrelationPage extends AbstractEapPage {

	private SimpleCorrelationPanel simpleCorrelationPanel;
	private SimpleCorrelationWithRulesPanel scenario3Panel;
	private boolean simpleCorrelationWithRules = false;
	// private AdvancedCorrelationPanel advancedCorrelationPanel;
	private final DropDownChoice<String> processSelect;
	private ProcessEditorModal processEditorModal;
	private final ArrayList<String> processNameList;
	private final ExistingCorrelationAlert existingCorrelationAlert;
	private final CorrelationPage correlationPage;
	protected ArrayList<TypeTreeNode> commonCorrelationAttributes = new ArrayList<TypeTreeNode>();
	private final Form<Void> layoutForm;

	public CorrelationPage() {
		super();
		this.correlationPage = this;

		this.processNameList = new ArrayList<String>();
		for (final CorrelationProcess process : CorrelationProcess.findAll()) {
			this.processNameList.add(process.getName());
		}

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);

		this.processSelect = new DropDownChoice<String>("processSelect", new Model<String>(), this.processNameList);
		this.processSelect.setOutputMarkupId(true);
		this.layoutForm.add(this.processSelect);

		final RadioChoice<String> simpleCorrelationWithRulesRadioChoice = new RadioChoice<String>(
				"simpleCorrelationWithRulesRadioChoice", new Model<String>(), Arrays.asList("same-name attributes",
						"correlation rules"));
		simpleCorrelationWithRulesRadioChoice.add(new AjaxFormChoiceComponentUpdatingBehavior() {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				CorrelationPage.this.simpleCorrelationWithRules = simpleCorrelationWithRulesRadioChoice
						.getModelObject().equals("correlation rules");
				CorrelationPage.this.updateSimpleCorrelationPanelComponents(target);
				CorrelationPage.this.updateSimpleCorrelationWithRulesPanelComponents(target);
			}
		});
		simpleCorrelationWithRulesRadioChoice.setModelObject("same-name attributes");
		this.layoutForm.add(simpleCorrelationWithRulesRadioChoice);

		this.addApplyButton(this.layoutForm);

		this.addCorrelationTabs();

		this.addProcessEditorModal();

		this.existingCorrelationAlert = new ExistingCorrelationAlert("warning",
				"Correlation exists! Do you want to override it?", this);
		this.existingCorrelationAlert.setVisible(false);
		this.existingCorrelationAlert.setOutputMarkupId(true);
		this.existingCorrelationAlert.setOutputMarkupPlaceholderTag(true);
		this.add(this.existingCorrelationAlert);
	}

	private void addProcessEditorModal() {
		this.processEditorModal = new ProcessEditorModal("processEditorModal", this.processSelect);
		this.add(this.processEditorModal);

		this.layoutForm.add(new AjaxLink<Void>("showProcessEditModal") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				CorrelationPage.this.processEditorModal.show(target);
			}
		});

	}

	private void addApplyButton(final Form<Void> layoutForm) {
		final BlockingAjaxButton applyButton = new BlockingAjaxButton("applyButton", layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				if ((CorrelationPage.this.simpleCorrelationWithRules && CorrelationPage.this
						.isSimpleCorrelationWithRulesPanelFilled())
						|| (!CorrelationPage.this.simpleCorrelationWithRules && CorrelationPage.this
								.isSimpleCorrelationPanelFilled())
						&& CorrelationPage.this.isAdvancedCorrelationPanelFilled()) {
					CorrelationPage.this.addEventTypesToProcessAndCorrelate(target);
				}
				target.add(CorrelationPage.this.correlationPage.getFeedbackPanel());
			}
		};

		layoutForm.add(applyButton);
	}

	private boolean isSimpleCorrelationPanelFilled() {
		if (this.processSelect.getValue().isEmpty()) {
			this.correlationPage.getFeedbackPanel().error("No process selected!");
			this.correlationPage.getFeedbackPanel().setVisible(true);
			return false;
		}
		if (this.simpleCorrelationPanel.getCorrelationEventTypes().isEmpty()) {
			this.correlationPage.getFeedbackPanel().error("No event types for correlation selected!");
			this.correlationPage.getFeedbackPanel().setVisible(true);
			return false;
		}
		if (this.simpleCorrelationPanel.getSelectedCorrelationAttributes().isEmpty()) {
			this.correlationPage.getFeedbackPanel().error("No correlation attributes selected!");
			this.correlationPage.getFeedbackPanel().setVisible(true);
			return false;
		}
		return true;
	}

	private boolean isSimpleCorrelationWithRulesPanelFilled() {
		if (this.processSelect.getValue().isEmpty()) {
			this.correlationPage.getFeedbackPanel().error("No process selected!");
			this.correlationPage.getFeedbackPanel().setVisible(true);
			return false;
		}
		if (this.scenario3Panel.getCorrelationRules().isEmpty()) {
			this.correlationPage.getFeedbackPanel().error("No correlation rules provided!");
			this.correlationPage.getFeedbackPanel().setVisible(true);
			return false;
		} else {
			for (final CorrelationRule correlationRule : this.scenario3Panel.getCorrelationRules()) {
				if (correlationRule.getFirstAttribute() == null || correlationRule.getSecondAttribute() == null) {
					this.correlationPage.getFeedbackPanel().error(
							"Some of the correlation rules are missing attributes!");
					this.correlationPage.getFeedbackPanel().setVisible(true);
					return false;
				}
			}
		}
		return true;
	}

	private boolean isAdvancedCorrelationPanelFilled() {
		// if (advancedCorrelationPanel.isTimeCorrelationSelected()) {
		// if (advancedCorrelationPanel.getTimeCondition()
		// .getSelectedEventType() == null) {
		// correlationPage.getFeedbackPanel().error(
		// "No event type for time correlation selected!");
		// correlationPage.getFeedbackPanel().setVisible(true);
		// return false;
		// }
		// if (advancedCorrelationPanel.getTimeCondition().getTimePeriod() == 0)
		// {
		// correlationPage.getFeedbackPanel().error(
		// "No minutes for time correlation inserted!");
		// correlationPage.getFeedbackPanel().setVisible(true);
		// return false;
		// }
		// if (advancedCorrelationPanel.getTimeCondition()
		// .getConditionString().isEmpty()) {
		// correlationPage.getFeedbackPanel().error(
		// "No condition for time correlation inserted!");
		// correlationPage.getFeedbackPanel().setVisible(true);
		// return false;
		// }
		// if (advancedCorrelationPanel.getTimeCondition()
		// .getConditionString().startsWith("=")
		// || advancedCorrelationPanel.getTimeCondition()
		// .getConditionString().endsWith("=")) {
		// correlationPage.getFeedbackPanel().error(
		// "Malformed condition for time correlation!");
		// correlationPage.getFeedbackPanel().setVisible(true);
		// return false;
		// }
		// }
		return true;
	}

	public void clearAdvancedCorrelationPanelComponents() {
		// correlationPage.getAdvancedCorrelationPanel()
		// .getTimeCorrelationEventTypeSelect()
		// .setChoices(new ArrayList<EapEventType>());
		// correlationPage.getAdvancedCorrelationPanel()
		// .getTimeCorrelationConditionAttributeSelect()
		// .setChoices(new ArrayList<String>());
		// correlationPage.getAdvancedCorrelationPanel()
		// .getTimeCorrelationConditionValueSelect()
		// .setChoices(new ArrayList<Serializable>());
	}

	public void updateAdvancedCorrelationPanelComponents(final AjaxRequestTarget target) {
		// target.add(correlationPage.getAdvancedCorrelationPanel()
		// .getTimeCorrelationEventTypeSelect());
		// target.add(correlationPage.getAdvancedCorrelationPanel()
		// .getTimeCorrelationConditionAttributeSelect());
		// target.add(correlationPage.getAdvancedCorrelationPanel()
		// .getTimeCorrelationConditionValueSelect());
	}

	private void addCorrelationTabs() {
		final List<ITab> tabs = new ArrayList<ITab>();
		tabs.add(new AbstractTab(new Model<String>("Correlation with same-name attributes")) {
			@Override
			public Panel getPanel(final String panelId) {
				CorrelationPage.this.simpleCorrelationPanel = new SimpleCorrelationPanel(panelId,
						CorrelationPage.this.correlationPage);
				return CorrelationPage.this.simpleCorrelationPanel;
			}
		});

		tabs.add(new AbstractTab(new Model<String>("Correlation with correlation rules")) {
			@Override
			public Panel getPanel(final String panelId) {
				CorrelationPage.this.scenario3Panel = new SimpleCorrelationWithRulesPanel(panelId,
						CorrelationPage.this.correlationPage);
				return CorrelationPage.this.scenario3Panel;
			}
		});

		// tabs.add(new AbstractTab(new Model<String>("Advanced correlation")) {
		// public Panel getPanel(String panelId) {
		// advancedCorrelationPanel = new AdvancedCorrelationPanel(panelId);
		// return advancedCorrelationPanel;
		// }
		// });

		this.layoutForm.add(new Collapsible("collapsible", tabs, Model.of(-1)));
	}

	private void addEventTypesToProcessAndCorrelate(final AjaxRequestTarget target) {
		final CorrelationProcess selectedProcess = CorrelationProcess.findByName(
				this.processSelect.getChoices().get(Integer.parseInt(this.processSelect.getValue()))).get(0);
		if (selectedProcess.hasCorrelation() || CorrelationProcessInstance.findByProcess(selectedProcess).size() > 0) {
			// showCorrelationExistsWarningModal(selectedProcess, target);
			this.correlationPage
					.getFeedbackPanel()
					.error("Correlation condition currently in use. Remove condition and all process instances to set new condition.");
			this.correlationPage.getFeedbackPanel().setVisible(true);
		} else {
			// addEventTypesToSelectedProcess(selectedProcess);
			this.correlateEvents(selectedProcess);
		}
	}

	// private void addEventTypesToSelectedProcess(
	// CorrelationProcess selectedProcess) {
	// if (simpleCorrelationWithRules) {
	// selectedProcess.setEventTypes(simpleCorrelationWithRulesPanel
	// .getCorrelationEventTypes());
	// } else {
	// selectedProcess.setEventTypes(simpleCorrelationPanel
	// .getCorrelationEventTypes());
	// }
	// selectedProcess.merge();
	// }

	// /**
	// * If correlation settings have been already determined for a process, the
	// * user is asked if the correlation settings shall be overwritten.
	// */
	// private void tryToCorrelateEvents(CorrelationProcess selectedProcess,
	// AjaxRequestTarget target) {
	// correlateEvents(selectedProcess);
	// }

	// private void showCorrelationExistsWarningModal(
	// CorrelationProcess selectedProcess, AjaxRequestTarget target) {
	// existingCorrelationAlert.setVisible(true);
	// existingCorrelationAlert.setSelectedProcess(selectedProcess);
	// target.add(existingCorrelationAlert);
	// }

	public void correlateEvents(final CorrelationProcess selectedProcess) {
		if (this.simpleCorrelationWithRules) {
			final Set<CorrelationRule> correlationRules = new HashSet<CorrelationRule>(
					this.scenario3Panel.getCorrelationRules());
			// if (advancedCorrelationPanel.isTimeCorrelationSelected()) {
			// RuleCorrelator.correlate(correlationRules, selectedProcess,
			// advancedCorrelationPanel.getTimeCondition());
			// } else {
			RuleCorrelator.correlate(correlationRules, selectedProcess, null);
			// }
		} else {
			final List<TypeTreeNode> correlationAttributes = this.simpleCorrelationPanel
					.getSelectedCorrelationAttributes();

			final Set<EapEventType> correlationEventTypes = this.simpleCorrelationPanel.getCorrelationEventTypes();
			// if (advancedCorrelationPanel.isTimeCorrelationSelected()) {
			// AttributeCorrelator.correlate(correlationEventTypes,
			// correlationAttributes, selectedProcess,
			// advancedCorrelationPanel.getTimeCondition());
			// } else {
			AttributeCorrelator.correlate(correlationEventTypes, correlationAttributes, selectedProcess, null);
			// }
		}
		this.correlationPage.getFeedbackPanel().success(
				"Correlation finished! " + CorrelationProcessInstance.findByProcess(selectedProcess).size()
						+ " process instances created!");
		this.correlationPage.getFeedbackPanel().setVisible(true);
	}

	public DropDownChoice<String> getProcessSelect() {
		return this.processSelect;
	}

	public void addProcessToProcessNameList(final String processName) {
		this.processNameList.add(processName);
	}

	public void removeProcessFromProcessNameList(final String processName) {
		this.processNameList.remove(processName);
	}

	public ExistingCorrelationAlert getAlert() {
		return this.existingCorrelationAlert;
	}

	public SimpleCorrelationPanel getSimpleCorrelationPanel() {
		return this.simpleCorrelationPanel;
	}

	public SimpleCorrelationWithRulesPanel getSimpleCorrelationWithRulesPanel() {
		return this.scenario3Panel;
	}

	// public AdvancedCorrelationPanel getAdvancedCorrelationPanel() {
	// return advancedCorrelationPanel;
	// }

	protected void updateSimpleCorrelationPanelComponents(final AjaxRequestTarget target) {
		target.add(this.simpleCorrelationPanel.getCorrelationAttributesSelect());
		target.add(this.simpleCorrelationPanel.getEventTypesCheckBoxMultipleChoice());
	}

	protected void updateSimpleCorrelationWithRulesPanelComponents(final AjaxRequestTarget target) {
		target.add(this.scenario3Panel.getAddCorrelationRuleButton());
		target.add(this.scenario3Panel.getCorrelationRuleMarkupContainer());
	}

	public void setValuesOfAdvancedCorrelationPanelComponents(final ArrayList<EapEventType> eventTypes) {
		// correlationPage.getAdvancedCorrelationPanel()
		// .getTimeCorrelationEventTypeSelect().setChoices(eventTypes);
		// if (eventTypes.isEmpty()) {
		// correlationPage.getAdvancedCorrelationPanel()
		// .getTimeCorrelationConditionAttributeSelect()
		// .setChoices(new ArrayList<String>());
		// correlationPage.getAdvancedCorrelationPanel()
		// .getTimeCorrelationConditionValueSelect()
		// .setChoices(new ArrayList<Serializable>());
		// } else {
		// correlationPage.getAdvancedCorrelationPanel()
		// .getTimeCorrelationConditionAttributeSelect()
		// .setChoices(eventTypes.get(0).getAttributeExpressions());
		// correlationPage
		// .getAdvancedCorrelationPanel()
		// .getTimeCorrelationConditionValueSelect()
		// .setChoices(
		// eventTypes.get(0).findAttributeValues(
		// eventTypes.get(0).getValueTypes().get(0)
		// .getAttributeExpression()));
		// }
	}

	public boolean isSimpleCorrelationWithRules() {
		return this.simpleCorrelationWithRules;
	}

}
