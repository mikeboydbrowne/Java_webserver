package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.Queue;

class HttpServer {
  
  static ServerQueue requestQueue;
  static Server server;
	
  public static void main(String args[])
  {
    // simple HTTP server
	if ((args.length == 3) && ((Integer.parseInt(args[0]) > 0) && (Integer.parseInt(args[0]) < 65536))) {
    	requestQueue = new ServerQueue();
        server = new Server(args[0], args[1], requestQueue);
    
    // servlet engine
	} else if ((args.length == 3) && ((Integer.parseInt(args[0]) > 0) && (Integer.parseInt(args[0]) < 65536))) {
    	
    }
  }
  
}
