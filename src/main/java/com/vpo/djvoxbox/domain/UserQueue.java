package com.vpo.djvoxbox.domain;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Session;
import com.vpo.vbclient.model.Song;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document
public class UserQueue {
	
	public UserQueue() {
		super();
		this.queue = new ArrayList<Play>();
		
	}
	public UserQueue(String roomCode) {
		super();
		this.roomCode = roomCode;
		this.active = true;
		this.queue = new ArrayList<Play>();
		this.organization = null;
	}
	
	@Id
	private String id;
	@NotNull
	@Indexed
	private String ownerId;
	@Indexed
	private String roomCode;
	private List<Play> queue;
	private boolean active;
	private Session session;
	private String organization;
	
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
	public String getRoomCode() {
		return roomCode;
	}
	public void setRoomCode(String roomCode) {
		this.roomCode = roomCode;
	}
	public List<Play> getQueue() {
		return queue;
	}
	public void setQueue(List<Play> queue) {
		this.queue = queue;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public Session getSession() {
		return session;
	}
	public void setSession(Session session) {
		this.session = session;
	}
	public String getOrganization() {
		return organization;
	}
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	public Play addSongToQueue(Song song) {
		Play p = playFromSong(song);
		this.queue.add(p);
		return p;
		
	}
	private Play playFromSong(Song song) {
		Play play = new Play();
		play.setArtist(song.getArtist());
		play.setId(song.getId());
		play.setTitle(song.getTitle());
		return play;
	}


}
