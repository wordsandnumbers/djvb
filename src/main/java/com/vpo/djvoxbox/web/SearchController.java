package com.vpo.djvoxbox.web;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.vpo.vbclient.model.Session;
import com.vpo.vbclient.song.Search;
import com.vpo.vbclient.song.SongClient;

@RestController
@RequestMapping("/songs")
public class SearchController {
	
	@Autowired
	SongClient songClient;
	

	@RequestMapping("/query")
	public @ResponseBody Search findSongs(@RequestParam Map<String,String> params) {
		Search search = createSearch(params);
		return songClient.findSongs(search);
		
	}


	private Search createSearch(Map<String, String> params) {
		Search search = new Search();
		search.setQuery(params.get("query"));
		search.setLanguage(params.get("language"));
		search.setPage(Integer.getInteger(params.get("page")));
		search.setPerPage(Integer.getInteger(params.get("per_page")));
		search.setTag(params.get("tag"));
		return search;
	}
	
}
