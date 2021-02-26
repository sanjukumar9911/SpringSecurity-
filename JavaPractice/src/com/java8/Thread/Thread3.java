package com.java8.Thread;

public class Thread3 {

	public static void main(String[] args) {
		System.out.println(Thread.currentThread().getName());
		Thread t1 = new Thread( () -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName());
			System.out.println("Hi");

		});
		t1.start();
		System.out.println("2"+
		Thread.currentThread().getName());

	}

}
