package com.esrx.services.benefit.group.onboarding.controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esrx.services.benefit.group.onboarding.conditionalproperty.TestBeanondition;
import com.esrx.services.benefit.group.onboarding.mongo.Domain;
import com.esrx.services.benefit.group.onboarding.mongorepository.DomainRepository;
import com.google.gson.Gson;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@CrossOrigin
@RequestMapping("/domain")
@Api(value="DomainController Controller", description="Domain in Online Store")
public class DomainController {
	private static final Logger logger = LoggerFactory.getLogger(DomainController.class);
	@Autowired
	DomainRepository mongoRepo;

	@Autowired
	Gson gson;
	
	@Autowired
	TestBeanondition test;
	
	@GetMapping("/testCondition")
	String testCondition(){
		return test.getName();
	}
	
	
	@PostMapping()
	@ApiOperation(value = "Save a domain Mongo", response = String.class)
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successfully retrieved list"),
	        @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	})
	String addDomain(@RequestBody Domain domain){
		
		domain.setEffdate(new Date());
		domain.setTermDate(new Date());
		mongoRepo.save(domain);
		return "SUCCESS";
	}
	
	
	@GetMapping("/{id}")
	@ApiOperation(value = "Get a domain Mongo WRT Id", response = Domain.class)
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successfully retrieved list"),
	        @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	})
	Domain getCustomer(@PathVariable Long id){

		Optional<Domain> domain = mongoRepo.findById(id);
		
		if(domain==null || domain.get()==null) {
			logger.info("No Data found ::::");
			return null;
		}
		Domain d = domain.get();
		
		logger.info("DATA ::",gson.toJson(d));
		
		List<Domain> domainDataCurrent = mongoRepo.findByDomaincurrent("N23WORK",new Date());
		
		logger.info(gson.toJson(domainDataCurrent));
		
		return d;
	}
	
	@GetMapping()
	@ApiOperation(value = "Get a domain Mongo WRT Id", response = Domain.class)
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successfully retrieved list"),
	        @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	})
	List<Domain> getAllCustomer(){

		List<Domain> domain = mongoRepo.findAll();
		
		if(domain==null || domain.isEmpty()) {
			logger.info("No Data found ::::");
			return null;
		}
		
		logger.info("DATA ::",gson.toJson(domain));
		
		return domain;
	}
}
