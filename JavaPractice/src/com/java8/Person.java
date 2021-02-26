package com.java8;

import java.util.ArrayList;
import java.util.List;

public class Person {

	private int id;
	private String name;
	private double salary;

	public static List<Person> getPersonListWithValues(){
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
		perList.add(p7);
		perList.add(p8);
		perList.add(p9);
		
		return perList;
	}
	@Override
	public String toString() {
		return "Person [id=" + id + ", name=" + name + ", salary=" + salary + "]";
	}
	public double getSalary() {
		return salary;
	}
	public void setSalary(double d) {
		this.salary = d;
	}
	public Person(String name, int id, double d) {
		super();
		this.id = id;
		this.name = name;
		this.salary = d;
	}
	public Person(String name, int id) {
		super();
		this.id = id;
		this.name = name;
	}
	public Person() {
		// TODO Auto-generated constructor stub
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
