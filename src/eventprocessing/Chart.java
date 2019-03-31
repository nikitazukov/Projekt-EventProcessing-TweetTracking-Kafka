package eventprocessing;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class Chart extends JFrame {

	private static final long serialVersionUID = 1L;

	public Chart(String title) {
		super(title);

		// Create dataset
		CategoryDataset dataset = createDataset();

		// Create chart
		JFreeChart chart = ChartFactory.createBarChart("", // Chart Title
				"Classes of " + KafkaTwitterConsumer.desiredLocation, // Category
																		// axis
				"Sentiments in " + KafkaTwitterConsumer.desiredLocation, // Value
				// axis
				dataset, PlotOrientation.VERTICAL, true, true, false);

		ChartPanel panel = new ChartPanel(chart);
		setContentPane(panel);
	}

	private CategoryDataset createDataset() {
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		dataset.addValue(KafkaTwitterConsumer.getZero(), "Very Negative", "Very Negative");
		dataset.addValue(KafkaTwitterConsumer.getOne(), "Negative", "Negative");
		dataset.addValue(KafkaTwitterConsumer.getTwo(), "Neutral", "Neutral");
		dataset.addValue(KafkaTwitterConsumer.getThree(), "Positive", "Positive");
		dataset.addValue(KafkaTwitterConsumer.getFour(), "Very Positive", "Very Positive");

		return dataset;
	}
}