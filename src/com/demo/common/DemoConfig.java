package com.demo.common;

import com.demo.blog.BlogController;
import com.demo.common.model._MappingKit;
import com.demo.index.IndexController;
import com.demo.log.Sl4jLogFactory;
import com.demo.rabbitmq.RabbitmqController;
import com.demo.rabbitmq.RabbitmqService;
import com.demo.rabbitmq.RabbitmqService2;
import com.demo.util.RabbitMQConfig;
import com.demo.util.RabbitMQPlugin;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.template.Engine;

/**
 * 本 demo 仅表达最为粗浅的 jfinal 用法，更为有价值的实用的企业级用法
 * 详见 JFinal 俱乐部: http://jfinal.com/club
 * 
 * API引导式配置
 */
public class DemoConfig extends JFinalConfig {
	
	/**
	 * 运行此 main 方法可以启动项目，此main方法可以放置在任意的Class类定义中，不一定要放于此
	 * 
	 * 使用本方法启动过第一次以后，会在开发工具的 debug、run config 中自动生成
	 * 一条启动配置，可对该自动生成的配置再添加额外的配置项，例如 VM argument 可配置为：
	 * -XX:PermSize=64M -XX:MaxPermSize=256M
	 */
	public static void main(String[] args) {
		/**
		 * 特别注意：Eclipse 之下建议的启动方式
		 */
		JFinal.start("WebRoot", 8117, "/", 5);

		/**
		 * 特别注意：IDEA 之下建议的启动方式，仅比 eclipse 之下少了最后一个参数
		 */
		// JFinal.start("WebRoot", 80, "/");
	}
	
	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		// 加载少量必要配置，随后可用PropKit.get(...)获取值
		PropKit.use("a_little_config.txt");
		me.setDevMode(PropKit.getBoolean("devMode", false));
		me.setLogFactory(new Sl4jLogFactory());
	}
	
	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		me.add("/", IndexController.class, "/index");	// 第三个参数为该Controller的视图存放路径
		me.add("/blog", BlogController.class);			// 第三个参数省略时默认与第一个参数值相同，在此即为 "/blog"
		me.add("/rabbitmq", RabbitmqController.class);
	}
	
	public void configEngine(Engine me) {
//		me.addSharedFunction("/common/_layout.html");
//		me.addSharedFunction("/common/_paginate.html");
	}
	
	public static DruidPlugin createDruidPlugin() {
		return new DruidPlugin(PropKit.get("jdbcUrl"), PropKit.get("user"), PropKit.get("password").trim());
	}
	
	public static RabbitMQPlugin createRabbitMQPlugin() {
		return new RabbitMQPlugin(PropKit.get("rabbitmq_host"), Integer.parseInt(PropKit.get("rabbitmq_port")), PropKit.get("rabbitmq_username"),PropKit.get("rabbitmq_password"));
	}
	
	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {
		// 配置C3p0数据库连接池插件
//		DruidPlugin druidPlugin = createDruidPlugin();
//		me.add(druidPlugin);
//		
//		// 配置ActiveRecord插件
//		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
//		// 所有映射在 MappingKit 中自动化搞定
//		_MappingKit.mapping(arp);
//		me.add(arp);
		
		//创建rabbitmq
		RabbitMQPlugin rabbitmqplugin = createRabbitMQPlugin();
		me.add(rabbitmqplugin);
	}
	
	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {
		
	}
	
	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
		
	}
	
	public void afterJFinalStart(){
//		RabbitMQConfig rmq=new RabbitMQConfig();
//		rmq.getDelayQueue();
//		rmq.getDelayExchange();
//		rmq.bindDelay();
//		rmq.getNormalQueue();
//		rmq.getNormalExchange();
//		rmq.bindNormal();
//		new RabbitMQConfig();
//		RabbitmqService.testBasicConsumer1();
		RabbitmqService2.initQuene();
		RabbitmqService2.monitorQuene();
//		RabbitmqService.ConsumeMQMsgFromQuene("queue_name",null,false,1);
//		RabbitmqService.ConsumeMQMsgFromQuene("testdelyquene",null,false,2);
	}
}
