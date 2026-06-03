package com.vpo.djvoxbox.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import com.vpo.djvoxbox.security.authentication.FirebaseAuthenticationProvider;
import com.vpo.djvoxbox.security.web.authentication.SpringSessionRememberMeServices;
import com.vpo.djvoxbox.mcp.McpOAuthService;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Autowired
	private FirebaseAuthenticationProvider firebaseAuthenticationProvider;

	@Autowired
	private SpringSessionRememberMeServices springSessionRememberMeServices;

	@Value("${mcp.apiToken:}")
	private String mcpApiToken;

	@Autowired
	private McpOAuthService mcpOAuthService;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(firebaseAuthenticationProvider);
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.ignoring().requestMatchers("/resources/**", "/actuator/health", "/actuator/health/**");
	}

	@Bean
	@Order(1)
	public SecurityFilterChain oauthChain(HttpSecurity http) throws Exception {
		return http.securityMatcher("/.well-known/oauth-authorization-server",
				"/.well-known/openid-configuration",
				"/.well-known/oauth-protected-resource",
				"/.well-known/oauth-protected-resource/**",
				"/oauth/**")
			.authorizeHttpRequests(a -> a.anyRequest().permitAll())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(c -> c.disable())
			.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain mcpChain(HttpSecurity http) throws Exception {
		return http.securityMatcher("/mcp", "/mcp/**")
			.authorizeHttpRequests(a -> a.anyRequest().permitAll())
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(c -> c.disable())
			.addFilterBefore(new McpBearerTokenFilter(mcpApiToken, mcpOAuthService), UsernamePasswordAuthenticationFilter.class)
			.build();
	}

	@Bean
	@Order(3)
	public SecurityFilterChain avatarChain(HttpSecurity http) throws Exception {
		return http.securityMatcher("/api/v1/user/avatar/**")
			.authorizeHttpRequests(a -> a.anyRequest().authenticated())
			.headers(h -> h.cacheControl(c -> c.disable()))
			.csrf(c -> c.disable())
			.exceptionHandling(e -> e.authenticationEntryPoint(
				(req, res, ex) -> {
					res.setStatus(SC_UNAUTHORIZED);
					res.setContentType("application/json");
					res.getWriter().write("{\"error\":\"Unauthorized\"}");
				}))
			.build();
	}

	@Bean
	@Order(4)
	public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
		return http.securityMatcher("/api/**")
			.authorizeHttpRequests(a -> a.anyRequest().authenticated())
			.rememberMe(r -> r.rememberMeServices(springSessionRememberMeServices))
			.csrf(c -> c.disable())
			.exceptionHandling(e -> e.authenticationEntryPoint(
				(req, res, ex) -> {
					res.setStatus(SC_UNAUTHORIZED);
					res.setContentType("application/json");
					res.getWriter().write("{\"error\":\"Unauthorized\"}");
				}))
			.build();
	}

	@Bean
	@Order(5)
	public SecurityFilterChain mainChain(HttpSecurity http) throws Exception {
		return http
			.sessionManagement(s -> s.sessionFixation().migrateSession())
			.csrf(c -> c.disable())
			.authorizeHttpRequests(a -> a.anyRequest().authenticated())
			.formLogin(f -> f
				.loginPage("/")
				.loginProcessingUrl("/login/login")
				.usernameParameter("name")
				.passwordParameter("idToken")
				.successHandler((req, res, auth) -> {})
				.permitAll())
			.logout(l -> l.permitAll())
			.build();
	}

	static class McpBearerTokenFilter extends OncePerRequestFilter {
		private final String token;
		private final McpOAuthService oauthService;

		McpBearerTokenFilter(String token, McpOAuthService oauthService) {
			this.token = token;
			this.oauthService = oauthService;
		}

		@Override
		protected void doFilterInternal(HttpServletRequest request,
				HttpServletResponse response, FilterChain filterChain)
				throws ServletException, IOException {
			String actual = request.getHeader(HttpHeaders.AUTHORIZATION);
			if (isAuthorized(actual)) {
				filterChain.doFilter(request, response);
				return;
			}
			response.setHeader(HttpHeaders.WWW_AUTHENTICATE, oauthService.authorizationChallenge(request));
			response.setStatus(SC_UNAUTHORIZED);
			response.setContentType("application/json");
			response.getWriter().write("{\"error\":\"Unauthorized\"}");
		}

		private boolean isAuthorized(String authorization) {
			if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
				return false;
			}
			String bearerToken = authorization.substring("Bearer ".length());
			if (StringUtils.hasText(token) && token.equals(bearerToken)) {
				return true;
			}
			return oauthService.isAccessTokenValid(bearerToken);
		}
	}

	public static class CsrfHeaderFilter extends OncePerRequestFilter {
		@Override
		protected void doFilterInternal(HttpServletRequest request,
				HttpServletResponse response, FilterChain filterChain)
				throws ServletException, IOException {
			CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
			if (csrf != null) {
				Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
				String token = csrf.getToken();
				if (cookie == null || token != null && !token.equals(cookie.getValue())) {
					cookie = new Cookie("XSRF-TOKEN", token);
					cookie.setPath("/");
					response.addCookie(cookie);
				}
			}
			filterChain.doFilter(request, response);
		}
	}

	public static CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName("X-XSRF-TOKEN");
		return repository;
	}
}
