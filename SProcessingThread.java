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

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class SProcessingThread implements Runnable {

	final String 	absolutePath;
	ServletEngine 	server;
	ServerQueue 	queue;
	ServerSocket 	serverSocket;
	Socket 			clientSocket;
	String 			fileName;
	String			urlMatch;
	String			urlOrig;
	String[]		cookieInfo;
	BufferedReader 	clientInput;
	BufferedImage 	imageInput;
	OutputStream 	serverOutput;
	Date 			currentDate;
	Boolean 		processing;

	public SProcessingThread(ServerQueue queue, String relativePath, ServletEngine server) {
		this.server			= server;
		this.queue			= queue;
		this.processing		= true;
		this.absolutePath	= relativePath;
		currentDate			= server.currentDate;

	}
	
	public Socket readFromQueue() {
		while (queue.isEmpty()) {
			// If the queue is empty, we push the current thread to waiting
			// state. Way to avoid polling.
			synchronized (queue) {
				System.out.println("Queue is currently empty ");
				try {
					queue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		// Otherwise consume element and notify waiting producer
		synchronized (queue) {
			queue.notifyAll();
			return queue.dequeue();
		}
	}
	
	public void run() {
		while (processing) {
			clientSocket 		= readFromQueue();	// get socket
			
			// processing request
			String request = "";
			try {
				
				// getting input stream from socket
				clientInput 		= new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String reqLine = clientInput.readLine();
				boolean firstLine = true;
				while (!reqLine.equalsIgnoreCase("")) {
					if (firstLine) {
						request += reqLine;
						firstLine = false;
					} else {
						request += "\r\n" + reqLine;
					}
					reqLine = clientInput.readLine();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			// parsing request
			String[]	requestParams 	= request.split("\n");
			String[]	fileReq 		= requestParams[0].split(" ");
			boolean servletReq			= false;			
			fileName 					= fileReq[1];
			urlOrig = "";
			
			// checking to see if request matches special servlet URL
			for (String s : server.urlMap.keySet()) {
				urlMatch = "";
				
				// modifying s to match with fileName
				if (s.endsWith("/*")) {
					urlMatch 	= s.substring(0, s.length() - 1);
				} else if (s.endsWith("*"))  {
					urlMatch 	= s.substring(0, s.length() - 1);
				}
				
				// matching with urls
				if (fileName.startsWith(urlMatch) || fileName.startsWith("/" + urlMatch) || fileName.startsWith(urlMatch + "/")) {
					urlOrig = s;
					servletReq = true;
				}
			}
			
			// run the correct type of request
			if (servletReq) {
				try {
					runServletRequest(request, clientInput);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				runServerRequest();
			}
		}
	}
		
	@SuppressWarnings("null")
	public int runServletRequest(String reqText, BufferedReader reader) throws IOException {
		
		// get the servlet to be run
		HttpServlet currServlet = server.servlets.get(server.urlMap.get(urlOrig));
		
		// creating the http request
		HRequest servReq = new HRequest(clientSocket, reader, reqText, urlMatch, server, currServlet);
		
//		// checking to see if the session is new or associated with the currentServlet
//		if (!server.sessionMap.containsKey(servSession.id)) {
//			server.sessionMap.put(servSession.id, currServlet); // new session
//		} else if (server.sessionMap.get(servSession.id) != currServlet) {
//			throw new IllegalStateException(); // session is incorrectly associated
//		}
		HSession servSession = (HSession) servReq.getSession();
		
		HResponse servRes = new HResponse(clientSocket.getOutputStream(), server, servSession, currServlet);
		
		try {
			currServlet.service(servReq, servRes);
			servRes.flushBuffer();
		} catch (ServletException e) {
			e.printStackTrace();
		}
		
		clientSocket.close();
		
		return 1;
	}

	public int runServerRequest() {
		DateFormat formatD 	= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
		server.reqMap.put(this, fileName);
		String res = "";

		// requesting a directory
		if (fileName.endsWith("/")) {

			try {
				File folder = new File(absolutePath + fileName);
				File[] listOfFiles = folder.listFiles();

				// creating the request
				res = "HTTP/1.1 200 OK\r\n" + "Date: "
						+ formatD.format(currentDate) + "\r\n"
						+ "Content-Type: text/html; charset=UTF-8\r\n"
						+ "Connection: close\r\n\r\n" + "<html>\r\n"
						+ "<head>\r\n" + "<title>Hellow WWW</title>\r\n"
						+ "</head>\r\n" + "<body>\r\n"
						+ "<h1>List of Files & Directories</h1>\r\n";

				for (int i = 0; i < listOfFiles.length; i++) {
					if (listOfFiles[i].isFile()) {
						res += "File: " + listOfFiles[i].getName() + "<br>";
					} else if (listOfFiles[i].isDirectory()) {
						res += "Directory: " + listOfFiles[i].getName()
								+ "<br>";
					}
				}

				res += "</body>\r\n</html>\r\n";

				// in case the directory doesn't exist
			} catch (NullPointerException n) {
				n.printStackTrace();

				res = "HTTP/1.1 404 Not Found\r\n" + "Date: "
						+ formatD.format(currentDate) + "\r\n"
						+ "Content-Type: text/html; charset=UTF-8\r\n"
						+ "Connection: close\r\n\r\n" + "<html>\r\n"
						+ "<head>\r\n" + "<title>404 Not Found</title>\r\n"
						+ "</head>\r\n" + "<body>\r\n"
						+ "<h1>Requested Directory Not Found</h1>\r\n"
						+ "</body>" + "</html>";
			}

			// requesting an image
		} else if (fileName.endsWith(".jpg") || fileName.endsWith(".gif")
				|| fileName.endsWith(".png")) {

			fileName = absolutePath + fileName;
			File image = new File(fileName);
			imageInput = null;

			try {

				imageInput = ImageIO.read(image);

				// creating the request
				res = "HTTP/1.1 200 OK\r\n" + "Date: "
						+ formatD.format(currentDate) + "\r\n"
						+ "Content-Type: image/jpg\r\n"
						+ "Connection: close\r\n\r\n";

			} catch (FileNotFoundException e) {
				e.printStackTrace();

				// creating the request
				res = "HTTP/1.1 404 Not Found\r\n" + "Date: "
						+ formatD.format(currentDate) + "\r\n"
						+ "Content-Type: text/html; charset=UTF-8\r\n"
						+ "Connection: close\r\n\r\n" + "<html>\r\n"
						+ "<head>\r\n" + "<title>404 Not Found</title>\r\n"
						+ "</head>\r\n" + "<body>\r\n"
						+ "<h1>Requested Page Not Found</h1>\r\n" + "</body>"
						+ "</html>";

			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}

			// requesting a file
		} else {

			// special '/shutdown' case
			if (fileName.equalsIgnoreCase("/shutdown")) {
				server.shutdown();

				// special '/control' case
			} else if (fileName.equalsIgnoreCase("/control")) {
				// creating the request
				res = "HTTP/1.1 200 OK\r\n"
						+ "Date: "
						+ formatD.format(currentDate)
						+ "\r\n"
						+ "Content-Type: text/html; charset=UTF-8\r\n"
						+ "Connection: close\r\n\r\n"
						+ " <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js\"></script>"
						+ "<script>\n"
						+ "function shutdownGet() {\n"
						+ "  console.log(\"I sense this!\");\n"
						+ "  $.get(\"http://localhost:"
						+ server.port
						+ "/shutdown"
						+ "\");\n"
						+ "}\n"
						+ "</script>"
						+ "Student: Mike Browne</br>"
						+ "Penn Login: mbrowne</br></br>"
						+ "<button onclick=\"shutdownGet()\"> Shutdown </button></br></br>"
						+ "##### Threads #####</br></br>";

				for (Thread t : server.threadPool) {
					if (t.getState().toString().equalsIgnoreCase("RUNNABLE")) {
						res += t.getName() + "   URL: "
								+ server.reqMap.get(server.threadMap.get(t))
								+ "</br>";
					} else {
						res += t.getName() + "   State: " + t.getState()
								+ "</br>";
					}
				}
			} else {

				// normal file requests
				fileName = absolutePath + fileName;
				File file = new File(fileName); // opening the file
				FileInputStream fileInput = null;

				// opening up the file stream
				try {
					fileInput = new FileInputStream(file);

					// creating the request
					res = "HTTP/1.1 200 OK\r\n" + "Date: "
							+ formatD.format(currentDate) + "\r\n"
							+ "Content-Type: text/html; charset=UTF-8\r\n"
							+ "Connection: close\r\n\r\n";
					int content;
					while ((content = fileInput.read()) != -1) {
						res += (char) content;
					}
					fileInput.close(); // Closing the file stream

				} catch (FileNotFoundException e) {
					e.printStackTrace();

					// creating the request
					res = "HTTP/1.1 404 Not Found\r\n" + "Date: "
							+ formatD.format(currentDate) + "\r\n"
							+ "Content-Type: text/html; charset=UTF-8\r\n"
							+ "Connection: close\r\n\r\n" + "<html>\r\n"
							+ "<head>\r\n" + "<title>404 Not Found</title>\r\n"
							+ "</head>\r\n" + "<body>\r\n"
							+ "<h1>Requested Page Not Found</h1>\r\n"
							+ "</body>" + "</html>";

				} catch (IOException e) {
					e.printStackTrace();
					return -1;
				}
			}
		}

		// responding to the request
		try {
			serverOutput = clientSocket.getOutputStream();

			// image requests
			if (fileName.endsWith(".jpg") | fileName.endsWith(".png")
					| fileName.endsWith(".gif")) {
				if (fileName.endsWith(".jpg")) {
					serverOutput.write(res.getBytes());
					ImageIO.write(imageInput, "jpg", serverOutput);
				} else if (fileName.endsWith(".png")) {
					serverOutput.write(res.getBytes());
					ImageIO.write(imageInput, "png", serverOutput);
				} else if (fileName.endsWith(".gif")) {
					serverOutput.write(res.getBytes());
					ImageIO.write(imageInput, "gif", serverOutput);
				}

				// page requests
			} else {
				serverOutput.write(res.getBytes());
			}
			clientSocket.close();
			Thread.sleep(50);
			server.reqMap.put(this, "Not processing a URL");
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
		return 1;
	}

}
