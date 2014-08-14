package gui;

import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import model.Flow;
import model.Session;
import shell.Access;

public class QueryProgress {

	JFrame frame;
	JPanel contentPane;
	JProgressBar progressBar;
	Task task;
	private String pw;
	private boolean isRefresh;

	public void startTask() {
		Task task = new Task();
		// task.addPropertyChangeListener(new ProgressBarListener());
		task.execute();
	}

	public void setupQueryGUI() {
		frame = new JFrame("Fetching Data");
		contentPane = (JPanel) frame.getContentPane();

		// Progress Bar setup
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);

		// Arrange UI
		contentPane.add(progressBar);
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Frame GUI setup
		frame.setSize(230, 60);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setLocationRelativeTo(null);
	}

	public QueryProgress(String pw, boolean isRefresh) {
		this.pw = pw;
		this.isRefresh = isRefresh;
		setupQueryGUI();
		startTask();
	}

	class Task extends SwingWorker<Void, Void> {
		// Main task. Executed in background thread.
		@Override
		protected Void doInBackground() throws Exception {
			// Do Flow queries in background
			for (Flow f : Session.session) {
				if (f.isEnabled()) {
					Access a = new Access(f, Session.ssoID, pw);
					a.startConnectionRoutine();
				}
			}
			return null;
		}

		// Executed in event dispatching thread
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			frame.dispose();

			/*
			 * After background query is finished, run abstract solution
			 */
			Session.doAbstract(isRefresh);
		}
	}

	// class ProgressBarListener implements PropertyChangeListener {
	// // Invoked when tasks' progress property changes
	// @Override
	// public void propertyChange(PropertyChangeEvent evt) {
	// if (evt.getPropertyName().equals("progress")) {
	// progressBar.setValue((Integer) evt.getNewValue());
	// }
	// }
	// }
}
