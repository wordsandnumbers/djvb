package com.vpo.djvoxbox.web;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.vpo.djvoxbox.app.UserService;
import com.vpo.djvoxbox.domain.Playlist;
import com.vpo.djvoxbox.domain.Playlists;
import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserRepository;

@RestController
@RequestMapping("/api/v1/playlists")
public class PlaylistsController {
	
	@Autowired UserRepository userRepository;
	@Autowired UserService userService;

	
	@RequestMapping(value="/", method=RequestMethod.GET)
	public @ResponseBody Playlists getPlaylists(Principal principal) {
	   User user = userRepository.getById(principal.getName());
	   return userService.getPlaylists(user);	 
	}

	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public @ResponseBody Playlist getPlaylist(Principal principal, @PathVariable("id") String id) {
	   User user = userRepository.getById(principal.getName());
	   return userService.getPlaylist(user, id);	 
	}
	
	@RequestMapping(value="/", method=RequestMethod.POST)
	public @ResponseBody Playlist addPlaylist(Principal principal, @RequestBody Playlist list) {
	   User user = userRepository.getById(principal.getName());
	   return userService.addPlaylist(user, list);	 
	}
	
	@RequestMapping(value="/", method=RequestMethod.DELETE)
	public void deletePlaylist(Principal principal, @RequestBody Playlist list) {
	   User user = userRepository.getById(principal.getName());
	   userService.removePlaylist(user, list);	 
	}

	@RequestMapping(value="/", method=RequestMethod.PUT)
	public @ResponseBody Playlist updatePlaylist(Principal principal, @RequestBody Playlist list) {
	   User user = userRepository.getById(principal.getName());
	   return userService.updatePlaylist(user, list);	 
	}
}
