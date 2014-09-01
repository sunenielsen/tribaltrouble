package com.oddlabs.osutil;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;

public final strictfp class MacOSXUtil extends OSUtil {
	private static File locateDir(String app_dir_name) {
		File current_dir = new File(".").getAbsoluteFile();
		while (current_dir != null && !current_dir.getName().equals(app_dir_name))
			current_dir = current_dir.getParentFile();
		return current_dir;
	}

	private static void convertPlist(File info_plist_file, String script_url, Map script_parameters) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder document_builder = factory.newDocumentBuilder();
			// Hack to avoid lookup of the DTD
			document_builder.setEntityResolver(new EntityResolver() {
				public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
					if (publicId.equals("-//Apple Computer//DTD PLIST 1.0//EN"))
						return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
					else
						return null;
				}
			});

			URL xslt_url = Thread.currentThread().getContextClassLoader().getResource(script_url);
			Source xsltSource = new StreamSource(xslt_url.toString());
			Source source = new StreamSource(info_plist_file);
			File tmp_file = File.createTempFile("Info", "plist");
			tmp_file.deleteOnExit();
			Result result = new StreamResult(tmp_file);

			TransformerFactory tf = TransformerFactory.newInstance();
			Templates transformation = tf.newTemplates(xsltSource);
			Transformer transformer = transformation.newTransformer();
			Iterator it = script_parameters.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry entry = (Map.Entry)it.next();
				transformer.setParameter((String)entry.getKey(), entry.getValue());
			}
			transformer.transform(source, result);
			tmp_file.renameTo(info_plist_file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public final void registerURLScheme(String gamename, URLAssociation association) {
		Map script_parameters = new HashMap();
		script_parameters.put("description", association.description);
		script_parameters.put("scheme", association.scheme);
		script_parameters.put("iconname", association.icon_name);
		convertPlist(gamename, "scripts/urlschemeplist.xml", script_parameters);
	}

	public final void registerAssociation(String gamename, Association association) {
		Map script_parameters = new HashMap();
		script_parameters.put("mimetype", association.mime_type);
		script_parameters.put("extension", association.extension);
		script_parameters.put("extension2", association.extension.toUpperCase());
		script_parameters.put("typename", association.mime_type);
		script_parameters.put("iconname", association.icon_name + ".icns");
		convertPlist(gamename, "scripts/filetypeplist.xml", script_parameters);
	}

	private static void convertPlist(String gamename, String script_url, Map script_parameters) {
		String app_dir_name = gamename + ".app";
		File app_dir = locateDir(app_dir_name);
		if (app_dir == null) {
			System.out.println("Failed to locate " + app_dir_name);
			return;
		}
		File info_plist_file = new File(new File(app_dir, "Contents"), "Info.plist");
		if (!info_plist_file.exists()) {
			System.out.println("Failed to locate " + info_plist_file);
			return;
		}
		convertPlist(info_plist_file, script_url, script_parameters);
	}
}
