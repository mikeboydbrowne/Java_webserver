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

public class ProcessingThread implements Runnable {

	final String	absolutePath;
	ServerQueue		queue;
	ServerSocket 	serverSocket;
	Socket			clientSocket;
	BufferedReader	clientInput;
	BufferedImage	imageInput;
	OutputStream 	serverOutput;
	Date 			currentDate;
	Boolean			status;
	
	public ProcessingThread(ServerQueue queue, String relativePath) {
		this.queue			= queue;
		this.status			= false;
		this.absolutePath 	= relativePath;
	}
	
	@Override
	public void run() {
		if (!queue.isEmpty()) {
			clientSocket = queue.dequeue();
			status = true;
			String request = "";
			DateFormat formatD = new SimpleDateFormat(
					"EEE, dd MMM yyyy HH:mm:ss zzz");

			try {
				clientInput = new BufferedReader(new InputStreamReader(
						clientSocket.getInputStream()));
				// parsing the request
				String reqLine = clientInput.readLine();
				while (!reqLine.equalsIgnoreCase("")) {
					request += "\r\n" + reqLine;
					reqLine = clientInput.readLine();
				}

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// getting the name of the requested resource
			request = request.substring(2);
			String[] requestParams = request.split("\n");
			String[] fileReq = requestParams[0].split(" ");
			String fileName = fileReq[1];
			String res = "";

			// requesting a directory
			if (fileName.endsWith("/")) { // directory

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

					res = "HTTP/1.1 404 Not Found\r\n"
							+ "Date: "
							+ formatD.format(currentDate)
							+ "\r\n"
							+ "Content-Type: text/html; charset=UTF-8\r\n"
							+ "Connection: close\r\n\r\n"
							+ "<html>\r\n" + "<head>\r\n"
							+ "<title>404 Not Found</title>\r\n"
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
							+ "<h1>Requested Page Not Found</h1>\r\n"
							+ "</body>" + "</html>";

				} catch (IOException e) {
					e.printStackTrace();
				}

				// requesting a file
			} else {

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
					res = "HTTP/1.1 404 Not Found\r\n"
							+ "Date: "
							+ formatD.format(currentDate)
							+ "\r\n"
							+ "Content-Type: text/html; charset=UTF-8\r\n"
							+ "Connection: close\r\n\r\n" 
							+ "<html>\r\n" + "<head>\r\n"
							+ "<title>404 Not Found</title>\r\n"
							+ "</head>\r\n" + "<body>\r\n"
							+ "<h1>Requested Page Not Found</h1>\r\n"
							+ "</body>" + "</html>";

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// responding to the request
			try {
				serverOutput = clientSocket.getOutputStream();

				// image requests
				if (fileName.endsWith(".jpg") | fileName.endsWith(".png") | fileName.endsWith(".gif")) {
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
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	

	}

}
