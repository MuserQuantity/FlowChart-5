package gui;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import model.Flow;
import model.Server;
import model.Session;
import shell.Access;

public class QueryProgress {

	JFrame frame;
	JPanel contentPane;
	JProgressBar progressBar;
	Task task;
	private String pw;
	private boolean isRefresh;

	public static int progress = 0;
	int serverSteps = 0;

	public void startTask() {
		Task task = new Task();
		task.addPropertyChangeListener(new ProgressBarListener());
		task.execute();
	}

	public void setupQueryGUI() {
		frame = new JFrame("Fetching Data");
		contentPane = (JPanel) frame.getContentPane();

		// Count total steps (enabled servers)
		for (Flow f : Session.session) {
			for (Server s : f.getServerList()) {
				if (s.isEnabled())
					serverSteps++;
			}
		}

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
			setProgress(0);
			// Do Flow queries in background
			for (Flow f : Session.session) {
				if (f.isEnabled()) {
					Access a = new Access(f, Session.ssoID, pw);
					a.startConnectionRoutine();
				}
				setProgress((100 / serverSteps) * progress);
			}
			progress = 0;
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
