package com.test.score;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class CountSuffixWords {

	public static void main(String[] args) {

		String str = "My name is khan and im not a terroristme";
		String suffix = "me";
		
		List<String> list = Arrays.asList(str.split(" "));
		 
		//int size = (int) list.stream().filter(s -> s.endsWith(suffix)).count();
		Predicate<String> p = s -> s.endsWith(suffix);
		int size = (int) list.stream().filter(p).count();
		System.out.println(size);
	}

}
