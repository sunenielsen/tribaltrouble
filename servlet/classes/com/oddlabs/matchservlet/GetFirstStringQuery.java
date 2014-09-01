package com.oddlabs.matchservlet;

import java.sql.ResultSet;
import java.sql.SQLException;

final strictfp class GetFirstStringQuery implements Query {
	public final Object process(ResultSet result) throws SQLException {
		result.first();
		return result.getString(1);
	}
}
