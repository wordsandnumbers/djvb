package com.vpo.djvoxbox.security.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.firebase.auth.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Component
public class FirebaseAuthenticationProvider implements AuthenticationProvider {

	@Autowired UserRepository userRepository;
	@Autowired RestTemplate restTemplate;
	@Value("${vb.organization}")
	private String vbOrganization;

	@Override
	public Authentication authenticate(Authentication authenticate)	throws AuthenticationException {
		// if the organization is not empty, add a delimiter
		String organization = (vbOrganization == null || vbOrganization.isEmpty()) ? "" : "|" + vbOrganization;
		// this is either the actual apiUrl, if digits, or else the email address
		String name = authenticate.getName();
		String idToken = authenticate.getCredentials().toString();
		authenticate = null;
		if(!idToken.isEmpty()) {
			User user;
			try {
				user = lookupOrCreateFirebaseUser(idToken, organization);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			List<GrantedAuthority> grantedAuths = new ArrayList<>();
			grantedAuths.add(new SimpleGrantedAuthority(user.getRole()));
			authenticate = new UsernamePasswordAuthenticationToken(user.getId(), idToken, grantedAuths);
		} else {
			// email only
			String email = name;
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

	private User lookupOrCreateFirebaseUser(String idToken, String organization) throws IOException, InterruptedException, ExecutionException {

		FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdTokenAsync(idToken).get();
		String uid = decodedToken.getUid();

		UserRecord fbUser = FirebaseAuth.getInstance().getUserAsync(uid).get();

		String identifier = uid + organization;
		User user = userRepository.findByIdentifier(identifier);
		if(user == null) {
			user = userRepository.save(new User(identifier, fbUser.getPhoneNumber(), fbUser.getEmail()));
		}
		return user;
	}

	@Override
    public boolean supports(Class<? extends Object> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

}
