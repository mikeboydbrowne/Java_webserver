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
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

public class Server {
	
	final int 				portNumber;
	final String			absolutePath;
	ServerQueue				queue;
	Set<ProcessingThread> 	threadPool;
	RequestThread			daemonThread;
	ServerSocket 			serverSocket;
	Socket					clientSocket;
	BufferedReader			clientInput;
	BufferedImage			imageInput;
	OutputStream 			serverOutput;
	Date 					currentDate;
	Boolean					processing;
	
	public Server(String portNum, String absolutePath, ServerQueue queue) {
		this.portNumber		= Integer.parseInt(portNum);
		this.absolutePath	= absolutePath;
		this.queue 			= queue;
		this.currentDate	= new Date();
		this.processing		= true;
		
		// creating the threadpool
		int numThreads = 100;
		
		this.threadPool = new HashSet<ProcessingThread>();
		for (int i = 0; i < numThreads; i++) {
			threadPool.add(new ProcessingThread(queue, absolutePath));
			
		}
		
		// creating the daemon thread
		daemonThread = new RequestThread(queue, portNumber);
		daemonThread.run();
		
		// processing requests
		processRequests();
	}
	
	public int shutdown() {
		
		this.processing = false;
		
		// close the server socket socket
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// stopping the daemon thread
		daemonThread.stop();
		
		// processing the remaining requests
		while (!queue.isEmpty()) {
			for (ProcessingThread s : threadPool) {
				s.run();
			}
		}
		
		return 1;
	}
	
	public int processRequests() {
		try {
			this.serverSocket 	= new ServerSocket(portNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (processing) {	
			// running all the threads in the threadPool to process all the requests
			while (!queue.isEmpty()) {
				for (ProcessingThread s : threadPool) {
					s.run();
				}
			}
		}
		
		return 1;

	}
}
