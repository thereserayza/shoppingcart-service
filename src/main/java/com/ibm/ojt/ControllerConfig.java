package com.ibm.ojt;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class ControllerConfig {
	
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("http://kariteun-shopping.mybluemix.net", "https://kariteun-shopping.mybluemix.net", "http://localhost:3000");
	}
}
