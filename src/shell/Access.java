package shell;

import java.util.LinkedList;

import log.Alerts;
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

	public boolean startConnectionRoutine() {

		LinkedList<Server> accessDenyList = new LinkedList<Server>();

		// For each Server in this Flow, start a new ServerShell query
		for (Server s : flow.getServerList()) {
			try {
				if (new ServerShell(s, username, password).query()) {
					// TODO logger
					System.out.println("Access granted for Server " + s.getServerName() + " in Flow: " + flow.getLabel());
					s.setAuthenticated(true);
				} else {
					// TODO logger
					System.err.println("Access denied for Server " + s.getServerName() + " in Flow: " + flow.getLabel());
					s.setAuthenticated(false);
					accessDenyList.add(s);
				}
			} catch (Exception e) {
				e.printStackTrace();
				// TODO logger
			}
		}

		if (!accessDenyList.isEmpty()) {
			// Let user know of bad auth Server rejections
			StringBuilder sb = new StringBuilder();
			sb.append("Credentials failed to authenticate for server hostnames: ");
			for (int i = 0; i < accessDenyList.size(); i++) {
				if (i == accessDenyList.size() - 1)
					sb.append(accessDenyList.get(i).getServerName());
				else
					sb.append(accessDenyList.get(i).getServerName() + ", ");
				if (i % 7 == 0)
					sb.append("\n");
			}
			Alerts.infoBox(sb.toString() + "\nUNIX box passwords may have been scheduled for reset.", "Username/Password Failure in Flow: " + flow.getLabel());
			return false;
		}
		return true;
	}
}
