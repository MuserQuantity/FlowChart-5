package control;

import gui.Login;

import javax.swing.DefaultListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import model.CmdScript;
import model.Flow;
import model.Server;
import model.Session;
import xml.Persist;

public class FlowManagerControls {

	public static void saveExitButtonAction() {
		Persist.sessionXMLSave(Session.session);
		LoginControls.flowManagerWindow.frame.setVisible(false);
		Login.toggleFlowManagerButton();
		Login.toggleRunButton();
		Login.toggleFlowListSelectable();
		Login.togglePaswordField();
		Login.toggleUsernameField();
		Login.frame.toFront();
		Login.frame.repaint();
	}

	public static void saveCmdChangesAction(String newCmd, DefaultTreeModel flowTreeModel, TreePath path) {
		// Memory alter
		if (!Session.editCommand(path, newCmd)) {
			// TODO logger
		} else {
			// Left pane tree alter
			CmdScript newCS = (CmdScript) ((DefaultMutableTreeNode) path.getPathComponent(3)).getUserObject();
			newCS.setData(newCmd);
			flowTreeModel.valueForPathChanged(path, newCS);
		}
	}

	public static void deleteFlowDoubleClick(int index, DefaultTreeModel flowTreeModel) {
		// Memory remove
		if (!Session.removeFlow((Flow) Session.flowListModel.get(index))) {
			// TODO logger
		} else {
			// Left pane tree remove
			flowTreeModel.removeNodeFromParent((DefaultMutableTreeNode) flowTreeModel.getChild(Session.root, index));

			// Right pane list remove
			Session.flowListModel.removeElementAt(index);
		}

	}

	public static void deleteServerDoubleClick(int index, DefaultTreeModel flowTreeModel, DefaultListModel<Server> serverListModel, TreePath path) {
		// Memory remove
		if (!Session.removeServer(path, (Server) serverListModel.get(index))) {
			// TODO logger
		} else {
			// Left pane tree remove
			flowTreeModel.removeNodeFromParent((DefaultMutableTreeNode) flowTreeModel.getChild(path.getLastPathComponent(), index));

			// Right pane list remove
			serverListModel.remove(index);
		}

	}

	public static void deleteCmdScriptDoubleClick(int index, DefaultTreeModel flowTreeModel, DefaultListModel<CmdScript> csListModel, TreePath path) {
		// Memory remove
		if (!Session.removeCS(path, (CmdScript) csListModel.get(index))) {
			// TODO logger
		} else {
			// Left pane tree remove
			flowTreeModel.removeNodeFromParent((DefaultMutableTreeNode) flowTreeModel.getChild(path.getLastPathComponent(), index));

			// Right pane list remove
			csListModel.remove(index);
		}

	}
}
