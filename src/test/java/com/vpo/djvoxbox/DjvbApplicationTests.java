package com.vpo.djvoxbox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * Lightweight smoke test for the application bootstrap.
 *
 * <p>A full {@code @SpringBootTest} contextLoads test would need real Mongo,
 * Redis, and Firebase credentials available at test time — none of which we
 * provision in CI today. Rather than introduce Testcontainers or
 * {@code @MockBean} for every infrastructure dependency, this test asserts
 * the structural contract of the application class: it stays annotated as
 * a Spring Boot app with caching and Redis-backed HTTP sessions enabled.
 *
 * <p>This catches accidental removal of the annotations and compilation
 * regressions, without depending on external services. A real
 * context-loads test can be added once a stable test profile (with
 * embedded or mocked Mongo/Redis) is in place.
 */
class DjvbApplicationTests {

    @Test
    void applicationIsAnnotatedAsSpringBootApplication() {
        assertThat(DjvbApplication.class.getAnnotation(SpringBootApplication.class))
                .as("@SpringBootApplication must remain on DjvbApplication for component scanning")
                .isNotNull();
    }

    @Test
    void cachingIsEnabled() {
        assertThat(DjvbApplication.class.getAnnotation(EnableCaching.class))
                .as("@EnableCaching must remain on DjvbApplication; "
                        + "Redis-backed cache regions rely on it")
                .isNotNull();
    }

    @Test
    void redisHttpSessionIsEnabled() {
        EnableRedisHttpSession ann = DjvbApplication.class.getAnnotation(EnableRedisHttpSession.class);
        assertThat(ann)
                .as("@EnableRedisHttpSession must remain on DjvbApplication; "
                        + "session persistence assumes Redis-backed sessions")
                .isNotNull();
        assertThat(ann.maxInactiveIntervalInSeconds())
                .as("session timeout matches the long-lived session policy in application.properties")
                .isEqualTo(31557600);
    }
}
