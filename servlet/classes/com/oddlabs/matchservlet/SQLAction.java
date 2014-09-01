package com.oddlabs.matchservlet;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;

strictfp interface SQLAction {
	void run() throws SQLException, ServletException, IOException;
}
