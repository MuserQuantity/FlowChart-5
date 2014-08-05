package shell;

import model.Flow;
import model.Server;

public class Access {

	Flow flow;
	private String username;
	private String password;

	public Access(Flow f, String ssoid, String pw) {
		flow = f;
		username = ssoid;
		password = pw;
	}

	public void startConnectionRoutine() {

		// For each Server in this Flow, start a new ServerShell query
		for (Server s : flow.getServerList()) {
			try {

				new ServerShell(s, username, password).query();

			} catch (Exception e) {
				e.printStackTrace();
				// TODO logger
			}
		}

	}

}
