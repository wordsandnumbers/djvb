package com.vpo.djvoxbox.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.vpo.djvoxbox.app.UpdateService;
import com.vpo.djvoxbox.domain.Manager;
import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserRepository;

@RestController
public class ResourceController {

	@Autowired UserRepository userRepository;
	@Autowired UpdateService updateService;
	
	@RequestMapping(value="/newManager/{name}", method=RequestMethod.GET)
	public @ResponseBody Manager newManager(Principal principal, @PathVariable("name") String name) {
		return updateService.createManager(name);
	}
	
	
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
	
	
	@RequestMapping("/resource/test")
	public @ResponseBody String test(Principal principal) {
		return principal.getName();
	  }
}
