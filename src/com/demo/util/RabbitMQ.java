package com.demo.util;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import com.rabbitmq.client.Connection;

public class RabbitMQ {

	static Connection mainconnection = null;
	
	private static final ConcurrentHashMap<Integer, Connection> ConnectionMap = new ConcurrentHashMap<Integer, Connection>();
	
	public static void addRabbitMQ(Connection connection) {
		if (connection == null)
			throw new IllegalArgumentException("connection can not be null");
		if (ConnectionMap.containsKey(connection.getPort()))
			throw new IllegalArgumentException("The connection name already exists");
		
		ConnectionMap.put(connection.getPort(), connection);
		if (mainconnection == null)
			mainconnection = connection;
	}
	
	
	public static Connection use(Integer connectionPort) {
		return ConnectionMap.get(connectionPort);
	}
	
	public static Connection removeConnection(Integer connectionPort) {
		return ConnectionMap.remove(connectionPort);
	}
	
	public static void closeAllConnection(){
		for(Connection c : ConnectionMap.values()) {
			try {
				c.close();
			} catch (IOException e) {
				throw new IllegalArgumentException("connection can not close");
			}
		}
		ConnectionMap.clear();
		
	}
}
