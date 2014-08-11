package log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

public class Logger {

	static File logFile;
	static final String logFileString = "FlowChart.log";

	public static void log(String message) {
		try {
			StringBuffer logString = new StringBuffer();
			logFile = new File(logFileString);
			if (!logFile.exists())
				logFile.createNewFile();

			// Append current sys time and message to log
			logString.append(new Date(System.currentTimeMillis()).toString() + '\t' + message);

			// Append logString message to last line of log file
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFileString, true)));
			out.println(logString.toString());

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			Alerts.infoBox("Logging Error", "log.Logger.java");
		}
	}
}
