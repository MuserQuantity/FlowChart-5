package shell;

import model.Flow;
import model.Session;
import responseModel.FlowResponse;

public class Access implements Runnable {

	Flow flow;
	String password;

	public Access(Flow f, String pw) {
		flow = f;
		password = pw;
	}

	@Override
	public void run() {
	}

	public void startConnectionRoutine() {
		Session.responses.responseCollection.add(getFlowResponse());
	}

	public FlowResponse getFlowResponse() {
		FlowResponse fr = new FlowResponse(flow.getLabel());

		return fr;
	}
}
