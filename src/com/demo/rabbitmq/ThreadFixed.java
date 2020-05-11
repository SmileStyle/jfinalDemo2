package com.demo.rabbitmq;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
* @author wm
* @version 创建时间：2019年12月10日 上午10:46:01
* 
*/
public class ThreadFixed {

	public static void main(String[] args) {

	    ExecutorService ex1 = Executors.newFixedThreadPool(5);
	    for (int i=0;i<20;i++){
	        ex1.execute(()-> System.out.println(Thread.currentThread().getName()));
	    }
	    ex1.shutdown();

	}

}
