package xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import model.CmdScript;
import model.Flow;
import model.Server;
import model.Session;
import nu.xom.Document;
import nu.xom.Serializer;

public class Converter {

	public static DefaultMutableTreeNode sessionToTreeNode(LinkedList<Flow> session) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(Session.ssoID);
		for (Flow f : session) {
			DefaultMutableTreeNode flowNode = new DefaultMutableTreeNode(f);
			for (Server s : f.getServerList()) {
				DefaultMutableTreeNode serverNode = new DefaultMutableTreeNode(s);
				for (CmdScript cs : s.getCmdScriptList()) {
					serverNode.add(new DefaultMutableTreeNode(cs));
				}
				flowNode.add(serverNode);
			}
			root.add(flowNode);
		}
		return root;
	}

	public static DefaultTreeModel sessionToTreeModel(LinkedList<Flow> session) {
		DefaultTreeModel dtm = new DefaultTreeModel(sessionToTreeNode(session));
		return dtm;
	}

	public static File cmdToShellScript(String cmd) {
		// Construct shell script containing relevant unix command
		String filename = "internal_shellscript.sh";
		File fstream = new File(filename);
		try {
			PrintStream out = new PrintStream(new FileOutputStream(fstream));
			out.println(cmd);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fstream;
	}

	public static void printPrettyXML(Document doc) {
		try {
			Serializer s = new Serializer(System.out, "ISO-8859-1");
			s.setIndent(4);
			s.setMaxLength(64);
			s.write(doc);
		} catch (Exception e) {
			// TODO logger
			e.printStackTrace();
		}
	}
}
