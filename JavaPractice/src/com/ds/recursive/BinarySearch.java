package com.ds.recursive;

public class BinarySearch {

	public static void main(String[] args) {

		int[] nums = new int[] {1,2,3,4,5,6,7,8,9,10,11};
		int pos = binarySearch(nums,3,0,nums.length-1);

		if(pos>=0)
			System.out.println("DATA ::"+(pos+1));
		else
			System.out.println("NO DATA FOUND ::");
	}

	private static int binarySearch(int[] nums, int searchNum, int start, int end) {
		int mid = 0;
		System.out.print("binarySearch :: ");
		System.out.print("START :: "+start);
		System.out.print(":: End :: "+end);
		try {
			if(nums[start] == searchNum) {
				return start;
			}
			if(start==end) {
				if(nums[start] != searchNum) {
					System.out.println("No Data Found");
					return -1;
				}
			}

			mid = findMid(nums.length,start,end);

			if(nums[mid] > searchNum) {
				return binarySearch(nums,searchNum,start,mid);
			}else if(nums[mid] < searchNum)
			{
				return binarySearch(nums,searchNum,mid,end);
			}else 
				return mid;
		}catch(Exception ex) {
			ex.printStackTrace();
			return -1;
		}
		//return mid;

	}

	private static int findMid(int length, int start, int end) {
		
		if(end-start == 1) {
			return end;
		}
		
		int mid = (end-start) / 2;
		mid = start+mid;
		System.out.println("  MID ::"+mid);
		return mid;
	}

}
