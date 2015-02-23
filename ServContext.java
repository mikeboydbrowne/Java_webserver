package edu.upenn.cis.cis455.webserver;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class ServContext implements ServletContext {

	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;
	
	public ServContext() {
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
	}
	
	public Object getAttribute(String arg0) {
		return attributes.get(arg0);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	public ServletContext getContext(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getInitParameter(String arg0) {
		return initParams.get(arg0);
	}

	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getMimeType(String arg0) {
		return null;
	}

	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public RequestDispatcher getNamedDispatcher(String arg0) {
		return null;
	}

	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null;
	}

	public URL getResource(String arg0) throws MalformedURLException {
		return null;
	}

	public InputStream getResourceAsStream(String arg0) {
		return null;
	}

	public Set getResourcePaths(String arg0) {
		return null;
	}

	public String getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public Servlet getServlet(String arg0) throws ServletException {
		return null;
	}

	public String getServletContextName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration getServletNames() {
		return null;
	}

	public Enumeration getServlets() {
		return null;
	}

	public void log(String arg0) {
	}

	public void log(Exception arg0, String arg1) {
	}
	
	public void log(String arg0, Throwable arg1) {
	}

	public void removeAttribute(String arg0) {
		attributes.remove(arg0);
	}

	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}

}
