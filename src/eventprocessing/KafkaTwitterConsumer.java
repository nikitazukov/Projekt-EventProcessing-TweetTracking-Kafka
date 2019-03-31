package eventprocessing;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class KafkaTwitterConsumer {

	private static int zero, one, two, three, four;
	public static String desiredLocation;

	public static void main(String[] args) throws Exception {

		// Defining Kafka Topic
		String topicName = Configuration.topicName();

		// Kafka consumer configuration settings
		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", "test");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

		// Instantiate consumer object
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);

		// Kafka Consumer subscribes to defined topics
		consumer.subscribe(Arrays.asList(topicName));

		// Print the subscribed topics
		System.out.println("Subscribed to topic " + topicName);

		// Store sentiment score of every tweet for a location
		Multimap<String, Integer> sentimentScoresByLocations = ArrayListMultimap.create();

		// Initialize the NLP-Library for sentiment analysis
		NLP.init();

		// Counter for counting processed tweets
		int i = 0;

		while (i < Configuration.tweetCount()) {
			// Get available records (messages) from Kafka-Server
			ConsumerRecords<String, String> records = consumer.poll(10);

			// Iterate through all fetched records (messages)
			for (ConsumerRecord<String, String> record : records) {
				System.out.printf("Location = %s, Message = %s\n", record.key(), record.value());

				// Calculate sentiment score for the current record (message)
				int sentimentScore = NLP.findSentiment(record.value());
				sentimentScoresByLocations.put(record.key(), sentimentScore);

				// Print out the calculated sentiment class
				System.out.println("Sentiment: " + getSentimentClassByScore(sentimentScore));
				i++;
			}
		}
		System.out.println("\n\nCalculating avarage sentiment score by locations...\n");

		// Create HashMap to store the average score of every location
		HashMap<String, Double> resultMap = new HashMap<String, Double>();

		// Iterate through the previously saved locations and scores
		Set<String> keys = sentimentScoresByLocations.keySet();

		for (String key : keys) {
			double sentimentScore = 0;
			int counter = 0;

			System.out.println("\nKey = " + key);

			// Get all values of the current key
			Collection<Integer> values = sentimentScoresByLocations.get(key);

			// Iterate through the values and sum up the scores
			for (Integer value : values) {
				System.out.println("Value = " + value);
				sentimentScore = sentimentScore + value;
				counter++;
			}
			// Calculate and save the average score for a key (location)
			resultMap.put(key, (sentimentScore / counter));
		}
		// Iterate through the results and print them out
		Set<String> resultMapKeys = resultMap.keySet();

		for (String key : resultMapKeys) {
			System.out.println("\n\nLocation: " + key);
			System.out.println("Average-Sentiment-Score: " + resultMap.get(key));
			System.out.println("Rounded-Sentiment-Score: " + Math.round(resultMap.get(key)));
			System.out.println("Sentiment-Class: " + getSentimentClassByScore((int) Math.round(resultMap.get(key))));
		}
		// Check if a chart should be plotted
		System.out.println("\n\nDo you want to plot the chart of a given location? (y/n)");
		Scanner scanner = new Scanner(System.in);
		String decision = scanner.nextLine();
		boolean plotChart;

		switch (decision.toLowerCase()) {
		case "y":
			plotChart = true;
			break;
		case "n":
			plotChart = false;
			break;
		default:
			plotChart = false;
		}

		if (plotChart) {
			System.out.println("\n\nThese are all available locations: ");
			for (String key : keys) {
				System.out.println(key);
			}
			System.out.println("\n\nWhich location do you want to plot?");
			desiredLocation = scanner.nextLine();
			scanner.close();

			// Get all values of desired location
			Collection<Integer> values = sentimentScoresByLocations.get(desiredLocation);

			for (Integer value : values) {
				safeScore(value);
			}

			// Visualize scores of desired location as chart
			SwingUtilities.invokeAndWait(() -> {
				Chart example = new Chart(desiredLocation + " - Sentiment Visualisation");
				example.setSize(800, 400);
				example.setLocationRelativeTo(null);
				example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				example.setVisible(true);
			});
		}

		// Close the consumer connection and terminate application
		consumer.close();
	}

	// Method to transform score to the corresponding class
	private static String getSentimentClassByScore(int score) {
		switch (score) {
		case 0:
			return ("Very Negative");
		case 1:
			return ("Negative");
		case 2:
			return ("Neutral");
		case 3:
			return ("Positive");
		case 4:
			return ("Very Positive");
		default:
			return ("N/A");
		}
	}

	// Method to safe score for chart creation
	private static void safeScore(int sentimentScore) {
		switch (sentimentScore) {
		case 0:
			zero++;
			break;
		case 1:
			one++;
			break;
		case 2:
			two++;
			break;
		case 3:
			three++;
			break;
		case 4:
			four++;
			break;
		}
	}

	// Safe score getter to use it in Chart configuration
	public static int getZero() {
		return zero;
	}

	public static int getOne() {
		return one;
	}

	public static int getTwo() {
		return two;
	}

	public static int getThree() {
		return three;
	}

	public static int getFour() {
		return four;
	}
}