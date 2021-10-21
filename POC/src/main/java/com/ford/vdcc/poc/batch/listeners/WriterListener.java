package com.ford.vdcc.poc.batch.listeners;

import java.util.List;

import org.h2.engine.Domain;
import org.springframework.batch.core.ItemWriteListener;

import java.util.List;


public class WriterListener implements ItemWriteListener<String> {

    @Override
    public void beforeWrite(List<? extends String> items) {
        System.out.println("ItemWriteListener - beforeWrite");
    }

    @Override
    public void afterWrite(List<? extends String> items) {
        System.out.println("ItemWriteListener - afterWrite");
    }

    @Override
    public void onWriteError(Exception exception, List<? extends String> items) {
        System.out.println("ItemWriteListener - onWriteError");
    }

}