package com.ibm.ojt;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class ControllerConfig  extends RepositoryRestConfigurerAdapter{

	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.exposeIdsFor(Cart.class);
	}
	
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("http://kariteun-shopping.mybluemix.net", "https://kariteun-shopping.mybluemix.net", "http://localhost:3000");
	}
}
