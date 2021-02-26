package com.java8;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sort8 {
	public static void main(String[] args) {

		Person p1 = new Person("sam1",2);
		Person p2 = new Person("sam3",3);
		Person p3 = new Person("sam2",1);
		Person p4 = new Person("sam4",5);
		Person p5 = new Person("sam2",4);
		
		List<Person> personList = new ArrayList<>();
		personList.add(p1);
		personList.add(p2);
		personList.add(p3);
		personList.add(p4);
		personList.add(p5);
		
		/*
		 * //With Types personList.sort( (Person pp1, Person pp2) ->
		 * pp1.getName().compareTo(pp2.getName())); personList.forEach(p ->
		 * System.out.println(p.getName() +" :: "+p.getId()));
		 * 
		 * //With No Types personList.sort( ( pp1, pp2) ->
		 * pp1.getName().compareTo(pp2.getName())); personList.forEach(p ->
		 * System.out.println(p.getName() +" :: "+p.getId()));
		 */		
		
		//Sort Extracted Comparators , 
		Collections.sort(personList, Comparator.comparing(Person::getId));
		personList.forEach(p ->
		  System.out.println(p.getName() +" :: "+p.getId()));
		
		Person per[] = {new Person("sam2",2), new Person("sam1",1) ,new Person("sam8",8) , new Person("sam3",3) , new Person("sam4",4)};
		List<Person> perLisss = Arrays.asList(per);
		
		//Cpmparing List with Collections with one conditions
		System.out.println("--------------------SINGLE COMPARATOR -----------------------");
		Collections.sort(perLisss,Comparator.comparing(Person::getName));
		perLisss.forEach(p -> System.out.println(p.getName() +" :: "+p.getId()));
		
		//Cpmparing List with Collections with one conditions
		System.out.println("--------------------DOUBLE COMPARATOR -----------------------");
		Collections.sort(perLisss, Comparator.comparing(Person::getName).thenComparing(Person::getId));
		perLisss.forEach(p -> System.out.println(p.getName() +" :: "+p.getId()));
		
		
		
		
		
	}
}
