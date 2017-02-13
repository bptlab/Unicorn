/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.application.pages.transformation.patternbuilder.PatternBuilderPanel;
import de.hpi.unicorn.transformation.element.FilterExpressionElement;
import de.hpi.unicorn.transformation.element.FilterExpressionOperatorEnum;
import de.hpi.unicorn.transformation.element.RangeElement;

public class FilterExpressionPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final Form<Void> layoutForm;
	private String leftHandSideExpression;
	private String rightHandSideExpression;
	private AttributeExpressionTextField rightHandSideExpressionInput;
	private final FilterExpressionElement element;
	private final PatternElementTreeTable table;
	private int leftEndpoint;
	private int rightEndpoint;
	private FilterExpressionOperatorEnum filterExpressionOperator;
	private RadioChoice<String> typeOfValuesRadioChoice;
	private String typeOfValues;
	private final PatternBuilderPanel panel;

	public FilterExpressionPanel(final String id, final FilterExpressionElement element, final PatternBuilderPanel panel) {
		super(id);

		this.layoutForm = new Form<Void>("layoutForm");

		this.element = element;
		this.table = panel.getPatternTreeTable();
		this.panel = panel;

		this.buildLeftHandSideExpressionInput();
		this.buildFilterExpressionOperatorDropDownChoice();
		this.buildRightHandSideExpressionInput();
		this.buildRightHandSideValuesBasedComponents();

		// List<String> expressions = new ArrayList<String>();
		// expressions.addAll(element.getFilterExpressions());
		// if (!expressions.contains("")) {
		// expressions.add("");
		// }

		// filterExpressionListView = new
		// ListView<String>("filterExpressionListView", expressions) {
		//
		// private static final long serialVersionUID = -8698730823614901057L;
		//
		// @Override
		// protected void populateItem(ListItem<String> item) {
		// String expression = item.getModelObject();
		// buildComponents(item, expression);
		// }
		//
		// private void buildComponents(ListItem<String> item, final String
		// expression) {
		//
		// final TextField<String> filterExpressionInput = new
		// TextField<String>("filterExpressionInput", new Model<String>()) {
		// private static final long serialVersionUID = 5931500662562159353L;
		//
		// @Override
		// public boolean isEnabled() {
		// return expression.isEmpty();
		// }
		// };
		// filterExpressionInput.setModelObject(expression);
		// filterExpressionInput.setOutputMarkupId(true);
		// item.add(filterExpressionInput);
		//
		// // AjaxButton deleteButton = new AjaxButton("deleteButton",
		// layoutForm) {
		// // private static final long serialVersionUID = 1609842059851860853L;
		// //
		// // @Override
		// // public boolean isVisible() {
		// // return !expression.isEmpty();
		// // }
		// //
		// // @Override
		// // public void onSubmit(AjaxRequestTarget target, Form<?> form) {
		// //
		// element.removeFilterExpression(filterExpressionInput.getModelObject());
		// // target.add(table);
		// // }
		// // };
		// // item.add(deleteButton);
		//
		// // AjaxButton editButton = new AjaxButton("editButton", layoutForm) {
		// // private static final long serialVersionUID = 1L;
		// // @Override
		// // public void onSubmit(AjaxRequestTarget target, Form<?> form) {
		// //
		// element.removeFilterExpression(filterExpressionInput.getModelObject());
		// // filterExpressionInput.setEnabled(true);
		// // target.add(filterExpressionInput);
		// // }
		// // };
		// // item.add(editButton);
		//
		// AjaxButton saveButton = new AjaxButton("saveButton", layoutForm) {
		// private static final long serialVersionUID = 1928837801022392147L;
		//
		// @Override
		// public boolean isVisible() {
		// return expression.isEmpty();
		// }
		//
		// @Override
		// public void onSubmit(AjaxRequestTarget target, Form<?> form) {
		// String rawInput = filterExpressionInput.getModelObject();
		// if (rawInput != null) {
		// String newExpression = rawInput.trim().replaceAll(" +", " ");
		// if (element.getFilterExpressions().contains(newExpression)) {
		// // TODO: show error that it exists already
		// } else {
		// element.addFilterExpression(newExpression);
		// filterExpressionInput.setEnabled(false);
		// target.add(filterExpressionInput);
		// target.add(table);
		// }
		// }
		// }
		// };
		// item.add(saveButton);
		// }
		// };
		// filterExpressionListView.setOutputMarkupId(true);
		// layoutForm.add(filterExpressionListView);

		this.add(this.layoutForm);
	}

	private void buildLeftHandSideExpressionInput() {

		this.leftHandSideExpression = this.element.getLeftHandSideExpression();

		final AttributeExpressionTextField leftHandSideExpressionInput = new AttributeExpressionTextField(
				"leftHandSideExpressionInput", new PropertyModel<String>(this, "leftHandSideExpression"),
				this.panel.getPatternTree());
		final OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {

			private static final long serialVersionUID = 2339672763583311932L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				FilterExpressionPanel.this.element
						.setLeftHandSideExpression(FilterExpressionPanel.this.leftHandSideExpression);
			}
		};
		leftHandSideExpressionInput.add(onChangeAjaxBehavior);
		leftHandSideExpressionInput.setOutputMarkupId(true);
		this.layoutForm.add(leftHandSideExpressionInput);
	}

	private void buildFilterExpressionOperatorDropDownChoice() {

		this.filterExpressionOperator = (FilterExpressionOperatorEnum) this.element.getValue();

		final DropDownChoice<FilterExpressionOperatorEnum> filterExpressionOperatorDropDownChoice = new DropDownChoice<FilterExpressionOperatorEnum>(
				"filterExpressionOperatorDropDownChoice", new PropertyModel<FilterExpressionOperatorEnum>(this,
						"filterExpressionOperator"), Arrays.asList(FilterExpressionOperatorEnum.values()),
				new ChoiceRenderer<FilterExpressionOperatorEnum>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(final FilterExpressionOperatorEnum element) {
						return element.getValue();
					}
				});
		filterExpressionOperatorDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = -5452061293278720695L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				FilterExpressionPanel.this.element.setValue(FilterExpressionPanel.this.filterExpressionOperator);
				FilterExpressionPanel.this.table.getSelectedElements().remove(FilterExpressionPanel.this.element);
				target.add(FilterExpressionPanel.this.table);
			}
		});
		filterExpressionOperatorDropDownChoice.setOutputMarkupId(true);
		this.layoutForm.add(filterExpressionOperatorDropDownChoice);
	}

	private void buildRightHandSideExpressionInput() {

		this.rightHandSideExpression = this.element.getRightHandSideExpression();

		this.rightHandSideExpressionInput = new AttributeExpressionTextField("rightHandSideExpressionInput",
				new PropertyModel<String>(this, "rightHandSideExpression"), this.panel.getPatternTree()) {
			private static final long serialVersionUID = 5931500662562159353L;

			@Override
			public boolean isVisible() {
				return !FilterExpressionPanel.this.isFilterExpressionOperatorWithValues();
			}
		};
		final OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = -4319775721171622640L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				FilterExpressionPanel.this.element
						.setRightHandSideExpression(FilterExpressionPanel.this.rightHandSideExpression);
			}
		};
		this.rightHandSideExpressionInput.add(onChangeAjaxBehavior);
		this.rightHandSideExpressionInput.setOutputMarkupId(true);
		this.layoutForm.add(this.rightHandSideExpressionInput);
	}

	private void buildRightHandSideValuesBasedComponents() {
		if (this.element.isRightHandSideRangeBased()) {
			this.typeOfValues = "range based";
		} else {
			this.typeOfValues = "list based";
		}

		this.typeOfValuesRadioChoice = new RadioChoice<String>("typeOfValuesRadioChoice", new PropertyModel<String>(
				this, "typeOfValues"), new ArrayList<String>(Arrays.asList("range based", "list based"))) {
			private static final long serialVersionUID = 2134778179415091830L;

			@Override
			public boolean isVisible() {
				return FilterExpressionPanel.this.isFilterExpressionOperatorWithValues();
			}
		};
		this.typeOfValuesRadioChoice.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1479085520139021981L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (FilterExpressionPanel.this.typeOfValues.equals("range based")) {
					FilterExpressionPanel.this.element.setRightHandSideRangeBased(true);
				} else {
					FilterExpressionPanel.this.element.setRightHandSideRangeBased(false);
				}
				target.add(FilterExpressionPanel.this.table);
			}
		});
		// typeOfValuesRadioChoice.setSuffix("&nbsp;");
		this.typeOfValuesRadioChoice.setOutputMarkupId(true);
		this.layoutForm.add(this.typeOfValuesRadioChoice);

		this.buildRightHandSideRangeBasedComponent();
		this.buildRightHandSideListBasedComponent();
	}

	private void buildRightHandSideRangeBasedComponent() {
		final RangeElement rangeElement = this.element.getRightHandSideRangeOfValues();

		this.leftEndpoint = rangeElement.getLeftEndpoint();

		final Label leftEndpointInputLabel = new Label("leftEndpointInputLabel", "Left endpoint") {
			private static final long serialVersionUID = 7258389748479790432L;

			@Override
			public boolean isVisible() {
				return FilterExpressionPanel.this.isFilterExpressionOperatorWithValues()
						&& FilterExpressionPanel.this.element.isRightHandSideRangeBased();
			}
		};
		this.layoutForm.add(leftEndpointInputLabel);

		final TextField<Integer> leftEndpointInput = new TextField<Integer>("leftEndpointInput",
				new PropertyModel<Integer>(this, "leftEndpoint")) {
			private static final long serialVersionUID = -8395573703349094639L;

			@Override
			public boolean isVisible() {
				return FilterExpressionPanel.this.isFilterExpressionOperatorWithValues()
						&& FilterExpressionPanel.this.element.isRightHandSideRangeBased();
			}
		};
		OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = -872013504057729558L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				rangeElement.setLeftEndpoint(FilterExpressionPanel.this.leftEndpoint);
			}
		};
		leftEndpointInput.add(onChangeAjaxBehavior);
		leftEndpointInput.setOutputMarkupId(true);
		this.layoutForm.add(leftEndpointInput);

		final CheckBox leftEndpointOpenCheckBox = new CheckBox("leftEndpointOpenCheckbox", Model.of(rangeElement
				.isLeftEndpointOpen())) {
			private static final long serialVersionUID = 7637316203250432004L;

			@Override
			public boolean isVisible() {
				return FilterExpressionPanel.this.isFilterExpressionOperatorWithValues()
						&& FilterExpressionPanel.this.element.isRightHandSideRangeBased();
			}
		};
		leftEndpointOpenCheckBox.setOutputMarkupId(true);
		this.layoutForm.add(leftEndpointOpenCheckBox);

		final Label leftEndpointOpenLabel = new Label("leftEndpointOpenLabel", "open range") {
			private static final long serialVersionUID = 7258389748479790432L;

			@Override
			public boolean isVisible() {
				return FilterExpressionPanel.this.isFilterExpressionOperatorWithValues()
						&& FilterExpressionPanel.this.element.isRightHandSideRangeBased();
			}
		};
		this.layoutForm.add(leftEndpointOpenLabel);

		final Label rightEndpointInputLabel = new Label("rightEndpointInputLabel", "Right endpoint") {
			private static final long serialVersionUID = -6342679018392936070L;

			@Override
			public boolean isVisible() {
				return FilterExpressionPanel.this.isFilterExpressionOperatorWithValues()
						&& FilterExpressionPanel.this.element.isRightHandSideRangeBased();
			}
		};
		this.layoutForm.add(rightEndpointInputLabel);

		this.rightEndpoint = rangeElement.getRightEndpoint();

		final TextField<Integer> rightEndpointInput = new TextField<Integer>("rightEndpointInput",
				new PropertyModel<Integer>(this, "rightEndpoint")) {
			private static final long serialVersionUID = -1841846175664472901L;

			@Override
			public boolean isVisible() {
				return FilterExpressionPanel.this.isFilterExpressionOperatorWithValues()
						&& FilterExpressionPanel.this.element.isRightHandSideRangeBased();
			}
		};
		onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = 7688362699342944026L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				rangeElement.setRightEndpoint(FilterExpressionPanel.this.rightEndpoint);
			}
		};
		rightEndpointInput.add(onChangeAjaxBehavior);
		rightEndpointInput.setOutputMarkupId(true);
		this.layoutForm.add(rightEndpointInput);

		final CheckBox rightEndpointOpenCheckBox = new CheckBox("rightEndpointOpenCheckbox", Model.of(rangeElement
				.isRightEndpointOpen())) {
			private static final long serialVersionUID = -8677379162792099253L;

			@Override
			public boolean isVisible() {
				return FilterExpressionPanel.this.isFilterExpressionOperatorWithValues()
						&& FilterExpressionPanel.this.element.isRightHandSideRangeBased();
			}
		};
		rightEndpointOpenCheckBox.setOutputMarkupId(true);
		this.layoutForm.add(rightEndpointOpenCheckBox);

		final Label rightEndpointOpenLabel = new Label("rightEndpointOpenLabel", "open range") {
			private static final long serialVersionUID = -9209530337733632817L;

			@Override
			public boolean isVisible() {
				return FilterExpressionPanel.this.isFilterExpressionOperatorWithValues()
						&& FilterExpressionPanel.this.element.isRightHandSideRangeBased();
			}
		};
		this.layoutForm.add(rightEndpointOpenLabel);
	}

	private void buildRightHandSideListBasedComponent() {
		final List<String> expressions = new ArrayList<String>();
		expressions.addAll(this.element.getRightHandSideListOfValues());
		if (!expressions.contains("")) {
			expressions.add("");
		}

		final ListView<String> filterExpressionListView = new ListView<String>("filterExpressionListView", expressions) {

			private static final long serialVersionUID = -8698730823614901057L;

			@Override
			public boolean isVisible() {
				return FilterExpressionPanel.this.isFilterExpressionOperatorWithValues()
						&& !FilterExpressionPanel.this.element.isRightHandSideRangeBased();
			}

			@Override
			protected void populateItem(final ListItem<String> item) {
				final String expression = item.getModelObject();
				this.buildComponents(item, expression);
			}

			private void buildComponents(final ListItem<String> item, final String expression) {

				final TextField<String> filterExpressionInput = new TextField<String>("filterExpressionInput",
						new Model<String>()) {
					private static final long serialVersionUID = 5931500662562159353L;

					@Override
					public boolean isEnabled() {
						return expression.isEmpty();
					}
				};
				filterExpressionInput.setModelObject(expression);
				filterExpressionInput.setOutputMarkupId(true);
				item.add(filterExpressionInput);

				final AjaxButton deleteButton = new AjaxButton("deleteButton", FilterExpressionPanel.this.layoutForm) {
					private static final long serialVersionUID = 1609842059851860853L;

					@Override
					public boolean isVisible() {
						return !expression.isEmpty();
					}

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
						FilterExpressionPanel.this.element.getRightHandSideListOfValues().remove(
								filterExpressionInput.getModelObject());
						target.add(FilterExpressionPanel.this.table);
					}
				};
				item.add(deleteButton);

				// AjaxButton editButton = new AjaxButton("editButton",
				// layoutForm) {
				// private static final long serialVersionUID = 1L;
				// @Override
				// public void onSubmit(AjaxRequestTarget target, Form<?> form)
				// {
				// element.removeFilterExpression(filterExpressionInput.getModelObject());
				// filterExpressionInput.setEnabled(true);
				// target.add(filterExpressionInput);
				// }
				// };
				// item.add(editButton);

				final AjaxButton saveButton = new AjaxButton("saveButton", FilterExpressionPanel.this.layoutForm) {
					private static final long serialVersionUID = 1928837801022392147L;

					@Override
					public boolean isVisible() {
						return expression.isEmpty();
					}

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
						final String rawInput = filterExpressionInput.getModelObject();
						if (rawInput != null) {
							final String newExpression = rawInput.trim().replaceAll(" +", " ");
							if (FilterExpressionPanel.this.element.getRightHandSideListOfValues().contains(
									newExpression)) {
								// TODO: show error that it exists already
							} else {
								FilterExpressionPanel.this.element.getRightHandSideListOfValues().add(newExpression);
								filterExpressionInput.setEnabled(false);
								target.add(filterExpressionInput);
								target.add(FilterExpressionPanel.this.table);
							}
						}
					}
				};
				item.add(saveButton);
			}
		};
		filterExpressionListView.setOutputMarkupId(true);
		this.layoutForm.add(filterExpressionListView);
	}

	private boolean isFilterExpressionOperatorWithValues() {
		return ((FilterExpressionOperatorEnum) this.element.getValue() == FilterExpressionOperatorEnum.IN)
				|| ((FilterExpressionOperatorEnum) this.element.getValue() == FilterExpressionOperatorEnum.NOT_IN);
	}
}
