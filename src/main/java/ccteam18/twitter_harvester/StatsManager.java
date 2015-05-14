package ccteam18.twitter_harvester;

import java.util.Date;

/**
 * Class to keep track from basic statistics. Usefull to
 * study the behavior of every machine running the
 * harvester.
 * 
 * @author pablo
 *
 */
public class StatsManager {
	private double postRead;
	private Date startTime;
	private Date lastTweetTime;
	
	public StatsManager() {
		postRead = 0;
		startTime = new Date();
		lastTweetTime = null;
	}
	
	public void updateStats() {
		postRead++;
		// garbage collection!
		if (lastTweetTime != null)
			lastTweetTime = null;
		lastTweetTime = new Date();
	}
	
	public void printStats() {
		System.out.println("----------------------------------------------------");
		System.out.println("Stats from the worker:");
		System.out.println("Running since: " + startTime);
		if (postRead > 0) {
			System.out.printf("# tweets read until now: %.0f\n", postRead);
			// sanity check
			if (lastTweetTime != null)
				System.out.println("Last tweet received at: " + lastTweetTime);
		} else
			System.out.println("No tweets have been received yet");
		System.out.println("----------------------------------------------------");
	}
}
