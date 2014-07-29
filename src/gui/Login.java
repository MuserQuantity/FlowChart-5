package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;

import model.Flow;
import model.Session;
import control.LoginControls;

@SuppressWarnings("serial")
public class Login extends JFrame {

	JPanel overPanel;
	JPanel saveFieldPanel;

	JTextField usernameField;
	JPasswordField passwordField;

	JCheckBox saveSession;
	boolean saveSessionBool;

	JList<Flow> flowList;

	static JButton flowManagerButton;
	static JButton runButton;
	JButton exitButton;

	public Login(boolean savedSession) {
		super("Login");
		overPanel = new JPanel();
		overPanel.setLayout(new BoxLayout(overPanel, BoxLayout.Y_AXIS));

		// Username, SSOID
		usernameField = new JTextField();
		Border usernameBorder = BorderFactory.createTitledBorder("Username");
		usernameField.setBorder(usernameBorder);

		// Password (UNIX boxes)
		passwordField = new JPasswordField();
		Border passwordBorder = BorderFactory.createTitledBorder("Password");
		passwordField.setBorder(passwordBorder);
		passwordField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Pressing enter runs query session
				runButton.doClick();
			}
		});

		// Save session/credentials checkbox and logic
		saveSessionBool = savedSession;
		saveFieldPanel = new JPanel();
		saveFieldPanel.setLayout(new BoxLayout(saveFieldPanel, BoxLayout.X_AXIS));
		saveSession = new JCheckBox("Save session");
		if (saveSessionBool) {
			saveSession.setSelected(true);
		} else
			saveSession.setSelected(false);
		saveFieldPanel.add(saveSession);
		saveSession.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getSource() == saveSession) {
					if (saveSession.isSelected()) {
						saveSessionBool = true;
					} else {
						saveSessionBool = false;
					}
				}
			}
		});
		saveFieldPanel.add(new JLabel("                                                          "));
		if (saveSessionBool)
			usernameField.setText(Session.ssoID);

		// Flow list
		flowList = new JList<Flow>(Session.flowListModel);
		flowList.setVisibleRowCount(12);
		JScrollPane listPane = new JScrollPane(flowList);
		Border listPaneBorder = BorderFactory.createTitledBorder("Flow List");
		listPane.setBorder(listPaneBorder);

		// New Flow Button
		flowManagerButton = new JButton("Flow Manager");
		flowManagerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoginControls.enterFlowManager();
			}
		});

		// Run button
		runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoginControls.runButton();
			}
		});

		// Save and exit button
		exitButton = new JButton("Save and Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoginControls.saveAndExitButtonAction(saveSessionBool);
			}
		});

		// GUI layout
		overPanel.add(usernameField);
		overPanel.add(passwordField);
		overPanel.add(saveFieldPanel);
		overPanel.add(listPane);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(runButton);
		buttonPanel.add(flowManagerButton);
		buttonPanel.add(exitButton);
		overPanel.add(buttonPanel);

		this.add(overPanel);
		this.setSize(290, 380);
		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// this.setLocationRelativeTo(null);
	}

	public static void toggleFlowManagerButton() {
		flowManagerButton.setEnabled(!flowManagerButton.isEnabled());
	}

	public static void toggleRunButton() {
		runButton.setEnabled(!runButton.isEnabled());
	}
}
