package com.test.score;
import java.util.*;
import java.io.*;

class Solution{
	public static void main(String []argh){
		Scanner in = new Scanner(System.in);
		int t=in.nextInt();
		String[] result = new String[3];
		if(t>=0 && t<=500) {
		for(int i=0;i<t;i++){
			int a = in.nextInt();
			int b = in.nextInt();
			int n = in.nextInt();
			int[] resultInt = new int[n];

			if(n>=1 && n<=15 && a >=0 && a <=50 && b >=0 && b <=50) {
			result[i] = "";
			for(int j=0;j<=n-1;j++) {
				if(j==0) {
				resultInt[j] = (int)(a + Math.pow(2, j)*b);
				}else {
					resultInt[j] = resultInt[j-1] + (int)( Math.pow(2, j)*b);
				}
			}
		}
			for(int res : resultInt) {
				result[i] = result[i] + res + " ";
			}
			
		}
		for(String res : result) {
			if(res!=null)
			System.out.println(res);
		}
		}

		in.close();
	}
}

