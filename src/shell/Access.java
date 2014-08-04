package shell;

import model.Flow;

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
	}

}
