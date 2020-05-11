package com.demo.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.demo.rabbitmq.RabbitmqService2;
import com.jfinal.log.Log;

public class Sl4jLog extends Log {
    private Logger log;
 
    Sl4jLog(Class<?> clazz) {
        log = LoggerFactory.getLogger(clazz.getName());
    }
 
    Sl4jLog(String name) {
        log = LoggerFactory.getLogger(name);
    }
 
    public static Sl4jLog getLog(Class<?> clazz) {
        return new Sl4jLog(clazz);
    }
 
    public static Sl4jLog getLog(String name) {
        return new Sl4jLog(name);
    }
 
    @Override
    public void debug(String message) {
        log.debug(message);
    }
 
    @Override
    public void debug(String message, Throwable t) {
        log.debug(message, t);
    }
 
    @Override
    public void info(String message) {
//    	log.info(message+"可将消息插入到队列中");
    	RabbitmqService2.sendMqMsg("logQuene",message);
        log.info(message);
    }
 
    @Override
    public void info(String message, Throwable t) {
        log.info(message, t);
    }
 
    @Override
    public void warn(String message) {
        log.warn(message);
    }
 
    @Override
    public void warn(String message, Throwable t) {
        log.warn(message, t);
    }
 
    @Override
    public void error(String message) {
    	log.error(message+"123456789");
        log.error(message);
    }
 
    @Override
    public void error(String message, Throwable t) {
        log.error(message, t);
    }
 
    @Override
    public void fatal(String message) {
 
    }
 
    @Override
    public void fatal(String message, Throwable t) {
       log.error(message, t);
 
    }
 
    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }
 
    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }
 
    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }
 
    @Override
    public boolean isErrorEnabled() {
        return log.isWarnEnabled();
    }
 
    @Override
    public boolean isFatalEnabled() {
        return false;
    }
 
}