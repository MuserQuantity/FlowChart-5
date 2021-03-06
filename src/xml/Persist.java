package xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import log.Alerts;
import log.Logger;
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

import org.xml.sax.SAXException;

public class Persist {

	private static String savePath;
	private static final String xsdName = "FlowChart_xml_schema.xsd";

	public static Document sessionToXMLDoc(LinkedList<Flow> session) {

		if (Session.ssoID == null) {
			Session.ssoID = "SSOID";
		}
		Document doc = null;
		if (!Session.ssoID.contains(" ")) {
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
			doc = new Document(root);
		}
		return doc;
	}

	// Only happens when loading save
	public static boolean startupXMLRoutine(File xmlSession) {
		try {
			// Check XSD validate XML file schema. make sure well-formed
			if (xmlSessionSchemaCheck(xmlSession)) {
				// If good, attach it to session
				Session.session = retrieveSessionfromSave(xmlSession);
				if (!Session.session.isEmpty()) { // Attach to DefaultListModel
					for (Flow f : Session.session) {
						Session.flowListModel.addElement(f);
					}
				}
				// Create and attach static root
				Session.root = Converter.sessionToTreeNode(Session.session);

				// Make sure script files exists from retrieved session
				int errorCount = scriptIntegrityCheck();
				if (errorCount > 0) { // If missing files detected
					// Display alert
					Alerts.infoBox(errorCount + " script files cannot be located.\nMissing script files will be highlighted red in the Flow Manager.",
							"Script Files Unresolved");
				}

				// Start Login window
				Session.startLogin(true);

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			Logger.log("Error detected in XML startup routine: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public static boolean xmlSessionSchemaCheck(File xml) throws Exception {
		// Only works if in Eclipse project root directory
		File schemaFile = new File(xsdName);
		boolean sourceFromZip = false;

		// Unzip from JAR executable if schemaFile isn't readily available
		if (!schemaFile.exists() && Session.class.getResource("Session.class").toString().contains("jar")) {
			// Retrieve schemaFile from JAR project executable/archive
			schemaFile = retrieveXSDSchemaFromJAR();
			sourceFromZip = true;
		}

		Source xmlFile = new StreamSource(xml);
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(schemaFile);
		Validator validator = schema.newValidator();
		try {
			validator.validate(xmlFile);
			return true;
		} catch (SAXException e) {
			Alerts.infoBox(xml.getName() + " is not a valid XML Session file.", "Invalid XML");
			Logger.log("Error loading XML file " + xml.getName() + " due to XML malformity. Please check schema.");
			return false;
		} finally {
			// Remove xsdFile after use (to prevent corruption)
			if (sourceFromZip)
				schemaFile.delete();
		}
	}

	static File retrieveXSDSchemaFromJAR() throws Exception {
		String jarName = Session.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		jarName = jarName.substring(jarName.lastIndexOf('/') + 1);

		ZipInputStream zis = new ZipInputStream(new FileInputStream(new File(jarName)));
		ZipEntry ze = zis.getNextEntry();

		while (ze != null) {
			String fileName = ze.getName();
			if (fileName.equals(xsdName)) { // Found the XSD schema file
				File xsdFile = new File(fileName);
				// Populate data in xsdFile
				FileOutputStream fos = new FileOutputStream(xsdFile);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				zis.closeEntry();
				zis.close();
				fos.close();

				return xsdFile;
			}
			ze = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();

		// Could not find XSD file
		Alerts.infoBox("Schema XSD: " + xsdName + " could not be found.", "Schema Validator File Missing");
		return null;
	}

	public static int scriptIntegrityCheck() {
		int errorCount = 0;
		for (Flow f : Session.session) {
			for (Server s : f.getServerList()) {
				for (CmdScript cs : s.getCmdScriptList()) {
					if (!cs.isCmd()) {
						if (!new File(cs.getData()).exists()) {
							cs.setEnabled(false);
							errorCount++;
						}
					}
				}
			}
		}
		return errorCount;
	}

	public static LinkedList<Flow> retrieveSessionfromSave(File xmlSession) {
		LinkedList<Flow> session = new LinkedList<Flow>();
		try {
			Builder builder = new Builder();
			FileInputStream is = new FileInputStream(xmlSession);

			Document doc = builder.build(is);

			Element root = doc.getRootElement();
			Elements flows = root.getChildElements();

			Session.ssoID = root.getLocalName();

			for (int i = 0; i < flows.size(); i++) {
				Element flow = flows.get(i);
				Elements servers = flow.getChildElements();
				Flow f = new Flow(flow.getAttributeValue("Flow_Label"));
				if (flow.getAttributeValue("isEnabled").equalsIgnoreCase("false"))
					f.setEnabled(false);
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
			Logger.log("Error retrieving XML session from save: " + xmlSession.getName());
			e.printStackTrace();
		}
		return session;
	}

	// TODO clean this up
	public static void sessionXMLSave(LinkedList<Flow> session) {
		File xmlFile = new File(savePath);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(xmlFile);
			Serializer s = new Serializer(fos, "ISO-8859-1");
			s.setIndent(4);
			s.setMaxLength(500);
			s.write(sessionToXMLDoc(session));
			fos.close();

		} catch (Exception e) {
			// sessionToXMLDoc is null because user quit without any SOOID input
			Logger.log("Cannot persist XML file due to user exiting application without username input");
			try {
				fos.close();
			} catch (IOException e1) {
				Logger.log("Error closing FileOutputStream when saving XML session file");
				e1.printStackTrace();
			}
			System.out.println(xmlFile.delete());
		}
	}

	public static void setSavePath(String scriptFilePath) {
		// Detect if .xml extension is added, if not, append it
		if (!scriptFilePath.substring(scriptFilePath.length() - 4).equalsIgnoreCase(".xml"))
			scriptFilePath += ".xml";

		savePath = scriptFilePath;
	}

	public static String getSavePath() {
		return savePath;
	}
}
