package com.vpo.djvoxbox.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class McpBearerTokenFilterTest {

    @Test
    void missingConfiguredTokenReturnsUnavailable() throws Exception {
        SecurityConfiguration.McpBearerTokenFilter filter =
                new SecurityConfiguration.McpBearerTokenFilter("");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getContentAsString()).contains("not configured");
    }

    @Test
    void wrongBearerTokenReturnsUnauthorized() throws Exception {
        SecurityConfiguration.McpBearerTokenFilter filter =
                new SecurityConfiguration.McpBearerTokenFilter("secret");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer wrong");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Unauthorized");
    }

    @Test
    void matchingBearerTokenAllowsRequestThrough() throws Exception {
        SecurityConfiguration.McpBearerTokenFilter filter =
                new SecurityConfiguration.McpBearerTokenFilter("secret");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/mcp");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
    }
}
