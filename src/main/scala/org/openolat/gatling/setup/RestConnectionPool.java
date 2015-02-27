package org.openolat.gatling.setup;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Implements a connection pool, each connection must have
 * an other user to log-in.
 *
 * Created by srosse on 20.02.15.
 */
public class RestConnectionPool implements Closeable {

	private final BlockingQueue<RestConnection> objects;

	public RestConnectionPool(Collection<RestConnection> objects) {
		this.objects = new ArrayBlockingQueue<>(objects.size(), false, objects);
	}

	public RestConnection borrow() {
		try {
			return objects.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void giveBack(RestConnection object) {
		if(object != null) {
			try {
				objects.put(object);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() {
		for(RestConnection object:objects) {
			try {
				object.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
