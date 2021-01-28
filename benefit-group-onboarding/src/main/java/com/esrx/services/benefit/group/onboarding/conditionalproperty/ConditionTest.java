package com.esrx.services.benefit.group.onboarding.conditionalproperty;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ConditionTest {

	@Bean
	@ConditionalOnProperty(name = "value" , havingValue = "Y")
	public TestBeanondition testBeanondition1(){
		TestBeanondition test = new TestBeanondition();
		test.setId(1234);
		test.setName("SAMMY");
		return test;
	}
	
	
	@Bean("TestBeanondition")
	@ConditionalOnProperty(name = "value" , havingValue = "N")
	public TestBeanondition testBeanondition2(){
		TestBeanondition test = new TestBeanondition();
		test.setId(9876);
		test.setName("SANJY");
		return test;
	}
	
}
