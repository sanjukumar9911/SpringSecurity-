package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
public class JwtSecurityHandler extends WebSecurityConfigurerAdapter{

	@Autowired
	MyUserDetailsService userDetailsService;
	
	@Override
	protected void configure(AuthenticationManagerBuilder builder) throws Exception {
		builder.userDetailsService(userDetailsService);
	}
}
