package control;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

import xml.Persist;

public class BootupControls {

	public static void loadXMLButtonAction(JFrame bootup) throws Exception {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Session XML File", "xml"));
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String scriptFilePath = fc.getSelectedFile().getCanonicalPath();

			if (!Persist.startupXMLRoutine(new File(scriptFilePath)))
				return;

			// Close bootup window when finished loading
			bootup.dispose();

			//
		} else {
			// Cancel load
			return;
		}
	}

	public static void newSessionButtonAction(JFrame bootup) throws Exception {

		bootup.dispose();
	}
}
