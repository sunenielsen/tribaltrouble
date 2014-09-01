package com.oddlabs.matchservlet;

import java.sql.SQLException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

final strictfp class SQLTools {
	static void doSQL(HttpServletResponse res, SQLAction action) throws ServletException, IOException {
		try {
			action.run();
		} catch (SQLException e) {
			e.printStackTrace();
			res.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
		}
	}
}
