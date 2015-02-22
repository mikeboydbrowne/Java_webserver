package edu.upenn.cis.cis455.webserver;

import java.net.Socket;
import java.util.ArrayList;

public class ServerQueue {
	
	ArrayList<Socket> queue;
	
	// constructor => initializes the underlying ArrayList
	public ServerQueue() {
		this.queue = new ArrayList<Socket>();
	}
	
	// adds an element to the back of the queue
	public int enqueue(Socket s) {
		if(queue.add(s)) {
			return 1;
		} else {
			return 0;
		}
	}
	
	// removes an element from the front of the queue
	public Socket dequeue() {
		return queue.remove(0);
	}
	
	// returns whether the queue is empty or not
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public int size() {
		return queue.size();
	}
	
}
