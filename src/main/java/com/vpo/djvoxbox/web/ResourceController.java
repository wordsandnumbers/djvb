package com.vpo.djvoxbox.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
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
import com.vpo.djvoxbox.domain.Playlist;
import com.vpo.djvoxbox.domain.Playlists;
import com.vpo.djvoxbox.domain.PlaylistsRepository;
import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserRepository;

@RestController
public class ResourceController {

	@Autowired UserRepository userRepository;
	@Autowired UpdateService updateService;
	@Autowired PlaylistsRepository playlistsRepository;
	
//	@RequestMapping(value="/newManager/{name}", method=RequestMethod.GET)
//	public @ResponseBody Manager newManager(Principal principal, @PathVariable("name") String name) {
//		return updateService.createManager(name);
//	}
//	
	
//	@RequestMapping("/resource/{identifier}")
//	  public @ResponseBody User home(@PathVariable String identifier) {
//		User user = userRepository.findByIdentifier(identifier);
//		if(user == null) {
//			user = new User();
//			user.setIdentifier(identifier);
//			userRepository.save(user);
//		}
//		return user;
//	  }
	
//	@RequestMapping("/util/cleanPlaylists")
//	public void cleanPlaylists() {
//		List<Playlists> playlists = playlistsRepository.findAll();
//		int count = 0;
//		for (Playlists playlist : playlists) {
//			if(userRepository.findById(playlist.getOwnerId()) == null) {
//				System.out.println("adios!");
//				playlistsRepository.delete(playlist);
//				count++;
//			}
//		}
//		System.out.println("count: " + count);
//	}
	
	
//	@RequestMapping("/resource/test")
//	public @ResponseBody String test(Principal principal) {
//		return principal.getName();
//	  }
	
}
