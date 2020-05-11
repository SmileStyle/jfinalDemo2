package com.demo.log;

import com.jfinal.log.ILogFactory;
import com.jfinal.log.Log;

public class Sl4jLogFactory implements ILogFactory {
	 
	 
    @Override
    public Log getLog(Class<?> clazz) {
        return Sl4jLog.getLog(clazz);
    }
 
    @Override
    public Log getLog(String name) {
        return Sl4jLog.getLog(name);
    }
}
