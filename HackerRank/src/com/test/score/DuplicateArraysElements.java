package com.test.score;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DuplicateArraysElements {

    // Complete the sockMerchant function below.
    static int sockMerchant(int n, int[] ar) {
            Map<Integer,Integer> collectionmap = new HashMap<Integer,Integer>();
            for(Integer l : ar){
                if(collectionmap.containsKey(l))
                {
                	int value = collectionmap.get(l) + 1;
                    collectionmap.put(l,value);
                }
                    else
                    collectionmap.put(l,1);
            }
            Integer Count = 0;
            for(Map.Entry<Integer, Integer> e  :collectionmap.entrySet()){
                Count = Count + e.getValue() % 2 ;
            }
            return Count;
    }

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(System.getenv("OUTPUT_PATH")));

        int n = scanner.nextInt();
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        int[] ar = new int[n];

        String[] arItems = scanner.nextLine().split(" ");
        scanner.skip("(\r\n|[\n\r\u2028\u2029\u0085])?");

        for (int i = 0; i < n; i++) {
            int arItem = Integer.parseInt(arItems[i]);
            ar[i] = arItem;
        }

        int result = sockMerchant(n, ar);

        bufferedWriter.write(String.valueOf(result));
        bufferedWriter.newLine();

        bufferedWriter.close();

        scanner.close();
    }
}
