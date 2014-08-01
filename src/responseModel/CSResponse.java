package responseModel;

import java.util.ArrayList;

public class CSResponse {

	String data;
	boolean isCmd;
	ArrayList<String> response;

	public CSResponse(boolean isCommand, String d) {
		data = d;
		isCmd = isCommand;
		response = new ArrayList<String>();
	}

}
