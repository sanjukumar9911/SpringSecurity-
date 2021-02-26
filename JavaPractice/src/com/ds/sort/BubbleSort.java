package com.ds.sort;
public class BubbleSort {

	public static void main(String[] args) {
		int[] A = {4,3,1,6,4,3,6,4};
		int n = A.length;
		for(int p : A) {
			System.out.print(p+",");
		}

		//Ascending Order
		for(int i = 0; i<n-1; i++) {
			for(int j=0; j<n-1-i ; j++) {
				if(A[j] > A[j+1]) {
					int temp = A[j];
					A[j] = A[j+1];
					A[j+1] = temp;
				}
			}
		}

		System.out.println("");
		for(int p : A) {
			System.out.print(p+",");
		}
		
		//Descending Order
		for(int i = 0; i<n-1; i++) {
			for(int j=0; j<n-1-i ; j++) {
				if(A[j] < A[j+1]) {
					int temp = A[j];
					A[j] = A[j+1];
					A[j+1] = temp;
				}
			}
		}

		System.out.println("");
		for(int p : A) {
			System.out.print(p+",");
		}
	}
}
