package gui;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

public class QueryProgress {

	JFrame frame;
	JPanel contentPane;
	JProgressBar progressBar;
	Task task;

	public void startTask() {
		Task task = new Task();
		task.addPropertyChangeListener(new ProgressBarListener());
		contentPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		task.execute();
	}

	public void setupQueryGUI() {
		frame = new JFrame("Fetching Data");
		contentPane = (JPanel) frame.getContentPane();

		// Progress Bar setup
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		// Arrange UI
		contentPane.add(progressBar);
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Frame GUI setup
		frame.setSize(270, 70);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setLocationRelativeTo(null);

	}

	public QueryProgress() {
		setupQueryGUI();
		startTask();
	}

	class Task extends SwingWorker<Void, Integer> {
		// Main task. Executed in background thread.
		@Override
		protected Void doInBackground() throws Exception {
			Random random = new Random();
			int progress = 0;
			// Initialize progress property.
			setProgress(0);
			while (progress < 100) {
				System.out.println("Progress: " + progress);

				// Sleep for up to one second.
				try {
					Thread.sleep(random.nextInt(1000));
				} catch (InterruptedException ignore) {
				}
				// Make random progress.
				progress += random.nextInt(10);
				setProgress(Math.min(progress, 100));
			}
			return null;
		}

		// Executed in event dispatching thread
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			contentPane.setCursor(null);
			frame.dispose();

		}
	}

	class ProgressBarListener implements PropertyChangeListener {
		// Invoked when tasks' progress property changes
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "progress") {
				int progress = (Integer) evt.getNewValue();
				progressBar.setValue(progress);
			}
		}
	}
}
