package control;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Session;
import xml.Persist;

public class BootupControls {

	public static void loadXMLButtonAction(JFrame bootup) throws Exception {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Session XML File", "xml"));
		fc.setDialogTitle("Select session XML file to load");
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String scriptFilePath = fc.getSelectedFile().getCanonicalPath();
			Persist.setSavePath(scriptFilePath);

			if (!Persist.startupXMLRoutine(new File(scriptFilePath)))
				return;

			// Close bootup window when finished loading prev session
			bootup.dispose();
		} else {
			// Cancel load
			return;
		}
	}

	public static void newSessionButtonAction(JFrame bootup) throws Exception {
		// Prompt user path to save new session XML file
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Session XML File", "xml"));
		fc.setDialogTitle("Save new session XML file");
		int returnVal = fc.showSaveDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String scriptFilePath = fc.getSelectedFile().getCanonicalPath();
			Persist.setSavePath(scriptFilePath);

			Session.startLogin(false);

			// Close bootup window when finished loading new session
			bootup.dispose();
		} else {
			// Cancel new session path selection
			return;
		}
	}
}
