package shell;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import model.CmdScript;
import model.Server;
import xml.Converter;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ServerShell {

	Server server;
	private String username;
	private String password;

	JSch jsch;
	Session session;
	Channel channel;

	ByteArrayOutputStream baos;
	PrintStream ps;

	File unixScript;
	FileInputStream fin;
	InputStream in;

	public ServerShell(Server s, String u, String p) {
		this.server = s;
		this.username = u;
		this.password = p;
	}

	public void query() throws IOException, JSchException {

		// Setup connection for this Server
		setupConnection();

		// For each CmdScript in this Server, retrieve responses
		for (CmdScript cs : server.getCmdScriptList()) {

			// Only run connection on it if it's enabled
			if (cs.isEnabled()) {
				// Create unixScript to be read by server
				if (cs.isCmd()) {
					unixScript = Converter.cmdToShellScript(cs.getData());
				} else {
					unixScript = new File(cs.getData());
				}

				// Prepare cmd/script for channel
				fin = new FileInputStream(unixScript);
				byte[] fileContent = new byte[(int) unixScript.length()];
				fin.read(fileContent);
				in = new ByteArrayInputStream(fileContent);

				// Issue to channel
				channel.setInputStream(in);
				channel.connect();

				// Capture response

			}

		}

	}

	boolean setupConnection() throws JSchException {
		jsch = new JSch();

		session = jsch.getSession(username, server.getServerName());
		session.setPassword(password);
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();

		channel = session.openChannel("shell");

		baos = new ByteArrayOutputStream();
		ps = new PrintStream(baos);
		channel.setOutputStream(ps);

		return true;
	}
}
