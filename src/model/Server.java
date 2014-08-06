package model;

import java.util.LinkedList;

public class Server {

	private String serverName;
	private LinkedList<CmdScript> cmdScriptList;
	private boolean enabled;
	private boolean authenticated;

	public Server(String sn) {
		this.serverName = sn;
		this.cmdScriptList = new LinkedList<CmdScript>();
		this.enabled = true;
		this.authenticated = true;
	}

	public String collateResponses() {
		StringBuilder sb = new StringBuilder();
		for (CmdScript cs : cmdScriptList) {
			sb.append(cs.getResponse() + '\n');
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return serverName;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public LinkedList<CmdScript> getCmdScriptList() {
		return cmdScriptList;
	}

	public void setCmdScriptList(LinkedList<CmdScript> cmdScriptList) {
		this.cmdScriptList = cmdScriptList;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
