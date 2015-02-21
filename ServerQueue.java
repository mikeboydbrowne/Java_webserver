package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;

public class ServerQueue {
	
	ArrayList<String> queue;
	
	// constructor => initializes the underlying ArrayList
	public ServerQueue() {
		this.queue = new ArrayList<String>();
	}
	
	// adds an element to the back of the queue
	public int enqueue(String s) {
		if(queue.add(s)) {
			return 1;
		} else {
			return 0;
		}
	}
	
	// removes an element from the front of the queue
	public String dequeue() {
		return queue.remove(0);
	}
	
	// returns whether the queue is empty or not
	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
