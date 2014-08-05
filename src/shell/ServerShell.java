package shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

	public void query() throws Exception {

		if (connect()) {

			// For each CmdScript in this Server
			for (CmdScript cs : server.getCmdScriptList()) {
				// Only run connection on it if CmdScript is enabled
				if (cs.isEnabled()) {
					// If CMD
					if (cs.isCmd()) {
						cs.setResponse(executeCommand(cs.getData()));
					} else { // If Script file

					}
				}
			}
			logout();
		} else {
			// TODO logger
		}

	}

	public String executeCommand(String command) throws Exception {
		// Open a session
		Session session = connection.openSession();

		// Execute the command
		session.execCommand(command);

		// Read the results
		StringBuilder sb = new StringBuilder();
		InputStream stdout = new StreamGobbler(session.getStdout());
		BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
		String line = br.readLine();
		while (line != null) {
			sb.append(line + "\n");
			line = br.readLine();
		}

		// TODO logger - DEBUG: dump the exit code
		// System.out.println("ExitCode: " + session.getExitStatus());

		// Close the session
		session.close();
		br.close();

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
}
