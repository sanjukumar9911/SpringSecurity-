package com.java8.Thread;

public class ThreadExt extends Thread{

	public void run() {
		System.out.println("RUNNINGGGGGGGGGGGGGGGG");
	}
	public static void main(String[] args) {
		ThreadExt thread = new ThreadExt();
		thread.start();
	}

}
