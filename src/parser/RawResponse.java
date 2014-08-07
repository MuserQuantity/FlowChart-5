package parser;

import gui.Login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import model.Flow;
import model.Session;

public class RawResponse {

	JFrame frame;
	JSplitPane splitPane;
	JPanel contentPane;
	JPanel rightPane;
	JPanel buttonPanel;

	JList<Flow> flowList;
	DefaultListModel<Flow> flowListModel;
	JScrollPane flowListScrollPane;

	JTextArea responseArea;
	JScrollPane responseScrollPane;

	JButton refreshButton;
	JButton exitButton;

	int dividerLocation;

	public RawResponse(LinkedList<Flow> session) {
		frame = new JFrame("Flow Raw Responses");
		contentPane = (JPanel) frame.getContentPane();

		// Left Flow list setup
		flowListModel = new DefaultListModel<Flow>();
		for (Flow f : session)
			flowListModel.addElement(f);
		flowList = new JList<Flow>(flowListModel);
		flowList.setVisibleRowCount(200);
		flowListScrollPane = new JScrollPane(flowList);
		flowListScrollPane.setBorder(BorderFactory.createTitledBorder("Flow List"));
		flowList.setCellRenderer(new DisabledFlowCellRenderer());
		flowList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 1) {
					int index = ((JList<?>) evt.getSource()).locationToIndex(evt.getPoint());
					if (index >= 0) {
						updateResponsePane(index);
					}
				}
			}
		});

		// Right response + button setup
		rightPane = new JPanel();
		rightPane.setLayout(new BorderLayout());
		responseArea = new JTextArea();
		responseScrollPane = new JScrollPane(responseArea);
		responseScrollPane.setBorder(BorderFactory.createTitledBorder("Flow Responses"));
		responseArea.setEditable(false);
		responseArea.setFont(new Font("Consolas", Font.PLAIN, 12));
		responseArea.setLineWrap(false);

		// Button panel setup
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
		refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Requery enabled Flows and refresh flowListModel
				Session.querySession(Login.getPWString(), true);
				int prevPos = flowList.getSelectedIndex();
				flowListModel.clear();
				for (Flow f : Session.session)
					flowListModel.addElement(f);
				flowList.setSelectedIndex(prevPos);
				updateResponsePane(prevPos);
			}
		});
		buttonPanel.add(refreshButton);
		exitButton = new JButton("Exit");
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		});
		buttonPanel.add(exitButton);
		rightPane.add(responseScrollPane, BorderLayout.CENTER);
		rightPane.add(buttonPanel, BorderLayout.PAGE_END);

		// Splitpane setup
		dividerLocation = 200;
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerLocation(dividerLocation);
		splitPane.setEnabled(true);
		splitPane.setDividerSize(2);

		splitPane.add(flowListScrollPane);
		splitPane.add(rightPane);

		// GUI setup
		contentPane.add(splitPane);
		contentPane.setPreferredSize(new Dimension(1000, 800));
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		for (int i = 0; i < session.size(); i++) {
			if (session.get(i).isEnabled()) {
				flowList.setSelectedIndex(i);
				updateResponsePane(i);
				break;
			}
		}
	}

	void updateResponsePane(int index) {
		responseArea.setText(flowListModel.getElementAt(index).collateResponses());
		responseArea.setCaretPosition(0);
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
