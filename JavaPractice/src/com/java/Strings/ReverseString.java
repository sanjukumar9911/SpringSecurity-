package com.java.Strings;

public class ReverseString {

	public static void main(String[] args) {

		//Input : sams maka jaka
		//Output : jaka maka sams
		String data = " sams maka jaka ";
		
		String[] dataArr = data.split(" ");
		
		for(int i = dataArr.length-1; i>=0; i--) {
			System.out.print(dataArr[i]+" ");
		}
		
		for(int i=0; i<dataArr.length; i++) {
		}
		
	}

}
