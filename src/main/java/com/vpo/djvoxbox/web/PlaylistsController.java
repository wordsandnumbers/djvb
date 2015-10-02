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
	   User user = userRepository.findById(principal.getName());
	   return userService.getPlaylists(user);	 
	}

	@RequestMapping(value="/{name}", method=RequestMethod.GET)
	public @ResponseBody Playlist getPlaylist(Principal principal, @PathVariable("name") String name) {
	   User user = userRepository.findById(principal.getName());
	   return userService.getPlaylist(user, name);	 
	}

	@RequestMapping(value="/{name}", method=RequestMethod.PUT)
	public @ResponseBody Playlist updatePlaylist(Principal principal, @PathVariable("name") String name, @RequestBody Playlist list) {
	   User user = userRepository.findById(principal.getName());
	   return userService.updatePlaylist(user, name, list);	 
	}
}
