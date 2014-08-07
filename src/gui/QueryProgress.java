package gui;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

public class QueryProgress {

	JFrame frame;
	JPanel contentPane;

	JProgressBar progressBar;

	public QueryProgress() {
		frame = new JFrame("Flow Query in Progress");
		contentPane = (JPanel) frame.getContentPane();

		// Progress Bar setup
		progressBar = new JProgressBar();
		progressBar.setValue(0);
		progressBar.setStringPainted(true);

		// Frame GUI setup
		frame.setSize(250, 200);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.setLocationRelativeTo(null);
	}

}
