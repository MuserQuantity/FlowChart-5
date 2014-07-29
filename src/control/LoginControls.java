package control;

import gui.FlowManager;
import gui.Login;

import java.io.File;

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
}
