package edu.upenn.cis.cis455.webserver;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

public class Server {
	
	final int 		portNumber;
	final String	absolutePath;
	ServerQueue 	queue;
	ServerSocket 	serverSocket;
	Socket			clientSocket;
	BufferedReader	clientInput;
	OutputStream 	serverOutput;
	Date 			currentDate;
	Boolean			listening;
	
	public Server(String portNumber, String absolutePath, ServerQueue queue) {
		this.portNumber		= Integer.parseInt(portNumber);
		this.absolutePath	= absolutePath;
		this.queue 			= queue;
		this.currentDate	= new Date();
		this.listening		= true;
		recieveRequests();
	}
	
	public int shutdown() {
		
		this.listening = false;
		
		// Close the socket
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;
	}
	
	public int recieveRequests() {
		try {
			this.serverSocket 	= new ServerSocket(portNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
		DateFormat formatD	= new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			
		while (true) {
			
			String request = "";
			
			// getting the client request
			try {
				clientSocket 	= serverSocket.accept();
				clientInput		= new BufferedReader(new 
									InputStreamReader(clientSocket.getInputStream()));
			
				// parsing the request
				String reqLine = clientInput.readLine();
				while (!reqLine.equalsIgnoreCase("")) {
					request += "\r\n" + reqLine;
					reqLine = clientInput.readLine();
				}
			
			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// getting the name of the requested resource 
			request = request.substring(2);
			String[] requestParams 	= request.split("\n");
			String[] fileReq		= requestParams[0].split(" ");
			String fileName			= fileReq[1];
			String res = "";
			
			// Requesting a directory
			if (fileName.endsWith("/")) {	// directory
				
				try {
					File folder = new File(absolutePath + fileName);
					File[] listOfFiles = folder.listFiles();
				
					// creating the request
					res = "HTTP/1.1 200 OK\r\n"		// Basic request header
							+ "Date: " +  formatD.format(currentDate)+ "\r\n"
							+ "Content-Type: text/html; charset=UTF-8\r\n"
							+ "Connection: close\r\n\r\n"
							+ "<html>\r\n"
							+ "<head>\r\n"
							+ "<title>Hellow WWW</title>\r\n"
							+ "</head>\r\n"
							+ "<body>\r\n"
							+ "<h1>List of Files & Directories</h1>\r\n";		
				
					for (int i = 0; i < listOfFiles.length; i++) {
						if (listOfFiles[i].isFile()) {
							res += "File: " + listOfFiles[i].getName() + "<br>";
						} else if (listOfFiles[i].isDirectory()) {
							res += "Directory: " + listOfFiles[i].getName() + "<br>";
						}
					}
					
					res +=  "</body>\r\n</html>\r\n";
				
				// in case the directory doesn't exist
				} catch (NullPointerException n) {
					n.printStackTrace();
					
					res = "HTTP/1.1 404 Not Found\r\n"
							+ "Date: " +  formatD.format(currentDate)+ "\r\n"
							+ "Content-Type: text/html; charset=UTF-8\r\n"
							+ "Connection: close\r\n\r\n"		// Basic request header
							+ "<html>\r\n"
							+ "<head>\r\n"
							+ "<title>404 Not Found</title>\r\n"
							+ "</head>\r\n"
							+ "<body>\r\n"
							+ "<h1>Requested Directory Not Found</h1>\r\n"
							+ "</body>"
							+ "</html>";
				}
			
			// Requesting an image
			} else if (fileName.endsWith(".jpg")) {
				
				fileName = absolutePath + fileName;
				File image = new File(fileName);
				FileInputStream imageInput = null;
				
				try {
					
					imageInput = new FileInputStream(image);
					BufferedInputStream imageStream = new BufferedInputStream(imageInput);
					
					int imgContent;
					
					// creating the request
					res = "HTTP/1.1 200 OK\r\n"
							+ "Date: " +  formatD.format(currentDate)+ "\r\n"
							+ "Content-Type: image/jpg\r\n"
							+ "Connection: close\r\n\r\n";		// Basic request header	
					
					while((imgContent = imageStream.read()) != -1) {
						String text = Integer.toString(imgContent, 2);
						while (text.length() < 8) {
							text = "0" + text;
						}
						res += text;
						System.out.println("Reading image: " + text);
					}
					
					imageStream.close();
					System.out.println("Done reading image");
					
//					Raster imageRaster = null;
//					DataBufferByte imageData = null;
//					int numArrs = 0;
//					
//					byte[] imageContent = Files.readAllBytes(image.toPath());
//					
//					res += imageContent;
					
//					imageRaster = imageInput.getData();
//					imageData = (DataBufferByte) imageRaster.getDataBuffer();
//					numArrs = imageData.getNumBanks() + 5;
//					
//					System.out.println(numArrs);
//					
//					while(numArrs > 0) {
//						res += imageData.getData();
//						numArrs--;
//					}
					
					
					
//					res += imageData.getData();
					
//					imageInput.
					
//					imageRaster.
//					
//					while ((dataBit = imageInput.get) != -1) {
//						res += ) dataBit;
//						System.out.println("Reading in image data: " + dataBit);
//					}
					
					System.out.println("I finish reading image data");
					System.out.println(res);
				
//					imageInput.close();
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					
					// creating the request
					res = "HTTP/1.1 404 Not Found\r\n"
							+ "Date: " +  formatD.format(currentDate)+ "\r\n"
							+ "Content-Type: text/html; charset=UTF-8\r\n"
							+ "Connection: close\r\n\r\n"		// Basic request header
							+ "<html>\r\n"
							+ "<head>\r\n"
							+ "<title>404 Not Found</title>\r\n"
							+ "</head>\r\n"
							+ "<body>\r\n"
							+ "<h1>Requested Page Not Found</h1>\r\n"
							+ "</body>"
							+ "</html>";
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			
			// Requesting a file
			} else  {
				
				fileName = absolutePath + fileName;
				File file = new File(fileName);			// opening the file
				FileInputStream fileInput = null;
			
				// opening up the file stream
				try {
					fileInput = new FileInputStream(file);
				
					// creating the request
					res = "HTTP/1.1 200 OK\r\n"
							+ "Date: " +  formatD.format(currentDate)+ "\r\n"
							+ "Content-Type: text/html; charset=UTF-8\r\n"
							+ "Connection: close\r\n\r\n";		// Basic request header
					
					int content;
					while((content = fileInput.read()) != -1) {	// Appending the html
						res += (char) content;
					}
					fileInput.close();							// Closing the file stream
				
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					
					// creating the request
					res = "HTTP/1.1 404 Not Found\r\n"
							+ "Date: " +  formatD.format(currentDate)+ "\r\n"
							+ "Content-Type: text/html; charset=UTF-8\r\n"
							+ "Connection: close\r\n\r\n"		// Basic request header
							+ "<html>\r\n"
							+ "<head>\r\n"
							+ "<title>404 Not Found</title>\r\n"
							+ "</head>\r\n"
							+ "<body>\r\n"
							+ "<h1>Requested Page Not Found</h1>\r\n"
							+ "</body>"
							+ "</html>";
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// Responding to the request
			try {
				serverOutput	= clientSocket.getOutputStream();
				serverOutput.write(res.getBytes());
				clientSocket.close();
			} catch (IOException e) {
				
			}
				
		}
		
	}
	
}
