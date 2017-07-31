package com.zcon.azure.configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.zcon.azure.filter.CORSFilter;

/**
 * @author Vyankatesh
 *
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.zcon.azure")
public class SpringAppConfiguration extends WebMvcConfigurerAdapter {
	/**
	 * Configure ViewResolvers to deliver preferred views.
	 */
	@Bean
	public CORSFilter corsFilter() {
		CORSFilter corsFilter = new CORSFilter();
		return corsFilter;
	}
}
