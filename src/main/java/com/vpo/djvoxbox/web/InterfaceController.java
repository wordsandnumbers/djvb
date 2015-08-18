package com.vpo.djvoxbox.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UrlPathHelper;

import com.vpo.djvoxbox.domain.UserRepository;
import com.vpo.djvoxbox.util.DigitsResponse;

@RestController
public class InterfaceController {

	@Autowired UserRepository userRepository;
	@Autowired RestTemplate restTemplate;
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
}
