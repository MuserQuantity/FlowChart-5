package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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

	public static JFrame frame;
	JPanel overPanel;

	JMenuBar menuBar;
	JMenu menu;
	JMenuItem exportSession;

	static JTextField usernameField;
	static JPasswordField passwordField;

	static JList<Flow> flowList;

	static JButton flowManagerButton;
	static JButton runButton;
	JButton exitButton;

	public Login(final boolean isOldSession) {
		frame = new JFrame("Login");
		frame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				if (isOldSession)
					passwordField.requestFocus();
				else
					frame.requestFocus();
			}
		});
		overPanel = new JPanel();
		overPanel.setLayout(new BoxLayout(overPanel, BoxLayout.Y_AXIS));

		// Export Session Menu
		menuBar = new JMenuBar();
		menu = new JMenu("Session");
		menu.setMnemonic(KeyEvent.VK_S);
		menu.getAccessibleContext().setAccessibleDescription("Export Session XML file");
		exportSession = new JMenuItem("Export");
		exportSession.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					LoginControls.exportSessionAction();
				} catch (Exception e) {
					e.printStackTrace();
					// TODO logger
				}
			}
		});
		menu.add(exportSession);
		menuBar.add(menu);

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
		passwordField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				toggleRunButton();
			}
		});

		// Username, SSOID
		usernameField = new JTextField();
		Border usernameBorder = BorderFactory.createTitledBorder("Username");
		usernameField.setBorder(usernameBorder);
		if (isOldSession)
			usernameField.setText(Session.ssoID);
		else
			usernameField.setText("Enter SSOID here to enter Flow Manager");
		usernameField.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (usernameField.getText().isEmpty()) {
					usernameField.getText().equals("Enter SSOID here to enter Flow Manager");
				} else
					flowManagerButton.setEnabled(true);

				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					flowManagerButton.doClick();
			}
		});
		usernameField.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent arg0) {
				if (usernameField.getText().equals("Enter SSOID here to enter Flow Manager"))
					usernameField.setText("");
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				if (usernameField.getText().isEmpty())
					usernameField.setText("Enter SSOID here to enter Flow Manager");
			}
		});
		usernameField.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if ((!Character.isAlphabetic(c)) && (!Character.isDigit(c)) && (c != KeyEvent.VK_BACK_SPACE)) {
					e.consume(); // ignore event
				}
			}
		});

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
				LoginControls.enterFlowManager(usernameField.getText());
			}
		});
		if (!isOldSession)
			flowManagerButton.setEnabled(false);

		// Run button
		runButton = new JButton("Run");
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoginControls.runButton(String.valueOf(passwordField.getPassword()));
			}
		});
		runButton.setEnabled(false);

		// Save and exit button
		exitButton = new JButton("Save and Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Maybe safe capture any inputted SSOID?
				Session.ssoID = usernameField.getText();
				LoginControls.saveAndExitButtonAction();
			}
		});

		// GUI layout
		overPanel.add(usernameField);
		overPanel.add(passwordField);
		overPanel.add(listPane);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(runButton);
		buttonPanel.add(flowManagerButton);
		buttonPanel.add(exitButton);
		overPanel.add(buttonPanel);

		frame.setJMenuBar(menuBar);
		frame.add(overPanel);
		frame.setSize(290, 380);
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
	}

	public static void toggleRunButton() {
		// Disable if Flow Manager Window is open/active
		if (LoginControls.flowManagerWindow != null && LoginControls.flowManagerWindow.frame.isVisible()) {
			runButton.setEnabled(false);
		} else if (Session.session.isEmpty()) {
			// Disable if there are no Flows in flowList
			runButton.setEnabled(false);
		} else {
			// Count number of active Flows
			int activeFlows = 0;
			for (Flow f : Session.session) {
				if (f.isEnabled())
					activeFlows++;
			}
			// If there are active Flows
			if (activeFlows > 0) {
				// And only if those active Flows have "full" paths
				if (Session.existsFullPath()) {
					// And only if passwordField is populated
					if (!new String(passwordField.getPassword()).isEmpty()) {
						runButton.setEnabled(true);
					} else {
						runButton.setEnabled(false);
					}
				} else {
					runButton.setEnabled(false);
				}
			} else {
				runButton.setEnabled(false);
			}
		}
	}

	public static void toggleFlowManagerButton() {
		flowManagerButton.setEnabled(!flowManagerButton.isEnabled());
	}

	public static void toggleFlowListSelectable() {
		flowList.setEnabled(!flowList.isEnabled());
	}

	public static void toggleUsernameField() {
		usernameField.setEnabled(!usernameField.isEnabled());
	}

	public static void togglePaswordField() {
		passwordField.setEnabled(!passwordField.isEnabled());
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

	public static String getPWString() {
		return new String(passwordField.getPassword());
	}
}
