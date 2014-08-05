package model;

public class CmdScript {

	Boolean isCmd;
	String data;
	boolean enabled;
	String response;

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public CmdScript(boolean isCommand, String d) {
		this.isCmd = isCommand;
		this.data = d;
		this.enabled = true;
		this.response = "";
	}

	@Override
	public String toString() {
		if (isCmd)
			return "[CMD] " + data;
		else
			return data;
	}

	public Boolean isCmd() {
		return isCmd;
	}

	public void setCmd(boolean isCmd) {
		this.isCmd = isCmd;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
