package edu.upenn.cis.cis455.webserver;

import java.io.File;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ServletEngine {
	
	int 	port;
	String 	absolutePath;
	String 	webxmlPath;
	
	public ServletEngine(String portNum, String absolutePath, String webxmlPath) {
		port 				= Integer.parseInt(portNum);	// setting port to listen on
		this.absolutePath 	= absolutePath;					// setting absolute path for requests
		this.webxmlPath 	= webxmlPath;					// setting path to web.xml	
		run();												// running the engine
	}
	
	private int run() {
		try {
			SHandler 	servHandler	= parseWebdotxml(webxmlPath);	// creating the handler for the servlet
			ServContext servContext	= createContext(servHandler);	// initializing the servlet's context
			
			HashMap<String,HttpServlet> servlets = createServlets(servHandler, servContext);
		
		} catch (Exception e) {
			e.printStackTrace();
			return -1;				// returns -1 on error
		}
		
		return 1;					// returns 1 on sucessful completion
	}
	
	/**
	 * @param  webdotxml	- path to web.xml
	 * @return SHandler		- servHandler object
	 * @throws Exception
	 * 
	 * Parses the web.xml file at the given location and returns a SHandler with
	 * the attributes obtained.
	 */
	
	private SHandler parseWebdotxml(String webdotxml) throws Exception {
		SHandler h = new SHandler();
		
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	/**
	 * @param  h			- servletHandler object
	 * @return ServContext	- servContext object
	 * 
	 * Given an SHandler, creates and returns an appropriate ServContext 
	 */
	
	private ServContext createContext(SHandler h) {
		ServContext fc = new ServContext();
		for (String param : h.m_contextParams.keySet()) {
			fc.setInitParam(param, h.m_contextParams.get(param));
		}
		return fc;
	}
	
	/**
	 * 
	 * @param h		- servletHandler object
	 * @param sc	- servletContext object
	 * @return
	 * @throws Exception
	 * 
	 * Given an SHandler and ServContext, creates a hashmap of servlet names and
	 * instantiated servlets. Initializes them with a configuration based off of
	 * the context instantiated in createContext(). 
	 */
	
	private HashMap<String,HttpServlet> createServlets(SHandler h, ServContext sc) throws Exception {
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.m_servlets.keySet()) {
			ServConfig config = new ServConfig(servletName, sc);
			String className = h.m_servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}

}
