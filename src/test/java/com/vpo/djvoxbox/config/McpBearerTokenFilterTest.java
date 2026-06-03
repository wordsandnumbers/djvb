package com.vpo.djvoxbox.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.vpo.djvoxbox.mcp.McpOAuthService;

class McpBearerTokenFilterTest {

    @Test
    void missingBearerTokenReturnsUnauthorizedWithOauthChallenge() throws Exception {
        McpOAuthService oauthService = mock(McpOAuthService.class);
        when(oauthService.authorizationChallenge(org.mockito.ArgumentMatchers.any()))
                .thenReturn("Bearer resource_metadata=\"http://localhost/.well-known/oauth-protected-resource/mcp\"");
        SecurityConfiguration.McpBearerTokenFilter filter =
                new SecurityConfiguration.McpBearerTokenFilter("", oauthService);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeader(HttpHeaders.WWW_AUTHENTICATE))
                .contains("oauth-protected-resource");
    }

    @Test
    void wrongBearerTokenReturnsUnauthorized() throws Exception {
        McpOAuthService oauthService = mock(McpOAuthService.class);
        when(oauthService.authorizationChallenge(org.mockito.ArgumentMatchers.any()))
                .thenReturn("Bearer resource_metadata=\"http://localhost/.well-known/oauth-protected-resource/mcp\"");
        SecurityConfiguration.McpBearerTokenFilter filter =
                new SecurityConfiguration.McpBearerTokenFilter("secret", oauthService);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer wrong");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Unauthorized");
    }

    @Test
    void matchingBearerTokenAllowsRequestThrough() throws Exception {
        McpOAuthService oauthService = mock(McpOAuthService.class);
        SecurityConfiguration.McpBearerTokenFilter filter =
                new SecurityConfiguration.McpBearerTokenFilter("secret", oauthService);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void oauthAccessTokenAllowsRequestThrough() throws Exception {
        McpOAuthService oauthService = mock(McpOAuthService.class);
        when(oauthService.isAccessTokenValid("oauth-token")).thenReturn(true);
        SecurityConfiguration.McpBearerTokenFilter filter =
                new SecurityConfiguration.McpBearerTokenFilter("", oauthService);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer oauth-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
