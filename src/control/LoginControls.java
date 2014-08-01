package control;

import gui.FlowManager;
import gui.Login;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Session;
import xml.Converter;
import xml.Persist;

public class LoginControls {

	public static FlowManager flowManagerWindow;

	public static void runButton() {

	}

	public static void enterFlowManager(String ssoid) {
		Session.ssoID = ssoid;
		Session.root = Converter.sessionToTreeNode(Session.session);

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
	}

	public static void saveAndExitButtonAction() {
		Session.root = Converter.sessionToTreeNode(Session.session);
		Persist.sessionXMLSave(Session.session);
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
}
