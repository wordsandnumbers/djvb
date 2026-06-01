package com.vpo.djvoxbox.mcp;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vpo.djvoxbox.mcp.McpOAuthService.AuthorizeRequest;
import com.vpo.djvoxbox.mcp.McpOAuthService.OAuthException;
import com.vpo.djvoxbox.mcp.McpOAuthService.TokenRequest;
import com.vpo.djvoxbox.mcp.McpOAuthService.TokenResponse;

@RestController
public class McpOAuthController {

    private final McpOAuthService oauthService;

    public McpOAuthController(McpOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    @GetMapping({
            "/.well-known/oauth-protected-resource",
            "/.well-known/oauth-protected-resource/mcp"
    })
    public Map<String, Object> protectedResourceMetadata(HttpServletRequest request) {
        return oauthService.protectedResourceMetadata(request);
    }

    @GetMapping({
            "/.well-known/oauth-authorization-server",
            "/.well-known/openid-configuration"
    })
    public Map<String, Object> authorizationServerMetadata(HttpServletRequest request) {
        return oauthService.authorizationServerMetadata(request);
    }

    @PostMapping(path = "/oauth/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oauthService.registerClient(body, request));
    }

    @GetMapping(path = "/oauth/authorize", produces = MediaType.TEXT_HTML_VALUE)
    public String authorizeForm(
            @RequestParam(name = "response_type") String responseType,
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "redirect_uri") String redirectUri,
            @RequestParam(name = "code_challenge") String codeChallenge,
            @RequestParam(name = "code_challenge_method") String codeChallengeMethod,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "scope", required = false) String scope) {
        oauthService.requireClient(clientId);
        return """
                <!doctype html>
                <html>
                <head><title>Authorize DJVB MCP</title></head>
                <body>
                  <h1>Authorize DJVB MCP</h1>
                  <p>This will allow an MCP client to use karaoke room tools for this DJVB instance.</p>
                  <p>Redirect URI: <code>%s</code></p>
                  <form method="post" action="/oauth/authorize">
                    <input type="hidden" name="response_type" value="%s">
                    <input type="hidden" name="client_id" value="%s">
                    <input type="hidden" name="redirect_uri" value="%s">
                    <input type="hidden" name="code_challenge" value="%s">
                    <input type="hidden" name="code_challenge_method" value="%s">
                    <input type="hidden" name="state" value="%s">
                    <input type="hidden" name="scope" value="%s">
                    <label>Authorization code
                      <input type="password" name="consent_code" autofocus>
                    </label>
                    <button type="submit">Authorize</button>
                  </form>
                </body>
                </html>
                """.formatted(
                html(redirectUri),
                html(responseType),
                html(clientId),
                html(redirectUri),
                html(codeChallenge),
                html(codeChallengeMethod),
                html(state),
                html(scope));
    }

    @PostMapping(path = "/oauth/authorize", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> authorize(
            @RequestParam(name = "response_type") String responseType,
            @RequestParam(name = "client_id") String clientId,
            @RequestParam(name = "redirect_uri") String redirectUri,
            @RequestParam(name = "code_challenge") String codeChallenge,
            @RequestParam(name = "code_challenge_method") String codeChallengeMethod,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam(name = "scope", required = false) String scope,
            @RequestParam(name = "consent_code") String consentCode) {
        String redirect = oauthService.authorize(new AuthorizeRequest(
                responseType, clientId, redirectUri, codeChallenge, codeChallengeMethod, state, scope, consentCode));
        return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, redirect).build();
    }

    @PostMapping(path = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Map<String, Object> token(
            @RequestParam(name = "grant_type") String grantType,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "redirect_uri", required = false) String redirectUri,
            @RequestParam(name = "client_id", required = false) String clientId,
            @RequestParam(name = "client_secret", required = false) String clientSecret,
            @RequestParam(name = "code_verifier", required = false) String codeVerifier,
            @RequestParam(name = "refresh_token", required = false) String refreshToken,
            HttpServletRequest request) {
        ClientAuth clientAuth = clientAuth(request);
        String resolvedClientId = StringUtils.hasText(clientAuth.clientId()) ? clientAuth.clientId() : clientId;
        String resolvedClientSecret = StringUtils.hasText(clientAuth.clientSecret()) ? clientAuth.clientSecret() : clientSecret;
        TokenRequest tokenRequest = new TokenRequest(
                grantType, code, redirectUri, resolvedClientId, resolvedClientSecret, codeVerifier, refreshToken);
        TokenResponse response;
        if ("authorization_code".equals(grantType)) {
            response = oauthService.exchangeAuthorizationCode(tokenRequest);
        } else if ("refresh_token".equals(grantType)) {
            response = oauthService.refresh(tokenRequest);
        } else {
            throw new OAuthException("unsupported_grant_type", "Only authorization_code and refresh_token are supported");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("access_token", response.accessToken());
        body.put("token_type", response.tokenType());
        body.put("expires_in", response.expiresIn());
        body.put("refresh_token", response.refreshToken());
        body.put("scope", response.scope());
        return body;
    }

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<Map<String, String>> oauthError(OAuthException e) {
        HttpStatus status = "invalid_client".equals(e.error()) ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of(
                "error", e.error(),
                "error_description", e.getMessage()));
    }

    private ClientAuth clientAuth(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Basic ")) {
            return new ClientAuth(null, null);
        }
        String decoded = new String(Base64.getDecoder().decode(authorization.substring("Basic ".length())),
                StandardCharsets.UTF_8);
        int delimiter = decoded.indexOf(':');
        if (delimiter < 0) {
            return new ClientAuth(decoded, null);
        }
        return new ClientAuth(decoded.substring(0, delimiter), decoded.substring(delimiter + 1));
    }

    private static String html(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private record ClientAuth(String clientId, String clientSecret) {
    }
}
