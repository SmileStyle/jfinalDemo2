package com.demo.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.demo.util.RabbitMQConfig;
import com.demo.util.RabbitMQ;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.rabbitmq.client.*;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer.Delivery;

public class RabbitmqService{

	

	
	//死信交换机
    public static final String DELAY_EXCHANGE = "delay-exchange";
    //死信路由
    public static final String DELAY_ROUTING_KEY = "delay-routing-key";
    //死信队列
    public static final String DELAY_QUEUE = "delay-queue";

    //普通交换机
    public static final String NORMAL_EXCHANGE = "normal-exchange";
    //普通路由
    public static final String NORMAL_ROUTING_KEY = "normal-routing-key";
    //普通队列
    public static final String NORMAL_QUEUE = "normal-queue";
	
	
    //队列名称  
    private final static String QUEUE_NAME = "queue-test";
    
    public static final Connection connection = RabbitMQ.use(5672);
    
	/**
	 * msg发送的消息
	 * @param msg
	 * @return
	 *//*
    public static String sendmsg(String msg) {
		try {
        //1.创建一个ConnectionFactory连接工厂connectionFactory
//        ConnectionFactory connectionFactory = new ConnectionFactory();
//        //2.通过connectionFactory设置RabbitMQ所在IP等信息
//        connectionFactory.setHost("106.12.42.247");
//              connectionFactory.setPort(5672); //指定端口
//              connectionFactory.setUsername("wm");//用户名
//              connectionFactory.setPassword("123456");//密码
////              connectionFactory.setVirtualHost("/vhost_A");
//        //3.通过connectionFactory创建一个连接connection
//        Connection connection = connectionFactory.newConnection();
//        //4.通过connection创建一个频道channel
        Channel channel = connection.createChannel();
//        //5.通过channel指定一个队列
//        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//        //6.通过channel向队列中添加消息，第一个参数是转发器，使用空的转发器（默认的转发器，类型是direct）
//        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
//        System.out.println("向" + QUEUE_NAME + "中添加了一条消息:" + msg);
        
        String EXCHANGE_NAME = "exchange.direct";
        String QUEUE_NAME = "queue_name";
        String ROUTING_KEY = "key";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
        String message = "Hello RabbitMQ:";
        for (int i = 0; i < 5; i++) {
            channel.basicPublish(EXCHANGE_NAME, ROUTING_KEY, null, (message + i).getBytes("UTF-8"));
        }

        channel.close();
//        connection.close();
//        //7.关闭频道
//        channel.close();
//        //8.关闭连接
//        connection.close();
        return msg+"发送成功";
		} catch (Exception e) {
			System.out.print(msg+"发送失败");
			return msg+"发送失败";
		}
		
	}

	public static String receivemsg(String msg) {
		try {
//        //1.创建一个ConnectionFactory连接工厂connectionFactory
//        ConnectionFactory connectionFactory = new ConnectionFactory();
//        //2.通过connectionFactory设置RabbitMQ所在IP等信息
//        connectionFactory.setHost("106.12.42.247");
//        connectionFactory.setPort(5672); //指定端口
//        connectionFactory.setUsername("wm");//用户名
//        connectionFactory.setPassword("123456");//密码
//        //3.通过connectionFactory创建一个连接connection
//        Connection connection = connectionFactory.newConnection();
        //4.通过connection创建一个频道channel
        final Channel channel = connection.createChannel();
        //5.通过channel指定队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //与发送消息不同的地方
        //6.创建一个消费者队列consumer,并指定channel
        Consumer consumer = new DefaultConsumer(channel) {
        	   @Override
        	   public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        	       String message = new String(body, "UTF-8");
        	       System.out.println(message);
//        	       channel.basicAck(envelope.getDeliveryTag(), false);
        	   }
        	};
//        QueueingConsumer consumer = new QueueingConsumer(channel);
        //7.为channel指定消费者
        channel.basicConsume(QUEUE_NAME, false, consumer);
//        while (true) {
//            //从consumer中获取队列中的消息,nextDelivery是一个阻塞方法,如果队列中无内容,则等待
//            Delivery delivery = consumer.nextDelivery();
//            String message = new String(delivery.getBody());
//            System.out.println("接收到了" + QUEUE_NAME + "中的消息:" + message);
//            return "接收到了" + QUEUE_NAME + "中的消息:" + message;
//        }
        return "";
		} catch (Exception e) {
			return "没有接收到消息";
		}
		
		
	}
	
    
    
    public static  void sendsomemsg(String msg){
    	try {
    	Channel channel = connection.createChannel();
    	//延时队列将消息传递到普通队列中
    	channel.exchangeDeclare(DELAY_EXCHANGE, "direct", true, false, null);
        Map args = new HashMap();
        args.put("x-dead-letter-exchange",NORMAL_EXCHANGE);
        args.put("x-dead-letter-routing-key",NORMAL_ROUTING_KEY);
         // 创建一个持久化、非排他的、非自动删除的队列
         channel.queueDeclare(DELAY_QUEUE, true, false, false, args);
         // 将交换器和队列通过路由绑定
         channel.queueBind(DELAY_QUEUE, DELAY_EXCHANGE, DELAY_ROUTING_KEY);
         
         
         //=========普通队列
         channel.exchangeDeclare(NORMAL_EXCHANGE, "direct", true, false, null);
         // 创建一个持久化、非排他的、非自动删除的队列
         channel.queueDeclare(NORMAL_QUEUE, true, false, false, args);
         // 将交换器和队列通过路由绑定
         channel.queueBind(NORMAL_QUEUE, NORMAL_EXCHANGE, NORMAL_ROUTING_KEY);

         // 设置延时属性
         AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
         // 持久性 non-persistent (1) or persistent (2)
         AMQP.BasicProperties properties = builder.expiration("5000").deliveryMode(2).build();
         channel.basicPublish(DELAY_EXCHANGE, DELAY_ROUTING_KEY, 
        		 properties, 
        		 msg.getBytes());
         channel.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
    }

	public static void testBasicConsumer1(){
		try {
			

//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setVirtualHost("/");
//        factory.setHost("127.0.0.1");
//        factory.setPort(AMQP.PROTOCOL.PORT);    // 5672
//        factory.setUsername("mengday");
//        factory.setPassword("mengday");
//
//        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        String EXCHANGE_NAME = "exchange.direct";
        String QUEUE_NAME = "queue_name";
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "key");
//        channel.basicQos(1);


//        GetResponse response = channel.basicGet(QUEUE_NAME, false);
//        byte[] body = response.getBody();
//        System.out.println(new String(body).toString());

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(message);
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };

        channel.basicConsume(QUEUE_NAME, false, consumer);

		} catch (Exception e) {
			// TODO: handle exception
		}
    }
	
	
	
    public static  void productmsg(String msg){
    	try {
    	 Channel channel = RabbitMQConfig.connection.createChannel();
         // 设置延时属性
         AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
         // 持久性 non-persistent (1) or persistent (2)
         AMQP.BasicProperties properties = builder.expiration("5000").deliveryMode(2).build();
         channel.basicPublish(DELAY_EXCHANGE, DELAY_ROUTING_KEY, 
        		 properties, 
        		 msg.getBytes());
         channel.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
    */
    
    
    /***
     * 创建一个带有路由和key绑定的普通队列
     * @param QueneName
     */
    public static void initQueneWithExchange(String QueneName){
    	try {
    		String NORMAL_EXCHANGE=QueneName+"_exchange";
    		String NORMAL_QUEUE=QueneName;
    		String NORMAL_ROUTING_KEY=QueneName+"_routingkey";
    		Channel channel = connection.createChannel();	
    		channel.exchangeDeclare(NORMAL_EXCHANGE, "direct", true, false, null);
    		// 创建一个持久化、非排他的、非自动删除的队列
    		channel.queueDeclare(NORMAL_QUEUE, true, false, false, null);
    		// 将交换器和队列通过路由绑定
    		channel.queueBind(NORMAL_QUEUE, NORMAL_EXCHANGE, NORMAL_ROUTING_KEY);
		} catch (Exception e) {
			LogKit.error(QueneName+"初始化失败（initQueneWithExchange）=="+e.getMessage());
		}
    	
    }
    
    

    /***
     * 
     * 
     * @param DelayQueneName 延迟队列名称
     * 在生成延迟队列过程中，会默认生成一个消费队列后面添加一个后缀
     * 
     */
    public static void initDelayQueneWithExchange(String QueneName){
    	
    	try {
    		String DELAY_EXCHANGE=QueneName+"_exchange";
    		String DELAY_QUEUE=QueneName;
    		String DELAY_ROUTING_KEY=QueneName+"_routingkey";
    		String NORMAL_EXCHANGE= "consume_"+DELAY_EXCHANGE;
    		String NORMAL_QUEUE= "consume_"+DELAY_QUEUE;
    		String NORMAL_ROUTING_KEY= "consume_"+DELAY_ROUTING_KEY;
    		Channel channel = connection.createChannel();	
    		//延时队列将消息传递到普通队列中
    		channel.exchangeDeclare(DELAY_EXCHANGE, "direct", true, false, null);
    		Map args = new HashMap();
    		args.put("x-dead-letter-exchange",NORMAL_EXCHANGE);
    		args.put("x-dead-letter-routing-key",NORMAL_ROUTING_KEY);
    		channel.queueDeclare(DELAY_QUEUE, true, false, false, args);
    		channel.queueBind(DELAY_QUEUE, DELAY_EXCHANGE, DELAY_ROUTING_KEY);
    		
    		//=========普通队列
    		channel.exchangeDeclare(NORMAL_EXCHANGE, "direct", true, false, null);
    		channel.queueDeclare(NORMAL_QUEUE, true, false, false, null);
    		channel.queueBind(NORMAL_QUEUE, NORMAL_EXCHANGE, NORMAL_ROUTING_KEY);
		} catch (Exception e) {
			LogKit.error(QueneName+"初始化失败（initDelayQueneWithExchange）=="+e.getMessage());
		}
    }
    
    
    /**
     * 向QueneName发送消息msg，消费端接受后会立即消费
     * @param QueneName
     * @param msg
     */
    public static void sendMQMsg(String QueneName, String msg){
    	try {
//    		initQueneWithExchange(QueneName);
//    		if(msg==null){
//    			msg="";
//    		}
    		String EXCHANGE=QueneName+"_exchange";
    		String ROUTING_KEY=QueneName+"_routingkey";
    		Channel channel = connection.createChannel();
    		channel.basicPublish(EXCHANGE, ROUTING_KEY, null, msg.getBytes("UTF-8"));
            channel.close();
   		} catch (Exception e) {
   			LogKit.error("向"+QueneName+"发送消息"+msg+"失败"+e.getMessage());
   		}
    }
    
    
    /**
     * 向QueneName发送消息msg，设置延时消费时间，时间到了消费端接受后会立即消费
     * 时间默认为1s，消息默认为空字符串
     * @param QueneName
     * @param msg
     * @param seconds
     */
    public static void sendMQMsg(String QueneName, String msg,Integer seconds){
    	try {
//    		initDelayQueneWithExchange(QueneName);
//    		if(msg==null){
//    			msg="";
//    		}
//    		if(seconds==null){
//    			seconds=1;
//    		}
//    		if(seconds==0){
//    			seconds=1;
//    		}
    		String DELAY_EXCHANGE=QueneName+"_exchange";
    		String DELAY_ROUTING_KEY=QueneName+"_routingkey";
    		Channel channel = RabbitMQConfig.connection.createChannel();
            // 设置延时属性
            AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
            // 持久性 non-persistent (1) or persistent (2)
            AMQP.BasicProperties properties = builder.expiration(seconds+"").deliveryMode(2).build();
            channel.basicPublish(DELAY_EXCHANGE, DELAY_ROUTING_KEY, 
           		 properties, 
           		 msg.getBytes());
            channel.close();
   		} catch (Exception e) {
   			LogKit.error("向"+QueneName+"发送消息"+msg+"失败"+e.getMessage());
   		}
    }

    
    /***
     * 从某个队列中消费消息
     * @param QueneName 队列名称
     * @param basicQos 每次消费多少个
     * @param autoAck 是否自动应答，true自动，false手动应答，建议选false
     * @param type type=1是正常队列，type=2是延时队列
     */
	public static void ConsumeMQMsgFromQuene(String QueneName,Integer basicQos,boolean autoAck,Integer type){
		try {
	        final Channel channel = connection.createChannel();
	       
			String nameheader="";
			if(type==2){
				nameheader="consume_";
			}
			if(basicQos!=null&&basicQos>0){
				channel.basicQos(basicQos);
			}
    		String QUEUE_NAME= nameheader+QueneName;
    		Consumer consumer = new DefaultConsumer(channel) {
    			@Override
    			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    				String message = new String(body, "UTF-8");
    				System.out.println("收到消息"+message);
    				channel.basicAck(envelope.getDeliveryTag(), false);
    			}
    		};
    		channel.basicConsume(QUEUE_NAME, autoAck, consumer);
		} catch (Exception e) {
			// TODO: handle exception
		}
    }
	
	/**
	 * 监听消费者
	 */
	public static void registerLisenter(){
		new Thread(()->ConsumeMQMsgFromQuene("queue_name",null,false,1)).start();
		new Thread(()->ConsumeMQMsgFromQuene("testdelyquene",null,false,2)).start();
	}

	/**
	 * 
	 * 初始化消息队列
	 */
	public static void initQuene() {
		String rabbitCommonQuene=PropKit.get("rabbitCommonQuene");
		String[] rabbitCommonQueneNames=rabbitCommonQuene.split(",");
		String rabbitDelyQuene=PropKit.get("rabbitDelyQuene");
		String[] rabbitDelyQueneNames=rabbitDelyQuene.split(",");
		
		for(String rabbitCommonQueneName:rabbitCommonQueneNames){
			initQueneWithExchange(rabbitCommonQueneName);
		}
		
		for(String rabbitDelyQueneName:rabbitDelyQueneNames){
			initDelayQueneWithExchange(rabbitDelyQueneName);
		}
		
		
	}
	
	/**
	 * 监听消费队列
	 */
	public static void monitorQuene() {
		String rabbitCommonQuene=PropKit.get("rabbitCommonQuene");
		String[] rabbitCommonQueneNames=rabbitCommonQuene.split(",");
		String rabbitDelyQuene=PropKit.get("rabbitDelyQuene");
		String[] rabbitDelyQueneNames=rabbitDelyQuene.split(",");
		
		for(String rabbitCommonQueneName:rabbitCommonQueneNames){
			new Thread(()->ConsumeMQMsgFromQuene(rabbitCommonQueneName,null,false,1)).start();
		}
		
		for(String rabbitDelyQueneName:rabbitDelyQueneNames){
			new Thread(()->ConsumeMQMsgFromQuene(rabbitDelyQueneName,null,false,2)).start();
		}
		
	}
	
	/**
	 * 处理某队列的消息
	 * @param QueneName
	 * @param message
	 * @return
	 */
	public static boolean dealMQMsg(String QueneName,String message ){
		try {
			if(QueneName.equals("queue_name")){
				LogKit.info("收到来自"+QueneName+"的消息"+message);
			}
			return true;
		} catch (Exception e) {
			return false;
		}

		
	}

}
