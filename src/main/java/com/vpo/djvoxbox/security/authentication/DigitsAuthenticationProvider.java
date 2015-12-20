package com.vpo.djvoxbox.security.authentication;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserRepository;
import com.vpo.djvoxbox.util.DigitsResponse;

@Component
public class DigitsAuthenticationProvider implements AuthenticationProvider {

	@Autowired UserRepository userRepository;
	@Autowired RestTemplate restTemplate;
	@Value("${digits.consumer.key}")
	private String digitsConsumerKey;
	@Value("${vb.organization}")
	private String vbOrganization;
	
	@Override
	public Authentication authenticate(Authentication authenticate)
			throws AuthenticationException {
		// if the organization is not empty, add a delimiter
		String organization = (vbOrganization == null || vbOrganization.isEmpty()) ? "" : "|" + vbOrganization;
		// this is either the actual apiUrl, if digits, or else the email address
		String apiUrl = authenticate.getName();
		String authHeader = authenticate.getCredentials().toString();
		authenticate = null;
		if(isDigits(apiUrl, authHeader)) {
			User user = lookupOrCreateDigitsUser(apiUrl, authHeader, organization);
			List<GrantedAuthority> grantedAuths = new ArrayList<>();
			grantedAuths.add(new SimpleGrantedAuthority(user.getRole()));
			authenticate = new UsernamePasswordAuthenticationToken(user.getId(), authHeader, grantedAuths);
		} else {
			// email only
			String email = apiUrl;
			String identifier = email + organization;
			User user = userRepository.findByIdentifier(identifier);
			if(user == null) {
				user = userRepository.save(new User(identifier, null, email));
			} 
			List<GrantedAuthority> grantedAuths = new ArrayList<>();
			grantedAuths.add(new SimpleGrantedAuthority(user.getRole()));
			authenticate = new UsernamePasswordAuthenticationToken(user.getId(), user.getEmail(), grantedAuths);
		}
		return authenticate;
	}

	private User lookupOrCreateDigitsUser(String apiUrl, String authHeader, String organization) {
		boolean uri_valid = true;
		URI uri = null;
		try {
			uri = new URI(apiUrl);
		} catch (URISyntaxException e) {
			uri_valid = false;
		}
		if(authHeader.contains(digitsConsumerKey) && uri_valid && uri != null && (uri.getHost().equals("api.digits.com") || uri.getHost().equals("api.twitter.com")) ) {
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.set("authorization", authHeader);
			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			ResponseEntity<DigitsResponse> response = restTemplate.exchange(apiUrl, HttpMethod.GET, entity, DigitsResponse.class);
			String identifier = response.getBody().getId() + organization;
			User user = userRepository.findByIdentifier(identifier);
			if(user == null) {
				user = userRepository.save(new User(identifier, response.getBody().getPhoneNumber(), null));
			}
			return user;
		} else {
			return null;
		}
	}

	private boolean isDigits(String apiUrl, String authHeader) {
		return apiUrl != null && !authHeader.isEmpty();
	}

	@Override
    public boolean supports(Class<? extends Object> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
