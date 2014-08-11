package model;

import gui.Bootup;
import gui.Login;
import gui.QueryProgress;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import log.Logger;
import parser.RawResponse;

public class Session {

	public static String ssoID;
	public static LinkedList<Flow> session = new LinkedList<Flow>();
	public static DefaultListModel<Flow> flowListModel = new DefaultListModel<Flow>();
	public static DefaultMutableTreeNode root;
	public static Login loginWindow;

	/*
	 * Abstraction members and vars under here
	 */
	static RawResponse responseWindow = null;

	// Open source abstraction layer
	public static void doAbstract(boolean isRefresh) {
		/*
		 * At this point, session is finished compiling results. Open source
		 * abstraction can be done with session as seen fit.
		 */

		// Only open up a response window if sourced from Login
		if (!isRefresh)
			responseWindow = new RawResponse(session);
		else { // Just a refresh call, don't need to reopen response window
			responseWindow.refreshResponsePane();
		}
	}

	/*
	 * Application code starting here
	 */
	public static void main(String[] args) {
		try {
			// Request session XML load/new on boot
			new Bootup();
		} catch (Exception e) {
			e.printStackTrace();
			Logger.log("Error at bootup option select");
		}
	}

	public static void querySession(String pw, boolean isRefresh) {
		// Start query progress bar
		new QueryProgress(pw, isRefresh);
	}

	// Kick off Login window
	public static void startLogin(boolean isOldSession) {
		loginWindow = new Login(isOldSession);
	}

	// Session (memory) add/remove operations
	public static boolean addNewFlow(Flow newFlow) {
		// Check if flow already exists in session
		for (Flow f : session) {
			if (f.getLabel().equals(newFlow.getLabel())) {
				return false;
			}
		}
		return session.add(newFlow);
	}

	public static boolean removeFlow(Flow flow) {
		for (int i = 0; i < session.size(); i++) {
			Flow f = session.get(i);
			if (f.getLabel().equalsIgnoreCase(flow.getLabel())) {
				session.remove(i); // Memory side removal
				return true;
			}
		}
		return false;
	}

	public static boolean addNewServer(TreePath path, Server newServer) {
		// Check if server already exists in specific Flow
		for (Flow f : session) {
			if (f.getLabel().equals(path.getPathComponent(1).toString())) {
				for (Server s : f.getServerList()) {
					if (s.getServerName().equalsIgnoreCase(newServer.getServerName())) {
						return false;
					}
				}
				return f.getServerList().add(newServer);
			}
		}
		return false;
	}

	public static boolean removeServer(TreePath path, Server server) {
		for (Flow f : session) {
			if (f.getLabel().equals(path.getPathComponent(1).toString())) {
				return f.getServerList().remove(server);
			}
		}
		return false;
	}

	public static boolean addNewCmdScript(TreePath path, CmdScript newCS) {
		// Check if CMD or Script already exists in specific Server, Flow
		for (Flow f : session) {
			if (f.getLabel().equals(path.getPathComponent(1).toString())) {
				for (Server s : f.getServerList()) {
					if (s.getServerName().equalsIgnoreCase(path.getPathComponent(2).toString())) {
						for (CmdScript cs : s.getCmdScriptList()) {
							if (cs.getData().equals(newCS.getData())) {
								return false;
							}
						}
						return s.getCmdScriptList().add(newCS);
					}
				}
			}
		}
		return false;
	}

	public static boolean removeCS(TreePath path, CmdScript cs) {
		for (Flow f : session) {
			if (f.getLabel().equals(path.getPathComponent(1).toString())) {
				for (Server s : f.getServerList()) {
					if (s.getServerName().equalsIgnoreCase(path.getPathComponent(2).toString())) {
						return s.getCmdScriptList().remove(cs);
					}
				}
			}
		}
		return false;
	}

	public static boolean editCommand(TreePath path, String newCmd) {
		for (Flow f : session) {
			if (f.getLabel().equals(path.getPathComponent(1).toString())) {
				for (Server s : f.getServerList()) {
					if (s.getServerName().equalsIgnoreCase(path.getPathComponent(2).toString())) {
						for (CmdScript cs : s.getCmdScriptList()) {
							if (cs.getData().equals(((CmdScript) ((DefaultMutableTreeNode) path.getPathComponent(3)).getUserObject()).getData())) {
								cs.setData(newCmd);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public static DefaultListModel<Server> getDLMofServers(TreePath path) throws Exception {
		if (session.isEmpty())
			return new DefaultListModel<Server>();

		String flowLabel = path.getPathComponent(path.getPathCount() - 1).toString();
		for (Flow f : session) {
			if (f.getLabel().equals(flowLabel)) {
				if (!f.getServerList().isEmpty()) {
					DefaultListModel<Server> serverListModel = new DefaultListModel<Server>();
					for (Server s : f.getServerList()) {
						serverListModel.addElement(s);
					}
					return serverListModel;
				}
			}
		}
		return new DefaultListModel<Server>();
	}

	public static DefaultListModel<CmdScript> getDLMofCmdScripts(TreePath path) throws Exception {
		if (session.isEmpty())
			return new DefaultListModel<CmdScript>();

		String flowLabel = path.getPathComponent(path.getPathCount() - 2).toString();
		String serverName = path.getPathComponent(path.getPathCount() - 1).toString();
		for (Flow f : session) {
			if (f.getLabel().equals(flowLabel)) {
				if (!f.getServerList().isEmpty()) {
					for (Server s : f.getServerList()) {
						if (s.getServerName().equals(serverName)) {
							if (!s.getCmdScriptList().isEmpty()) {
								DefaultListModel<CmdScript> csListModel = new DefaultListModel<CmdScript>();
								for (CmdScript cs : s.getCmdScriptList()) {
									csListModel.addElement(cs);
								}
								return csListModel;
							}
						}
					}
				}
			}
		}
		return new DefaultListModel<CmdScript>();
	}

	public static String getCSData(TreePath path) throws Exception {
		CmdScript cs = (CmdScript) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
		// If CmdScript is disabled, for now refers to broken path
		if (!cs.isEnabled())
			return "Unresolved file path. No content available.\nPlease delete entry and re-add file.";
		// If CmdScript is not a CMD, it's a script, read file contents
		if (!cs.isCmd()) {
			return new String(Files.readAllBytes(Paths.get(cs.getData())), StandardCharsets.UTF_8);
		}
		// Otherwise, just a CMD, return data verbatim
		return cs.getData();
	}

	public static boolean existsFullPath() {
		if (!session.isEmpty()) {
			for (Flow f : session) {
				if (!f.getServerList().isEmpty()) {
					for (Server s : f.getServerList()) {
						if (!s.getCmdScriptList().isEmpty()) {
							for (CmdScript cs : s.getCmdScriptList()) {
								if (cs.isEnabled())
									return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public static String getFlowString() {
		StringBuilder sb = new StringBuilder();
		for (Flow f : session) {
			sb.append(f.getLabel() + '\n');
			for (Server s : f.getServerList()) {
				sb.append("   " + s.getServerName() + '\n');
				for (CmdScript cs : s.getCmdScriptList()) {
					sb.append("      " + cs.getData() + ": ");
					// if (cs.getResponse().equals("")) {
					// sb.append("no response\n");
					// } else {
					// sb.append("response!\n");
					// }
					// sb.append(cs.getResponse() + '\n');
				}
			}
		}
		return sb.toString();
	}
}
