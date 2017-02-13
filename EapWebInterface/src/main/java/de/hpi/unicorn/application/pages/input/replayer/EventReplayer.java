package de.hpi.unicorn.application.pages.input.replayer;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.ReplayEvent;
import de.hpi.unicorn.eventhandling.Broker;

public class EventReplayer {

	public enum TimeMode {
		UNCHANGED, ALIGNED, ALIGNED_MULTIPLE, NOW
	}

	private static final String FORMAT_STRING = "[%s] Sending event %d of type %s - simulated time: %s";
	private static final SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm:ss.SSS");
	private static final SimpleDateFormat simTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private int scaleFactor;
	// private final List<EapEvent> events = new ArrayList<EapEvent>();
	/**
	 * Represents the events to be replayed, sorted by the timestamp. Because
	 * the compare-method of the Comparator is used instead of equals when
	 * adding elements to the set, we sort ReplayEvents with the same timestamp
	 * arbitrarily to not lose one of them.
	 */
	private TreeSet<ReplayEvent> replayList = new TreeSet<ReplayEvent>(new ReplayEventComparator());
	public Date simulationStartPoint;
	public TimeMode mode = TimeMode.UNCHANGED;
	private int replayed = -1, total = -1;
	public List<ReplayFileBean> beans;
	public String category;
	public Long fixedOffset;

	/**
	 * Empty constructor. Scale factor defaults to 1.
	 */
	public EventReplayer() {
		this(1);
	}

	/**
	 * Sets the scale factor.
	 * 
	 * @param scaleFactor
	 */
	public EventReplayer(int scaleFactor) {
		this(null, scaleFactor);
	}

	/**
	 * Sets a list of initial traces that might be unsorted. Each String is
	 * converted to a ReplayEvent and puts into the replay list. Scale factor
	 * defaults to 1.
	 * 
	 * @param events
	 */
	public EventReplayer(List<EapEvent> events) {
		this(events, 1);
	}

	public EventReplayer(List<EapEvent> events, int scaleFactor) {
		if (events != null) {
			for (EapEvent e : events) {
				addEvent(e);
			}
			// this.events.addAll(events);
		}
		if (scaleFactor == 0) {
			this.scaleFactor = 1;
		} else {
			this.scaleFactor = scaleFactor;
		}
	}

	/**
	 * This constructor takes the additional parameter simulationTimeInit that
	 * is used to set the timestamp of the replayed events.
	 * 
	 * @param events
	 *            - a list of EapEvent objects
	 * @param simulationTimeInit
	 *            - a Date
	 * @param scaleFactor
	 * @param useCurrentTime
	 * @param useCurrentTime
	 */
	public EventReplayer(List<EapEvent> events, int scaleFactor, String category, List<ReplayFileBean> selectedFiles,
			TimeMode mode, Date simulationTimeInit, Long fixedOffset) {
		this(events, scaleFactor);
		this.simulationStartPoint = simulationTimeInit;
		this.mode = mode;
		this.category = category;
		this.beans = selectedFiles;
		this.fixedOffset = fixedOffset;
	}

	/**
	 * Creates a ReplayEvent from the given XML String and adds it to the replay
	 * list, setting the offset.
	 * 
	 * @param e
	 */
	public void addEvent(EapEvent e) {
		ReplayEvent re = new ReplayEvent(e);
		ReplayEvent ceiling = replayList.ceiling(re);
		ReplayEvent floor = replayList.floor(re);
		// update the offsets
		if (ceiling != null)
			ceiling.setOffset(ceiling.getTime() - re.getTime());
		if (floor != null) {
			re.setOffset(re.getTime() - floor.getTime());
		} else {
			re.setOffset(0l);
		}
		replayList.add(re);
	}

	public int getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(int scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	/**
	 * Starts the replay in another thread. This thread sends events to the EAP
	 * and prints info to System.out.
	 * 
	 * @param stub
	 */
	public void replay() {
		this.new ReplayThread().start();
	}

	public SortedSet<ReplayEvent> getReplayTree() {
		return replayList;
	}

	public Long getTotalDuration() {
		Long duration = 0l;
		for (ReplayEvent r : replayList) {
			duration += r.getOffset();
		}
		return duration;
	}

	public int getTotalNumberOfEvents() {
		return total;
	}

	public int getReplayedNumberOfEvents() {
		return replayed;
	}

	/**
	 * A ReplayThread encapsulates the replay logic and uses the instance
	 * variables of the enclosing class EventReplayer.
	 * 
	 * @author marcin.hewelt, tsunyin.wong
	 */
	private class ReplayThread extends Thread {
		@Override
		public void run() {
			replayed = 0;
			total = replayList.size();
			replay();
		}

		private void replay() {
			Long startTime = null;
			if (mode == TimeMode.ALIGNED || mode == TimeMode.ALIGNED_MULTIPLE) {

				startTime = simulationStartPoint.getTime();
			}
			System.out.println("Starting replay");
			for (ReplayEvent event : replayList) {
				if (mode == TimeMode.ALIGNED || mode == TimeMode.ALIGNED_MULTIPLE) {
					if (fixedOffset != null) {
						startTime += fixedOffset;
					} else {
						startTime += event.getOffset();
					}
					event.setTimestamp(new Date(startTime));
				} else if (mode == TimeMode.NOW) {
					event.setTimestamp(new Date());
				}
				try {
					if (fixedOffset != null) {
						Thread.sleep(fixedOffset / scaleFactor);
					} else {
						Thread.sleep(event.getOffset() / scaleFactor);
					}
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				long timePoint = System.currentTimeMillis();

				// TODO creating new EapEvent is not sooo elegant
				Broker.getEventImporter().importEvent(
						new EapEvent(event.getEventType(), event.getTimestamp(), event.getValues()));

				System.out.println(String.format(FORMAT_STRING, outputFormat.format(new Date(timePoint)), replayed++,
						event.getEventType(), simTimeFormat.format(new Date(event.getTime()))));

			}
		}
	}

	public List<ReplayFileBean> getBeans() {
		return beans;
	}

	public void setReplayList(TreeSet<ReplayEvent> replayEvents) {
		this.replayList = replayEvents;
	}

	class ReplayEventComparator implements Comparator<ReplayEvent>, Serializable {
		private static final long serialVersionUID = 3997754637546323723L;

		@Override
		public int compare(ReplayEvent e1, ReplayEvent e2) {
			long diff = e1.getTime() - e2.getTime();
			return (diff == 0l) ? -1 : (int) diff;
		}
	}

}
