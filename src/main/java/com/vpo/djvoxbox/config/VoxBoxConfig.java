package com.vpo.djvoxbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class VoxBoxConfig {
	// configure beans
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
