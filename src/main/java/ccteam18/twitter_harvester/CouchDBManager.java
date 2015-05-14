package ccteam18.twitter_harvester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The configuration is inside the file "couchdb.properties"
 * available in the classpath.
 * @author pablo
 *
 */
public class CouchDBManager {
	final static Logger logger = Logger.getLogger(CouchDBManager.class);

	// Implemented as a Singleton to guarantee Thread safety
	private static CouchDBManager instance = null;
	private CouchDbClient dbClient;
	private final String propFileName = "couchdb.properties";
	private final Properties props;
	
	private CouchDBManager() {
		// this code avoids possible classpath errors
		props = new Properties();
		try {
			File file = new File(propFileName);
			FileInputStream fileInputStream = new FileInputStream(file);
			props.load(fileInputStream);
			fileInputStream.close();
			
			dbClient = new CouchDbClient(
					new CouchDbProperties(props.getProperty("couchdb.name"), 
							Boolean.parseBoolean(props.getProperty("couchdb.createdb.if-not-exist")), 
							props.getProperty("couchdb.protocol"), 
							props.getProperty("couchdb.host"), 
							Integer.parseInt(props.getProperty("couchdb.port")), 
							props.getProperty("couchdb.username"), 
							props.getProperty("couchdb.password")));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	// Thread safe
	public synchronized static CouchDBManager getInstance() {
		if (instance == null)
			instance = new CouchDBManager();
		return instance;
	}
	
	/**
	 * Push directly the json object into the couchdb database.
	 * @param tweet
	 */
	public void saveTweet(JsonObject tweet) {
		try {
			// enforce to replace the couchdb id using the tweet id
			tweet.addProperty("_id", tweet.get("id").getAsString());
			logger.debug("Tweet after modifications = [" + tweet + "]");
			// save the copy
			dbClient.save(tweet);
		} catch (Exception e) {
			// it doesn't matter... skip any problem...
			e.printStackTrace();
		}
	}
	
	/**
	 * This method must be called before finish the threads execution
	 */
	public synchronized void closeConnection() {
		dbClient.shutdown();
	}
}