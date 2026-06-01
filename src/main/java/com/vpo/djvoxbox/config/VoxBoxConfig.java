package com.vpo.djvoxbox.config;

import java.time.Duration;

import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import com.vpo.djvoxbox.security.web.authentication.SpringSessionRememberMeServices;
import com.vpo.vbclient.currentsong.CurrentSongClient;
import com.vpo.vbclient.feedback.FeedbackClient;
import com.vpo.vbclient.queue.QueueClient;
import com.vpo.vbclient.room.RoomClient;
import com.vpo.vbclient.session.SessionClient;
import com.vpo.vbclient.song.SongClient;

@Configuration
@EnableScheduling
public class VoxBoxConfig {

	@Value("${vb.organization}")
	private String vbOrganization;

	@Value("${vb.rootUrl:https://voiceboxpdx.com}")
	private String vbRootUrl;

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	SongClient songClient() {
		if (vbOrganization.isEmpty()) {
			return new SongClient(vbRootUrl, "");
		} else {
			return new SongClient(vbRootUrl, vbOrganization);
		}
	}

	@Bean
	SessionClient sessionClient() {
		if (vbOrganization.isEmpty()) {
			return new SessionClient(vbRootUrl, "");
		} else {
			return new SessionClient(vbRootUrl, vbOrganization);
		}
	}

	@Bean
	QueueClient queueClient() {
		if (vbOrganization.isEmpty()) {
			return new QueueClient(vbRootUrl, "");
		} else {
			return new QueueClient(vbRootUrl, vbOrganization);
		}
	}

	@Bean
	RoomClient roomClient() {
		if (vbOrganization.isEmpty()) {
			return new RoomClient(vbRootUrl, "");
		} else {
			return new RoomClient(vbRootUrl, vbOrganization);
		}
	}

	@Bean
	CurrentSongClient currentSongClient() {
		if (vbOrganization.isEmpty()) {
			return new CurrentSongClient(vbRootUrl, "");
		} else {
			return new CurrentSongClient(vbRootUrl, vbOrganization);
		}
	}

	@Bean
	FeedbackClient feedbackClient() {
		if (vbOrganization.isEmpty()) {
			return new FeedbackClient(vbRootUrl, "");
		} else {
			return new FeedbackClient(vbRootUrl, vbOrganization);
		}
	}

	@Bean
	public static SpringSessionRememberMeServices springSessionRememberMeServices() {
		return new SpringSessionRememberMeServices();
	}

	@Bean
	public static ConfigureRedisAction configureRedisAction() {
		return ConfigureRedisAction.NO_OP;
	}

	@Bean
	RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
		StringRedisSerializer keySerializer = new StringRedisSerializer();

		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(keySerializer);
		template.setHashKeySerializer(keySerializer);
		template.setValueSerializer(valueSerializer);
		template.setHashValueSerializer(valueSerializer);
		template.afterPropertiesSet();
		return template;
	}

	@Bean
	RedisCacheConfiguration redisCacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofHours(24))
				.prefixCacheNameWith("djvb:cache:")
				.serializeValuesWith(RedisSerializationContext.SerializationPair
						.fromSerializer(new GenericJackson2JsonRedisSerializer()));
	}

	@Bean
	public CookieSerializer cookieSerializer() {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		serializer.setCookieName("JSESSIONID");
		serializer.setCookiePath("/");
		serializer.setCookieMaxAge(31557600);
		return serializer;
	}

	@Bean
	public Filter shallowEtagHeaderFilter() {
		return new ShallowEtagHeaderFilter();
	}

	@Component
	public static class ServletCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

		@Override
		public void customize(ConfigurableServletWebServerFactory factory) {
			MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
			mappings.add("ttf", "application/x-font-truetype");
			factory.setMimeMappings(mappings);
		}
	}
}
