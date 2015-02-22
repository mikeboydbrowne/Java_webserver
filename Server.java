package edu.upenn.cis.cis455.webserver;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class Server {
	
	final int 							portNumber;
	final String						absolutePath;
	ServerQueue							queue;
	Set<Thread> 						threadPool;
	HashMap<Thread, ProcessingThread>	threadMap;
	HashMap<ProcessingThread, String> 	reqMap;
	Thread								daemonThread;
	ServerSocket 						serverSocket;
	Socket								clientSocket;
	BufferedReader						clientInput;
	BufferedImage						imageInput;
	OutputStream 						serverOutput;
	Date 								currentDate;
	Boolean								processing;
	
	public Server(String portNum, String absolutePath, ServerQueue queue) {
		this.portNumber		= Integer.parseInt(portNum);
		this.absolutePath	= absolutePath;
		this.queue 			= queue;
		this.currentDate	= new Date();
		this.processing		= true;
		
		// creating the daemon thread
		daemonThread = new Thread(new RequestThread(queue, portNumber, this));
		
		// creating the threadpool
		int numThreads = 120;
		this.threadPool = new HashSet<Thread>();
		this.threadMap 	= new HashMap<Thread, ProcessingThread>();
		this.reqMap		= new HashMap<ProcessingThread, String>();
		for (int i = 0; i < numThreads; i++) {
			ProcessingThread procThread = new ProcessingThread(queue, absolutePath, this);
			Thread reqThread 			= new Thread(procThread);
			
			threadPool.add(reqThread);
			threadMap.put(reqThread, procThread);
			reqMap.put(procThread, "Not processing a URL");
		}
		
		// processing requests
		processRequests();
	}
	
	public int shutdown() {
		
		// stopping the daemon thread
		daemonThread.interrupt();
		
		while (!queue.isEmpty()) {
			System.out.println("Queue hasn't been emptied");
		}
		
		// processing the remaining requests
		if (queue.isEmpty()) {
			for (Thread s : threadPool) {
				s.interrupt();
			}
		}
		
		for (Thread t : threadPool) {
			if (!t.isInterrupted()) {
				t.interrupt();
			}
		}
		
		System.out.println("Server is shut down!");
		
		System.exit(1);
		
		return 1;
	}
	
	public int processRequests() {
		
			
		// starting up the daemon thread
		daemonThread.start();

		// starting the threads in the threadPool
		for (Thread s : threadPool) {
			s.start();
		}
		
		return 1;
		
	}
}
