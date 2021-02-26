package com.ds.map;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class Maps {

	public static void main(String[] args) {

		Map<String,String> testMap = new HashMap<>();
		System.out.println("MAPs -> "
				+ "1. HashMap -> Unordered keys, It accept  Null Value as a key, duplicate keys not allowed"
				+ "						EntrySet -> provide entires from the Map, which has a key and value pair."
				+ "						get keySet provides only keys from map"
				+ "						get Value using key will provide Value"
				+ "						get (Key), containKey(key) and ContainsValue(value) "
				+ "2. Linked Hash Map -> Ordered Keys"
				+ "3. Tree Map -> sorted Keys");
		System.out.println("HashMap :: It doesn't Maintaine the order");
		testMap.put("1", "a");
		testMap.put("2", "s");
		testMap.put("3", "d");
		testMap.put("4", "f");
		testMap.put("5", "g");
		testMap.put("6", "t");
		testMap.put("10", "r");
		testMap.put("9", "t");
		testMap.put("8", "g");
		testMap.put("nul", "g");
		System.out.println("testMap ::"+testMap);
		
		Set<Map.Entry<String,String>> testSet = testMap.entrySet();
		testSet.stream().forEach(System.out::println);
		
		System.out.println("LinkedHashMap :: It Maintaine the order");
		Map<String,String> testLinkedMap = new LinkedHashMap<>();
		testLinkedMap.put("1", "a");
		testLinkedMap.put("2", "s");
		testLinkedMap.put("3", "d");
		testLinkedMap.put("4", "f");
		testLinkedMap.put("5", "g");
		testLinkedMap.put("6", "t");
		testLinkedMap.put("10", "r");
		testLinkedMap.put("9", "t");
		testLinkedMap.put("8", "g");
		testLinkedMap.put("nul", "g");
		Set<Map.Entry<String,String>> testLinkedSet = testLinkedMap.entrySet();
		testLinkedSet.stream().forEach(System.out::println);
		
		
		Map<String,String> tesTreetMap = new TreeMap<>();
		System.out.println("TreeMap :: It doesn't Maintaine the order");
		tesTreetMap.put("1", "a");
		tesTreetMap.put("2", "s");
		tesTreetMap.put("3", "d");
		tesTreetMap.put("4", "f");
		tesTreetMap.put("5", "g");
		tesTreetMap.put("6", "t");
		tesTreetMap.put("10", "r");
		tesTreetMap.put("9", "t");
		tesTreetMap.put("8", "g");
		tesTreetMap.put("nul", "g");
		
		System.out.println("TreeMap ::"+tesTreetMap);
		
		Set<Map.Entry<String,String>> testTreeSet = tesTreetMap.entrySet();
		
		testTreeSet.stream().forEach(System.out::println);
		
		Map<Integer,String> tesTreetIntMap = new TreeMap<>();
		System.out.println("TreeMap :: It doesn't Maintaine the order");
		tesTreetIntMap.put(1, "a");
		tesTreetIntMap.put(2, "s");
		tesTreetIntMap.put(3, "d");
		tesTreetIntMap.put(4, "f");
		tesTreetIntMap.put(5, "g");
		tesTreetIntMap.put(6, "t");
		tesTreetIntMap.put(10, "r");
		tesTreetIntMap.put(9, "t");
		tesTreetIntMap.put(8, "g");
		tesTreetIntMap.put(15, "g");
		
		System.out.println("TreeMap Int Keys::"+tesTreetMap);
		
		Set<Map.Entry<Integer,String>> testIntTreeSet = tesTreetIntMap.entrySet();
		
		testIntTreeSet.stream().forEach(System.out::println);
		
		
	}

}
