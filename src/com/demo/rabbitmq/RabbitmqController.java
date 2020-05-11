package com.demo.rabbitmq;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.LogKit;
import com.jfinal.plugin.activerecord.Record;

public class RabbitmqController extends Controller{
	
/*	
	public void sendmsg(){
		String msg=getPara("msg");
		String result=RabbitmqService2.sendmsg(msg);
		renderJson(new Record().set("result", result));
	}
	
	
	public void receivemsg(){
		String msg=getPara("msg");
		String result=RabbitmqService2.receivemsg(msg);
		renderJson(new Record().set("result", result));
	}
	
	
	public void sendsomemsg(){
		String msg=getPara("msg");
		RabbitmqService2.sendsomemsg(msg);
		renderJson("1");
	}
	
	public void testBasicConsumer1() throws Exception{
		RabbitmqService2.testBasicConsumer1();
		renderJson("1");
	}*/
	
	
	public void sendMQMsg(){
		String QueneName=getPara("QueneName");
		String msg=getPara("msg");
		RabbitmqService2.sendMqMsg(QueneName,msg);
		renderJson("1");
	}
	
	
	public void ConsumeMQMsgFromQuene(){
		String QueneName=getPara("QueneName");
		Integer basicQos=getParaToInt("basicQos");
		boolean autoAck=getParaToBoolean("autoAck");
		Integer type=getParaToInt("type",1);
		RabbitmqService2.consumeMqMsgFromQuene(QueneName,basicQos,autoAck,type);
		renderJson("1");
	}
	
	public void sendMQMsg2(){
		String QueneName=getPara("QueneName");
		String msg=getPara("msg");
		Integer seconds=getParaToInt("seconds");
		RabbitmqService2.sendMqMsg(QueneName,msg,seconds);
		renderJson("1");
	}
	
	
	public void testlog(){
		System.out.println("123");
		LogKit.error("发生错误");
		System.out.println("ttttt");
		renderText("1");
	}
	
	
	public void addlog(){
		String name = getPara("name","admin");
		String content = getPara("content","homepage");
		String time = LocalDateTime.now()+"";
		Map<String, String> map = new HashMap<String,String>(3);
		map.put("name", name);
		map.put("content", content);
		map.put("createtime", time);
		LogKit.info(JsonKit.toJson(map));
		
		renderText("ok");
		
		
	}

}
