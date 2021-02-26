package com.java8;

class GFG  
{ 

//Global array for hole values 
static int hole[] = { 1, 0, 0, 0, 1, 0, 1, 0, 2, 1 }; 

//Function to return the count 
//of holes in num 
static int countHoles(int num) 
{ 
 int holes = 0; 

 while (num > 0)  
 { 

     // Last digit in num 
     int d = num % 10; 

     // Update holes 
     holes += hole[d]; 

     // Remove last digit 
     num /= 10; 
 } 

 // Return the count of holes 
 // in the original num 
 return holes; 
} 

//Driver code 
public static void main (String[] args)  
{ 
 int num = 1288; 
 System.out.println(countHoles(num)); 
} 
} 

//This code is contributed by  
//shk 