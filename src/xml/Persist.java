package xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;

import model.CmdScript;
import model.Flow;
import model.Server;
import model.Session;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Serializer;

public class Persist {

	public static Document sessionToXMLDoc(LinkedList<Flow> session) {
		if (session == null) {

		}
		Element root = new Element(Session.ssoID);
		for (Flow f : session) {
			Element flow = new Element("flow");
			flow.addAttribute(new Attribute("Flow_Label", f.getLabel()));
			flow.addAttribute(new Attribute("isEnabled", Boolean.toString(f.isEnabled())));
			for (Server s : f.getServerList()) {
				Element server = new Element("server");
				server.addAttribute(new Attribute("Server_Name", s.getServerName()));
				for (CmdScript cs : s.getCmdScriptList()) {
					Element cmdScript = new Element("csData");
					cmdScript.addAttribute(new Attribute("isCmd", cs.isCmd().toString()));
					cmdScript.addAttribute(new Attribute("CS_Data", cs.getData()));
					server.appendChild(cmdScript);
				}
				flow.appendChild(server);
			}
			root.appendChild(flow);
		}
		Document doc = new Document(root);
		return doc;
	}

	public static boolean startupXMLRoutine() {
		try {
			// Check if previous session xml file exists
			File xmlFile = new File("session.xml");
			if (!xmlFile.exists()) { // File doesn't exist, create new
				xmlFile.createNewFile();
				return false;
			} else { // File exists, retrieve session data
				Session.session = retrieveSessionfromSave();
				if (!Session.session.isEmpty()) { // Attach to DefaultListModel
					for (Flow f : Session.session) {
						Session.flowListModel.addElement(f);
					}
				}
				// Create and attach static root
				Session.root = Converter.sessionToTreeNode(Session.session);
				return true;
			}
		} catch (Exception e) {
			// TODO logger
			e.printStackTrace();
		}
		return false;
	}

	public static LinkedList<Flow> retrieveSessionfromSave() {
		LinkedList<Flow> session = new LinkedList<Flow>();
		try {
			File xmlFile = new File("session.xml");
			Builder builder = new Builder();
			FileInputStream is = new FileInputStream(xmlFile);

			Document doc = builder.build(is);

			Element root = doc.getRootElement();
			Elements flows = root.getChildElements();

			Session.ssoID = root.getLocalName();

			for (int i = 0; i < flows.size(); i++) {
				Element flow = flows.get(i);
				Elements servers = flow.getChildElements();
				Flow f = new Flow(flow.getAttributeValue("Flow_Label"));
				for (int j = 0; j < servers.size(); j++) {
					Element server = servers.get(j);
					Elements cmdScripts = server.getChildElements();
					Server s = new Server(server.getAttributeValue("Server_Name"));
					for (int k = 0; k < cmdScripts.size(); k++) {
						Element cmdScript = cmdScripts.get(k);
						boolean isCmd = true;
						if (cmdScript.getAttributeValue("isCmd").equalsIgnoreCase("false"))
							isCmd = false;
						CmdScript cs = new CmdScript(isCmd, cmdScript.getAttributeValue("CS_Data"));
						s.getCmdScriptList().add(cs);
					}
					f.getServerList().add(s);
				}
				session.add(f);
			}
		} catch (Exception e) {
			// TODO logger
			e.printStackTrace();
		}
		return session;
	}

	public static void sessionXMLSave(LinkedList<Flow> session) {
		try {
			File xmlFile = new File("session.xml");
			FileOutputStream fos = new FileOutputStream(xmlFile);
			Serializer s = new Serializer(fos, "ISO-8859-1");
			s.setIndent(4);
			s.setMaxLength(500);
			s.write(sessionToXMLDoc(session));
			fos.close();
		} catch (Exception e) {
			// TODO logger
			e.printStackTrace();
		}

	}
}
