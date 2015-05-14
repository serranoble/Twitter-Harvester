package ccteam18.twitter_harvester;

/**
 * Main class
 * @author pablo
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Harvester harvester = new Harvester();
    	harvester.start();
    }
}