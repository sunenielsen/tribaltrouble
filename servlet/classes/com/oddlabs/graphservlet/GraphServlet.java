package com.oddlabs.graphservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DriverManager;
import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;

public final class GraphServlet extends HttpServlet {
	private final int IMAGE_WIDTH = 532;
	private final int IMAGE_HEIGHT = 200;

	private final int BACKGROUND_COLOR = 0xFFFFFF;

	private final static Connection getConnection() {
		try {
			// Obtain our environment naming context
			Context initCtx = new InitialContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			// Look up our data source
			DataSource ds = (DataSource) envCtx.lookup("jdbc/graphDB");
			// Allocate and use a connection from the pool
			return ds.getConnection();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	} 
	
	private final int[][] getGameData(Connection conn, int game_id) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("SELECT tick, team1, team2, team3, team4, team5, team6 FROM game_reports WHERE game_id = ?");
		stmt.setInt(1, game_id);
		ResultSet result = stmt.executeQuery();
		ArrayList list = new ArrayList();
		while (result.next()) {
			list.add(new int[]{result.getInt("tick"), result.getInt("team1"), result.getInt("team2"), result.getInt("team3"), result.getInt("team4"), result.getInt("team5"), result.getInt("team6")});
		}
		int[][] array = new int[list.size()][];
		for (int i = 0; i < array.length; i++) {
			array[i] = (int[])list.get(i);
		}
		return array;
	}

	private final void printResult(OutputStream out, int[][] data) {
		BufferedImage img = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setColor(new Color(BACKGROUND_COLOR));
		g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
		g.setColor(Color.BLACK);
		g.drawLine(0, IMAGE_HEIGHT - 1, IMAGE_WIDTH - 1, IMAGE_HEIGHT - 1);
		g.drawLine(0, 0, 0, IMAGE_HEIGHT - 1);


		if (data.length > 0) {
			int max_x = 0;
			int max_y = 0;
			for (int i = 0; i < data.length; i++) {
				if (data[i][0] > max_x)
					max_x = data[i][0];
				for (int j = 1; j < data[i].length; j++) {
					if (data[i][j] > max_y)
						max_y = data[i][j];
				}
			}
			for (int i = 0; i < data.length; i++) {
				int a = i + 1;
				int x = data[i][0]*IMAGE_WIDTH/max_x;
				int y = IMAGE_HEIGHT - 1;
				if ((a % 30) == 0) {
					g.drawLine(x, y, x, y - 10);
				} else if ((a % 15) == 0) {
					g.drawLine(x, y, x, y - 7);
				} else if ((a % 3) == 0) {
					g.drawLine(x, y, x, y - 3);
				}
			}
			Color[] team_colors = new Color[]{
				new Color(1f, .75f, 0f),
				new Color(0f, .5f, 1f),
				new Color(1f, 0f, .25f),
				new Color(0f, 1f, .75f),
				new Color(.75f, 0f, 1f),
				new Color(.75f, 1f, 0f)};
			
			for (int j = 1; j < data[0].length; j++) {
				g.setColor(team_colors[j-1]);
				int last_x = 0;
				int last_y = IMAGE_HEIGHT;
				for (int i = 0; i < data.length; i++) {
					int x = data[i][0]*IMAGE_WIDTH/max_x;
					int y = IMAGE_HEIGHT - data[i][j]*IMAGE_HEIGHT/max_y;
					g.drawLine(last_x, last_y, x, y);
					last_x = x;
					last_y = y;
				}
			}
		}
		try {
			ImageIO.write(img, "png", out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		res.setContentType("image/png");

		String game_id_string = req.getParameter("game_id");
		int game_id;
		try {
			game_id = Integer.parseInt(game_id_string);
		} catch (NumberFormatException e) {
			res.sendError(500, e.getMessage());
			return;
		}
	
		try {
			Connection conn = getConnection();
			try {
				int[][] data = getGameData(conn, game_id);
				printResult(res.getOutputStream(), data);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}
