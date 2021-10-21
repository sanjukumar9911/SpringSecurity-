package com.ford.vdcc.poc.batch.processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import com.google.gson.Gson;

public class BatItemProcessor implements ItemProcessor<String, String>{

	private static final Logger logger = LoggerFactory.getLogger(BatItemProcessor.class);
	@Override
	public String process(String data) throws Exception {
		Gson gson = new Gson();
		//logger.info("Person Data ::"+data);
		return data;
	}
}
