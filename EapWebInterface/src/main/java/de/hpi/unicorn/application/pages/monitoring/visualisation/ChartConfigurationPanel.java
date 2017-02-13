/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.visualisation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.odlabs.wiquery.ui.slider.AjaxSlider;

import de.agilecoders.wicket.markup.html.bootstrap.common.NotificationPanel;
import de.hpi.unicorn.application.pages.eventrepository.eventtypeeditor.model.EventTypeNamesProvider;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.visualisation.ChartConfiguration;
import de.hpi.unicorn.visualisation.ChartTypeEnum;

/**
 * This panel is used to configure and save a new @see ChartConfiguration Object
 * This will create a new attribute chart.
 */
public class ChartConfigurationPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private DropDownChoice<String> eventTypeSelect;
	private DropDownChoice<TypeTreeNode> attributeSelect;
	private final EventTypeNamesProvider eventTypeNameProvider = new EventTypeNamesProvider();
	List<TypeTreeNode> attributes = new ArrayList<TypeTreeNode>();
	private AttributeChartPage parentPage;
	private final String selectedEventTypeName = "";
	private final TypeTreeNode selectedAttribute = null;
	private final String chartTitle = "";
	private final List<ChartTypeEnum> chartTypes = Arrays.asList(ChartTypeEnum.values());
	private final IModel<ChartTypeEnum> chartType = Model.of(ChartTypeEnum.COLUMN);
	private TextField<String> chartTitleInput;
	private DropDownChoice<ChartTypeEnum> chartTypeSelect;

	private ChartConfigurationPanel panel;

	private NotificationPanel feedbackPanel;
	private EapEventType selectedEventType;
	private Form<Void> layoutForm;
	private AjaxSlider slider;
	private Integer sliderValue = 1;
	private Label sliderLabel;
	private WebMarkupContainer sliderContainer;

	public ChartConfigurationPanel(final String id, final AttributeChartPage visualisationPage) {
		super(id);
		this.panel = this;

		this.parentPage = visualisationPage;

		this.layoutForm = new Form<Void>("layoutForm");
		this.add(this.layoutForm);
		this.addFeedbackPanel(this.layoutForm);

		this.layoutForm.add(this.addChartTitleInput());
		this.layoutForm.add(this.addChartTypeSelect());
		this.layoutForm.add(this.addEventTypeSelect());
		this.updateAttributes();
		this.layoutForm.add(this.addAttributeSelect());

		this.sliderContainer = new WebMarkupContainer("sliderDiv") {
			@Override
			public boolean isVisible() {
				return ChartConfigurationPanel.this.isSliderVisible();
			};
		};
		this.sliderContainer.setOutputMarkupPlaceholderTag(true);
		this.sliderContainer.add(this.addSlider());
		this.sliderContainer.add(this.addSliderLabel());
		this.layoutForm.add(this.sliderContainer);

		this.addButtonsToForm(this.layoutForm);
	}

	/**
	 * creates a slider that defines the size of the integer ranges, that define
	 * the attribute-value ranges for the distribution chart
	 * 
	 * @return slider
	 */
	private Component addSlider() {

		this.slider = new AjaxSlider("slider", 1, 100) {
			@Override
			public boolean isVisible() {
				return ChartConfigurationPanel.this.isSliderVisible();
			}
		};
		this.slider.setOutputMarkupPlaceholderTag(true);
		this.slider.setAjaxStopEvent(new AjaxSlider.ISliderAjaxEvent() {

			private static final long serialVersionUID = 1L;

			@Override
			public void onEvent(final AjaxRequestTarget target, final AjaxSlider slider1, final int value,
					final int[] values) {
				ChartConfigurationPanel.this.sliderValue = value;
				ChartConfigurationPanel.this.sliderLabel.detach();
				target.add(ChartConfigurationPanel.this.sliderLabel);
			}

		});
		return this.slider;
	}

	/**
	 * creates the label that displays the value of the slider
	 * 
	 * @return slider label
	 */
	private Component addSliderLabel() {
		this.sliderLabel = new Label("sliderLabel", new PropertyModel<Integer>(this, "sliderValue")) {
			@Override
			public boolean isVisible() {
				return ChartConfigurationPanel.this.isSliderVisible();
			}
		};
		this.sliderLabel.setOutputMarkupPlaceholderTag(true);
		return this.sliderLabel;
	}

	/**
	 * updates the slider range in dependence of the selected attribute and the
	 * actual in the database existing values
	 */
	public void updateSlider() {
		if (this.isSliderInvisible()) {
			return;
		}
		// find smallest ang biggest value from selected attribute
		final long min = EapEvent.getMinOfAttributeValue(this.selectedAttribute.getName(), this.selectedEventType);
		final long max = EapEvent.getMaxOfAttributeValue(this.selectedAttribute.getName(), this.selectedEventType);
		// the maximum should be the difference from the min-value to the
		// max-value
		this.slider.setMax(Math.abs(max) + Math.abs(min));
		this.sliderContainer.detach();
	}

	public boolean isSliderVisible() {
		return (this.selectedAttribute != null && this.chartType != null
				&& this.selectedAttribute.getType() == AttributeTypeEnum.INTEGER && this.chartType.getObject() == ChartTypeEnum.COLUMN);
	}

	public boolean isSliderInvisible() {
		return !this.isSliderVisible();
	}

	private void addFeedbackPanel(final Form<Void> layoutForm) {
		this.feedbackPanel = new NotificationPanel("feedback");
		this.feedbackPanel.setOutputMarkupId(true);
		this.feedbackPanel.setOutputMarkupPlaceholderTag(true);
		layoutForm.add(this.feedbackPanel);
	}

	private Component addChartTitleInput() {
		this.chartTitleInput = new TextField<String>("chartTitleInput", new PropertyModel<String>(this, "chartTitle"));
		return this.chartTitleInput;
	}

	private Component addEventTypeSelect() {
		this.eventTypeSelect = new DropDownChoice<String>("eventTypeSelect", new PropertyModel<String>(this,
				"selectedEventTypeName"), this.eventTypeNameProvider);
		this.eventTypeSelect.setOutputMarkupId(true);

		this.eventTypeSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				ChartConfigurationPanel.this.selectedEventType = EapEventType
						.findByTypeName(ChartConfigurationPanel.this.selectedEventTypeName);
				ChartConfigurationPanel.this.updateAttributes();
				target.add(ChartConfigurationPanel.this.attributeSelect);
			}
		});
		return this.eventTypeSelect;
	}

	private Component addChartTypeSelect() {
		this.chartTypeSelect = new DropDownChoice<ChartTypeEnum>("chartTypeSelect", this.chartType, this.chartTypes);
		this.chartTypeSelect.setOutputMarkupId(true);

		this.chartTypeSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				ChartConfigurationPanel.this.updateAttributes();
				ChartConfigurationPanel.this.updateSlider();
				target.add(ChartConfigurationPanel.this.attributeSelect);
				target.add(ChartConfigurationPanel.this.sliderContainer);
			}
		});
		return this.chartTypeSelect;
	}

	private Component addAttributeSelect() {
		this.updateAttributes();
		this.attributeSelect = new DropDownChoice<TypeTreeNode>("attributeSelect", new PropertyModel<TypeTreeNode>(
				this, "selectedAttribute"), this.attributes);
		this.attributeSelect.setOutputMarkupId(true);
		this.attributeSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				ChartConfigurationPanel.this.updateSlider();
				target.add(ChartConfigurationPanel.this.sliderContainer);
			}
		});

		return this.attributeSelect;
	}

	/**
	 * update attributes in dependence of selected event type
	 */
	private void updateAttributes() {
		this.attributes.clear();
		// collect attributes of event type
		if (this.selectedEventType != null) {
			if (this.chartType.getObject() == null) {
				this.attributes.addAll(this.selectedEventType.getValueTypes());
			} else if (this.chartType.getObject() == ChartTypeEnum.COLUMN) {
				// BarChart only for String, Integer, Float
				for (final TypeTreeNode attribute : this.selectedEventType.getValueTypes()) {
					if (attribute.getType() == AttributeTypeEnum.STRING
							|| attribute.getType() == AttributeTypeEnum.INTEGER
							|| attribute.getType() == AttributeTypeEnum.FLOAT) {
						this.attributes.add(attribute);
					}
				}
			} else if (this.chartType.getObject() == ChartTypeEnum.SPLATTER) {
				// SplatterChart only Integer, Float
				for (final TypeTreeNode attribute : this.selectedEventType.getValueTypes()) {
					if (attribute.getType() == AttributeTypeEnum.INTEGER
							|| attribute.getType() == AttributeTypeEnum.FLOAT) {
						this.attributes.add(attribute);
					}
				}
			}
		}
	}

	private void addButtonsToForm(final Form<Void> layoutForm) {

		final AjaxButton createButton = new AjaxButton("createButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				ChartConfigurationPanel.this.panel.getFeedbackPanel().setVisible(true);
				boolean error = false;

				if (ChartConfigurationPanel.this.chartTitle == null) {
					ChartConfigurationPanel.this.panel.getFeedbackPanel().error("Chart needs a title!");
					ChartConfigurationPanel.this.panel.getFeedbackPanel().setVisible(true);
					target.add(ChartConfigurationPanel.this.panel.getFeedbackPanel());
					error = true;
				}
				;
				if (ChartConfigurationPanel.this.selectedEventType == null) {
					ChartConfigurationPanel.this.panel.getFeedbackPanel().error("eventType must be chosen!");
					ChartConfigurationPanel.this.panel.getFeedbackPanel().setVisible(true);
					target.add(ChartConfigurationPanel.this.panel.getFeedbackPanel());
					error = true;
				}
				;
				if (ChartConfigurationPanel.this.chartType == null) {
					ChartConfigurationPanel.this.panel.getFeedbackPanel().error("Choose a chart type!");
					ChartConfigurationPanel.this.panel.getFeedbackPanel().setVisible(true);
					target.add(ChartConfigurationPanel.this.panel.getFeedbackPanel());
					error = true;
				}
				;
				if (ChartConfigurationPanel.this.selectedAttribute == null) {
					ChartConfigurationPanel.this.panel.getFeedbackPanel().error("Choose an attribute!");
					ChartConfigurationPanel.this.panel.getFeedbackPanel().setVisible(true);
					target.add(ChartConfigurationPanel.this.panel.getFeedbackPanel());
					error = true;
				}
				;

				if (error == false) {
					// create new ChartConfiguration
					final String attributeName = ChartConfigurationPanel.this.selectedAttribute
							.getAttributeExpression();
					final AttributeTypeEnum attributeType = ChartConfigurationPanel.this.selectedAttribute.getType();
					final ChartConfiguration newOptions = new ChartConfiguration(
							ChartConfigurationPanel.this.selectedEventType, attributeName, attributeType,
							ChartConfigurationPanel.this.chartTitle,
							ChartConfigurationPanel.this.chartType.getObject(),
							ChartConfigurationPanel.this.sliderValue);
					newOptions.save();
					final AttributeChartPage visualisation = ChartConfigurationPanel.this.parentPage;
					visualisation.getOptions().detach();
					target.add(visualisation.listview.getParent());
					// close this Panel
					visualisation.addChartModal.close(target);
				}
				;
			};
		};

		layoutForm.add(createButton);
	}

	public NotificationPanel getFeedbackPanel() {
		return this.feedbackPanel;
	}

}
