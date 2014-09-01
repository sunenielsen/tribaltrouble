package com.oddlabs.matchservlet;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

final strictfp class DBInterface {
	private static void checkArguments(Object[] arguments) throws SQLException {
		for (int i = 0; i < arguments.length; i++)
			if (arguments[i] == null)
				throw new SQLException("Argument " + i + " is null");
	}

	private static void assignArguments(PreparedStatement stmt, Object ... arguments) throws SQLException {
		for (int i = 0; i < arguments.length; i++)
			stmt.setObject(i + 1, arguments[i]);
	}

	static int executeUpdate(DataSource source, String sql, Object ... arguments) throws SQLException {
		Connection conn = source.getConnection();
		try {
			return executeUpdate(conn, sql, arguments);
		} finally {
			conn.close();
		}
	}

	static int executeUpdate(Connection conn, String sql, Object ... arguments) throws SQLException {
		checkArguments(arguments);
		PreparedStatement stmt = conn.prepareStatement(sql);
		try {
			assignArguments(stmt, arguments);
			return stmt.executeUpdate();
		} finally {
			stmt.close();
		}
	}

	static int getIntSetting(Connection conn, String setting) throws SQLException {
		return Integer.parseInt(getSetting(conn, setting));
	}

	static String getSetting(Connection conn, String setting) throws SQLException {
		return (String)executeQuery(conn, new GetFirstStringQuery(), "SELECT value FROM settings WHERE property = ?", setting);
	}

	static int getIntSetting(DataSource source, String setting) throws SQLException {
		Connection conn = source.getConnection();
		try {
			return getIntSetting(conn, setting);
		} finally {
			conn.close();
		}
	}

	static String getSetting(DataSource source, String setting) throws SQLException {
		Connection conn = source.getConnection();
		try {
			return getSetting(conn, setting);
		} finally {
			conn.close();
		}
	}

	static Object executeQuery(DataSource source, Query query, String sql, Object ... arguments) throws SQLException {
		Connection conn = source.getConnection();
		try {
			return executeQuery(conn, query, sql, arguments);
		} finally {
			conn.close();
		}
	}

	static Object executeQuery(Connection conn, Query query, String sql, Object ... arguments) throws SQLException {
		checkArguments(arguments);
		PreparedStatement stmt = conn.prepareStatement(sql);
		try {
			assignArguments(stmt, arguments);
			ResultSet result = stmt.executeQuery();
			try {
				return query.process(result);
			} finally {
				result.close();
			}
		} finally {
			stmt.close();
		}
	}
}
