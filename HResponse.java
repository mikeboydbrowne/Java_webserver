package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

public class HResponse implements HttpServletResponse {
	
	HSession		currentSession;
	Locale			currentLocale;
	ServletEngine	server;
	HttpServlet		servlet;
	String 			contentType;
	String 			characterEncoding;
	SWriter			servletOutput;
	boolean			isCommitted;
	
	public HResponse(OutputStream out, ServletEngine server, HSession s, HttpServlet servlet) {
		currentSession	= s;
		this.server		= server;
		this.servlet	= servlet;
		currentLocale 	= null;
		servletOutput 	= new SWriter(out, s);
		isCommitted 	= false;
	}
	
	public void flushBuffer() throws IOException {
		setContentType("text/html");
		setCharacterEncoding(getCharacterEncoding());
		servletOutput.flush();
		isCommitted = servletOutput.committed;
	}

	public int getBufferSize() {
		return servletOutput.bufferSize;
	}

	public String getCharacterEncoding() {
		return "ISO-8859-1";
	}

	public String getContentType() {
		if (contentType == null) {
			return "text/html";
		} else {
			return contentType;
		}
	}

	public Locale getLocale() {
		return currentLocale;
	}
	
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}

	public PrintWriter getWriter() throws IOException {
		return servletOutput;
	}

	public boolean isCommitted() {
		isCommitted = servletOutput.committed;
		return isCommitted;
	}

	public void reset() {
		servletOutput.buffer 		= new StringBuffer();
		servletOutput.bufferSize 	= Integer.MAX_VALUE;
		servletOutput.headers 		= new HashMap<String,String>();
		servletOutput.errorOccured	= false;
	}

	public void resetBuffer() {
		servletOutput.buffer 		= new StringBuffer();
		servletOutput.bufferSize 	= Integer.MAX_VALUE;
	}

	public void setBufferSize(int arg0) {
		servletOutput.setBufferSize(arg0);
	}

	public void setCharacterEncoding(String arg0) {
		servletOutput.headers.put("character-encoding", arg0);
	}

	public void setContentLength(int arg0) {
		servletOutput.headers.put("Content-Length", "" + arg0);
	}

	public void setContentType(String arg0) {
		servletOutput.headers.put("Content-Type", arg0);
	}

	public void setLocale(Locale arg0) {
		currentLocale = arg0;

	}

	public void addCookie(Cookie arg0) {
		servletOutput.cookieVals.put(arg0.getName(), arg0.getValue());
	}

	public void addDateHeader(String arg0, long arg1) {
		Date date = new Date(arg1);
		servletOutput.headers.put(arg0, date.toString());
	}

	public void addHeader(String arg0, String arg1) {
		servletOutput.headers.put(arg0, arg1);
	}

	public void addIntHeader(String arg0, int arg1) {
		servletOutput.headers.put(arg0, "" + arg1);
	}

	public boolean containsHeader(String arg0) {
		return servletOutput.headers.containsKey(arg0);
	}

	public String encodeRedirectUrl(String arg0) {
		try {
			return new String(arg0.getBytes("UTF-8"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return arg0;
	}
	
	@Override
	public String encodeRedirectURL(String arg0) {
		try {
			return new String(arg0.getBytes("UTF-8"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return arg0;
	}

	public String encodeURL(String arg0) {
		return null;
	}

	public String encodeUrl(String arg0) {
		return null;
	}

	public void sendError(int arg0) throws IOException {
		if (!isCommitted()) {
			servletOutput.buffer = new StringBuffer();
			servletOutput.headers = new HashMap<String, String>();
			setContentType("text/html");
			servletOutput.setError();
			servletOutput.setStatus(arg0);
			
		} else {
			throw new IllegalStateException();
		}
		flushBuffer();
	}

	public void sendError(int arg0, String arg1) throws IOException {
		if (!isCommitted()) {
			servletOutput.buffer = new StringBuffer();
			servletOutput.headers = new HashMap<String, String>();
			setContentType("text/html");
			servletOutput.setError();
			servletOutput.setStatus(arg0, arg1);
			
		} else {
			throw new IllegalStateException();
		}
		flushBuffer();
	}

	public void sendRedirect(String arg0) throws IOException {
		if (!isCommitted()) {
			servletOutput.buffer = new StringBuffer();
			servletOutput.headers = new HashMap<String, String>();
			setContentType("text/html");
			servletOutput.errorOccured = false;
			// absolute URL
			if (arg0.contains("localhost")) {
				servletOutput.headers.put("Location", encodeRedirectUrl(arg0));
			// relative URL
			} else {
				arg0 = server.absolutePath + arg0;
				servletOutput.headers.put("Location", encodeRedirectUrl(arg0));
			}
			servletOutput.setStatus(302);
		} else {
			throw new IllegalStateException();
		}
		flushBuffer();
	}

	public void setDateHeader(String arg0, long arg1) {
		Date date = new Date(arg1);
		servletOutput.headers.put(arg0, date.toString());
	}

	public void setHeader(String arg0, String arg1) {
		servletOutput.headers.put(arg0, arg1);
	}

	public void setIntHeader(String arg0, int arg1) {
		servletOutput.headers.put(arg0, "" + arg1);
	}

	public void setStatus(int arg0) {
		servletOutput.setStatus(arg0);
	}

	public void setStatus(int arg0, String arg1) {
		servletOutput.setStatus(arg0);
	}
}
