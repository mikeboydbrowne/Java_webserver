package edu.upenn.cis.cis455.webserver;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class HSession implements HttpSession {
	
	HashMap<String, Object>	attributes;
	ServletEngine			server;
	ServletContext			context;
	String					id;
	long 					creationTime;
	long					lastAccessed;
	int						maxInactive;
	boolean					isInvalid;
	boolean					isNew;
	
	public HSession(ServletEngine server, ServContext context, String sessionId) {
		this.server			= server;								// linking server
		this.context		= context;								// linking context
		this.creationTime 	= System.currentTimeMillis();			// setting the time at which the Session is created
		this.lastAccessed 	= System.currentTimeMillis();			// setting lastAccessed
		this.id				= sessionId;							// incrementing the sessionCounter
		this.isNew			= true;									// just instantiating to it is new
		this.attributes		= context.attributes;					// set attributes = to the context's
		server.sessionCounter++;
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

	public long getCreationTime() {
		return creationTime;
	}

	public String getId() {
		return id;
	}

	public long getLastAccessedTime() {
		return lastAccessed;
	}

	public int getMaxInactiveInterval() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ServletContext getServletContext() {
		return context;
	}

	// deprecated
	public Object getValue(String arg0) {
		return null;
	}

	// deprecated
	public String[] getValueNames() {
		return null;
	}

	public void invalidate() {
		isInvalid = true;
		attributes = new HashMap<String, Object>();
	}
	
	public boolean isInvalid() {
		return isInvalid;
	}

	
	public boolean isNew() {
		return isNew;
	}

	// deprecated
	public void putValue(String arg0, Object arg1) {
	}

	public void removeAttribute(String arg0) {
		attributes.remove(arg0);
	}

	// deprecated
	public void removeValue(String arg0) {
	}

	public void setAttribute(String arg0, Object arg1) {
		attributes.put(arg0, arg1);

	}

	public void setMaxInactiveInterval(int arg0) {
		maxInactive = arg0;
	}
	
	// deprecated
	@SuppressWarnings("deprecation")
	public HttpSessionContext getSessionContext() {
		return null;
	}

}
