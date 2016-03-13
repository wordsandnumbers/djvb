package com.vpo.djvoxbox.web;


import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserRepository;
import com.vpo.djvoxbox.util.SessionUtils;
import com.vpo.vbclient.song.Search;
import com.vpo.vbclient.song.SongClient;

@RestController
@RequestMapping("/api/v1/songs")
public class SearchController {
	
	@Autowired
	SongClient songClient;
	
	@Autowired
	UserRepository userRepository;
	
	@Value("${default.language}")
	private String defaultLanguage;

	@RequestMapping("/query")
	public @ResponseBody Search findSongs(@RequestParam Map<String,String> params, Principal principal) {
		User user = userRepository.findById(principal.getName());
		Search search = createSearch(params);
		search.setSession(SessionUtils.makeSession(user));
		return songClient.findSongs(search);
		
	}

	@RequestMapping("/browse")
	public @ResponseBody Search browseSongs(@RequestParam Map<String,String> params, Principal principal) {
		User user = userRepository.findById(principal.getName());
		Search search = createSearch(params);
		search.setBrowse(true);
		search.setSession(SessionUtils.makeSession(user));
		return songClient.findSongs(search);
		
	}

	
	@RequestMapping("/favorites")
	public @ResponseBody Search getFavorites(@RequestParam Map<String,String> params, Principal principal) {
		User user = userRepository.findById(principal.getName());
		Search search = createSearch(params);
		search.setSession(SessionUtils.makeSession(user));
		search.setFavorites(true);
		return songClient.findSongs(search);
		
	}
	
	@RequestMapping("/playHistory")
	public @ResponseBody Search getHistory(@RequestParam Map<String,String> params, Principal principal) {
		User user = userRepository.findById(principal.getName());
		Search search = createSearch(params);
		search.setSession(SessionUtils.makeSession(user));
		search.setPlayHistory(true);
		return songClient.findSongs(search);
		
	}

	

	private Search createSearch(Map<String, String> params) {
		Search search = new Search();
		search.setQuery(params.get("query"));
		search.setLanguage((params.get("language") != null) ? params.get("language") : defaultLanguage);
		search.setPage((params.get("page") == null) ? null : Integer.valueOf(params.get("page")));
		search.setPerPage((params.get("per_page") == null) ? null : Integer.valueOf(params.get("per_page")));
		search.setTag(params.get("tag"));
		search.setBy(params.get("by"));
		return search;
	}
	
}
