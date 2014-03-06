package application;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class MessageDispatcher extends Thread {

	Queue<Message> mMessageQueue;
	Semaphore mMessageSemaphore;
	public MessageDispatcher() {
		setDaemon(true);
		mMessageQueue = new ConcurrentLinkedQueue<Message>();
		mMessageSemaphore = new Semaphore(0);
	}
	@Override
	public void run() {
		while(true){
			for(Message m : mMessageQueue);
				
		}
	}
	
}
