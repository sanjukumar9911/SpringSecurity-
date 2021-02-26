package com.java8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ListofListSort {

	public static void main(String[] args) {

		List list = new ArrayList<>();
		List list2 = new ArrayList<>();
		List list3 = new ArrayList<>();
		List list4 = new ArrayList<>();
		List list5 = new ArrayList<>();
		List<List> outList = new ArrayList<>(); 
		List<List> mainList = new ArrayList<>(); 

		AtomicInteger ai = new AtomicInteger (0);

		list.add("Sanje"); list.add(1); list.add(9);
		list2.add("Garuda"); list2.add(4); list2.add(3);
		list3.add("Pedre"); list3.add(5); list3.add(5);
		list4.add("Chahu"); list4.add(2); list4.add(3);
		list5.add("Pahu"); list5.add(4); list5.add(8);
		mainList.add(list);
		mainList.add(list2);
		mainList.add(list3);
		mainList.add(list4);
		mainList.add(list5);

		System.out.println("Size ::"+mainList.size());
		System.out.println("Data ::"+mainList);

		final int k= 0;
		Map<String,List> map = new HashMap<>();
		int i = 0;
		for (List object : mainList) {
			i++;
			String key = "";
			if(object.get(k) instanceof Integer) {
				key = String.valueOf(object.get(k));
			}
			else if(object.get(k) instanceof String) {
				key =(String) object.get(k);
			}

			if(map!=null && map.containsKey(key)) {
				map.put(key+""+i, object);
			}else {
				map.put(key, object);
			}
		}

		//List sorted = map.keySet().stream().sorted().collect(Collectors.toList());

		//	sort in order small to big
		map.keySet().stream().sorted().forEach(i1-> {
			outList.add(map.get(i1));
		});;

		//		sort in reverse order
		//		map.keySet().stream().sorted(Comparator.reverseOrder()).forEach(i1-> {
		//			outList.add(map.get(i1));
		//		});;

		/*
		 * for (Object key : sorted) {
		 * 
		 * String key1 = (String) key; outList.add(map.get(key1)); }
		 */		
		//System.out.println("DATA ::"+sorted);
		System.out.println("DATA ::"+outList);

		int page = 2;
		int pageSize = 2;
		int count = outList.size() / pageSize;
		count = outList.size() % pageSize + count;

		int min=0;

		if(page==0) {
			min = 0;
		}else if(page==1) {
			min = pageSize ;
		}else if(page>1) {
			min = pageSize * (page);
		}
		
		if(min>=outList.size())
		{
			System.out.println("No range found");
		}

		int max = pageSize;

		System.out.println("min ::"+min);
		System.out.println("max ::"+max);
		System.out.println("count ::"+count);
		
		
		for(int j=0;j<max;j++) {
			try {
				if(outList!=null &&!outList.isEmpty() && outList.get(j+min)!=null && !outList.get(j+min).isEmpty()) {
					System.out.println(outList.get(j+min));
				}
			}catch(Exception ex) {

			}
		}

	}



}

