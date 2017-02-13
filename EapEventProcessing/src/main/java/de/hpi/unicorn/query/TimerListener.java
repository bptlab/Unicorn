/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query;

import java.util.Date;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;

/**
 * This thread is indicated to wait for the specified time duration and then to
 * send a timer event to Esper. It is used for monitoring of attached
 * intermediate timer events.
 * 
 * @author micha
 */
public class TimerListener extends Thread {

	private final EapEvent timerEvent;
	private final EapEventType boundaryTimerEventType;
	private final float timeDuration;

	public TimerListener(final EapEvent timerEvent, final EapEventType boundaryTimerEventType, final float timeDuration) {
		this.timerEvent = timerEvent;
		this.boundaryTimerEventType = boundaryTimerEventType;
		this.timeDuration = timeDuration;
	}

	@Override
	public void run() {
		try {
			// Millisekunden = Minuten * 1000 * 60
			final float time = this.timeDuration * 1000 * 60;
			final int sleepTime = Math.round(time);

			Thread.sleep(sleepTime);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		System.err.println("Send Boundary Timer Event to Esper" + this.createBoundaryTimerEvent());

		Broker.getEventImporter().importEvent(this.createBoundaryTimerEvent(), true);
	}

	private EapEvent createBoundaryTimerEvent() {
		final EapEvent boundaryTimerEvent = new EapEvent(this.boundaryTimerEventType, new Date());
		boundaryTimerEvent.setProcessInstances(this.timerEvent.getProcessInstances());
		return boundaryTimerEvent;
	}

}
