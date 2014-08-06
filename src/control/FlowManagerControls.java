package control;

import gui.Login;
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

	public static void saveCmdChangesAction() {

	}
}
