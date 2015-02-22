package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RequestThread implements Runnable {

	Server			server;
	ServerSocket 	socket;
	ServerQueue		queue;
	int 			portNum;
	Boolean			listening;
	
	public RequestThread(ServerQueue queue, int portNum, Server server) {
		try {
			this.server = server;
			this.socket = new ServerSocket(portNum);
			this.queue = queue;
			this.listening = true;
			this.portNum = portNum;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void addToQueue(Socket s) throws InterruptedException {
		
		//wait if the queue is full
		while (queue.size() > 300) {
			// Synchronizing on the sharedQueue to make sure no more than one
			// thread is accessing the queue same time.
			synchronized (queue) {
				System.out.println("Queue is full!");
				queue.wait();
				// We use wait as a way to avoid polling the queue to see if
				// there was any space for the producer to push.
			}
		}

		//Adding element to queue and notifying all waiting consumers
		synchronized (queue) {
			queue.enqueue(s);
			queue.notifyAll();
		}
	}
	
	public void run() {
		while (listening) {
			try {
				addToQueue(socket.accept());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void interrupt() throws IOException {
		socket.close();
		listening = false;
	}

}
