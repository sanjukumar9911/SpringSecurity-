package com.java8;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StreamSort8 {

	public static void main(String args[]) {

		Person[] arraylist = {new Person("a",2), new Person("b",3), new Person("d",1), new Person("c",2),new Person("p",3)};
		List <Person> pList = Arrays.asList(arraylist);

//		This can't be done with objects		
//		List<Person> sortedList = pList.stream().sorted().collect(Collectors.toList());
		
		//With Comparator
		System.out.println("With Comparator");
		Comparator<Person> comp = (p1,p2) -> p1.getName().compareTo(p2.getName());
		List<Person> sortedList = pList.stream().sorted(comp).collect(Collectors.toList());
		sortedList.forEach(s-> System.out.println(s.getId() +" --"+s.getName()));
		
		//With Comparatoy.comparing
		System.out.println("With Comparator.comparing");
		List<Person> sortedListDoublecon = pList.stream().sorted(Comparator.comparing(Person::getId)).collect(Collectors.toList());
		sortedListDoublecon.forEach(s-> System.out.println(s.getId() +" --"+s.getName()));

		//With Comparatoy.comparing
		System.out.println("With Comparator.comparing DOUBLE");
		List<Person> sortedListDoublecon1 = pList.stream().sorted(Comparator.comparing(Person::getId).thenComparing(Person::getName)).collect(Collectors.toList());
		sortedListDoublecon1.forEach(s-> System.out.println(s.getId() +" --"+s.getName()));

	}
}
