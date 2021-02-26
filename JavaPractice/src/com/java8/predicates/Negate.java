package com.java8.predicates;

import java.util.Arrays;
import java.util.List;
import java.util.function.*;
import java.util.stream.Collectors;
public class Negate {

	public static void main(String[] args) {

		List list = Arrays.asList(1,2,3,4,5,6,7,8);
		
		Predicate<Integer> isEven = i -> (i % 2) == 0 ;
		Predicate<Integer> isodd = isEven.negate();
		Predicate<Integer> isPrime = i -> {
			
			for(int j = 2; j<=i/2; j++) {
				if(i % j == 0) {
					return false;
				}
			}
			
			return true;
		};
		
		System.out.println("EVEN List ::"+list.stream().filter(isEven).collect(Collectors.toList()));
		System.out.println("ODD List ::"+list.stream().filter(isodd).collect(Collectors.toList()));
		System.out.println("ODD List ::"+list.stream().filter(isPrime).collect(Collectors.toList()));
		System.out.println("LIST :: "+list);
		
		
		

	}

}
