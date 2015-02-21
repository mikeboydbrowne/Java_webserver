package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;

public class RequestThread implements Runnable {

	ServerSocket 	socket;
	ServerQueue		queue;
	int 			portNum;
	Boolean			listening;
	
	public RequestThread(ServerQueue queue, int portNum) {
		try {
			this.socket = new ServerSocket(portNum);
			this.queue = queue;
			this.listening = true;
			this.portNum = portNum;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	@Override
	public void run() {
		int counter = 0;
		while (listening) {
			if (counter > 5) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				counter = 0;
			}
			try {
				queue.enqueue(socket.accept());
			} catch (IOException e) {
				e.printStackTrace();
			}
			counter++;
		}
	}
	
	public void stop() {
		listening = false;
	}

}
