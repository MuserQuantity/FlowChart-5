package gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import control.BootupControls;

public class Bootup {

	JFrame frame;
	JPanel overPanel;
	JPanel buttonPanel;

	JLabel label;

	JButton loadXMLButton;
	JButton newSessionButton;

	public Bootup() {

		frame = new JFrame("Load Session");
		overPanel = new JPanel();
		overPanel.setLayout(new GridLayout(2, 1, 5, 5));
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));

		loadXMLButton = new JButton("Load Session XML");
		loadXMLButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					BootupControls.loadXMLButtonAction(frame);
				} catch (Exception e) {
					e.printStackTrace();
					// TODO logger
				}
			}
		});
		newSessionButton = new JButton("Start New Session");
		newSessionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					BootupControls.newSessionButtonAction(frame);
				} catch (Exception e) {
					e.printStackTrace();
					// TODO logger
				}
			}
		});

		label = new JLabel("Load existing XML Flow session?", SwingConstants.CENTER);

		buttonPanel.add(loadXMLButton);
		buttonPanel.add(newSessionButton);

		overPanel.add(label);
		overPanel.add(buttonPanel);

		frame.add(overPanel);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}
