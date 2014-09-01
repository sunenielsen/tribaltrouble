package com.oddlabs.matchservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.DataOutput;
import java.util.ArrayList;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.GeneralSecurityException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import javax.sql.DataSource;

import java.nio.ByteBuffer;

import com.oddlabs.registration.RegistrationKey;
import com.oddlabs.util.CryptUtils;

public final class Login extends HttpServlet {
	private final static String SIGN_ALGORITHM = "SHA1WithRSA";

	private PrivateKey getPrivateKey() {
		return (PrivateKey)getServletContext().getAttribute("private_key");
	} 

	private DataSource getDataSource() {
		return (DataSource)getServletContext().getAttribute("db");
	} 

	private void login(String reg_key, String username, String password_digest) throws SQLException {
		int num_matches = (Integer)DBInterface.executeQuery(getDataSource(), new GetFirstIntQuery(),
				"SELECT COUNT(*) FROM match_valid_user U, match_valid_key K WHERE K.reg_key = ? AND LOWER(U.username) = LOWER(?) AND U.password = ?",
				reg_key, username, CryptUtils.digest(password_digest));
		assert num_matches <= 1;
		if (num_matches == 0)
			throw new SQLException("USER_ERROR_NO_SUCH_USER");
	}

	private static boolean checkChars(String value, String allowed_chars) {
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (allowed_chars.indexOf(c) == -1)
				return false;
		}
		return true;
	}

	private void createUser(String reg_key, String username, String password_digest, String email) throws SQLException {
		Connection conn = getDataSource().getConnection();
		try {
			conn.setAutoCommit(false);
			int min_username_length = DBInterface.getIntSetting(conn, "min_username_length");
			if (username.length() < min_username_length)
				throw new SQLException("USERNAME_ERROR_TOO_SHORT");

			int max_username_length = DBInterface.getIntSetting(conn, "max_username_length");
			if (username.length() > max_username_length)
				throw new SQLException("USERNAME_ERROR_TOO_LONG");

			String allowed_chars = DBInterface.getSetting(conn, "allowed_chars");
			if (!checkChars(username, allowed_chars))
				throw new SQLException("USERNAME_ERROR_INVALID_CHARACTERS");
			if (!Validation.isValidEmail(email) || !checkChars(email, allowed_chars))
				throw new SQLException("USER_ERROR_INVALID_EMAIL");

			int too_many = (Integer)DBInterface.executeQuery(conn, new GetFirstIntQuery(),
					"SELECT COUNT(*) FROM match_user WHERE reg_key = ?", reg_key);
			if (too_many > 0)
				throw new SQLException("USERNAME_ERROR_TOO_MANY");
			int existing = (Integer)DBInterface.executeQuery(conn, new GetFirstIntQuery(), 
					"SELECT COUNT(*) FROM match_user WHERE username = ?", username);
			if (existing > 0)
				throw new SQLException("USERNAME_ERROR_ALREADY_EXISTS");
			int num_rows = DBInterface.executeUpdate(conn,
					"INSERT INTO match_user (reg_key, username, password, email) VALUES (?, ?, ?, ?)",
					reg_key, username, CryptUtils.digest(password_digest), email);
			assert num_rows <= 1;
			conn.commit();
		} finally {
			conn.close();
		}
	}

	private void writeSigned(HttpServletResponse res, String username) throws ServletException, IOException {
		try {
			Signature signer = Signature.getInstance(SIGN_ALGORITHM);
			signer.initSign(getPrivateKey());
			ByteArrayOutputStream sign_bytes = new ByteArrayOutputStream();
			DataOutputStream sign_data = new DataOutputStream(sign_bytes);
			sign_data.writeUTF(username);
			long timestamp = System.currentTimeMillis();
			sign_data.writeLong(timestamp);
			byte[] sign_bytes_array = sign_bytes.toByteArray();
			signer.update(sign_bytes_array);
			byte[] signed_bytes = signer.sign();
			DataOutputStream out = new DataOutputStream(ServletUtil.createOutput(res));
			try {
				out.writeLong(timestamp);
				ServletUtil.writeByteArray(out, signed_bytes);
			} finally {
				out.close();
			}
		} catch (GeneralSecurityException e) {
			throw new ServletException(e);
		}
	}

	private String normalizeKey(String key) {
		return RegistrationKey.normalize(key);
	}

	public final void doPost(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		SQLTools.doSQL(res, new SQLAction() {
			public final void run() throws SQLException, ServletException, IOException {
				String email = req.getParameter("email");
				String username = req.getParameter("username");
				createUser(normalizeKey(req.getParameter("reg_key")), username, req.getParameter("password"), email);
				writeSigned(res, username);
			}
		});
	}

	public final void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
		SQLTools.doSQL(res, new SQLAction() {
			public final void run() throws SQLException, ServletException, IOException {
				String username = req.getParameter("username");
				login(normalizeKey(req.getParameter("reg_key")), username, req.getParameter("password"));
				writeSigned(res, username);
			}
		});
	}
}
