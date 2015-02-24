package edu.upenn.cis.cis455.webserver;

class HttpServer {
  
  static ServerQueue 	requestQueue;
  static Server 		server;
  static ServletEngine 	servletEngine;
	
  public static void main(String args[])
  {
    // simple HTTP server
	if ((args.length == 2) && ((Integer.parseInt(args[0]) > 0) && (Integer.parseInt(args[0]) < 65536))) {
    	
		requestQueue = new ServerQueue();						// instantiating the server queue
        server = new Server(args[0], args[1], requestQueue);	// starting the HTTP Server
    
    // servlet engine
	} else if ((args.length == 3) && ((Integer.parseInt(args[0]) > 0) && (Integer.parseInt(args[0]) < 65536))) {
    	
		requestQueue = new ServerQueue();											// instantiating the server queue
		servletEngine = new ServletEngine(args[0], args[1], args[2], requestQueue);	// starting the Servlet Engine
		
    }
  }
  
}
