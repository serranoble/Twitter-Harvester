package ccteam18.twitter_harvester;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Class designed as a Singleton to manage the config file 
 * associated to the project. The config file contains all
 * the security parameters linked to the harvester.
 * 
 * @author pablo
 *
 */
public class ConfigManager {
	
	private final String propFileName = "config.properties";
	private final Properties props;
	private static ConfigManager instance = null;
	
	private ConfigManager() {
		props = new Properties();
		try {
			File file = new File(propFileName);
			FileInputStream fileInputStream = new FileInputStream(file);
			props.load(fileInputStream);
			fileInputStream.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static ConfigManager getInstance() {
		if (instance == null)
			instance = new ConfigManager();
		return instance;
	}
	
	public String getValue(String key) {
		return props.getProperty(key);
	}
}
