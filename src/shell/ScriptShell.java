package shell;

import java.io.File;
import java.io.FileInputStream;

import model.Server;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class ScriptShell {

	public static void sftpScriptFile(Server server, String username, String password, File script) {
		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;

		System.out.println("preparing host info for sftp");

		try {
			JSch jsch = new JSch();
			session = jsch.getSession(username, server.getServerName());
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();

			System.out.println("Host connected.");

			channel = session.openChannel("sftp");
			channel.connect();

			System.out.println("sftp channel opened and connected.");

			channelSftp = (ChannelSftp) channel;
			channelSftp.cd("/tmp/");
			channelSftp.put(new FileInputStream(script), script.getName());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			channelSftp.exit();
			System.out.println("sftp channel exited");
			channel.disconnect();
			System.out.println("Channel disconnected.");
			session.disconnect();
			System.out.println("Host Session disconnected.");
		}
	}
}
