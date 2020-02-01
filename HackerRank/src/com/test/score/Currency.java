package com.test.score;


import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

public class Currency {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double payment = scanner.nextDouble();
        scanner.close();
        DecimalFormat IndianCurrencyFormat = new DecimalFormat("##,##,###.00");
        
        if(payment>=0 && payment <= (Math.pow(10,9)-1)){
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            NumberFormat nf1 = NumberFormat.getCurrencyInstance(Locale.CHINA);
            NumberFormat nf2 = NumberFormat.getCurrencyInstance(Locale.FRANCE);
           System.out.println("US: " + nf.format(payment));
        System.out.println("India: Rs." + IndianCurrencyFormat.format(payment));
        System.out.println("China: "+  nf1.format(payment));
        System.out.println("France: "+ nf2.format(payment));
        }
         
        
    }
}

