package com.demo.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.*;

import com.jfinal.kit.LogKit;
import com.jfinal.kit.PropKit;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

public class RabbitMQConfig {

	
	public static final Connection connection = RabbitMQ.use(Integer.parseInt(PropKit.get("rabbitmq_port")));
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

    public void InitQuene(){
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
    		channel.queueDeclare(NORMAL_QUEUE, true, false, false, null);
    		// 将交换器和队列通过路由绑定
    		channel.queueBind(NORMAL_QUEUE, NORMAL_EXCHANGE, NORMAL_ROUTING_KEY);
    	} catch (Exception e) {
			LogKit.error("初始化InitQuene失败=="+e.getMessage());
		
    	}
    }
//    /**
//     * 创建延时队列
//     */
//    public Queue getDelayQueue(){
//        Map args = new HashMap();
//        /**
//         * 消息发送给延时队列
//         * 设置延时队列的过期时间为5秒钟
//         * 5秒之后，延时队列将消息发送给普通队列
//         */
//        args.put("x-dead-letter-exchange",NORMAL_EXCHANGE);
//        args.put("x-dead-letter-routing-key",NORMAL_ROUTING_KEY);
//        args.put("x-message-ttl",5000);
//        return QueueBuilder.durable(DELAY_QUEUE).withArguments(args).build();
//    }
//    //创建延时交换机
//    public Exchange getDelayExchange(){
//        return ExchangeBuilder.directExchange(DELAY_EXCHANGE).durable(true).build();
//    }
//    //延时与延时交换机进行绑定
//    public Binding bindDelay(){
//        return BindingBuilder.bind(getDelayQueue()).to(getDelayExchange()).with(DELAY_ROUTING_KEY).noargs();
//    }
//
//    //创建普通队列
//    public Queue getNormalQueue(){
//        return new Queue(NORMAL_QUEUE);
//    }
//    //创建普通交换机
//    public Exchange getNormalExchange(){
//        return ExchangeBuilder.directExchange(NORMAL_EXCHANGE).durable(true).build();
//    }
//    //普通队列与普通交换机进行绑定
//    public Binding bindNormal(){
//        return BindingBuilder.bind(getNormalQueue()).to(getNormalExchange()).with(NORMAL_ROUTING_KEY).noargs();
//    }
    
    public RabbitMQConfig(){
    	this.InitQuene();
    }
}
