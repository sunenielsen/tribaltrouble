package com.oddlabs.http;

import com.oddlabs.event.Deterministic;
import com.oddlabs.net.Callable;
import com.oddlabs.net.Task;
import com.oddlabs.net.TaskThread;
import com.oddlabs.util.CryptUtils;

import javax.net.ssl.HttpsURLConnection;
import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.Iterator;

public final strictfp class HttpRequest {
	public static Task doPost(TaskThread task_thread, HttpRequestParameters parameters, HttpResponseParser parser, HttpCallback callback) {
		try {
			URL url = new URL(parameters.url);
			return spawnPostRequest(task_thread, url, parameters, parser, callback);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public static Task doGet(TaskThread task_thread, HttpRequestParameters parameters, HttpResponseParser parser, HttpCallback callback) {
		URL url = constructURL(parameters);
		return spawnGetRequest(task_thread, url, parser, callback);
	}

	private static Task spawnPostRequest(TaskThread task_thread, final URL url, final HttpRequestParameters parameters, final HttpResponseParser parser, final HttpCallback callback) {
		return task_thread.addTask(new Callable() {
			public Object call() throws IOException {
				return runPostRequest(url, parameters, parser);
			}

			public void taskCompleted(Object result) {
				((HttpResponse)result).notify(callback);
			}

			public void taskFailed(Exception e) {
				callback.error((IOException)e);
			}
		});
	}

	private static Task spawnGetRequest(TaskThread task_thread, final URL url, final HttpResponseParser parser, final HttpCallback callback) {
		return task_thread.addTask(new Callable() {
			public Object call() throws IOException {
				return runGetRequest(url, parser);
			}

			public void taskCompleted(Object result) {
				((HttpResponse)result).notify(callback);
			}

			public void taskFailed(Exception e) {
				callback.error((IOException)e);
			}
		});
	}

	private static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[4096];
		int ret;
		while ((ret = is.read(buf)) > 0) {
			if (os != null)
				os.write(buf, 0, ret);
		}
	}

	private static HttpResponse runPostRequest(URL url, HttpRequestParameters parameters, HttpResponseParser parser) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)openConnection(url);
		conn.setRequestMethod("POST");
		conn.setDoOutput(true);
		ByteArrayOutputStream byte_os = new ByteArrayOutputStream();
		OutputStreamWriter out = new OutputStreamWriter(byte_os, "UTF-8");

		String query_string = parameters.createQueryString();
		out.write(query_string, 0, query_string.length());
		out.close();

		conn.setRequestProperty("Content-Length", String.valueOf(byte_os.size())); 
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 

		byte_os.writeTo(conn.getOutputStream()); 
		return readResponse(conn, parser);
	}

	private static HttpResponse runGetRequest(URL url, HttpResponseParser parser) throws IOException {
		URLConnection conn = openConnection(url);
		return readResponse(conn, parser);
	}

	public static URLConnection openConnection(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		try {
			if (conn instanceof HttpsURLConnection)
				CryptUtils.setupHttpsConnection((HttpsURLConnection)conn);
			return conn;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static HttpResponse readResponse(URLConnection conn, HttpResponseParser parser) throws IOException {
		try {
			InputStream is = conn.getInputStream();
			try {
				HttpResponse response = new OkResponse(parser.parse(is));
				copy(is, null); // Make sure the entire stream is read
				return response;
			} finally {
				is.close();
			}
		} catch (IOException e) {
			int response_code = ((HttpURLConnection)conn).getResponseCode();
			String response_message = ((HttpURLConnection)conn).getResponseMessage();
			ByteArrayOutputStream byte_os = new ByteArrayOutputStream();
			InputStream es = ((HttpURLConnection)conn).getErrorStream();
			try {
				copy(es, byte_os);
			} finally {
				es.close();
			}
			return new ErrorResponse(response_code, response_message);
		}
	}

	private static URL constructURL(HttpRequestParameters parameters) {
		try {
			return new URL(parameters.url + "?" + parameters.createQueryString());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}
	
/*	private static void testLogin(Deterministic deterministic, HttpResponseParser parser, HttpCallback callback) {
		Map parameters = new java.util.HashMap();
		parameters.put("reg_key", "K6AA-Y33X-C7ZT-K4TF");
		parameters.put("username", "blah");
		parameters.put("password", "blah");
		doGet(deterministic, new HttpRequestParameters("https://localhost/matchservlet/login", parameters), parser, callback);
	}

	private static void testCreateUser(Deterministic deterministic, HttpResponseParser parser, HttpCallback callback) {
		Map parameters = new java.util.HashMap();
		parameters.put("reg_key", "K6AA-Y33X-C7ZT-K4TF");
		parameters.put("username", "blah");
		parameters.put("password", "blah");
		parameters.put("email", "blah@blah.com");
		doPost(deterministic, new HttpRequestParameters("https://localhost/matchservlet/login", parameters), parser, callback);
	}

	public final static void main(String[] args) throws IOException {
		final Deterministic deterministic = new com.oddlabs.event.NotDeterministic();
		final HttpResponseParser parser = new HttpResponseParser() {
			public final Object parse(InputStream in) throws IOException {
				return "OK!";
			}

		};
		testCreateUser(deterministic, parser, new DefaultHttpCallback() {
			public final void success(Object response) {
				testLogin(deterministic, parser, new DefaultHttpCallback() {
					public final void success(Object response) {
System.out.println("response = " + response);
					}

					public final void error(IOException e) {
						System.out.println("e = " + e);
					}
				});
			}

			public final void error(IOException e) {
				System.out.println("e = " + e);
			}
		});
		com.oddlabs.net.NetworkSelector.initSelector();
		while (true) {
			NetworkSelector.processTasks();
		}
	}*/
}
