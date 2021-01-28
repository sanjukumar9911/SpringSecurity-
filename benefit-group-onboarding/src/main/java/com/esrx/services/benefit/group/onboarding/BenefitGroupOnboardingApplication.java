package com.esrx.services.benefit.group.onboarding;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;


@SpringBootApplication
@EnableJpaRepositories
@EntityScan("com.esrx.services.benefit.group.onboarding.entity")
@EnableMongoRepositories(basePackages = "com.esrx.services.benefit.group.onboarding.mongorepository")
public class BenefitGroupOnboardingApplication {

	@PostConstruct
	public void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("EST"));
	}
	
	public static void main(String[] args) {
		SpringApplication.run(BenefitGroupOnboardingApplication.class, args);
	}

}
