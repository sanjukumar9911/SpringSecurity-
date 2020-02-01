package com.security.db.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
	
	@GetMapping("/start")
	public String starter() {
		return "Welcome to Starter";
	}

}
