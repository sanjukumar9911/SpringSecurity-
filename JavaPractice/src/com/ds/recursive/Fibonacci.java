package com.ds.recursive;

public class Fibonacci {

	public static void main(String[] args) {

		int n=2;
		int result = fib(n);
		System.out.println("RESULT :: "+result);
		
		
		System.out.print("Series :::: ");
		fibSeries(n);
	}

	private static void fibSeries(int n) {
		if(n > 0 && n>=1)
			System.out.print("0");
		if(n > 0 && n>=2)
			System.out.print(",1");
		if(n>=3) {
			int f1 = 0, f2=1;
			for(int i = 3; i<=n ; i++) {
				System.out.print(","+(f1+f2));
				f2 = f1 + f2;
				f1 = f2-f1;
			}
		}
		
	}

	private static int fib(int n) {
		
		if(n==1 || n==2) {
			return n-1;
		}
		return fib(n-1) + fib(n-2);
		
	}
	
	

}
