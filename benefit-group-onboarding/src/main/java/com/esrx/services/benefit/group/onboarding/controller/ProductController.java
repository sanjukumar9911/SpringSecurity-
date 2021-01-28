package com.esrx.services.benefit.group.onboarding.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.esrx.services.benefit.group.onboarding.entity.ProductEntity;
import com.esrx.services.benefit.group.onboarding.mongo.Domain;
import com.esrx.services.benefit.group.onboarding.mongorepository.DomainRepository;
import com.esrx.services.benefit.group.onboarding.repository.ProductRepository;
import com.google.gson.Gson;

@RestController
@RequestMapping("/entity")
@Api(value="EntityBenefitManagement Controller", description="products in Online Store")
public class ProductController {
	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
	@Autowired
	ProductRepository repo;

	@ApiOperation(value = "View a list of available products with ID as input", response = List.class)
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successfully retrieved list"),
	        @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	})
	@GetMapping("/product/{id}")
	ResponseEntity<Object> getProduct(@PathVariable Long id) throws Exception {
		List<ProductEntity> productEntityList = new ArrayList<ProductEntity>();
		
		logger.info("ID :::: "+id);
		
		productEntityList = repo.findAll();
		
		if(productEntityList.isEmpty()) {
			return  ResponseEntity.status(HttpStatus.OK).body("No Data Found");
		}
		return ResponseEntity.ok(productEntityList);
	}
	
	@ApiOperation(value = "Add product with Dynamic ID as input", response = String.class)
	@ApiResponses(value = {
	        @ApiResponse(code = 200, message = "Successfully retrieved list"),
	        @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
	        @ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
	        @ApiResponse(code = 404, message = "The resource you were trying to reach is not found")
	})
	@PostMapping("/product")
	ResponseEntity<String> postProduct(@RequestBody ProductEntity entity) throws Exception{
		try {
			logger.info("ProductName :::: "+entity.getProductName());
			logger.info("ProductOwner :::: "+entity.getProductOwner());
			logger.info("ProductPlace :::: "+entity.getProductPlace());
			logger.info("ProductPrice :::: "+entity.getProductPrice());
			logger.info("ProductType :::: "+entity.getProductType());
			
			
		repo.save(entity);
		
		}catch(Exception ex) {
			return  ResponseEntity.status(HttpStatus.NOT_FOUND).body("FAILED");
		}
		return ResponseEntity.ok("SUCCESS");
	}
	
}
