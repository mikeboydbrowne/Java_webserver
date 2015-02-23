package edu.upenn.cis.cis455.webserver;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ServConfig implements ServletConfig {

	private String 					name;
	private ServContext 			context;
	private HashMap<String,String> 	initParams;
	
	public ServConfig(String name, ServContext context) {
		this.name = name;
		this.context = context;
		initParams = new HashMap<String,String>();
	}
	
	public String getInitParameter(String arg0) {
		return initParams.get(arg0);
	}

	@SuppressWarnings("rawtypes")
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	public ServletContext getServletContext() {
		return context;
	}

	public String getServletName() {
		return name;
	}
	
	void setInitParam(String name, String value) {
		initParams.put(name, value);
	}

}
