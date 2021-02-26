package com.java8.Thread;

public class Runnable8 {

	public static void main(String[] args) {

		System.out.println("Main RUN "+Thread.currentThread().getName());
		Runnable run1 = new Runnable() {
			@Override
			public void run() {
				System.out.println("RUN 1"+Thread.currentThread().getName());
			}
		};
		Thread t1 = new Thread(run1);
		
		Runnable run2 = () -> System.out.println("RUN 2"+Thread.currentThread().getName());
		Thread t2 = new Thread(run2);
		
		t1.start();
		t2.start();
		
	}

}
