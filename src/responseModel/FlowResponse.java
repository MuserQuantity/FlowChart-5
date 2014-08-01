package responseModel;

import java.util.ArrayList;

public class FlowResponse {

	String label;
	ArrayList<ServerResponse> responses;

	public FlowResponse(String flowName) {
		label = flowName;
		responses = new ArrayList<ServerResponse>();
	}

	public String aggregateResponse() {
		StringBuilder sb = new StringBuilder();
		for (ServerResponse sr : responses) {
			sb.append(sr.aggregateResponse());
			sb.append('\n');
		}
		return sb.toString();
	}
}
