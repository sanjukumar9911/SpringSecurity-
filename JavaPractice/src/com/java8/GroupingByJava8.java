package com.java8;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupingByJava8 {

	public static void main(String[] args) {

		List<Person> perList = Person.getPersonListWithValues();
		
		Map<Double, List<Person>> perMap = perList.stream().collect(Collectors.groupingBy(Person::getSalary));
		
		System.out.println(perMap);
		
		Map<Double, Long> perCount = perList.stream().collect(Collectors.groupingBy(Person::getSalary, Collectors.counting()));
		
		System.out.println(perCount);
		
		

	}

}
