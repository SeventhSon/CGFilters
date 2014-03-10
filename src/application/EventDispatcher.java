package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class EventDispatcher extends Thread {

	Queue<Event> mEventQueue;
	Semaphore mEventSemaphore;
	Map<String, ArrayList<Listener>> mListeners;

	private static volatile EventDispatcher instance = null;

	public static EventDispatcher getInstance() {
		if (instance == null) {
			synchronized (EventDispatcher.class) {
				if (instance == null) {
					instance = new EventDispatcher();
				}
			}
		}
		return instance;
	}
	
	private EventDispatcher() {
		setDaemon(true);
		mEventQueue = new ConcurrentLinkedQueue<Event>();
		mEventSemaphore = new Semaphore(0);
		mListeners = new HashMap<String, ArrayList<Listener>>();
		start();
	}

	public void notifyListeners(Event e) {
		if (e == null)
			return;
		ArrayList<Listener> listeners = mListeners.get(e.getClass().toString());
		if(listeners==null||listeners.isEmpty())
			return;
		for(Listener l: listeners)
			e.notify(l);
	}

	public void publish(Event e){
		mEventQueue.add(e);
		mEventSemaphore.release();
	}
	
	public void subscribe(Listener l, String eventType){
		ArrayList<Listener> listeners = mListeners.get(eventType);
		if(listeners==null){
			listeners = new ArrayList<Listener>();
			mListeners.put(eventType, listeners);
		}
		listeners.add(l);
	}
	
	@Override
	public void run() {
		while (true) {
			for (int i = 0; i < mEventQueue.size(); i++) {
				notifyListeners(mEventQueue.poll());
			}
			try {
				sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}


