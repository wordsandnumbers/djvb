package com.vpo.djvoxbox.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vpo.vbclient.model.Song;

@JsonIgnoreProperties
public class Playlist {
	private String id;
	private List<Song> songs;

	public Playlist() {
		super();
		this.setId(UUID.randomUUID().toString());
		this.setSongs(new ArrayList<Song>());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Song> getSongs() {
		return songs;
	}

	public void setSongs(List<Song> songs) {
		this.songs = songs;
	}
}