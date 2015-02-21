package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.Queue;

class HttpServer {
  
  static ServerQueue requestQueue;
  static Server server;
	
  public static void main(String args[])
  {
    if ((args.length < 3) && (Integer.parseInt(args[0]) > 0)) {
    	requestQueue = new ServerQueue();
        server = new Server(args[0], args[1], requestQueue);
    } else {
    	
    }
  }
  
}
