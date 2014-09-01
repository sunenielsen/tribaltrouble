package com.oddlabs.regservlet;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import javax.sql.DataSource;

import com.oddlabs.registration.RegistrationKey;
import com.oddlabs.registration.RegistrationInfo;
import com.oddlabs.registration.RegistrationRequest;

public final strictfp class DBInterface {
	private final static RegistrationInfo getRegistrationInfo(Connection conn, long key) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("SELECT reg_key, reg_email, reg_time, name, address1, address2, zip, city, state, country FROM registrations R WHERE R.reg_key = ? AND NOT R.disabled");
		try {
			stmt.setString(1, RegistrationKey.encode(key));
			ResultSet result = stmt.executeQuery();
			if (!result.next()) {
				throw new SQLException("Failed to locate reg_key: " + key);
			}
			return new RegistrationInfo(key,
					result.getString("reg_key"),
					result.getString("reg_email"),
					result.getString("reg_time"),
					result.getString("name"),
					result.getString("address1"),
					result.getString("address2"),
					result.getString("zip"),
					result.getString("city"),
					result.getString("state"),
					result.getString("country"),
					System.currentTimeMillis());
		} finally {
			stmt.close();
		}
	}

	public final static RegistrationInfo registerKey(DataSource ds, RegistrationRequest reg_request) throws SQLException {
//		boolean first_reg = false;
		Connection conn = ds.getConnection();
		try {
			String encoded_key = RegistrationKey.encode(reg_request.getKey());
			conn.setAutoCommit(false);
			PreparedStatement stmt = conn.prepareStatement("UPDATE registrations SET affiliate = ?, num_reg = num_reg + 1 WHERE reg_key = ? AND num_reg = 0 AND coupon = '' AND reg_email <> '' ");
			try {
				stmt.setString(1, reg_request.getAffiliate());
				stmt.setString(2, encoded_key);
				//			int row_count = stmt.executeUpdate();
				//			first_reg = (row_count == 1);
			} finally {
				stmt.close();
			}
			/*		if (first_reg) {
					PreparedStatement stmt = DBUtils.createStatement("INSERT INTO demo_restrictions (version, use_time_limit, max_time, force_quit, max_num_games) " +
					"VALUES (?, ?, ?, ?, ?)");
					try {
					int index = 0;
					stmt.setInt(++index, demo_data.getDemoRestrictionsVersion());
					stmt.setBoolean(++index, demo_data.getUseTimeLimit());
					stmt.setInt(++index, demo_data.getMaxTime());
					stmt.setBoolean(++index, demo_data.getForceQuit());
					stmt.setInt(++index, demo_data.getMaxNumGames());
					int row_count = stmt.executeUpdate();
					assert row_count == 1;
					} finally {
					stmt.getConnection().close();
					}   
					}*/
			stmt = conn.prepareStatement("UPDATE registrations SET num_reg = num_reg + 1 WHERE reg_key = ?");
			try {
				stmt.setString(1, encoded_key);
				int row_count = stmt.executeUpdate();
				if (row_count != 1)
					throw new SQLException("Failed to locate key");
			} finally {
				stmt.close();
			}
			RegistrationInfo info = getRegistrationInfo(conn, reg_request.getKey());
			conn.commit();
			return info;
		} finally {
			conn.close();
		}
	}
}
