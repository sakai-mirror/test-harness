package org.sakaiproject.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 * 
 */
public class SakaiTestTimer {
	private static final Log log = LogFactory.getLog(SakaiTestTimer.class);
	private String task;
	private long start;

	/**
	 * Create a new SakaiTestTimer
	 * 
	 * @param task
	 *            The name of the task being timed
	 */
	public SakaiTestTimer(String task) {
		this.task = task;
		restart();
	}

	public void restart() {
		start = System.currentTimeMillis();
	}

	/**
	 * Log the elapsed time since this was created or reset.
	 */
	public void logTimeElapsed() {
		log.debug(task + ": " + (System.currentTimeMillis() - start) + " ms");
	}
}
