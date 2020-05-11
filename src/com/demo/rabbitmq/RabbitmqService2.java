package com.demo.rabbitmq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.demo.util.RabbitMQ;
import com.demo.util.RabbitMQConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jfinal.kit.LogKit;
import com.jfinal.kit.PropKit;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * @author wm
 * @version 创建时间：2019年12月7日 下午3:54:09
 * 
 */
public class RabbitmqService2 {



	public static final Connection CONNECTION = RabbitMQ.use(5672);
	
	private final static String EXCHANGE_SUFFIX = "_exchange";
	
	private final static String ROUTINGKEY_SUFFIX = "_routingkey";
	
	private final static String CONSUME_PREFIX = "consume_";
	
	
	
	
	

	/***
	 * 创建一个带有路由和key绑定的普通队列
	 * 
	 * @param QueneName
	 */
	public static void initQueneWithExchange(String queneName) {
		try {
			String normalExchange = queneName + EXCHANGE_SUFFIX;
			String normalQueue = queneName;
			String normalRoutingKey = queneName + ROUTINGKEY_SUFFIX;
			Channel channel = CONNECTION.createChannel();
			channel.exchangeDeclare(normalExchange, "direct", true, false, null);
			// 创建一个持久化、非排他的、非自动删除的队列
			channel.queueDeclare(normalQueue, true, false, false, null);
			// 将交换器和队列通过路由绑定
			channel.queueBind(normalQueue, normalExchange, normalRoutingKey);
		} catch (Exception e) {
			LogKit.error(queneName + "初始化失败（initQueneWithExchange）==" + e.getMessage());
		}

	}

	/***
	 * 
	 * 
	 * @param DelayQueneName
	 *            延迟队列名称 在生成延迟队列过程中，会默认生成一个消费队列后面添加一个后缀
	 * 
	 */
	public static void initDelayQueneWithExchange(String queneName) {

		try {
			String delayExchange = queneName + EXCHANGE_SUFFIX;
			String delayQueue = queneName;
			String delayRoutingKey = queneName + ROUTINGKEY_SUFFIX;
			String normalExchange = CONSUME_PREFIX + delayExchange;
			String normalQueue = CONSUME_PREFIX + delayQueue;
			String normalRoutingKey = CONSUME_PREFIX + delayRoutingKey;
			Channel channel = CONNECTION.createChannel();
			// 延时队列将消息传递到普通队列中
			channel.exchangeDeclare(delayExchange, "direct", true, false, null);
			Map<String,Object> args = new HashMap<String,Object>(2);
			args.put("x-dead-letter-exchange", normalExchange);
			args.put("x-dead-letter-routing-key", normalRoutingKey);
			channel.queueDeclare(delayQueue, true, false, false, args);
			channel.queueBind(delayQueue, delayExchange, delayRoutingKey);

			// =========普通队列
			channel.exchangeDeclare(normalExchange, "direct", true, false, null);
			channel.queueDeclare(normalQueue, true, false, false, null);
			channel.queueBind(normalQueue, normalExchange, normalRoutingKey);
		} catch (Exception e) {
			LogKit.error(queneName + "初始化失败（initDelayQueneWithExchange）==" + e.getMessage());
		}
	}

	/**
	 * 向QueneName发送消息msg，消费端接受后会立即消费
	 * 
	 * @param QueneName
	 * @param msg
	 */
	public static void sendMqMsg(String queneName, String msg) {
		try {
			String exchange = queneName + EXCHANGE_SUFFIX;
			String routingkey = queneName + ROUTINGKEY_SUFFIX;
			Channel channel = CONNECTION.createChannel();
			channel.basicPublish(exchange, routingkey, null, msg.getBytes("UTF-8"));
			channel.close();
		} catch (Exception e) {
			LogKit.error("向" + queneName + "发送消息" + msg + "失败" + e.getMessage());
		}
	}

	/**
	 * 向QueneName发送消息msg，设置延时消费时间，时间到了消费端接受后会立即消费 时间默认为1s，消息默认为空字符串
	 * 
	 * @param QueneName
	 * @param msg
	 * @param seconds
	 */
	public static void sendMqMsg(String queneName, String msg, Integer seconds) {
		try {
			String delayExchange = queneName + EXCHANGE_SUFFIX;
			String delayRoutingKey = queneName + ROUTINGKEY_SUFFIX;
			Channel channel = CONNECTION.createChannel();
			// 设置延时属性
			AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
			// 持久性 non-persistent (1) or persistent (2)
			AMQP.BasicProperties properties = builder.expiration(seconds*1000 + "").deliveryMode(2).build();
			channel.basicPublish(delayExchange, delayRoutingKey, properties, msg.getBytes());
			channel.close();
		} catch (Exception e) {
			LogKit.error("向" + queneName + "发送消息" + msg + "失败" + e.getMessage());
		}
	}

	/***
	 * 从某个队列中消费消息
	 * 
	 * @param QueneName
	 *            队列名称
	 * @param basicQos
	 *            每次消费多少个
	 * @param autoAck
	 *            是否自动应答，true自动，false手动应答，建议选false
	 * @param type
	 *            type=1是正常队列，type=2是延时队列
	 */
	public static void consumeMqMsgFromQuene(String queneName, Integer basicQos, boolean autoAck, Integer type) {
		try {
			final Channel channel = CONNECTION.createChannel();

			String nameheader = "";
			if (type == 2) {
				nameheader = CONSUME_PREFIX;
			}
			if (basicQos != null && basicQos > 0) {
				channel.basicQos(basicQos);
			}
			String consumerQueueName = nameheader + queneName;
			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					String message = new String(body, "UTF-8");
					//System.out.println("收到来自" + queneName + "的消息" + message);
					dealMqMsg(queneName,message);
					channel.basicAck(envelope.getDeliveryTag(), false);
				}
			};
			channel.basicConsume(consumerQueueName, autoAck, consumer);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 监听消费者
	 */
//	public static void registerLisenter() {
//		new Thread(() -> ConsumeMQMsgFromQuene("queue_name", null, false, 1)).start();
//		new Thread(() -> ConsumeMQMsgFromQuene("testdelyquene", null, false, 2)).start();
//	}

	/**
	 * 
	 * 初始化消息队列
	 */
	public static void initQuene() {
		String rabbitCommonQuene = PropKit.get("rabbitCommonQuene");
		String[] rabbitCommonQueneNames = rabbitCommonQuene.split(",");
		String rabbitDelyQuene = PropKit.get("rabbitDelyQuene");
		String[] rabbitDelyQueneNames = rabbitDelyQuene.split(",");
		String rabbitLogQuene = PropKit.get("rabbitLogQuene");
		String[] rabbitLogQueneNames = rabbitLogQuene.split(",");
		
		

		for (String rabbitCommonQueneName : rabbitCommonQueneNames) {
			initQueneWithExchange(rabbitCommonQueneName);
		}

		for (String rabbitDelyQueneName : rabbitDelyQueneNames) {
			initDelayQueneWithExchange(rabbitDelyQueneName);
		}
		
		for (String rabbitLogQueneName : rabbitLogQueneNames) {
			initQueneWithExchange(rabbitLogQueneName);
		}

	}

	/**
	 * 监听消费队列
	 */
	public static void monitorQuene() {
		String rabbitCommonQuene = PropKit.get("rabbitCommonQuene");
		String[] rabbitCommonQueneNames = rabbitCommonQuene.split(",");
		String rabbitDelyQuene = PropKit.get("rabbitDelyQuene");
		String[] rabbitDelyQueneNames = rabbitDelyQuene.split(",");
		/**
         * 手动创建线程池
         */
        // 创建线程工厂
        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("thread-call-runner-%d").build();
        // 创建通用线程池
        /**
         * 参数含义：
         *      corePoolSize : 线程池中常驻的线程数量。核心线程数，默认情况下核心线程会一直存活，即使处于闲置状态也不会受存keepAliveTime限制。除非将allowCoreThreadTimeOut设置为true。
         *      maximumPoolSize : 线程池所能容纳的最大线程数。超过这个数的线程将被阻塞。当任务队列为没有设置大小的LinkedBlockingDeque时，这个值无效。
         *      keepAliveTime : 当线程数量多于 corePoolSize 时，空闲线程的存活时长，超过这个时间就会被回收
         *      unit : keepAliveTime 的时间单位
         *      workQueue : 存放待处理任务的队列，该队列只接收 Runnable 接口
         *      threadFactory : 线程创建工厂
         *      handler : 当线程池中的资源已经全部耗尽，添加新线程被拒绝时，会调用RejectedExecutionHandler的rejectedExecution方法，参考 ThreadPoolExecutor 类中的内部策略类
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(rabbitCommonQueneNames.length+rabbitDelyQueneNames.length, rabbitCommonQueneNames.length+rabbitDelyQueneNames.length, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1024),
                threadFactory);
		for (String rabbitCommonQueneName : rabbitCommonQueneNames) {
            threadPoolExecutor.execute(() -> 
            consumeMqMsgFromQuene(rabbitCommonQueneName, 3, false, 1));
		}
		
//		for (String rabbitCommonQueneName : rabbitCommonQueneNames) {
//            threadPoolExecutor.execute(() -> 
//            consumeMqMsgFromQuene2(rabbitCommonQueneName, null, false, 1));
//		}

		for (String rabbitDelyQueneName : rabbitDelyQueneNames) {
            threadPoolExecutor.execute(() -> 
            consumeMqMsgFromQuene(rabbitDelyQueneName, null, false, 2));
		}
		
		  threadPoolExecutor.shutdown();

	}

	/**
	 * 处理某队列的消息可以单出独立出来一个类，进行处理方法的管理
	 * 
	 * @param QueneName
	 * @param message
	 * @return
	 */
	public static boolean dealMqMsg(String queneName, String message) {
		try {
			if (queneName.equals("queue_name")) {
				LogKit.info("收到来自" + queneName + "的消息" + message);
//				System.out.println("收到来自" + queneName + "的消息" + message);
			}
			return true;
		} catch (Exception e) {
			return false;
		}

	}
	
	
	public static void consumeMqMsgFromQuene2(String queneName, Integer basicQos, boolean autoAck, Integer type) {
		try {
			final Channel channel = CONNECTION.createChannel();

			String nameheader = "";
			if (type == 2) {
				nameheader = CONSUME_PREFIX;
			}
			if (basicQos != null && basicQos > 0) {
				channel.basicQos(basicQos);
			}
			String consumerQueueName = nameheader + queneName;
			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					String message = new String(body, "UTF-8");
					System.out.println("2222收到来自" + queneName + "的消息" + message);
//					dealMqMsg(queneName,message);
					channel.basicAck(envelope.getDeliveryTag(), false);
				}
			};
			channel.basicConsume(consumerQueueName, autoAck, consumer);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
