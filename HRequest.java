package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class HRequest implements HttpServletRequest {
	
	// variables for plug-in methods (set according to spec)
	String 		characterEncoding;
	boolean 	characterEncodingRun;
	String 		contentType;
	Locale		reqLocale;
	boolean		localeSet;
	
	// variables for dynamic methods (not set according to spec)
	HashMap<String, String> parameters		= new HashMap<String, String>();
	HashMap<String, String> headers			= new HashMap<String, String>();
	HashMap<String, String>	cookieVals		= new HashMap<String, String>();
	BufferedReader			requestReader;
	ServletEngine 			server;
	HttpServlet				servlet;
	Socket					requestSocket;
	String					queryString;
	String					requestedPath;
	String					requestMethod;
	String					requestParams;
	String					request;
	String					servletAddr;
	
	public HRequest (Socket clientSocket, BufferedReader req, String request, 
					String servletAddr, ServletEngine server, HttpServlet servlet) {
		this.requestSocket	= clientSocket;
		this.requestReader	= req;
		this.server 		= server;
		this.servlet		= servlet;
		this.request 		= request;
		this.servletAddr	= servletAddr;
		this.queryString	= "";
		processRequest();	// processing the request string
	}
	
	private boolean processRequest() {
		requestMethod 					= request.split("\r\n")[0].split(" ")[0];
		requestedPath					= request.split("\r\n")[0].split(" ")[1];
		ArrayList<String> headerList 	= new ArrayList<String>();
		boolean contentLength			= false;
		// initializing headers
		for (String s : request.split("\r\n")) {
			// getting header values
			if (s.contains(": ") ) {
				if (s.contains("Content-Length: "))
					contentLength = true;
				headerList.add(s);
			}
			// getting post request information
			if (contentLength) {
				requestParams = s;
				contentLength = false;
			}
		}
		
		// separating between header info and cookie info
		for (String t : headerList) {
			String[] headerArr = t.split(": ");
			if (headerArr[0].equalsIgnoreCase("Cookie")) {
				for (String s : headerArr[1].split(";")) {
					cookieVals.put(s.split("=")[0], s.split("=")[1]);
				}
			} else {
				headers.put(headerArr[0], headerArr[1]);
			}
		}
		
		// initializing parameters
		if (requestMethod.equalsIgnoreCase("GET")) {
			if (requestedPath.split("\\?").length > 1) {					// checking to see if parameters passed
				String paramVals 	= requestedPath.split("\\?")[1];		// splitting off at '?' mark
				queryString 		= paramVals;
				for (String p : paramVals.split("&")) {					// getting each param tuple via '&'
					parameters.put(p.split("=")[0], p.split("=")[1]);	// splitting each tuple around '='
				}
			}
		} else if (requestMethod.equalsIgnoreCase("HEAD")) {
			if (requestedPath.split("\\?").length > 1) {					// checking to see if parameters passed
				String paramVals 	= requestedPath.split("\\?")[1];		// splitting off at '?' mark
				queryString 		= paramVals;
				for (String p : paramVals.split("&")) {					// getting each param tuple via '&'
					parameters.put(p.split("=")[0], p.split("=")[1]);	// splitting each tuple around '='
				}
			}
		} else if (requestMethod.equalsIgnoreCase("POST")) {
			for (String s : requestParams.split("&")) {
				parameters.put(s.split("=")[0], s.split("=")[1]);
			}
		}
		
		return true;
	}

	
	public Object getAttribute(String arg0) {
		return servlet.getServletContext().getAttribute(arg0);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		return servlet.getServletContext().getAttributeNames();
	}

	public String getCharacterEncoding() {
		if (characterEncodingRun) {
			return characterEncoding;
		} else {
			return "ISO-8859-1";
		}
	}

	public int getContentLength() {
		return Integer.parseInt(headers.get("Content-Length"));
	}

	public String getContentType() {
		return "text/html";
	}

	public ServletInputStream getInputStream() throws IOException {
		return null;
	}

	public String getLocalAddr() {
		InetAddress localAddress = null;
		try {
			localAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return localAddress.toString();
	}

	public String getLocalName() {
		return "localhost:";
	}

	public int getLocalPort() {
		return server.port;
	}

	public Locale getLocale() {
		if (localeSet) {
			return reqLocale;
		} else {
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getLocales() {
		return null;
	}

	public String getParameter(String arg0) {
		return parameters.get(arg0);
	}

	@SuppressWarnings("rawtypes")
	public Map getParameterMap() {
		return parameters;
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getParameterNames() {
		Set<String> keys = parameters.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	public String[] getParameterValues(String arg0) {
		// getting parameter values
		ArrayList<String> paramVals = new ArrayList<String>();
		for (String s : parameters.keySet()) {
			paramVals.add(parameters.get(s));
		}
		
		// putting the parameter values in a STring[]
		String[] retVals = new String[paramVals.size()];
		int i = 0;
		for (String v : paramVals) {
			retVals[i] = v;
			i++;
		}
		return retVals;
	}

	public String getProtocol() {
		return requestMethod;
	}

	public BufferedReader getReader() throws IOException {
		return requestReader;
	}

	public String getRealPath(String arg0) {
		return "http://" + getLocalName() + getLocalPort() + requestedPath;
	}

	public String getRemoteAddr() {
		return requestSocket.getRemoteSocketAddress().toString();
	}

	public String getRemoteHost() {
		return "127.0.0.1";
	}

	public int getRemotePort() {
		return requestSocket.getPort();
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	public String getScheme() {
		return "HTTP";
	}

	public String getServerName() {
		return server.toString();
	}

	public int getServerPort() {
		return server.port;
	}

	public boolean isSecure() {
		return false;
	}

	public void removeAttribute(String arg0) {
		servlet.getServletContext().removeAttribute(arg0);

	}

	public void setAttribute(String arg0, Object arg1) {
		servlet.getServletContext().setAttribute(arg0, arg1);

	}

	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		characterEncodingRun = true;
		headers.put("Content-Encoding", arg0);
	}

	public String getAuthType() {
		return "BASIC";
	}

	public String getContextPath() {
		return requestedPath;
	}

	public Cookie[] getCookies() {
		// Has to do with sessions - 
		return null;
	}

	public long getDateHeader(String arg0) {
		String 		dateString 	= headers.get(arg0);
		DateFormat 	dateFormat 	= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
		Date 		date		= null;
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}

	public String getHeader(String arg0) {
		return headers.get(arg0);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getHeaderNames() {
		Set<String> keys = headers.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getHeaders(String arg0) {
		String headerVals = headers.get(arg0);
		Set<String> keys = new HashSet<String>();
		for (String s : headerVals.split(",")) {
			keys.add(s);
		}
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	public int getIntHeader(String arg0) {
		return Integer.parseInt(headers.get(arg0));
	}

	public String getMethod() {
		return requestMethod;
	}

	public String getPathInfo() {
		return requestedPath;
	}

	public String getPathTranslated() {
		return null;
	}

	public String getQueryString() {
		return queryString;
	}

	public String getRemoteUser() {
		// relates to the HTTPSession
		return null;
	}

	public String getRequestURI() {
		if (queryString.equalsIgnoreCase("")) {
			return requestedPath;
		} else {
			return requestedPath.split("?")[0];
		}
	}

	public StringBuffer getRequestURL() {
		return new StringBuffer(requestedPath);
	}

	public String getRequestedSessionId() {
		return cookieVals.get("JSessionID");
	}

	public String getServletPath() {
		return servletAddr;
	}

	public HttpSession getSession() {
		if (server.sessionMap.containsKey(cookieVals.get("JSessionID"))) {
			return server.sessionName.get(cookieVals.get("JSessionID"));
		} else {
			return new HSession(server, (ServContext) servlet.getServletContext(), "JSessionID=" + server.sessionCounter);
		}
			
	}

	public HttpSession getSession(boolean arg0) {
		if (server.sessionMap.containsKey(cookieVals.get("JSessionID"))) {
			return server.sessionName.get(cookieVals.get("JSessionID"));
		} else if (arg0) {
			return new HSession(server, (ServContext) servlet.getServletContext(), "JSessionID=" + server.sessionCounter);
		}
		return null;
	}

	public Principal getUserPrincipal() {
		return null;
	}

	public boolean isRequestedSessionIdFromCookie() {
		return cookieVals.containsKey("JSessionID");
	}

	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	public boolean isRequestedSessionIdValid() {
		return server.sessionMap.containsKey(cookieVals.get("JSessionID"));
	}

	public boolean isUserInRole(String arg0) {
		return false;
	}

}
