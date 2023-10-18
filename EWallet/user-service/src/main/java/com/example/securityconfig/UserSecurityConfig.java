package com.example.securityconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.example.UserConstants;
import com.example.service.UserService;

@Configuration
public class UserSecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired
	UserService userService;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		auth.userDetailsService(userService);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		// csrf.disable added so that we can use post(other than get) all
		// request can use by postman.
		
		http.httpBasic().and().csrf().disable()
			.authorizeHttpRequests()
			.antMatchers(HttpMethod.POST, "/user/**").permitAll()
			.antMatchers(HttpMethod.GET, "/user/**").hasAuthority(UserConstants.SERVICE_AUTHORITY)
			.antMatchers("/getUser/**").hasAuthority(UserConstants.USER_AUTHORITITY)
			.antMatchers("/updateUser/**").hasAuthority(UserConstants.USER_AUTHORITITY)
			.antMatchers("/admin/**").hasAuthority(UserConstants.SERVICE_AUTHORITY)
			.antMatchers("/home/**").permitAll()
			.antMatchers("/user/**").hasAuthority(UserConstants.USER_AUTHORITITY)
			.and().formLogin();
	}
}
