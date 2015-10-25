package com.vpo.djvoxbox.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserRepository;
import com.vpo.djvoxbox.util.DigitsResponse;
import com.vpo.vbclient.model.Session;
import com.vpo.vbclient.session.SessionClient;

@RestController
public class UserController {

	@Autowired UserRepository userRepository;
	@Autowired RestTemplate restTemplate;
	@Autowired SessionClient sessionClient;
	@Value("${digits.consumer.key}")
	private String digitsConsumerKey;
	
	@RequestMapping("/login/verify")
	public @ResponseBody boolean verifyLogin(@RequestParam String authHeader, @RequestParam String apiUrl) {
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
			System.out.println(response.getBody());
		} else {
			System.out.println("rejected");
			return false;
		}
		return true;
	}
	 
	@RequestMapping("/login/test")
	public String test() {
		return "foo";
	}
	
	@RequestMapping(value ="/api/v1/user/user", method=RequestMethod.GET)
	public @ResponseBody User getLoggedInUser(Principal principal) {
		User user = userRepository.findById(principal.getName());
		// check here for a valid session?
		Session session = confirmAndEnsureSession(user);
		if(session != null && (user.getSessionId() == null || !user.getSessionId().equals(session.getSession()))) {
			user.setSessionId(session.getSession());
			userRepository.save(user);
		}
		return user;
	}
	
	private Session confirmAndEnsureSession(final User user) {
		if(user.getSessionId() == null && user.getEmail() != null) {
			// create a session
			return createSessionFromUser(user);
		} else if(user.getSessionId() != null) {
			Session session = null;
			try {
			session =  sessionClient.getSessionById(user.getSessionId());
			} catch (HttpClientErrorException e) {
				return null;
			}
			if(session == null) {
				return createSessionFromUser(user);
			}
			return session;
		}
		return null;
	}

	private Session createSessionFromUser(final User user) {
		Session session = new Session();
		session.setEmail(user.getEmail());
		if(user.getScreenName() != null && !user.getScreenName().isEmpty()) {
			session.setHandle(user.getScreenName());
		}
		return sessionClient.createSession(session);
	}

	@RequestMapping(value = "/api/v1/user", method=RequestMethod.PUT)
	public @ResponseBody User updateUser(@RequestBody User user, Principal principal) {
		User lUser = userRepository.findById(principal.getName());
		lUser.setEmail(user.getEmail());
		lUser.setName(user.getName());
		lUser.setScreenName(user.getScreenName());
		
		userRepository.save(lUser);
		Session session = createOrUpdateSessionForUser(lUser);
		if(session != null && (lUser.getSessionId() == null || !lUser.getSessionId().equals(session.getSession()))) {
			lUser.setSessionId(session.getSession());
			userRepository.save(lUser);
		}
		return lUser;
		
	}

	private Session createOrUpdateSessionForUser(User user) {
		Session session = confirmAndEnsureSession(user);
		if(session != null) {
			if(user.getScreenName() != null && !user.getScreenName().isEmpty()) {
				session.setHandle(user.getScreenName());
			}
			return sessionClient.updateSession(session);
		}
		return null;
	}
	
}
