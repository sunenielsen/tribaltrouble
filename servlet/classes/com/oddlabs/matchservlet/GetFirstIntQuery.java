package com.oddlabs.matchservlet;

import java.sql.ResultSet;
import java.sql.SQLException;

final strictfp class GetFirstIntQuery implements Query {
	public final Object process(ResultSet result) throws SQLException {
		result.first();
		return (Integer)result.getInt(1);
	}
}
