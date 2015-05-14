package ccteam18.twitter_harvester;

import org.apache.log4j.Logger;


/**
 * Harvester class that executes a worker as an independent
 * thread. The main thread is used as a console to query
 * the service: use alive, stats, exit.
 * 
 * @author pablo
 *
 */
public class Harvester {
	final static Logger logger = Logger.getLogger(Harvester.class);
	// TODO: must be an observer looking for thread inconsistencies
	private Worker worker;
	private StatsManager stats;
	
	public Harvester() {
		logger.info("Harvester is being created...");
		stats = new StatsManager();
		worker = new Worker(stats);
		logger.info("Harvester was created successfully");
	}

	public void start() {
		// executed as part of the main thread
		logger.info("worker is lanched now!");
		worker.run();
	}
}
