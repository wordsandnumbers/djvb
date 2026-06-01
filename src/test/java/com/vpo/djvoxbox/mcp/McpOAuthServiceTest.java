package com.vpo.djvoxbox.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.vpo.djvoxbox.mcp.McpOAuthService.AuthorizeRequest;
import com.vpo.djvoxbox.mcp.McpOAuthService.OAuthException;
import com.vpo.djvoxbox.mcp.McpOAuthService.TokenRequest;

class McpOAuthServiceTest {

    private static final String VERIFIER = "abcdefghijklmnopqrstuvwxyz0123456789";
    private McpOAuthService service;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        service = new McpOAuthService("https://djvb.example.com", "consent", "", "",
                300, 3600, 2592000);
        request = new MockHttpServletRequest("POST", "/mcp");
    }

    @Test
    void metadataAdvertisesProtectedResourceAndAuthorizationServer() {
        Map<String, Object> resource = service.protectedResourceMetadata(request);
        Map<String, Object> auth = service.authorizationServerMetadata(request);

        assertThat(resource.get("resource")).isEqualTo("https://djvb.example.com/mcp");
        assertThat(resource.get("authorization_servers")).isEqualTo(List.of("https://djvb.example.com"));
        assertThat(auth.get("authorization_endpoint")).isEqualTo("https://djvb.example.com/oauth/authorize");
        assertThat(auth.get("token_endpoint")).isEqualTo("https://djvb.example.com/oauth/token");
        assertThat(auth.get("registration_endpoint")).isEqualTo("https://djvb.example.com/oauth/register");
    }

    @Test
    void dynamicRegistrationAuthorizationCodeAndRefreshFlow() {
        Map<String, Object> registered = service.registerClient(Map.of(
                "redirect_uris", List.of("https://claude.example.com/callback")), request);
        String clientId = (String) registered.get("client_id");

        String redirect = service.authorize(new AuthorizeRequest(
                "code",
                clientId,
                "https://claude.example.com/callback",
                challenge(VERIFIER),
                "S256",
                "state-1",
                "mcp offline_access",
                "consent"));

        assertThat(redirect).startsWith("https://claude.example.com/callback?code=");
        assertThat(redirect).contains("state=state-1");
        String code = redirect.substring(redirect.indexOf("code=") + "code=".length(), redirect.indexOf("&state="));

        var token = service.exchangeAuthorizationCode(new TokenRequest(
                "authorization_code",
                code,
                "https://claude.example.com/callback",
                clientId,
                null,
                VERIFIER,
                null));

        assertThat(token.tokenType()).isEqualTo("Bearer");
        assertThat(service.isAccessTokenValid(token.accessToken())).isTrue();

        var refreshed = service.refresh(new TokenRequest(
                "refresh_token",
                null,
                null,
                clientId,
                null,
                null,
                token.refreshToken()));

        assertThat(service.isAccessTokenValid(refreshed.accessToken())).isTrue();
    }

    @Test
    void badConsentCodeDoesNotIssueAuthorizationCode() {
        Map<String, Object> registered = service.registerClient(Map.of(
                "redirect_uris", List.of("https://claude.example.com/callback")), request);
        String clientId = (String) registered.get("client_id");

        assertThatThrownBy(() -> service.authorize(new AuthorizeRequest(
                "code",
                clientId,
                "https://claude.example.com/callback",
                challenge(VERIFIER),
                "S256",
                null,
                null,
                "wrong")))
                .isInstanceOf(OAuthException.class)
                .hasMessageContaining("Invalid authorization code");
    }

    private static String challenge(String verifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(digest.digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
