package control;

import gui.Login;

import javax.swing.DefaultListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import log.Logger;
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
			Logger.log("Error saving text edit changes. Cannot apply: " + newCmd);
		} else {
			// Left pane tree alter
			CmdScript newCS = (CmdScript) ((DefaultMutableTreeNode) path.getPathComponent(3)).getUserObject();
			newCS.setData(newCmd);
			flowTreeModel.valueForPathChanged(path, newCS);
		}
	}

	public static void deleteFlowAction(int index, DefaultTreeModel flowTreeModel) {
		// Memory remove
		if (!Session.removeFlow((Flow) Session.flowListModel.get(index))) {
			Logger.log("Error removing Flow from memory: " + Session.flowListModel.get(index).getLabel());
		} else {
			// Left pane tree remove
			flowTreeModel.removeNodeFromParent((DefaultMutableTreeNode) flowTreeModel.getChild(Session.root, index));

			// Right pane list remove
			Session.flowListModel.removeElementAt(index);
		}

	}

	public static void deleteServerAction(int index, DefaultTreeModel flowTreeModel, DefaultListModel<Server> serverListModel, TreePath path) {
		// Memory remove
		if (!Session.removeServer(path, (Server) serverListModel.get(index))) {
			Logger.log("Error removing Server from memory: " + serverListModel.get(index).getServerName());
		} else {
			// Left pane tree remove
			flowTreeModel.removeNodeFromParent((DefaultMutableTreeNode) flowTreeModel.getChild(path.getLastPathComponent(), index));

			// Right pane list remove
			serverListModel.remove(index);
		}

	}

	public static void deleteCmdScriptAction(int index, DefaultTreeModel flowTreeModel, DefaultListModel<CmdScript> csListModel, TreePath path) {
		// Memory remove
		if (!Session.removeCS(path, (CmdScript) csListModel.get(index))) {
			Logger.log("Error removing command or script from memory: " + csListModel.get(index).getData());
		} else {
			// Left pane tree remove
			flowTreeModel.removeNodeFromParent((DefaultMutableTreeNode) flowTreeModel.getChild(path.getLastPathComponent(), index));

			// Right pane list remove
			csListModel.remove(index);
		}

	}
}
