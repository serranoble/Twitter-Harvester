package ccteam18.twitter_harvester;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.Location.Coordinate;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.event.Event;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;

/**
 * Worker class that connects to the Stream API using HBC twitter. All the code
 * comes from the basic example provided by HBC authors.
 * 
 * @author pablo
 *
 */
public class Worker implements Runnable {

	final static Logger logger = Logger.getLogger(Worker.class);
	private Client hosebirdClient;
	private StatsManager myStats;

	public Worker(StatsManager stats) {
		myStats = stats;
	}

	public void stop() {
		logger.warn("Stop has been requested!");
		if (!hosebirdClient.isDone()) {
			// shutdown the connection safely
			CouchDBManager.getInstance().closeConnection();
			logger.debug("CouchDB connection was closed");
			hosebirdClient.stop();
			logger.debug("Twitter API connection was closed");
		}
		// A thread can't be destroyed in Java 8!!!
		Thread.currentThread().interrupt();
		logger.info("Worker has been stopped successfully");
	}

	@Override
	public void run() {
		// Sanity check to avoid unexpected behavior
		while (!Thread.currentThread().isInterrupted()) {
			processStream();
		}
	}

	// TODO: I have to clean this!!
	private void processStream() {
		/**
		 * Set up your blocking queues: Be sure to size these properly based on
		 * expected TPS of your stream
		 */
		BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
		BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<Event>(1000);

		/**
		 * Declare the host you want to connect to, the endpoint, and
		 * authentication (basic auth or oauth)
		 */
		Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
		StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
		/** Set up the coordinates defining the area to be analyzed */
		// google maps give these values in reverse order (latitude,longitude)
		Coordinate csw = new Coordinate(Double.parseDouble(ConfigManager
				.getInstance().getValue("swLongitude")),
				Double.parseDouble(ConfigManager.getInstance().getValue(
						"swLatitude")));
		Coordinate cne = new Coordinate(Double.parseDouble(ConfigManager
				.getInstance().getValue("neLongitude")),
				Double.parseDouble(ConfigManager.getInstance().getValue(
						"neLatitude")));
		// define a location (part of a city)
		Location lcity = new Location(csw, cne);
		// generate a list with all the parts to be analyzed
		List<Location> locations = Lists.newArrayList(lcity);
		// add the list to the endpoint configuration
		hosebirdEndpoint.locations(locations);
		//hosebirdEndpoint.trackTerms(Lists.newArrayList("#twitter"));

		// These secrets should be read from a config file
		Authentication hosebirdAuth = new OAuth1(ConfigManager.getInstance()
				.getValue("consumerKey"), ConfigManager.getInstance().getValue(
				"consumerSecret"), ConfigManager.getInstance()
				.getValue("token"), ConfigManager.getInstance().getValue(
				"tokenSecret"));

		// Create the client builder
		ClientBuilder builder = new ClientBuilder()
				.name("Hosebird-Client-01")
				// optional: mainly for the logs
				.hosts(hosebirdHosts).authentication(hosebirdAuth)
				.endpoint(hosebirdEndpoint)
				.processor(new StringDelimitedProcessor(msgQueue))
				.eventMessageQueue(eventQueue); // optional: use this if you
												// want to process client events

		// Initiate the client
		hosebirdClient = builder.build();
		logger.debug("Twitter client was built");
		// Attempts to establish a connection.
		hosebirdClient.connect();
		logger.info("Twitter API was connected successfully");

		// on a different thread, or multiple different threads....
		try {
			Gson gson = new Gson();
			logger.info("Starting twitter streaming extraction");
			while (!hosebirdClient.isDone()) {
				// recover the message from Tweeter API
				String msg = msgQueue.take();
				// transform it into a java json object
				JsonObject tweet = gson.fromJson(msg, JsonElement.class)
						.getAsJsonObject();
				// debug purposes
				System.out.println(tweet.get("text").getAsString());
				logger.debug("Twit received = [" + msg + "]");
				// push the json object into the couchdb database
				CouchDBManager.getInstance().saveTweet(tweet);
				logger.debug("Twitt was pushed into Couchdb");
				// update the daemon statistics
				myStats.updateStats();
				logger.debug("Stats updated, parsing next twitt!");
			}
		} catch (InterruptedException ie) {
			logger.error("An exception has happended during twitts parsing = [" + ie + "]");
			ie.printStackTrace();
			logger.error("Critical STOP has been ordered!");
			stop();
		}
	}

}
