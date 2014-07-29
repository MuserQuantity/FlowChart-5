package gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import log.Alerts;
import model.CmdScript;
import model.Flow;
import model.Server;
import model.Session;

public class FlowManager {

	JFrame frame;
	JPanel contentPane;

	JSplitPane splitPane;
	DefaultTreeModel flowTreeModel;
	JTree flowTree;
	JScrollPane treeScrollPane;

	JPanel flowEditor; // Displayed when ssoid "root" is focused
	JPanel serverEditor; // Displayed when flow is focused
	JPanel cmdScriptEditor; // Displayed when server is focused
	JPanel cmdScriptViewer; // Displayed when cmdScript is focused
	JPanel buttonPanel; // Save and exit button panel
	JPanel specialScriptButtonPanel;

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
	JEditorPane csViewer;

	// Button Panel elements
	JButton addScriptButton;
	JButton saveButton;
	JButton saveButton2;
	JButton exitButton;
	JButton exitButton2;

	// Boolean switch for non committed user changes
	boolean hasChanged;

	/*
	 * Panel switch toggle values. 0) flow editor 1) server editor 2) cmdScript
	 * editor 3) cmdScript viewer
	 */
	int panelNum;
	int dividerLocation;

	public FlowManager() {

		panelNum = 0;
		frame = new JFrame("Flow Manager");
		contentPane = (JPanel) frame.getContentPane();

		// Dynamic editor panes (right pane)
		// Button Panel for save and exit
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		saveButton = new JButton("Save Changes");
		buttonPanel.add(saveButton);
		saveButton.setEnabled(false);
		exitButton = new JButton("Exit Flow Manager");
		buttonPanel.add(exitButton);

		// Button Panel specifically for importing script files
		saveButton2 = new JButton("Save Changes");
		exitButton2 = new JButton("Exit Flow Manager");
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
							hasChanged = true;
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
		specialScriptButtonPanel.add(saveButton2);
		saveButton.setEnabled(false);
		specialScriptButtonPanel.add(exitButton2);

		// Flow Editor
		flowEditor = new JPanel();
		flowEditor.setLayout(new BoxLayout(flowEditor, BoxLayout.Y_AXIS));
		flowList = new JList<Flow>(Session.flowListModel);
		flowList.setVisibleRowCount(100);
		flowListScrollPane = new JScrollPane(flowList);
		flowListScrollPane.setBorder(BorderFactory.createTitledBorder("Flow List"));
		flowList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				// Deleting flows
				if (evt.getClickCount() == 2) {
					int index = ((JList<?>) evt.getSource()).locationToIndex(evt.getPoint());
					if (index >= 0) {
						// Left pane tree remove
						flowTreeModel.removeNodeFromParent((DefaultMutableTreeNode) flowTreeModel.getChild(Session.root, index));
						// Memory remove
						if (!Session.removeFlow((Flow) Session.flowListModel.get(index))) {
							// TODO logger
						}
						// Right pane list remove
						Session.flowListModel.removeElementAt(index);

						hasChanged = true;
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
							hasChanged = true;
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
						TreePath path = flowTree.getSelectionPath();
						// Left pane tree remove
						flowTreeModel.removeNodeFromParent((DefaultMutableTreeNode) flowTreeModel.getChild(path.getLastPathComponent(), index));
						// Memory remove
						if (!Session.removeServer(path, (Server) serverListModel.get(index))) {
							// TODO logger
							System.out.println("problem");
						}
						// Right pane list remove
						serverListModel.remove(index);

						hasChanged = true;
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
							hasChanged = true;
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
						TreePath path = flowTree.getSelectionPath();
						// Left pane tree remove
						flowTreeModel.removeNodeFromParent((DefaultMutableTreeNode) flowTreeModel.getChild(path.getLastPathComponent(), index));
						// Memory remove
						if (!Session.removeCS(path, (CmdScript) csListModel.get(index))) {
							// TODO logger
							System.out.println("problem");
						}
						// Right pane list remove
						csListModel.remove(index);

						hasChanged = true;
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
							hasChanged = true;
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
		cmdScriptViewer.setLayout(new BoxLayout(cmdScriptViewer, BoxLayout.Y_AXIS));
		csViewer = new JEditorPane();
		csViewer.setBorder(BorderFactory.createTitledBorder("Command/Script Content Viewer"));
		csViewer.setEditable(false);
		csViewer.setFont(new Font("Consolas", Font.PLAIN, 12));
		cmdScriptViewer.add(csViewer);

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
		for (int i = 0; i < flowTree.getRowCount(); i++)
			flowTree.expandRow(i);

		// Split Pane setup
		this.dividerLocation = 500;
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
			// csList.setCellRenderer(new ScriptBlueText());
			splitPane.add(cmdScriptEditor);
		} else if (panNum == 3) { // Switch to cmd script viewer
			splitPane.remove(2);
			cmdScriptViewer.add(buttonPanel);
			splitPane.add(cmdScriptViewer);
		} else {
			System.err.println("Shouldn't happen. See logs.");
			// TODO logger
		}
		splitPane.setDividerLocation(dividerLocation);
		contentPane.revalidate();
		contentPane.repaint();
	}

	@SuppressWarnings("serial")
	class ScriptBlueText extends JLabel implements ListCellRenderer<Object> {
		public ScriptBlueText() {
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<?> arg0, Object arg1, int arg2, boolean arg3, boolean arg4) {
			setText(arg1.toString());
			if (!((CmdScript) arg1).isCmd()) {
				setForeground(Color.BLUE);
				setBackground(Color.WHITE);
			} else {
				setForeground(Color.BLACK);
				setBackground(Color.WHITE);
			}
			return this;
		}
	}
}