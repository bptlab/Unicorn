/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.decomposition;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNSubProcess;

public class SubProcessComponent extends Component {

	private static final long serialVersionUID = 1L;
	private BPMNSubProcess subProcess;

	public SubProcessComponent(final AbstractBPMNElement entryPoint, final AbstractBPMNElement sourceElement,
			final AbstractBPMNElement exitPoint, final AbstractBPMNElement sinkElement) {
		super(entryPoint, sourceElement, exitPoint, sinkElement);
		this.type = IPattern.SUBPROCESS;
	}

	public void setSubProcess(final BPMNSubProcess subProcess) {
		this.subProcess = subProcess;
	}

	public BPMNSubProcess getSubProcess() {
		return this.subProcess;
	}

}
