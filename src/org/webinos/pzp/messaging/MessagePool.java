/**
 * 
 */
package org.webinos.pzp.messaging;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author johl
 *
 */
public class MessagePool {

	Queue<WebinosMessage> messageQueue = new ConcurrentLinkedQueue<WebinosMessage>();
	Queue<WebinosMessage> unhandledMessages = new ConcurrentLinkedQueue<WebinosMessage>();
	
	Set<MessageConsumer> consumers = new HashSet<>();
	Set<MessageProducer> producers = new HashSet<>();
	
	public void addConsumer(MessageConsumer consumer) {
		consumers.add(consumer);
		processUnhandled();
	}
	
	public void addProducer(MessageProducer producer) {
		producers.add(producer);
	}
	
	public synchronized void addMessage(WebinosMessage msg) {
		messageQueue.add(msg);
		processQueue();
	}
	
	private void processUnhandled() {
		Iterator<WebinosMessage> it = unhandledMessages.iterator();
		while (it.hasNext()) {
			WebinosMessage msg = it.next();
			if (handleMessage(msg)) {
				unhandledMessages.remove(msg);
			}
		}
	}
	
	private boolean handleMessage(WebinosMessage msg) {
		for (MessageConsumer consumer : consumers) {
			if (consumer.acceptsMessage(msg)) {
				consumer.consume(msg);
				return true;
			}
		}
		return false;
	}
	
	private void processQueue() {
		while (!messageQueue.isEmpty()) {
			WebinosMessage msg = messageQueue.poll();
			if (msg != null) {
				if (!handleMessage(msg)) {
					unhandledMessages.add(msg);
					System.out.println("Could not find a processor for " + msg.getType());
				}
			}
		}
	}
	
}
