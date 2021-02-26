package com.java8;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchJava8 {

	public static void main(String[] args) {
		List<Person> perList = new ArrayList<>();

		Person p1 = new Person("Sanjeev",1,10.9);
		Person p2 = new Person("Sanjeev",2,10.9);
		Person p3 = new Person("Sanjeev",3,10.9);
		Person p4 = new Person("Sanjeev",4,11.9);
		Person p5 = new Person("Sanjeev",5,11.9);
		Person p6 = new Person("Sanjeev",6,11.9);
		Person p7 = new Person("Sanjeev",4,12.9);
		Person p8 = new Person("Sanjeev",5,12.9);
		Person p9 = new Person("Sanjeev",6,12.9);

		perList.add(p1);
		perList.add(p2);
		perList.add(p3);
		perList.add(p4);
		perList.add(p5);
		perList.add(p6);
		
		List<Integer> perListInt = (List<Integer>)perList.stream().filter(p->p.getName().equalsIgnoreCase("Sanjeev")).map(p->p.getId()).collect(Collectors.toList());

		for(int i : perListInt) {
			System.out.println(i);
		}
	}

}
