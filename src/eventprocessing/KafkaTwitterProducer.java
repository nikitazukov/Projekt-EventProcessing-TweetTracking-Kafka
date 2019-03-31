package eventprocessing;

import java.util.Properties;
import java.util.Scanner;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class KafkaTwitterProducer {

	static boolean full = false;

	public static void main(String[] args) throws Exception {

		// Twitter API-Keys
		String consumerKey = "95fpArvBMcndakeAqtN9wXnCM";
		String consumerSecret = "fLP3WeqnlPqrsUVZaft6Y9SrTzh4fXRIc1xVDpebrVUQQIlbcU";
		String accessToken = "1084833813983834112-OcIcbM39dY9ePcyms2QhXEwAVtk80c";
		String accessTokenSecret = "ilNQLPHP1n8WQL9NEvSGl0GWTbQMbVIqZjd4PLlwir9Cz";

		// Defining Kafka topic
		String topicName = Configuration.topicName();

		// Define keywords to filter tweets
		String[] keyWords = Configuration.keyWords();

		// Connect to twitter using the API-Keys
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
				.setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret);

		// Add Kafka producer config settings
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

		// Instantiate producer object
		Producer<String, String> producer = new KafkaProducer<String, String>(props);

		// Question what keyword to use, tweet count and location format
		System.out.println("Define keyword to filter incoming tweets...");
		Scanner scanner = new Scanner(System.in);
		String keyword = scanner.nextLine();
		System.out.println("Location format: full or country?");
		String format = scanner.nextLine();

		full = false;

		switch (format) {
		case "full":
			full = true;
			break;
		case "country":
			full = false;
			break;
		default:
			full = false;
		}

		// Get Twitter-Stream and implement listener
		TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
		StatusListener listener = new StatusListener() {

			// Counter for counting processed tweets
			int counter = 0;

			// Gets called for every incoming tweet
			@Override
			public void onStatus(Status status) {

				// Check if current tweet has location information
				if (status.getPlace() != null) {
					counter++;
					System.out.println(
							"@" + status.getUser().getScreenName() + "; message: " + status.getText() + "; country: "
									+ status.getPlace().getCountry() + " city: " + status.getPlace().getFullName());

					// Extract message and location from tweet
					String message = status.getText();
					String country = status.getPlace().getCountry();
					String cityState = status.getPlace().getFullName();
					String location = null;

					if (full) {
						location = cityState + " - " + country;
					} else {
						location = status.getPlace().getCountry();
					}

					// Send new record (message) to Kafka-Server
					producer.send(new ProducerRecord<String, String>(topicName, location, message));

					// Close producer and shutdown stream
					if (counter == Configuration.tweetCount()) {
						producer.close();
						twitterStream.shutdown();
					}
				}
			}

			@Override
			public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
				// System.out.println("Got a status deletion notice id:" +
				// statusDeletionNotice.getStatusId());
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
				// System.out.println("Got track limitation notice:" +
				// numberOfLimitedStatuses);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
				// System.out.println("Got scrub_geo event userId:" + userId +
				// "upToStatusId:" + upToStatusId);
			}

			@Override
			public void onStallWarning(StallWarning warning) {
				// System.out.println("Got stall warning:" + warning);
			}

			@Override
			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};
		// Add implemented listener to opened stream
		twitterStream.addListener(listener);

		// Create filter for defined keywords
		FilterQuery query = new FilterQuery().track(keyword);
		twitterStream.filter(query);
	}
}
