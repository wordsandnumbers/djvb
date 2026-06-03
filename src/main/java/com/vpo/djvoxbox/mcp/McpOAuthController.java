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
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Authorize DJVB MCP</title>
                  <style>
                    :root {
                      color-scheme: light;
                      --border: #d8d8d8;
                      --ink: #222;
                      --muted: #666;
                      --page: #f5f5f5;
                      --panel: #fff;
                      --primary: #387ef5;
                      --primary-active: #2d6ed8;
                    }

                    * {
                      box-sizing: border-box;
                    }

                    body {
                      margin: 0;
                      min-height: 100vh;
                      background: var(--page);
                      color: var(--ink);
                      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                      line-height: 1.45;
                    }

                    .bar {
                      min-height: 44px;
                      border-bottom: 1px solid var(--border);
                      background: #f8f8f8;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      padding: 0 16px;
                    }

                    .bar-title {
                      font-size: 17px;
                      font-weight: 600;
                    }

                    main {
                      width: min(100%%, 480px);
                      margin: 0 auto;
                      padding: 28px 16px;
                    }

                    .panel {
                      background: var(--panel);
                      border: 1px solid var(--border);
                      border-radius: 2px;
                      overflow: hidden;
                      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
                    }

                    .section {
                      padding: 18px;
                      border-bottom: 1px solid #eee;
                    }

                    .section:last-child {
                      border-bottom: 0;
                    }

                    h1 {
                      margin: 0 0 6px;
                      font-size: 22px;
                      font-weight: 500;
                    }

                    p {
                      margin: 0;
                      color: var(--muted);
                    }

                    .permission-list {
                      margin: 14px 0 0;
                      padding: 0;
                      list-style: none;
                    }

                    .permission-list li {
                      display: flex;
                      gap: 10px;
                      padding: 8px 0;
                      color: #333;
                    }

                    .check {
                      width: 20px;
                      color: var(--primary);
                      font-weight: 600;
                      text-align: center;
                    }

                    .meta-label,
                    label {
                      display: block;
                      margin: 0 0 7px;
                      color: #444;
                      font-size: 13px;
                      font-weight: 600;
                    }

                    code {
                      display: block;
                      max-width: 100%%;
                      overflow-wrap: anywhere;
                      padding: 10px;
                      border: 1px solid #e2e2e2;
                      border-radius: 2px;
                      background: #fafafa;
                      color: #444;
                      font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
                      font-size: 12px;
                    }

                    input {
                      display: block;
                      width: 100%%;
                      min-height: 44px;
                      padding: 10px 12px;
                      border: 1px solid #ccc;
                      border-radius: 2px;
                      background: #fff;
                      color: var(--ink);
                      font-size: 16px;
                    }

                    input:focus {
                      border-color: var(--primary);
                      outline: 2px solid rgba(56, 126, 245, 0.18);
                      outline-offset: 0;
                    }

                    .actions {
                      display: flex;
                      gap: 10px;
                      align-items: center;
                      justify-content: flex-end;
                      padding: 14px 18px 18px;
                    }

                    button {
                      min-height: 44px;
                      min-width: 120px;
                      border: 1px solid var(--primary);
                      border-radius: 2px;
                      background: var(--primary);
                      color: #fff;
                      cursor: pointer;
                      font-size: 16px;
                      font-weight: 600;
                    }

                    button:hover,
                    button:focus {
                      background: var(--primary-active);
                      border-color: var(--primary-active);
                    }

                    .note {
                      margin-top: 12px;
                      color: #777;
                      font-size: 13px;
                    }

                    @media (max-width: 420px) {
                      main {
                        padding: 18px 10px;
                      }

                      .section,
                      .actions {
                        padding-left: 14px;
                        padding-right: 14px;
                      }

                      .actions,
                      button {
                        width: 100%%;
                      }
                    }
                  </style>
                </head>
                <body>
                  <header class="bar">
                    <div class="bar-title">DJ Voice Box</div>
                  </header>
                  <main>
                    <form class="panel" method="post" action="/oauth/authorize">
                      <section class="section">
                        <h1>Authorize MCP Access</h1>
                        <p>This will allow an MCP client to use karaoke room tools for this DJVB instance.</p>
                        <ul class="permission-list" aria-label="Requested access">
                          <li><span class="check" aria-hidden="true">&#10003;</span><span>Search the karaoke catalog</span></li>
                          <li><span class="check" aria-hidden="true">&#10003;</span><span>View room and queue state</span></li>
                          <li><span class="check" aria-hidden="true">&#10003;</span><span>Manage queue and playback controls</span></li>
                        </ul>
                      </section>
                      <section class="section">
                        <span class="meta-label">Redirect URI</span>
                        <code>%s</code>
                      </section>
                      <section class="section">
                        <label for="consent_code">Authorization code</label>
                        <input id="consent_code" type="password" name="consent_code" autocomplete="one-time-code" autofocus required>
                        <p class="note">Use the MCP authorization code provided by the DJVB operator.</p>
                      </section>
                      <div class="actions">
                        <button type="submit">Authorize</button>
                      </div>
                    <input type="hidden" name="response_type" value="%s">
                    <input type="hidden" name="client_id" value="%s">
                    <input type="hidden" name="redirect_uri" value="%s">
                    <input type="hidden" name="code_challenge" value="%s">
                    <input type="hidden" name="code_challenge_method" value="%s">
                    <input type="hidden" name="state" value="%s">
                    <input type="hidden" name="scope" value="%s">
                  </form>
                  </main>
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
