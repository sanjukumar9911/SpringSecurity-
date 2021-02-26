package com.java8;

public class fibonaccii {

	public static void main(String[] args) {
		int n = 10, k=0,l=1;
		System.out.println(k);
		System.out.println(l);
		
		for(int i=3; i<=n ;i ++) {
			int sum = k + l;
			k = l;
			l = sum;
			System.out.println(l);
		}
	}
}
