package shell;

import java.io.File;
import java.io.FileInputStream;

import log.Logger;
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

		try {
			JSch jsch = new JSch();
			session = jsch.getSession(username, server.getServerName());
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();

			channel = session.openChannel("sftp");
			channel.connect();

			channelSftp = (ChannelSftp) channel;

			// Automatically cd move to /tmp/ directory
			channelSftp.cd("/tmp/");

			channelSftp.put(new FileInputStream(script), script.getName());

		} catch (Exception e) {
			e.printStackTrace();
			Logger.log("* Error SFTP script file: " + script.getName() + " to Server hostname: " + server.getServerName());
		} finally {
			channelSftp.exit();
			channel.disconnect();
			session.disconnect();
		}
	}
}
