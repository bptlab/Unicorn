/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import de.hpi.unicorn.event.EapEvent;

/**
 * A basic listener that just informs about the received event.
 */
public class CEPListener implements UpdateListener {

	@Override
	public void update(final EventBean[] newData, final EventBean[] oldData) {
		if (newData[0].getUnderlying() instanceof EapEvent) {
			newData[0].getUnderlying();
		}
		// System.out.println("Event received: " + newData[0].getUnderlying());
	}

}