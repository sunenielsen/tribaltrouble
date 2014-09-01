package com.oddlabs.regservlet;

import com.oddlabs.registration.RegistrationKey;
import com.oddlabs.registration.RegistrationRequest;
import com.oddlabs.registration.RegClientInterface;
import com.oddlabs.registration.RegServiceInterface;
import com.oddlabs.registration.RegistrationInfo;
import com.oddlabs.event.Deterministic;
import com.oddlabs.event.NotDeterministic;
import com.oddlabs.util.KeyManager;
import com.oddlabs.util.PasswordKey;

import java.security.PrivateKey;
import java.security.Signature;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import java.security.SignedObject;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.InitialContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

public final strictfp class RegServlet extends HttpServlet {
	private final static String GG_IGNITION_URL = "http://www.garagegames.com/ignition/validate.php?ProductId=0xFA00E87&Key=";
	private final static String LOCAL_KEYGEN_URL = "http://oddlabs.com/keygen_gg.php?";

	private PrivateKey private_reg_key;

	public final void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			String password = config.getInitParameter("reg_key_pass");
			Cipher decrypt_cipher = KeyManager.createPasswordCipherFromPassword(password.toCharArray(), Cipher.DECRYPT_MODE);
			private_reg_key = PasswordKey.readPrivateKey(decrypt_cipher, RegServiceInterface.PRIVATE_KEY_FILE, RegServiceInterface.KEY_ALGORITHM);
		} catch (ClassNotFoundException e) {
			throw new ServletException(e);
		} catch (GeneralSecurityException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}
	
	private final static boolean parseBoolean(String val) {
		return Boolean.valueOf(val).booleanValue();
	}
	
	private final static int parseInt(String val, int default_value) {
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return default_value;
		}
	}
	
	private final static String readReplyFromURL(URL url) throws IOException {
		InputStream in = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String reply = reader.readLine();
		return reply;
	}
	
	private final String validateGGKey(String gg_key) throws IOException {
		String reply = readReplyFromURL(new URL(GG_IGNITION_URL + gg_key));
		// parse GG reply
		String err_prefix = "Error: ";
		String success_prefix = "Valid: ";
		if (reply.startsWith(err_prefix)) {
			String err_msg = reply.substring(err_prefix.length());
			throw new IOException("GarageGames server validation failed: " + err_msg);
		} else if (reply.startsWith(success_prefix)) {
			String username = reply.substring(success_prefix.length());
			log("GarageGames username = " + username);
			return username;
		} else
			throw new IOException("Unknown reply from GarageGames: " + reply);
	}
	
	private final static String generateGGKey(String request_key_str, String gg_username) throws IOException {
		URL keygen_url = new URL(LOCAL_KEYGEN_URL + "gg_key=" + request_key_str  + "&username=" + gg_username);
		String reply = readReplyFromURL(keygen_url);
		return reply;
	}
	
	private final String createKey(String request_key_str, String affiliate_id) throws IOException {
		if (affiliate_id != null && affiliate_id.equals("garagegames")) {
			String gg_username = validateGGKey(request_key_str);
			String local_key = generateGGKey(request_key_str, gg_username);
			log("GarageGames local key = " + local_key);
			return local_key;
		} else
			return request_key_str;
	}
	
	private final static String normalizeKey(String key) throws IOException {
		String result = key.toUpperCase();
		result = result.replaceAll("-", "");
		if (result.length() != 16)
			throw new IOException("Invalid key: " + key);
		result = result.substring(0, 4) + '-' + result.substring(4, 8) + '-' + result.substring(8, 12) + '-' + result.substring(12, 16);
		return result;
	}
	
	public final void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String request_key_str = normalizeKey(req.getParameter("key"));
		String affiliate_id = req.getParameter("affiliate_id");
		String current_affiliate_id = req.getParameter("current_affiliate_id");
		int version = parseInt(req.getParameter("version"), 0);
		boolean timelimit = parseBoolean(req.getParameter("timelimit"));
		int maxtime = parseInt(req.getParameter("maxtime"), 0);
		boolean forcequit = parseBoolean(req.getParameter("forcequit"));
		int maxgames = parseInt(req.getParameter("maxgames"), 0);
		
log("Oddlabs: got key request: remote host = " + req.getRemoteHost() + " | key_str = " + request_key_str + " | current_affiliate_id = " + current_affiliate_id + " | affiliate_id = " + affiliate_id + " | version = " + version + " | timelimit = " + timelimit + " | maxtime = " + maxtime + " | forcequit = " + forcequit + " | maxgames = " + maxgames);
		
		String key_str = createKey(request_key_str, current_affiliate_id);
		long key = RegistrationKey.decode(key_str);
		try {
			SignedObject signed_registration = register(new RegistrationRequest(key, affiliate_id, version, timelimit, maxtime, forcequit, maxgames));
			res.setContentType("application/octet-stream");

			ServletOutputStream out = res.getOutputStream();
			ObjectOutputStream obj_out = new ObjectOutputStream(out);
			obj_out.writeObject(signed_registration);
			obj_out.close();
			log("Oddlabs: Registered key: " + key_str);
		} catch (Exception e) {
			log("got exception while registering: " + e);
			res.sendError(500, e.toString());
		}
	}

	private SignedObject sign(Serializable obj) throws ServletException {
		try {
			Signature reg_signer = Signature.getInstance(RegServiceInterface.SIGN_ALGORITHM);
			return new SignedObject(obj, private_reg_key, reg_signer);
		} catch (GeneralSecurityException e) {
			throw new ServletException(e);
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}

	private static DataSource getDataSource() throws ServletException {
		try {
			Context envCtx = (Context)new InitialContext().lookup("java:comp/env");
			return (DataSource)envCtx.lookup("jdbc/regDB");
		} catch(NamingException e) {
			throw new ServletException(e);
		}
	}

	private SignedObject register(RegistrationRequest reg_request) throws SQLException, ServletException {
		long reg_key = reg_request.getKey();
		String affiliate_id = reg_request.getAffiliate();
		String encoded_key = RegistrationKey.encode(reg_key);
		RegistrationInfo reg_info = DBInterface.registerKey(getDataSource(), reg_request);
		return sign(reg_info);
	}
}
