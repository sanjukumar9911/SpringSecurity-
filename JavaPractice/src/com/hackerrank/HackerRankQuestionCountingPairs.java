package com.hackerrank;

import java.util.HashSet;
import java.util.Set;

public class HackerRankQuestionCountingPairs {

	public static void main(String[] args) {
		
		int[] nums = {1,1,2,2,3,3};
		int k = 1;

		Set<String> res = new HashSet<>();
		for(int i : nums) {

			for(int j:nums) {
				//System.out.println(i+" + "+k+" = "+j);
				if((j-i) == k) {
					res.add("("+i+","+j+")");
				}
			}
		}
		
		System.out.println(res);
	}
		

}
