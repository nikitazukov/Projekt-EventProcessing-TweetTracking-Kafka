package eventprocessing;

public class Configuration {

	// Twitter configuration parameter
	private static int tweetCount = 10;
	private static String[] keyWords = { "trump" };

	// Kafka topic name
	private static String topicName = "test";

	public static int tweetCount() {
		return tweetCount;
	}

	public static String topicName() {
		return topicName;
	}

	public static String[] keyWords() {
		return keyWords;
	}
}
