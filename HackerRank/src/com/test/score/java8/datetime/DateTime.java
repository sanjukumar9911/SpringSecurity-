package com.test.score.java8.datetime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DateTime {

	 public static void main(String[] args) { 
	 
		 LocalDate date = LocalDate.now();
		 System.out.println("date ::"+date);
		 //Result : date ::2019-12-16
		 
		 LocalDate date1 = LocalDate.parse("2018-12-16");
		 System.out.println("date1 ::"+date1);
		 //Result : date ::2018-12-16
		 
		 LocalDate date2 = LocalDate.now().minus(1, ChronoUnit.MONTHS);
		 System.out.println("date1 ::"+date2);
		 //Result : date ::2019-11-16
		 
		 DayOfWeek date3 = LocalDate.now().getDayOfWeek();
		 int value = date3.getValue();
		 System.out.println("VALUE ::"+value);
		 
		 String value1 = date3.toString();
		 System.out.println("Value = "+value1);
			 
		 
		 
	 }
}
