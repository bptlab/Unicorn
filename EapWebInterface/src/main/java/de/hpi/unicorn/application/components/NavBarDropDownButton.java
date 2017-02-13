/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components;

import org.apache.wicket.model.IModel;

import de.agilecoders.wicket.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonSize;
import de.agilecoders.wicket.markup.html.bootstrap.button.ButtonType;
import de.agilecoders.wicket.markup.html.bootstrap.button.dropdown.DropDownButton;
import de.agilecoders.wicket.markup.html.bootstrap.navbar.Navbar;

/**
 * Subclass of {@link DropDownButton} for a {@link Navbar}. This DropDownButton
 * solves the problem for being displayed correctly after a refresh.
 * 
 * @author micha
 */
public class NavBarDropDownButton extends DropDownButton {

	private static final long serialVersionUID = 1L;
	static final String COMPONENT_ID = "component";

	/**
	 * Constructor for the {@link NavBarDropDownButton}.
	 * 
	 * @param model
	 *            the label of this dropdown button
	 */
	public NavBarDropDownButton(final IModel<String> model) {
		super(NavBarDropDownButton.COMPONENT_ID, model);
	}

	@Override
	protected void onConfigure() {
		super.onConfigure();

		this.setRenderBodyOnly(true);
		this.getParent().add(new CssClassNameAppender("dropdown"));
	}

	@Override
	protected void addButtonBehavior(final IModel<ButtonType> buttonType, final IModel<ButtonSize> buttonSize) {
		// do nothing, because navbar dropdown button inherits its styles from
		// navbar.
	}

}
