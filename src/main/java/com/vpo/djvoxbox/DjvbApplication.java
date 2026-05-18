package com.vpo.djvoxbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableCaching
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 31557600)
@SpringBootApplication
public class DjvbApplication {

    public static void main(String[] args) {
        SpringApplication.run(DjvbApplication.class, args);
    }
}
