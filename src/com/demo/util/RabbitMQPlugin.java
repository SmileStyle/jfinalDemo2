package com.demo.util;

import java.io.IOException;

import com.jfinal.kit.LogKit;
import com.jfinal.plugin.IPlugin;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQPlugin implements IPlugin{

	

	private String host;
	private Integer port;
	private String username;
	private String password;
	private String VirtualHost="/";
	
	private ConnectionFactory connectionFactory;
	private boolean isStarted = false;
	
	
	
	public RabbitMQPlugin(String host, Integer port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	public RabbitMQPlugin(String host, Integer port, String username, String password, String VirtualHost) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.VirtualHost = VirtualHost;
	}
	
	
	@Override
	public boolean start() {
		if(isStarted){
			return true;
		}
        connectionFactory = new ConnectionFactory();
        //2.通过connectionFactory设置RabbitMQ所在IP等信息
        connectionFactory.setHost(host);
        connectionFactory.setPort(port); //指定端口
        connectionFactory.setUsername(username);//用户名
        connectionFactory.setPassword(password);//密码
//        connectionFactory.setVirtualHost(VirtualHost);
        //3.通过connectionFactory创建一个连接connection
        try {
			Connection connection = connectionFactory.newConnection();
			RabbitMQ.addRabbitMQ(connection);
		} catch (Exception e) {
			return false;
		}
		
		return true;
			
	}

	@Override
	public boolean stop() {
		RabbitMQ.closeAllConnection();
		return true;
	}

}
