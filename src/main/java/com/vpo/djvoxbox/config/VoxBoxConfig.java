package com.vpo.djvoxbox.config;

import jakarta.servlet.Filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import com.vpo.djvoxbox.security.web.authentication.SpringSessionRememberMeServices;
import com.vpo.vbclient.queue.QueueClient;
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
	public static SpringSessionRememberMeServices springSessionRememberMeServices() {
		return new SpringSessionRememberMeServices();
	}

	@Bean
	public static ConfigureRedisAction configureRedisAction() {
		return ConfigureRedisAction.NO_OP;
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

	@Bean
	public Filter httpsEnforcerFilter() {
		return new HttpsEnforcer();
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
