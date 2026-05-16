package com.vpo.djvoxbox.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
@Document
public class Playlists {

	public Playlists() {
		super();
	}

	public Playlists(String ownerId) {
		super();
		this.ownerId = ownerId;
		this.lists = new HashSet<Playlist>();
		// make default lists
		this.lists.add(new Playlist("Faves"));
	}

	@Id
	private String id;
	@NotNull
	@Indexed(unique=true)
	private String ownerId;
	private Set<Playlist> lists;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public Set<Playlist> getLists() {
		return lists;
	}

	public void setLists(Set<Playlist> lists) {
		this.lists = lists;
	}
	
}
