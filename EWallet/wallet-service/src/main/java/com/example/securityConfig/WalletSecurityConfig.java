package com.example.securityConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.example.UserConstants;
import com.example.service.WalletService;

@Configuration
public class WalletSecurityConfig extends WebSecurityConfigurerAdapter{
	
	@Autowired
	WalletService walletService;
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
		auth.userDetailsService(walletService);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception{
		// csrf.disable added so that we can use post(other than get) all
		// request can use by postman.
		
		http.httpBasic().and().csrf().disable()
			.authorizeHttpRequests()
			.antMatchers("/wallet/**").hasAuthority(UserConstants.USER_AUTHORITITY)
			.and().formLogin();
	}
	
}
