package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

	@GetMapping("/")
	public String default1() {
		return "WELCOME ALL";
	}
	
	@GetMapping("/user")
	public String user() {
		return "WELCOME USER";
	}
	
	@GetMapping("/admin")
	public String admin() {
		return "WELCOME ADMIN";
	}
	
	
}
