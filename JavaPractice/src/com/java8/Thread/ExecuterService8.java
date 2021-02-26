package com.java8.Thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuterService8 {

	public static void main(String[] args) {

		
		//Single Thread Asynchronous Way.
		ExecutorService exec = Executors.newSingleThreadExecutor();
		exec.submit(() -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			String threadName = Thread.currentThread().getName();
			System.out.println("Thread Name : "+threadName);
		});
		System.out.println("DONE1" );
		
		
		//Many fixed Threads Asynchronous Way
		ExecutorService execmult = Executors.newFixedThreadPool(10);
		Runnable run1 = () -> System.out.println("HI "+Thread.currentThread().getName());
		execmult.execute(run1);
 		System.out.println("DONE2" );
		
		
	}

}
