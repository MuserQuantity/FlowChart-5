package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import log.Alerts;
import model.CmdScript;
import model.Flow;
import model.Server;
import model.Session;
import control.FlowManagerControls;

public class FlowManager {

	public JFrame frame;
	JPanel contentPane;

	JSplitPane splitPane;
	DefaultTreeModel flowTreeModel;
	static JTree flowTree;
	JScrollPane treeScrollPane;

	JPanel flowEditor; // Displayed when ssoid "root" is focused
	JPanel serverEditor; // Displayed when flow is focused
	JPanel cmdScriptEditor; // Displayed when server is focused
	JPanel cmdScriptViewer; // Displayed when cmdScript is focused
	JPanel buttonPanel; // Save and exit button panel
	JPanel specialScriptButtonPanel;
	JPanel specialViewerButtonPanel;

	// Current working path of flow
	TreePath path;

	// Flow editor elements
	JList<Flow> flowList;
	JScrollPane flowListScrollPane;
	JTextField flowTextField;

	// Server editor elements
	DefaultListModel<Server> serverListModel;
	JList<Server> serverList;
	JScrollPane serverListScrollPane;
	JTextField serverTextField;

	// Cmd/Script editor elements
	DefaultListModel<CmdScript> csListModel;
	JList<CmdScript> csList;
	JScrollPane csListScrollPane;
	JTextField csTextField;

	// Cmd/Script viewer elements
	JScrollPane csViewerScrollPane;
	JTextArea csViewer;

	// Button Panel elements
	JButton saveCmdChangesButton;
	JButton addScriptButton;
	JButton exitButton;
	JButton exitButton2;
	JButton exitButton3;

	/*
	 * Panel switch toggle values. 0) flow editor 1) server editor 2) cmdScript
	 * editor 3) cmdScript viewer
	 */
	int panelNum;
	int dividerLocation;

	public FlowManager() {

		panelNum = 0;
		frame = new JFrame("Flow Manager");
		frame.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				flowTextField.requestFocus();
			}
		});
		contentPane = (JPanel) frame.getContentPane();

		// Dynamic editor panes (right pane)
		// Button Panel for save and exit
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		exitButton = new JButton("Save and Exit Flow Manager");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FlowManagerControls.saveExitButtonAction();
			}
		});
		buttonPanel.add(exitButton);

		// Button Panel specifically for importing script files
		exitButton2 = new JButton("Save and Exit Flow Manager");
		exitButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FlowManagerControls.saveExitButtonAction();
			}
		});
		addScriptButton = new JButton("+Script");
		addScriptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					csTextField.setText("");
					JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new FileNameExtensionFilter("Shell Script Files", "sh", "bash"));
					int returnVal = fc.showOpenDialog(fc);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						String scriptFilePath = fc.getSelectedFile().getCanonicalPath();
						CmdScript csToAdd = new CmdScript(false, scriptFilePath);
						TreePath path = flowTree.getSelectionPath();
						// Memory add
						if (!Session.addNewCmdScript(path, csToAdd)) {
							Alerts.infoBox("Script already exists for this Server.\nPress SPACE to retry.");
						} else {
							// Right pane list add
							csListModel.addElement(csToAdd);
							// Left pane tree add
							flowTreeModel.insertNodeInto(new DefaultMutableTreeNode(csToAdd), (DefaultMutableTreeNode) path.getLastPathComponent(),
									csListModel.size() - 1);

							treeScrollPane.getVerticalScrollBar().setValue(treeScrollPane.getVerticalScrollBar().getMaximum());
						}
					} else {
						// Cancel option
					}
				} catch (Exception ex) {
					// TODO logger
					ex.printStackTrace();
				}
			}
		});
		specialScriptButtonPanel = new JPanel();
		specialScriptButtonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		specialScriptButtonPanel.add(addScriptButton);
		specialScriptButtonPanel.add(exitButton2);

		// Button Panel specifically for saving/editing CMDs
		saveCmdChangesButton = new JButton("Saved");
		saveCmdChangesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FlowManagerControls.saveCmdChangesAction(csViewer.getText().trim(), flowTreeModel, flowTree.getSelectionPath());
				saveCmdChangesButton.setEnabled(false);
				saveCmdChangesButton.setText("Saved");
			}
		});
		exitButton3 = new JButton("Save and Exit Flow Manager");
		exitButton3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FlowManagerControls.saveExitButtonAction();
			}
		});
		specialViewerButtonPanel = new JPanel();
		specialViewerButtonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		specialViewerButtonPanel.add(saveCmdChangesButton);
		specialViewerButtonPanel.add(exitButton3);

		// Flow Editor
		flowEditor = new JPanel();
		flowEditor.setLayout(new BoxLayout(flowEditor, BoxLayout.Y_AXIS));
		flowList = new JList<Flow>(Session.flowListModel);
		flowList.setVisibleRowCount(100);
		flowListScrollPane = new JScrollPane(flowList);
		flowListScrollPane.setBorder(BorderFactory.createTitledBorder("Flow List"));
		flowList.setCellRenderer(new DisabledFlowCellRenderer());
		flowList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				// Deleting flows
				if (evt.getClickCount() == 2) {
					int index = ((JList<?>) evt.getSource()).locationToIndex(evt.getPoint());
					if (index >= 0) {
						FlowManagerControls.deleteFlowDoubleClick(index, flowTreeModel);
					}
				}
			}
		});
		flowTextField = new JTextField();
		flowTextField.setBorder(BorderFactory.createTitledBorder("New Flow"));
		flowTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, flowTextField.getPreferredSize().height));
		flowTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// Adding flows
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (!flowTextField.getText().equals("")) {
						Flow flowToAdd = new Flow(flowTextField.getText());
						// Memory add
						if (!Session.addNewFlow(flowToAdd)) {
							Alerts.infoBox("Flow with that label already exists.\nPress SPACE to retry.");
						} else {
							// Right pane list add
							Session.flowListModel.addElement(flowToAdd);
							// Left pane tree add
							flowTreeModel.insertNodeInto(new DefaultMutableTreeNode(flowToAdd), Session.root, Session.flowListModel.size() - 1);

							flowTextField.setText("");
							treeScrollPane.getVerticalScrollBar().setValue(treeScrollPane.getVerticalScrollBar().getMaximum());
							expandRowsInJTree();
						}

					}
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
		flowEditor.setBorder(BorderFactory.createTitledBorder("Flow Editor for " + Session.ssoID));
		flowEditor.add(flowListScrollPane);
		flowEditor.add(flowTextField);
		flowEditor.add(buttonPanel);

		// Server Editor
		serverEditor = new JPanel();
		serverEditor.setLayout(new BoxLayout(serverEditor, BoxLayout.Y_AXIS));

		serverListModel = new DefaultListModel<Server>();
		serverList = new JList<Server>(serverListModel);
		serverList.setVisibleRowCount(100);
		serverListScrollPane = new JScrollPane(serverList);
		serverListScrollPane.setBorder(BorderFactory.createTitledBorder("Server List"));
		serverList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					int index = ((JList<?>) evt.getSource()).locationToIndex(evt.getPoint());
					if (index >= 0) {
						FlowManagerControls.deleteServerDoubleClick(index, flowTreeModel, serverListModel, flowTree.getSelectionPath());
					}
				}
			}
		});
		serverTextField = new JTextField();
		serverTextField.setBorder(BorderFactory.createTitledBorder("New Server"));
		serverTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, serverTextField.getPreferredSize().height));
		serverTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (!serverTextField.getText().equals("")) {
						Server serverToAdd = new Server(serverTextField.getText());
						TreePath path = flowTree.getSelectionPath();

						// Memory add
						if (!Session.addNewServer(path, serverToAdd)) {
							Alerts.infoBox("Server already exists in this Flow.\nPress SPACE to retry.");
						} else {
							// Right pane list add
							serverListModel.addElement(serverToAdd);
							// Left pane tree add
							flowTreeModel.insertNodeInto(new DefaultMutableTreeNode(serverToAdd), (DefaultMutableTreeNode) path.getLastPathComponent(),
									serverListModel.size() - 1);

							serverTextField.setText("");
							expandRowsInJTree();
						}
					}
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
		serverEditor.add(serverListScrollPane);
		serverEditor.add(serverTextField);

		// Cmd/Script Editor
		cmdScriptEditor = new JPanel();
		cmdScriptEditor.setLayout(new BoxLayout(cmdScriptEditor, BoxLayout.Y_AXIS));

		csListModel = new DefaultListModel<CmdScript>();
		csList = new JList<CmdScript>(csListModel);
		csList.setVisibleRowCount(100);
		csListScrollPane = new JScrollPane(csList);
		csListScrollPane.setBorder(BorderFactory.createTitledBorder("Command/Script List"));
		csList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					int index = ((JList<?>) evt.getSource()).locationToIndex(evt.getPoint());
					if (index >= 0) {
						FlowManagerControls.deleteCmdScriptDoubleClick(index, flowTreeModel, csListModel, flowTree.getSelectionPath());
					}
				}
			}
		});
		csTextField = new JTextField();
		csTextField.setBorder(BorderFactory.createTitledBorder("New Command/Script"));
		csTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, csTextField.getPreferredSize().height));
		csTextField.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (!csTextField.getText().equals("")) {
						CmdScript csToAdd = new CmdScript(true, csTextField.getText());
						TreePath path = flowTree.getSelectionPath();
						// Memory add
						if (!Session.addNewCmdScript(path, csToAdd)) {
							Alerts.infoBox("CMD already exists for this Server.\nPress SPACE to retry.");
						} else {
							// Right pane list add
							csListModel.addElement(csToAdd);
							// Left pane tree add
							flowTreeModel.insertNodeInto(new DefaultMutableTreeNode(csToAdd), (DefaultMutableTreeNode) path.getLastPathComponent(),
									csListModel.size() - 1);

							csTextField.setText("");
							expandRowsInJTree();
						}
					}
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
		cmdScriptEditor.add(csListScrollPane);
		cmdScriptEditor.add(csTextField);

		// Cmd/Script Viewer
		cmdScriptViewer = new JPanel();
		cmdScriptViewer.setLayout(new BorderLayout());
		csViewer = new JTextArea();
		csViewerScrollPane = new JScrollPane(csViewer);
		csViewer.setEditable(false);
		csViewer.setFont(new Font("Consolas", Font.PLAIN, 12));
		csViewer.setLineWrap(false);
		csViewer.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				saveCmdChangesButton.setEnabled(true);
				saveCmdChangesButton.setText("Save Changes to CMD");
			}
		});
		cmdScriptViewer.add(csViewerScrollPane, BorderLayout.CENTER);

		// Flow tree setup (left Pane)
		flowTree = new JTree(Session.root);
		flowTreeModel = (DefaultTreeModel) flowTree.getModel();
		treeScrollPane = new JScrollPane();
		treeScrollPane.getViewport().add(flowTree);
		flowTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) flowTree.getLastSelectedPathComponent();
				if (node != null) {
					dividerLocation = splitPane.getDividerLocation();
					path = flowTree.getPathForLocation(e.getX(), e.getY());
					if (node.getUserObject() instanceof String) {
						panelNum = 0;
					} else if (node.getUserObject() instanceof Flow) {
						panelNum = 1;
					} else if (node.getUserObject() instanceof Server) {
						panelNum = 2;
					} else if (node.getUserObject() instanceof CmdScript) {
						panelNum = 3;
					} else {
						System.err.println("Shouldn't happen. See logs.");
						// TODO logger
					}
					if (path != null) {
						try {
							updateUI(panelNum, path);
						} catch (Exception ex) {
							// TODO logger
							ex.printStackTrace();
						}
					}
				}
			}
		});
		treeScrollPane.setBorder(BorderFactory.createTitledBorder("Floverview"));
		flowTree.setCellRenderer(new ScriptDisabledRedTreeRenderer());
		expandRowsInJTree();

		// Split Pane setup
		dividerLocation = 500;
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(dividerLocation);
		splitPane.setEnabled(true);
		splitPane.setDividerSize(2);

		splitPane.add(treeScrollPane);
		splitPane.add(flowEditor);

		// GUI Setup
		contentPane.add(splitPane);
		contentPane.setPreferredSize(new Dimension(1000, 800));
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	public void updateUI(int panNum, TreePath path) throws Exception {

		if (panNum == 0) { // Switch to flow editor
			splitPane.remove(2);
			flowEditor.add(buttonPanel);
			flowList = new JList<Flow>(Session.flowListModel);
			splitPane.add(flowEditor);
		} else if (panNum == 1) { // Switch to server editor
			splitPane.remove(2);
			serverEditor.add(buttonPanel);
			serverListModel = Session.getDLMofServers(path);
			serverList.setModel(serverListModel);
			serverEditor.setBorder(BorderFactory.createTitledBorder("Server Editor for Flow: " + path.getLastPathComponent()));
			splitPane.add(serverEditor);
		} else if (panNum == 2) { // Switch to cmd script editor
			splitPane.remove(2);
			cmdScriptEditor.add(specialScriptButtonPanel);
			csListModel = Session.getDLMofCmdScripts(path);
			csList.setModel(csListModel);
			cmdScriptEditor.setBorder(BorderFactory.createTitledBorder("CMD/Script Editor for Server: " + path.getLastPathComponent()));
			csList.setCellRenderer(new ScriptBlueDisabledRedTextRenderer());
			splitPane.add(cmdScriptEditor);
			// TODO enable when feature complete
			addScriptButton.setEnabled(false);
		} else if (panNum == 3) { // Switch to cmd script viewer
			splitPane.remove(2);
			// Different GUI logic depending on whether item is CMD/Script
			if (((CmdScript) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject()).isCmd()) {
				csViewer.setEditable(true);
				saveCmdChangesButton.setEnabled(false);
				saveCmdChangesButton.setText("Saved");
				cmdScriptViewer.setBorder(BorderFactory.createTitledBorder("Command Content Editor"));
			} else {
				saveCmdChangesButton.setEnabled(false);
				saveCmdChangesButton.setText("Edit not available for scripts");
				csViewer.setEditable(false);
				cmdScriptViewer.setBorder(BorderFactory.createTitledBorder("Script Content Viewer"));
			}
			cmdScriptViewer.add(specialViewerButtonPanel, BorderLayout.PAGE_END);
			csViewer.setText(Session.getCSData(path));
			csViewer.setCaretPosition(0);
			splitPane.add(cmdScriptViewer);
		} else {
			System.err.println("Shouldn't happen. See logs.");
			// TODO logger
		}
		splitPane.setDividerLocation(dividerLocation);
		contentPane.revalidate();
		contentPane.repaint();
	}

	static void expandRowsInJTree() {
		for (int i = 0; i < flowTree.getRowCount(); i++)
			flowTree.expandRow(i);
	}

	@SuppressWarnings("serial")
	class ScriptDisabledRedTreeRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if (leaf) {
				if (((DefaultMutableTreeNode) value).getUserObject() instanceof CmdScript) {
					CmdScript cs = (CmdScript) ((DefaultMutableTreeNode) value).getUserObject();
					if (!cs.isCmd()) {
						if (!cs.isEnabled()) {
							setForeground(Color.RED);
						} else {
							setForeground(Color.BLUE);
						}
					} else {
						setForeground(Color.BLACK);
					}
				}
			} else {
				if (((DefaultMutableTreeNode) value).getUserObject() instanceof Flow) {
					Flow f = (Flow) ((DefaultMutableTreeNode) value).getUserObject();
					if (!f.isEnabled()) {
						setForeground(Color.ORANGE);
					}
				}

			}
			return this;
		}
	}

	@SuppressWarnings("serial")
	class ScriptBlueDisabledRedTextRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (!((CmdScript) value).isCmd()) {
				if (!((CmdScript) value).isEnabled()) {
					setForeground(Color.RED);
				} else {
					setForeground(Color.BLUE);
				}
			} else {
				setForeground(Color.BLACK);
			}
			return this;
		}
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
