package com.ford.vdcc.poc.test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FIleComp {
    public static void main(String[] srt) throws IOException {


        File fileqa = new File("C:\\missingqa.txt");

        BufferedReader mqabr = new BufferedReader(new FileReader(fileqa));

        List<String> missingqaList = new ArrayList<>();
        String mqast;
        while ((mqast = mqabr.readLine()) != null)
            missingqaList.add(mqast);

        System.out.println("MISSING QA LIST SIZE :: "+missingqaList.size());

        File fileprod = new File("C:\\missingprod.txt");

        BufferedReader mprodbr = new BufferedReader(new FileReader(fileprod));

        List<String> missingprodList = new ArrayList<>();
        String mprodst;
        while ((mprodst = mprodbr.readLine()) != null)
            missingprodList.add(mprodst);
        System.out.println("MISSING PROD LIST SIZE :: "+missingprodList.size());

        File file = new File("C:\\test1prod.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        List<String> prodList = new ArrayList<>();
        String st;
        while ((st = br.readLine()) != null)
            prodList.add(st);

        System.out.println("PROD ::"+prodList.size());


        File file1 = new File("C:\\test1qa.txt");
        List<String> qaList = new ArrayList<>();
        BufferedReader br1 = new BufferedReader(new FileReader(file1));

        String st1;
        while ((st1 = br1.readLine()) != null)
            qaList.add(st1);

        System.out.println("QA ::"+qaList.size());


        List<String> profDiff = prodList.stream()
                .filter(element -> !qaList.contains(element))
                .collect(Collectors.toList());
        System.out.println("PROD Diff::"+profDiff.size());

        List<String> qaDiff = qaList.stream()
                .filter(element -> !prodList.contains(element))
                .collect(Collectors.toList());

        System.out.println("QA Diff::"+qaDiff.size());

        System.out.println("----------------------------");

        for(String str : qaDiff){
            System.out.println(str);
        }

        System.out.println("----------------------------");

        List<String> prodDiff = prodList.stream()
                .filter(element -> !qaList.contains(element))
                .collect(Collectors.toList());

        System.out.println("PROD Diff::"+prodDiff.size());



        File file3 = new File("C:\\QAList.txt");

        BufferedReader br3 = new BufferedReader(new FileReader(file3));

        System.out.println("-------------DIFF---------------");
        List<String> QAList = new ArrayList<>();
        String st3;
        Map<String,String> map = new HashMap<>();
        while ((st3 = br3.readLine()) != null) {

            QAList.add(st3);

            String[] d1 = st3.split("[,]");
            map.put(d1[0],d1[1]);



        }


        System.out.println("Size  QAList:: "+QAList.size());
        System.out.println("-------------DIFF---------------");


        System.out.println("PROD ::"+prodList.size());


        System.out.println("-------------FINAL DIFF---------------");
        for(String p : qaDiff){
            if(map.containsKey(p)){
                System.out.println(p+","+map.get(p));
            }
        }
        System.out.println("-------------FINAL DIFF---------------");


        System.out.println("-------------MISSING DIFF START---------------");
        List<String> missdqa = new ArrayList<>();
        for(String pmqa : missingqaList){
            if(map.containsKey(pmqa)  ){
                if(map.get(pmqa).equalsIgnoreCase("P702"))
                System.out.println(pmqa +" -- "+map.get(pmqa));
                missdqa.add(pmqa);
            }
        }
        System.out.println("MISSING P702 Data List QA::"+missdqa.size());
        System.out.println("------------ ---------------");
        List<String> missdprod = new ArrayList<>();
        for(String pmprod : missingprodList){
            if(map.containsKey(pmprod)  ){
                if(map.get(pmprod).equalsIgnoreCase("P702"))
                System.out.println(pmprod +" -- "+map.get(pmprod));
                missdprod.add(pmprod);
            }
        }
        System.out.println("MISSING P702 Data List PROD::"+missdprod.size());
        System.out.println("-------------MISSING DIFF END---------------");





        List<String> missingDifference = missdqa.stream()
                .filter(element -> !missdprod.contains(element))
                .collect(Collectors.toList());

        System.out.println("MISSING Difference :: "+missingDifference.size());
        System.out.println("MISSING DARA :: ");

        for(String s : missingDifference){
            System.out.println("VIN :: "+s);
        }


    }
}
