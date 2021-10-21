package com.ford.vdcc.poc.batch.listeners;


import org.h2.engine.Domain;
import org.springframework.batch.core.ItemReadListener;


public class ReaderListener implements ItemReadListener<String> {

    @Override
    public void beforeRead() {
        System.out.println("ItemReadListener - beforeRead");
    }

    @Override
    public void afterRead(String item) {
        System.out.println("ItemReadListener - afterRead");
    }

    @Override
    public void onReadError(Exception ex) {
        System.out.println("ItemReadListener - onReadError");
    }

}
