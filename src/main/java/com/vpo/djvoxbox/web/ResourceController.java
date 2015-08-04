package com.vpo.djvoxbox.web;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserRepository;

@RestController
public class ResourceController {

	@Autowired UserRepository userRepository;
	
	@RequestMapping("/resource/{identifier}")
	  public @ResponseBody User home(@PathVariable String identifier) {
		User user = userRepository.findByIdentifier(identifier);
		if(user == null) {
			user = new User();
			user.setIdentifier(identifier);
			userRepository.save(user);
		}
		return user;
	  }
	
}
