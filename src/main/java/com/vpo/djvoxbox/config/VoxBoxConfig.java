package com.vpo.djvoxbox.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.vpo.vbclient.song.SongClient;

@Configuration
public class VoxBoxConfig {
	
	@Value("${vb.organization}")
	private String vbOrganization;
	
	// configure beans
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	SongClient songClient() {
		if(vbOrganization.isEmpty()) {
			return new SongClient();
		} else {
			return new SongClient(null, vbOrganization);
		}
	}
}
