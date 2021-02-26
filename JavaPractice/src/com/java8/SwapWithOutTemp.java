package com.java8;

public class SwapWithOutTemp {

	public static void main(String[] args) {

		int i = 10, j = 20;
		
		i = i+j;   // i = 30
		j = i - j; //j = 10
		i = i-j;  //i = 20
		
		System.out.println("J "+j);
		System.out.println("I "+i);
		
		String a = "abc", b = "xyza";
		
		a = b + a;    // xyzaabc
		b = a.substring(b.length());  //abc
		a = a.substring(0,a.length()-b.length());
	//	b = a.		
		System.out.println("B =="+b);
		System.out.println("A =="+a);
				
				
		
		
	}

}
