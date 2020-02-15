package com.security.db.demo;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@org.springframework.stereotype.Controller
public class Controller {
	
	@GetMapping("/start")
	public String starter() {
		return "Welcome to Starter";
	}

	@GetMapping("/login")
	public String login() {
		return "login.html";
	}

	@GetMapping("/logout")
	public String logout() {
		return "logout.html";
	}

	@RequestMapping("user")
	@ResponseBody
	public Principal user(Principal principal) {
		return principal;
	}
}
