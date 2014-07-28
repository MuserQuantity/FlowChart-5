package log;

import javax.swing.JOptionPane;

public class Alerts {
	public static void infoBox(String infoMessage, String location) {
		JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + location, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void infoBox(String infoMessage) {
		JOptionPane.showMessageDialog(null, infoMessage, "", JOptionPane.INFORMATION_MESSAGE);
	}
}
