package com.vpo.djvoxbox.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class McpOAuthControllerTest {

    @Test
    void authorizeFormRendersStyledConsentPage() {
        McpOAuthService service = new McpOAuthService("http://localhost:8080", "consent", "", "",
                300, 3600, 2592000);
        McpOAuthController controller = new McpOAuthController(service);
        Map<String, Object> registered = service.registerClient(Map.of(
                "redirect_uris", List.of("http://localhost:5173/callback")),
                new MockHttpServletRequest("POST", "/oauth/register"));

        String html = controller.authorizeForm(
                "code",
                (String) registered.get("client_id"),
                "http://localhost:5173/callback",
                "challenge",
                "S256",
                "state",
                "mcp offline_access");

        assertThat(html)
                .contains("DJ Voice Box")
                .contains("Authorize MCP Access")
                .contains("permission-list")
                .contains("Authorization code")
                .contains("http://localhost:5173/callback")
                .contains("width: min(100%, 480px)");
    }
}
