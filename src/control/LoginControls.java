package control;

import gui.FlowManager;
import gui.Login;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Session;
import xml.Persist;

public class LoginControls {

	public static FlowManager flowManagerWindow;

	public static void enterFlowManager() {
		if (flowManagerWindow == null)
			flowManagerWindow = new FlowManager();
		else if (!flowManagerWindow.frame.isVisible()) {
			flowManagerWindow.frame.setVisible(true);
		}
		Login.toggleFlowManagerButton();
		Login.toggleRunButton();
		Login.toggleFlowListSelectable();
		Login.togglePaswordField();
		Login.toggleUsernameField();
		Login.toggleSaveSessionBox();
	}

	public static void saveAndExitButtonAction(boolean saveSessionBool) {
		if (saveSessionBool) {
			Persist.sessionXMLSave(Session.session);
		} else {
			File fileToRemove = new File("session.xml");
			fileToRemove.delete();
		}
		System.exit(0);
	}

	public static void exportSessionAction() throws Exception {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Shell Script Files", "sh", "bash"));
		int returnVal = fc.showSaveDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String scriptFilePath = fc.getSelectedFile().getCanonicalPath();
			System.out.println(scriptFilePath);
		} else {
			// Cancel export
		}
	}

	public static void importSessionAction() throws Exception {

	}

	public static void runButton() {

	}
}
