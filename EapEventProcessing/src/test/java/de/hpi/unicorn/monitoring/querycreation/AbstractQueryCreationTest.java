/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.querycreation;

import org.junit.Assert;

import de.hpi.unicorn.monitoring.AbstractMonitoringTest;
import de.hpi.unicorn.monitoring.bpmn.BPMNQueryMonitor;
import de.hpi.unicorn.monitoring.bpmn.DetailedQueryStatus;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceStatus;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * This class centralizes methods for all tests, which test the creation of BPMN
 * queries and monitor their execution.
 * 
 * @author micha
 */
public abstract class AbstractQueryCreationTest extends AbstractMonitoringTest {

	@Override
	protected void assertQueryStatus() {
		// Auf Listener hören
		final BPMNQueryMonitor queryMonitor = BPMNQueryMonitor.getInstance();
		for (final CorrelationProcessInstance processInstance : CorrelationProcessInstance.findAll()) {
			Assert.assertTrue(queryMonitor.getStatus(processInstance) == ProcessInstanceStatus.Finished);
			for (final DetailedQueryStatus detailedQueryStatus : queryMonitor.getDetailedStatus(processInstance)
					.getElements()) {
				Assert.assertTrue(detailedQueryStatus.getViolationStatus().isEmpty());
			}
		}
	}

}
