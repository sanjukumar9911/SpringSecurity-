package com.java8;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MapJava8 {

	public static void main(String[] args) {

		List<Person> perList = new ArrayList<>();

		Person p1 = new Person("Sanjeev",1,10.9);
		Person p2 = new Person("Sanjeev",2,10.9);
		Person p3 = new Person("Sanjeev",3,10.9);
		Person p4 = new Person("Sanjeev",4,10.9);
		Person p5 = new Person("Sanjeev",5,10.9);
		Person p6 = new Person("Sanjeev",6,10.9);

		perList.add(p1);
		perList.add(p2);
		perList.add(p3);
		perList.add(p4);
		perList.add(p5);
		perList.add(p6);

		List<String> pList = perList.stream().map(p->p.getName()).collect(Collectors.toList());

		pList.stream().forEach(System.out::println);

		perList.stream().map(p-> p.getSalary() * 2).forEach(System.out::println);;

		System.out.println(" OUT PUT :: :: :: ::");
		perList = perList.stream().map(p->{
			Person pp = new Person(); 
			pp.setName(p.getName());
			pp.setId(p.getId());
			if(p.getSalary() > 10) {
				pp.setSalary(p.getSalary() * 4);
			}
			return pp;
		}).collect(Collectors.toList());
		 
		for(Person ppp : perList) {
			System.out.println(ppp);
		}

	}

}
