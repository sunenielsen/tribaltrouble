package com.oddlabs.tt.bugclient;

import com.oddlabs.http.HttpRequest;
import com.oddlabs.http.MultiPartFormOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import javax.swing.ProgressMonitor;
import java.util.regex.Pattern;

import org.lwjgl.opengl.Display;

final strictfp class Uploader {
	private final static String STD_OUT_PATTERN = "std\\.out";
	private final static String STD_ERR_PATTERN = "std\\.err";
	private final static String EVENT_LOG_PATTERN = "event\\.log";
	private final static String PID_PATTERN = "hs_err_pid.*\\.log";
	private final static String MAC_PID_PATTERN = "JavaNativeCrash_pid.*\\.log";

	private final static String[] LOG_FILE_PATTERNS = {
		STD_OUT_PATTERN,
		STD_ERR_PATTERN,
		EVENT_LOG_PATTERN,
		PID_PATTERN,
		MAC_PID_PATTERN};

	static void upload(URL url, int revision, String email, String comment, File log_dir, Progress progress) throws IOException {
		File[] LOG_FILE_DIRS = {
			log_dir,
			log_dir,
			log_dir,
			new File("."),
			new File(System.getProperty("user.home") + File.separator + "Library" + File.separator + "Logs")};

		HttpURLConnection conn = (HttpURLConnection)HttpRequest.openConnection(url);
		conn.setRequestMethod("POST");
//		conn.setInstanceFollowRedirects(true);
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setUseCaches(false);
		conn.setDefaultUseCaches(false);
		conn.setRequestProperty("Accept", "*/*");
		String boundary = MultiPartFormOutputStream.createBoundary();
		conn.setRequestProperty("Content-Type", MultiPartFormOutputStream.getContentType(boundary));
		conn.setRequestProperty("Connection", "Keep-Alive");
		conn.setRequestProperty("Cache-Control", "no-cache");
		MultiPartFormOutputStream out = new MultiPartFormOutputStream(conn.getOutputStream(), boundary);
		out.writeField("revision", revision);
		out.writeField("email", email);
		out.writeField("comment", comment);
		out.writeField("adapter", Display.getAdapter());
		out.writeField("version", Display.getVersion());
		out.writeField("javaversion", System.getProperty("java.version"));
		out.writeField("javavendor", System.getProperty("java.vender"));
		out.writeField("osname", System.getProperty("os.name"));
		out.writeField("osarch", System.getProperty("os.arch"));
		out.writeField("osversion", System.getProperty("os.version"));
		float progress_value = .01f;
		progress.setProgress(progress_value);
		byte[] buffer = new byte[65536];
		for (int i = 0; i < LOG_FILE_DIRS.length; i++) {
			File dir = LOG_FILE_DIRS[i];
			final Pattern pattern = Pattern.compile(LOG_FILE_PATTERNS[i]);
			System.out.println("Checking dir '" + dir + "'");
			File[] files = dir.listFiles(new FilenameFilter() {
				public final boolean accept(File dir, String name) {
					return pattern.matcher(name).matches();
				}
			});
			if (files != null) {
				for (int j = 0; j < files.length; j++) {
					ByteArrayOutputStream byte_out = new ByteArrayOutputStream();
					GZIPOutputStream zip_stream = new GZIPOutputStream(byte_out);
					InputStream file_input = new FileInputStream(files[j]);
					try {
						int num_bytes;
						while ((num_bytes = file_input.read(buffer)) != -1) {
							zip_stream.write(buffer, 0, num_bytes);
						}
					} finally {
						file_input.close();
					}
					zip_stream.close();
					System.out.println("Adding file '" + files[j] + "'");
					out.writeFile("file[]", "application/octet-stream", files[j].getName() + ".gz", byte_out.toByteArray());
				}
			}
		}
		out.close();
		progress_value += .01f;
		progress.setProgress(progress_value);
		System.out.println("Response code: " + conn.getResponseCode());
		System.out.println("Response message: " + conn.getResponseMessage());
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = "";
		while ((line = in.readLine()) != null) {
			progress_value = StrictMath.min(progress_value + .01f, .99f);
			progress.setProgress(progress_value);
			System.out.println(line);
		}
		in.close();
		progress_value = 1f;
		progress.setProgress(progress_value);
	}

	strictfp interface Progress {
		void setProgress(float p);
	}
}
