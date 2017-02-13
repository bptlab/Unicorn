/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.decomposition;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;

public class LoopComponent extends Component {

	private static final long serialVersionUID = 1L;

	public LoopComponent(final AbstractBPMNElement entryPoint, final AbstractBPMNElement sourceElement,
			final AbstractBPMNElement exitPoint, final AbstractBPMNElement sinkElement) {
		super(entryPoint, sourceElement, exitPoint, sinkElement);
	}

}
