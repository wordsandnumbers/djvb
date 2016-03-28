package com.vpo.djvoxbox.config;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import com.vpo.djvoxbox.security.authentication.DigitsAuthenticationProvider;
import com.vpo.djvoxbox.security.web.authentication.SpringSessionRememberMeServices;

@Configuration
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private DigitsAuthenticationProvider digitsAuthenticationProvider;
	
	@Autowired private static SpringSessionRememberMeServices springSessionRememberMeServices;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(digitsAuthenticationProvider);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
            .ignoring()
            .antMatchers("/resources/**");
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
		http
        	.sessionManagement()
        	.sessionFixation().migrateSession()
        	.and()
        	.csrf().disable()
            .authorizeRequests()
                .anyRequest().authenticated()
                .and()
/*                .addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
                    .csrf().csrfTokenRepository(csrfTokenRepository())
                .and()*/
            .formLogin()
                .loginPage("/")
                .loginProcessingUrl("/login/login")
                .usernameParameter("apiUrl")
                .passwordParameter("authHeader")
                .successHandler(new LoginSuccessHandler())
                .permitAll()
             .and()
             .logout()
             .permitAll();
    }

	@Configuration
	public static class ApiWebSecurityConfig extends WebSecurityConfigurerAdapter {
		private boolean csrfDisabled = true;
		
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.antMatcher("/api/**")
			.authorizeRequests()
			.anyRequest().authenticated()
			.and().rememberMe()
			.rememberMeServices(springSessionRememberMeServices);
			
			configureCsrf(http, csrfDisabled)
				.exceptionHandling()
					.authenticationEntryPoint(new AuthenticationEntryPoint() {
						@Override
						public void commence(HttpServletRequest request, HttpServletResponse response,
								AuthenticationException ex) throws IOException, ServletException {
					        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");							
						}
					});
		}
	}
		
	public class LoginSuccessHandler implements AuthenticationSuccessHandler {
		
		public void onAuthenticationSuccess(HttpServletRequest request,
				HttpServletResponse response, Authentication auth)
				throws IOException, ServletException {
		}
	}
	
	private static HttpSecurity configureCsrf(HttpSecurity http, boolean disabled) throws Exception {
		if (disabled) {
			return http.csrf().disable();
		} else {
			http.addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
				.csrf().csrfTokenRepository(csrfTokenRepository());
			return http;
		}
	}

	public static class CsrfHeaderFilter extends OncePerRequestFilter {
		@Override
		protected void doFilterInternal(HttpServletRequest request,
				HttpServletResponse response, FilterChain filterChain)
				throws ServletException, IOException {
			CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class
					.getName());
			if (csrf != null) {
				Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
				String token = csrf.getToken();
				if (cookie == null || token != null
						&& !token.equals(cookie.getValue())) {
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