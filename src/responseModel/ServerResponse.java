package responseModel;

import java.util.ArrayList;

public class ServerResponse {

	String serverName;
	ArrayList<CSResponse> responses;

	public ServerResponse(String sn) {
		serverName = sn;
		responses = new ArrayList<CSResponse>();
	}

	public String aggregateResponse() {
		StringBuilder sb = new StringBuilder();
		for (CSResponse csr : responses) {
			sb.append(csr.response);
			sb.append('\n');
		}
		return sb.toString();
	}
}
