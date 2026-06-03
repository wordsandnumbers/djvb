package com.vpo.djvoxbox.mcp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class McpOAuthService {

    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "::1");
    private static final String MCP_SCOPE = "mcp";
    private static final String OFFLINE_SCOPE = "offline_access";

    private final SecureRandom secureRandom = new SecureRandom();
    private final Clock clock = Clock.systemUTC();
    private final Map<String, OAuthClient> clients = new ConcurrentHashMap<>();
    private final Map<String, AuthorizationCode> authorizationCodes = new ConcurrentHashMap<>();
    private final Map<String, AccessToken> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, RefreshToken> refreshTokens = new ConcurrentHashMap<>();

    private final String publicBaseUrl;
    private final String consentCode;
    private final String staticClientId;
    private final String staticClientSecret;
    private final long authorizationCodeTtlSeconds;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;

    public McpOAuthService(
            @Value("${mcp.publicBaseUrl:}") String publicBaseUrl,
            @Value("${mcp.oauth.consentCode:${mcp.apiToken:}}") String consentCode,
            @Value("${mcp.oauth.clientId:}") String staticClientId,
            @Value("${mcp.oauth.clientSecret:}") String staticClientSecret,
            @Value("${mcp.oauth.authorizationCodeTtlSeconds:300}") long authorizationCodeTtlSeconds,
            @Value("${mcp.oauth.accessTokenTtlSeconds:3600}") long accessTokenTtlSeconds,
            @Value("${mcp.oauth.refreshTokenTtlSeconds:2592000}") long refreshTokenTtlSeconds) {
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.consentCode = consentCode;
        this.staticClientId = staticClientId;
        this.staticClientSecret = staticClientSecret;
        this.authorizationCodeTtlSeconds = authorizationCodeTtlSeconds;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    public Map<String, Object> protectedResourceMetadata(HttpServletRequest request) {
        return Map.of(
                "resource", mcpResource(request),
                "authorization_servers", List.of(issuer(request)),
                "bearer_methods_supported", List.of("header"));
    }

    public Map<String, Object> authorizationServerMetadata(HttpServletRequest request) {
        String issuer = issuer(request);
        return Map.ofEntries(
                Map.entry("issuer", issuer),
                Map.entry("authorization_endpoint", issuer + "/oauth/authorize"),
                Map.entry("token_endpoint", issuer + "/oauth/token"),
                Map.entry("registration_endpoint", issuer + "/oauth/register"),
                Map.entry("response_types_supported", List.of("code")),
                Map.entry("grant_types_supported", List.of("authorization_code", "refresh_token")),
                Map.entry("code_challenge_methods_supported", List.of("S256")),
                Map.entry("token_endpoint_auth_methods_supported",
                        List.of("none", "client_secret_post", "client_secret_basic")),
                Map.entry("scopes_supported", List.of(MCP_SCOPE, OFFLINE_SCOPE)));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> registerClient(Map<String, Object> body, HttpServletRequest request) {
        List<String> redirectUris = (List<String>) body.get("redirect_uris");
        if (redirectUris == null || redirectUris.isEmpty()) {
            throw new OAuthException("invalid_client_metadata", "redirect_uris is required");
        }
        for (String redirectUri : redirectUris) {
            requireSafeRedirectUri(redirectUri);
        }
        String clientId = "djvb-" + UUID.randomUUID();
        OAuthClient client = new OAuthClient(clientId, null, Set.copyOf(redirectUris), false);
        clients.put(clientId, client);
        return Map.of(
                "client_id", clientId,
                "client_id_issued_at", clock.instant().getEpochSecond(),
                "redirect_uris", redirectUris,
                "grant_types", List.of("authorization_code", "refresh_token"),
                "response_types", List.of("code"),
                "token_endpoint_auth_method", "none");
    }

    public OAuthClient requireClient(String clientId) {
        if (StringUtils.hasText(staticClientId) && staticClientId.equals(clientId)) {
            return new OAuthClient(staticClientId, blankToNull(staticClientSecret), Set.of(), true);
        }
        OAuthClient client = clients.get(clientId);
        if (client == null) {
            throw new OAuthException("invalid_client", "Unknown client_id");
        }
        return client;
    }

    public String authorize(AuthorizeRequest request) {
        OAuthClient client = requireClient(request.clientId());
        if (!"code".equals(request.responseType())) {
            throw new OAuthException("unsupported_response_type", "Only response_type=code is supported");
        }
        if (!"S256".equals(request.codeChallengeMethod())) {
            throw new OAuthException("invalid_request", "Only S256 PKCE is supported");
        }
        validateRedirectUri(client, request.redirectUri());
        if (!StringUtils.hasText(request.codeChallenge())) {
            throw new OAuthException("invalid_request", "code_challenge is required");
        }
        if (!StringUtils.hasText(consentCode) || !consentCode.equals(request.consentCode())) {
            throw new OAuthException("access_denied", "Invalid authorization code");
        }
        String code = randomToken();
        authorizationCodes.put(code, new AuthorizationCode(
                code,
                request.clientId(),
                request.redirectUri(),
                request.codeChallenge(),
                scopeOrDefault(request.scope()),
                clock.instant().plusSeconds(authorizationCodeTtlSeconds),
                false));
        UriComponentsBuilder redirect = UriComponentsBuilder.fromUriString(request.redirectUri())
                .queryParam("code", code);
        if (StringUtils.hasText(request.state())) {
            redirect.queryParam("state", request.state());
        }
        return redirect.build().toUriString();
    }

    public TokenResponse exchangeAuthorizationCode(TokenRequest request) {
        OAuthClient client = requireClient(request.clientId());
        requireClientSecretIfNeeded(client, request);
        AuthorizationCode code = authorizationCodes.get(request.code());
        if (code == null || code.used() || code.expiresAt().isBefore(clock.instant())) {
            throw new OAuthException("invalid_grant", "Authorization code is invalid or expired");
        }
        if (!code.clientId().equals(request.clientId())) {
            throw new OAuthException("invalid_grant", "Authorization code was not issued to this client");
        }
        if (!code.redirectUri().equals(request.redirectUri())) {
            throw new OAuthException("invalid_grant", "redirect_uri does not match");
        }
        if (!verifyPkce(code.codeChallenge(), request.codeVerifier())) {
            throw new OAuthException("invalid_grant", "PKCE verification failed");
        }
        authorizationCodes.put(request.code(), code.markUsed());
        return issueTokens(request.clientId(), code.scope());
    }

    public TokenResponse refresh(TokenRequest request) {
        OAuthClient client = requireClient(request.clientId());
        requireClientSecretIfNeeded(client, request);
        RefreshToken refresh = refreshTokens.remove(request.refreshToken());
        if (refresh == null || refresh.expiresAt().isBefore(clock.instant())) {
            throw new OAuthException("invalid_grant", "Refresh token is invalid or expired");
        }
        if (!refresh.clientId().equals(request.clientId())) {
            throw new OAuthException("invalid_grant", "Refresh token was not issued to this client");
        }
        return issueTokens(request.clientId(), refresh.scope());
    }

    public boolean isAccessTokenValid(String token) {
        AccessToken accessToken = accessTokens.get(token);
        if (accessToken == null) {
            return false;
        }
        if (accessToken.expiresAt().isBefore(clock.instant())) {
            accessTokens.remove(token);
            return false;
        }
        return true;
    }

    public String mcpResource(HttpServletRequest request) {
        return baseUrl(request) + "/mcp";
    }

    public String authorizationChallenge(HttpServletRequest request) {
        return "Bearer resource_metadata=\"" + baseUrl(request) + "/.well-known/oauth-protected-resource/mcp\"";
    }

    public String issuer(HttpServletRequest request) {
        return baseUrl(request);
    }

    private TokenResponse issueTokens(String clientId, String scope) {
        Instant now = clock.instant();
        String accessToken = randomToken();
        String refreshToken = randomToken();
        accessTokens.put(accessToken,
                new AccessToken(accessToken, clientId, scope, now.plusSeconds(accessTokenTtlSeconds)));
        refreshTokens.put(refreshToken,
                new RefreshToken(refreshToken, clientId, scope, now.plusSeconds(refreshTokenTtlSeconds)));
        return new TokenResponse(accessToken, "Bearer", accessTokenTtlSeconds, refreshToken, scope);
    }

    private void validateRedirectUri(OAuthClient client, String redirectUri) {
        requireSafeRedirectUri(redirectUri);
        if (client.dynamic() && !client.redirectUris().contains(redirectUri)) {
            throw new OAuthException("invalid_request", "redirect_uri is not registered for this client");
        }
    }

    private void requireSafeRedirectUri(String redirectUri) {
        try {
            var uri = java.net.URI.create(redirectUri);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (!StringUtils.hasText(scheme) || !StringUtils.hasText(host)) {
                throw new IllegalArgumentException("missing scheme or host");
            }
            if ("https".equalsIgnoreCase(scheme)) {
                return;
            }
            if ("http".equalsIgnoreCase(scheme) && LOOPBACK_HOSTS.contains(host)) {
                return;
            }
        } catch (IllegalArgumentException e) {
            throw new OAuthException("invalid_request", "redirect_uri must be HTTPS or loopback HTTP");
        }
        throw new OAuthException("invalid_request", "redirect_uri must be HTTPS or loopback HTTP");
    }

    private void requireClientSecretIfNeeded(OAuthClient client, TokenRequest request) {
        if (!StringUtils.hasText(client.clientSecret())) {
            return;
        }
        String supplied = blankToNull(request.clientSecret());
        if (!client.clientSecret().equals(supplied)) {
            throw new OAuthException("invalid_client", "Invalid client_secret");
        }
    }

    private boolean verifyPkce(String expectedChallenge, String verifier) {
        if (!StringUtils.hasText(verifier)) {
            return false;
        }
        return expectedChallenge.equals(s256(verifier));
    }

    private String s256(String verifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compute PKCE challenge", e);
        }
    }

    private String scopeOrDefault(String scope) {
        return StringUtils.hasText(scope) ? scope : MCP_SCOPE + " " + OFFLINE_SCOPE;
    }

    private String baseUrl(HttpServletRequest request) {
        if (StringUtils.hasText(publicBaseUrl)) {
            return publicBaseUrl;
        }
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(null)
                .replaceQuery(null)
                .build()
                .toUriString();
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String trimTrailingSlash(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    public record OAuthClient(String clientId, String clientSecret, Set<String> redirectUris, boolean staticClient) {
        boolean dynamic() {
            return !staticClient;
        }
    }

    public record AuthorizeRequest(
            String responseType,
            String clientId,
            String redirectUri,
            String codeChallenge,
            String codeChallengeMethod,
            String state,
            String scope,
            String consentCode) {
    }

    public record TokenRequest(
            String grantType,
            String code,
            String redirectUri,
            String clientId,
            String clientSecret,
            String codeVerifier,
            String refreshToken) {
    }

    public record TokenResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            String refreshToken,
            String scope) {
    }

    private record AuthorizationCode(
            String code,
            String clientId,
            String redirectUri,
            String codeChallenge,
            String scope,
            Instant expiresAt,
            boolean used) {
        AuthorizationCode markUsed() {
            return new AuthorizationCode(code, clientId, redirectUri, codeChallenge, scope, expiresAt, true);
        }
    }

    private record AccessToken(String token, String clientId, String scope, Instant expiresAt) {
    }

    private record RefreshToken(String token, String clientId, String scope, Instant expiresAt) {
    }

    public static class OAuthException extends RuntimeException {
        private final String error;

        OAuthException(String error, String message) {
            super(message);
            this.error = error;
        }

        public String error() {
            return error;
        }
    }
}
