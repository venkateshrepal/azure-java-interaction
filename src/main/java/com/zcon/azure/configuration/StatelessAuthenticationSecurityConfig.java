package com.zcon.azure.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import com.zcon.azure.filter.CORSFilter;

/**
 * @author Vyankatesh
 *
 */
@EnableWebSecurity
@Configuration
@Order(2)
public class StatelessAuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	CORSFilter corsFilter;

	public StatelessAuthenticationSecurityConfig() {
		super(true);
	}

	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
	}

	protected void configure(HttpSecurity http) throws Exception {
		http.exceptionHandling().and().anonymous().and().servletApi().and().authorizeRequests()
				.antMatchers(HttpMethod.POST, "/api/login").permitAll().antMatchers("/", "/app/**").permitAll()

				.antMatchers("/", "/home").permitAll().antMatchers("/favicon.ico").permitAll()
				.antMatchers("/resources/**").permitAll().antMatchers("/api/user/**").permitAll().antMatchers("/**")
				.permitAll().antMatchers(HttpMethod.GET, "/api/setpassword/**").permitAll()
				.antMatchers(HttpMethod.POST, "/api/setpassword/**").permitAll()
				.antMatchers(HttpMethod.POST, "/api/setpassword/updatepassword/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/api/setpassword/updatepassword/**").permitAll()
				.antMatchers(HttpMethod.GET, "/api/forgotPassword/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/api/forgotPassword/**").permitAll()
				.antMatchers(HttpMethod.OPTIONS, "/*/**").permitAll();
	}

}
