package com.vpo.djvoxbox.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.vpo.vbclient.song.SongClient;

@Configuration
public class VoxBoxConfig {
	// configure beans
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	SongClient songClient() {
		return new SongClient();
	}
}
