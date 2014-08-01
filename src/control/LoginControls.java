package control;

import gui.FlowManager;
import gui.Login;

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Session;
import nu.xom.Serializer;
import xml.Converter;
import xml.Persist;

public class LoginControls {

	public static FlowManager flowManagerWindow;

	public static void runButton(String pw) {
		Session.querySession(pw);
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
		// Prompt user path to save new session XML file
		@SuppressWarnings("serial")
		JFileChooser fc = new JFileChooser() {
			@Override
			public void approveSelection() {
				File f = getSelectedFile();
				if (f.exists() && getDialogType() == SAVE_DIALOG) {
					int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
					switch (result) {
					case JOptionPane.YES_OPTION:
						super.approveSelection();
						return;
					case JOptionPane.NO_OPTION:
						return;
					case JOptionPane.CLOSED_OPTION:
						return;
					case JOptionPane.CANCEL_OPTION:
						cancelSelection();
						return;
					}
				}
				super.approveSelection();
			}
		};
		fc.setFileFilter(new FileNameExtensionFilter("Session XML File", "xml"));
		fc.setDialogTitle("Export current session to XML file");
		int returnVal = fc.showSaveDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String savePath = fc.getSelectedFile().getCanonicalPath();
			if (!savePath.substring(savePath.length() - 4).equalsIgnoreCase(".xml"))
				savePath += ".xml";
			try {
				File xmlFile = new File(savePath);
				FileOutputStream fos = new FileOutputStream(xmlFile);
				Serializer s = new Serializer(fos, "ISO-8859-1");
				s.setIndent(4);
				s.setMaxLength(500);
				s.write(Persist.sessionToXMLDoc(Session.session));
				fos.close();

			} catch (Exception e) {
				// TODO logger
				e.printStackTrace();
			}
		} else {
			// Cancel export Session xml
			return;
		}
	}
}
