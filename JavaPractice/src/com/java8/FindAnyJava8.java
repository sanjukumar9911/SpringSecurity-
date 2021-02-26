package com.java8;

import java.util.List;

public class FindAnyJava8 {

	public static void main(String[] args) {
		
		List<Person> perList = Person.getPersonListWithValues();
		
		Person per = perList.stream().filter(p->p.getName().equalsIgnoreCase("Sanjeev")).findAny().orElse(null);
		
		System.out.println(per);
		
		Person per1 = perList.stream().filter(p->p.getName().equalsIgnoreCase("Sanjeev")).findFirst().orElse(null);
		
		System.out.println(per1);
		

	}

}
