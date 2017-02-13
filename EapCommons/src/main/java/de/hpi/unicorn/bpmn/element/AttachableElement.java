/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.element;

/**
 * This interface should be implemented from BPMN elements that could have
 * attached intermediate events.
 * 
 * @author micha
 */
public interface AttachableElement {

	public boolean hasAttachedIntermediateEvent();

	public BPMNBoundaryEvent getAttachedIntermediateEvent();

	public void setAttachedIntermediateEvent(BPMNBoundaryEvent attachedEvent);

}
