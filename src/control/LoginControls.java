package control;

import gui.FlowManager;

import java.io.File;

import model.Session;
import xml.Persist;

public class LoginControls {

	public static FlowManager flowManagerWindow;

	public static void newFlowButtonAction() {
		if (flowManagerWindow == null) {
			flowManagerWindow = new FlowManager();
		} else {

		}
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
