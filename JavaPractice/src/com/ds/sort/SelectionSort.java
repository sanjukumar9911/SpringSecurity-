package com.ds.sort;

public class SelectionSort {

	public static void main(String[] args) {
		// find min value's index and swap with curent interation start value.

		int A[] = {6,5,4,3,1};
		int n = A.length;

		
		System.out.println("");
		for(int p : A) {
			System.out.print(p+",");
		}
		
		for(int i=0;i<n-1;i++) {
			int minIndex = i;
			for(int j=i;j<n;j++) {
				if(A[j] < A[minIndex]) {
					minIndex = j;
				}
			}
			int temp = A[minIndex];
			A[minIndex] = A[i];
			A[i] = temp;
			
		}
		
		System.out.println("");
		for(int p : A) {
			System.out.print(p+",");
		}

	}

}
