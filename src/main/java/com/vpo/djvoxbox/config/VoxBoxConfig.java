package com.vpo.djvoxbox.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.web.client.RestTemplate;

import com.vpo.vbclient.queue.QueueClient;
import com.vpo.vbclient.session.SessionClient;
import com.vpo.vbclient.song.SongClient;

@Configuration
@EnableScheduling
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
	
	@Bean
	SessionClient sessionClient() {
		if(vbOrganization.isEmpty()) {
			return new SessionClient();
		} else {
			return new SessionClient(null, vbOrganization);
		}
	}
	
	@Bean
	QueueClient queuClient() {
		if(vbOrganization.isEmpty()) {
			return new QueueClient();
		} else {
			return new QueueClient(null, vbOrganization);
		}
	}
	
	@Bean
	public static ConfigureRedisAction configureRedisAction() {
	    return ConfigureRedisAction.NO_OP;
	}
}
