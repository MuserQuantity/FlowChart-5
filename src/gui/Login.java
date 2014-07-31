package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import model.Flow;
import model.Session;
import control.LoginControls;

public class Login {

	JFrame frame;
	JPanel overPanel;
	JPanel saveFieldPanel;

	JTextField usernameField;
	JPasswordField passwordField;

	JCheckBox saveSession;
	boolean saveSessionBool;

	static JList<Flow> flowList;

	static JButton flowManagerButton;
	static JButton runButton;
	JButton exitButton;

	public Login(boolean savedSession) {
		frame = new JFrame("Login");
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
		flowList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				int index = ((JList<?>) evt.getSource()).locationToIndex(evt.getPoint());
				if (evt.getClickCount() == 2) {
					if (index >= 0) {
						// Disable/Enable Flow when double clicked
						Session.session.get(index).setEnabled(!Session.session.get(index).isEnabled());
						flowList.setCellRenderer(new DisabledFlowCellRenderer());
						toggleRunButton();
					}
				}
			}
		});
		flowList.setCellRenderer(new DisabledFlowCellRenderer());

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
		toggleRunButton();

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

		frame.add(overPanel);
		frame.setSize(290, 380);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		// this.setLocationRelativeTo(null);
	}

	public static void toggleFlowManagerButton() {
		flowManagerButton.setEnabled(!flowManagerButton.isEnabled());
	}

	public static void toggleRunButton() {
		// Disable if Flow Manager Window is open/active
		if (LoginControls.flowManagerWindow != null && LoginControls.flowManagerWindow.frame.isVisible()) {
			runButton.setEnabled(false);
		} else if (Session.session.isEmpty()) {
			// Disable if there are no Flows
			runButton.setEnabled(false);
		} else {
			int activeFlows = 0;
			for (Flow f : Session.session) {
				if (f.isEnabled())
					activeFlows++;
			}
			// Enable if there are active Flows
			if (activeFlows > 0) {
				// And only if those active Flows have "full" paths
				if (Session.existsFullPath())
					runButton.setEnabled(true);
				else
					runButton.setEnabled(false);
			} else
				// Disable if there are no active Flows
				runButton.setEnabled(false);
		}
	}

	public static void toggleFlowListSelectable() {
		flowList.setEnabled(!flowList.isEnabled());
	}

	@SuppressWarnings("serial")
	class DisabledFlowCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (!((Flow) value).isEnabled()) {
				setForeground(Color.ORANGE);
			}
			return this;
		}
	}
}
