package model;

import gui.Login;

import java.util.LinkedList;

import javax.swing.DefaultListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import xml.Persist;

public class Session {

	public static String ssoID;
	public static LinkedList<Flow> session = new LinkedList<Flow>();
	public static DefaultListModel<Flow> flowListModel = new DefaultListModel<Flow>();
	public static DefaultMutableTreeNode root;
	public static Login loginWindow;

	public static void main(String[] args) {
		try {
			// Display login window & kick off XML persistence logic
			loginWindow = new Login(Persist.startupXMLRoutine());

		} catch (Exception e) {
			e.printStackTrace();
			// TODO logger
		}

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
					if (s.getServerName().equalsIgnoreCase(newServer.serverName)) {
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

	public static void popTestSession() {
		Flow f1 = new Flow("test flow 1");
		Server s11 = new Server("server11");
		Server s12 = new Server("server12");
		Server s13 = new Server("server13");
		CmdScript cs111 = new CmdScript(true, "ls");
		CmdScript cs121 = new CmdScript(false, "/some/path/of/script1.sh");
		CmdScript cs122 = new CmdScript(true, "ps -ef");
		CmdScript cs123 = new CmdScript(false, "/some/path/of/script2.sh");
		CmdScript cs131 = new CmdScript(true, "pwd");
		CmdScript cs132 = new CmdScript(false, "/some/path/of/script3.sh");
		s11.cmdScriptList.add(cs111);
		s12.cmdScriptList.add(cs121);
		s12.cmdScriptList.add(cs122);
		s12.cmdScriptList.add(cs123);
		s13.cmdScriptList.add(cs131);
		s13.cmdScriptList.add(cs132);
		f1.serverList.add(s11);
		f1.serverList.add(s12);
		f1.serverList.add(s13);

		Flow f2 = new Flow("test flow 2");
		Server s21 = new Server("server21");
		Server s22 = new Server("server22");
		Server s23 = new Server("server23");
		CmdScript cs211 = new CmdScript(true, "ls -al");
		CmdScript cs221 = new CmdScript(false, "/some/path/of/script4.sh");
		CmdScript cs222 = new CmdScript(true, "bash");
		CmdScript cs223 = new CmdScript(false, "/some/path/of/script5.sh");
		CmdScript cs231 = new CmdScript(true, "ps");
		CmdScript cs232 = new CmdScript(false, "/some/path/of/script6.sh");
		s21.cmdScriptList.add(cs211);
		s22.cmdScriptList.add(cs221);
		s22.cmdScriptList.add(cs222);
		s22.cmdScriptList.add(cs223);
		s23.cmdScriptList.add(cs231);
		s23.cmdScriptList.add(cs232);
		f2.serverList.add(s21);
		f2.serverList.add(s22);
		f2.serverList.add(s23);

		Flow f3 = new Flow("test flow 3");
		Server s31 = new Server("server31");
		Server s32 = new Server("server32");
		Server s33 = new Server("server33");
		CmdScript cs311 = new CmdScript(true, "cd -");
		CmdScript cs321 = new CmdScript(false, "/some/path/of/script7.sh");
		CmdScript cs322 = new CmdScript(true, "cd");
		CmdScript cs323 = new CmdScript(false, "/some/path/of/script8.sh");
		CmdScript cs331 = new CmdScript(true, "mkdir temp");
		CmdScript cs332 = new CmdScript(false, "/some/path/of/script9.sh");
		s31.cmdScriptList.add(cs311);
		s32.cmdScriptList.add(cs321);
		s32.cmdScriptList.add(cs322);
		s32.cmdScriptList.add(cs323);
		s33.cmdScriptList.add(cs331);
		s33.cmdScriptList.add(cs332);
		f3.serverList.add(s31);
		f3.serverList.add(s32);
		f3.serverList.add(s33);

		session.add(f1);
		session.add(f2);
		session.add(f3);
	}

	public static void printSessionTree() {
		for (Flow f : session) {
			System.out.println(f.label);
			for (Server s : f.serverList) {
				System.out.println("  " + s.serverName);
				for (CmdScript cs : s.cmdScriptList) {
					System.out.println("      " + cs.data);
				}
			}
		}
	}
}
