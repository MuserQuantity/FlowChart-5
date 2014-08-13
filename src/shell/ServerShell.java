package shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import log.Logger;
import model.CmdScript;
import model.Server;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.InteractiveCallback;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class ServerShell {

	private Server server;
	private String username;
	private static String password;

	private Connection connection;

	public ServerShell(Server s, String u, String p) {
		this.server = s;
		this.username = u;
		password = p;
	}

	public boolean query() throws Exception {

		if (connect()) {

			// For each CmdScript in this Server
			for (CmdScript cs : server.getCmdScriptList()) {
				// Only run connection on it if CmdScript is enabled
				if (cs.isEnabled()) {
					// If CMD
					if (cs.isCmd()) {
						cs.setResponse(executeCommand(cs.getData()));
						Logger.log("User: " + username + " has CMD queried server hostname: " + server.getServerName());
					} else { // If Script file
						cs.setResponse(executeScript(new File(cs.getData())));
						Logger.log("User: " + username + " has SCRIPT queried server hostname: " + server.getServerName());
					}
				}
			}
			logout();
			return true;
		}
		return false;
	}

	public String executeScript(File script) throws Exception {
		// Send script file over to /tmp/ directory
		ScriptShell.sftpScriptFile(server, username, password, script);

		// Convert ^M endline carriages to UNIX readable endline
		// Generate random ID to tag onto end of translated script file
		String randomID = String.valueOf(randInt(1000, 9999));

		// Convert original script file name to name+randomID
		executeCommand("mv /tmp/" + script.getName() + " /tmp/" + script.getName() + randomID);

		// Convert randomID file to UNIX-good file w/ original filename
		executeCommand("tr -d '\r' < /tmp/" + script.getName() + randomID + " > /tmp/" + script.getName());

		// Give script executable permissions
		executeCommand("chmod +x /tmp/" + script.getName());

		// Execute new script file and extract response
		String scriptResponse = executeCommand("./tmp/" + script.getName());

		// Clean up both files
		executeCommand("rm /tmp/" + script.getName());
		executeCommand("rm /tmp/" + script.getName() + randomID);

		return scriptResponse;
	}

	public String executeCommand(String command) throws Exception {
		// Open a session
		Session session = connection.openSession();

		// Execute the command
		session.execCommand(command);

		// Initialize output and error readers
		StringBuilder sb = new StringBuilder();
		InputStream stdout = new StreamGobbler(session.getStdout());
		BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
		InputStream stderr = new StreamGobbler(session.getStderr());
		BufferedReader brErr = new BufferedReader(new InputStreamReader(stderr));

		// Read stdOut
		String line = br.readLine();
		while (line != null) {
			sb.append(line + "\n");
			line = br.readLine();
		}

		// Read error out
		String errLine = brErr.readLine();
		while (errLine != null) {
			errLine = brErr.readLine();
			Logger.log("* Error executing command: " + command + " on server hostname: " + server.getServerName() + " - " + errLine);
			return errLine;
		}

		Logger.log("Command execution Exit Code: " + session.getExitStatus());

		// Close the session
		session.close();
		br.close();
		brErr.close();

		// Return the results
		return sb.toString();
	}

	public boolean connect() throws IOException {
		// Connect to the serverhostname
		connection = new Connection(server.getServerName());
		connection.connect();

		// Using keyboard-interactive authentication method
		InteractiveLogic il = new InteractiveLogic();
		boolean result = connection.authenticateWithKeyboardInteractive(username, il);

		return result;
	}

	public void logout() {
		connection.close();
	}

	// Keyboard interactive logic
	static class InteractiveLogic implements InteractiveCallback {
		int promptCount = 0;

		public InteractiveLogic() {
		}

		/*
		 * the callback may be invoked several times, depending on how many
		 * questions-sets the server sends
		 */
		public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt, boolean[] echo) throws IOException {
			String[] result = new String[numPrompts];

			for (int i = 0; i < numPrompts; i++) {
				result[i] = password;
				promptCount++;
			}

			return result;
		}

		/*
		 * We maintain a prompt counter - this enables the detection of
		 * situations where the ssh server is signaling "authentication failed"
		 * even though it did not send a single prompt.
		 */
		public int getPromptCount() {
			return promptCount;
		}
	}

	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
}
