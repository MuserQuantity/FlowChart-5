package model;

import java.util.LinkedList;

public class Flow {

	String label;
	LinkedList<Server> serverList;
	boolean enabled;

	public Flow(String l) {
		this.label = l;
		this.serverList = new LinkedList<Server>();
		this.enabled = true;
	}

	public String collateResponses() {
		StringBuilder sb = new StringBuilder();
		for (Server s : serverList) {
			sb.append(s.collateResponses() + '\n' + '\n');
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public LinkedList<Server> getServerList() {
		return serverList;
	}

	public void setServerList(LinkedList<Server> serverList) {
		this.serverList = serverList;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
