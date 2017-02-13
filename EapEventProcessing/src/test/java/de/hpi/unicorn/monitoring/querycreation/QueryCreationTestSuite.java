/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.querycreation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.hpi.unicorn.monitoring.querycreation.complex.ComplexProcessTest;
import de.hpi.unicorn.monitoring.querycreation.complex.ComplexProcessWithSomeMonitoringPointsTest;
import de.hpi.unicorn.monitoring.querycreation.subprocess.ProcessWithTwoEndEventsTest;
import de.hpi.unicorn.monitoring.querycreation.subprocess.SubProcessTest;
import de.hpi.unicorn.monitoring.querycreation.subprocess.SubProcessWithCancelEventTest;
import de.hpi.unicorn.monitoring.querycreation.timer.MessageAndTimerTest;
import de.hpi.unicorn.monitoring.querycreation.timer.MessageAndTimerWithTimerTest;
import de.hpi.unicorn.monitoring.querycreation.timer.MessageAndTimerWithZeroTimeTest;

/**
 * Testsuite with all tests for query creation and monitoring of the execution
 * of BPMN processes.
 * 
 * @author micha
 */
@RunWith(Suite.class)
@SuiteClasses({ SimpleSequenceTest.class, SimpleSequenceStateTransitionTest.class, AndTest.class, XORTest.class,
		LoopTest.class, ComplexProcessTest.class, ComplexProcessWithSomeMonitoringPointsTest.class,
		ProcessWithTwoEndEventsTest.class, SubProcessTest.class, SubProcessWithCancelEventTest.class,
		MessageAndTimerTest.class, MessageAndTimerWithTimerTest.class, MessageAndTimerWithZeroTimeTest.class })
public class QueryCreationTestSuite {

}
