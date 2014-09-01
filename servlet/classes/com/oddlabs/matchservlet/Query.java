package com.oddlabs.matchservlet;

import java.sql.ResultSet;
import java.sql.SQLException;

strictfp interface Query {
	Object process(ResultSet result) throws SQLException;
}
