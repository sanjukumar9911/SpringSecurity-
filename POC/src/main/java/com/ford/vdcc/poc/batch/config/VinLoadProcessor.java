package com.ford.vdcc.poc.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class VinLoadProcessor implements ItemProcessor {
    private static final Logger logger = LoggerFactory.getLogger(VinLoadWriterWriter.class);

    @Override
    public Object process(Object item) throws Exception {

       // logger.info("process item :: "+item);
        return item;
    }
}
