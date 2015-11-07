package com.vpo.djvoxbox.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
		
	}
	public UserQueue(String roomCode) {
		super();
		this.roomCode = roomCode;
		this.active = true;
		this.organization = null;
	}
	
	@Id
	private String id;
	@NotNull
	@Indexed
	private String ownerId;
	@Indexed
	private String roomCode;
	private List<Play> queued = new ArrayList<Play>();
	private List<Play> queue  = new ArrayList<Play>();
	private boolean active;
	private Session session;
	private String organization;
	private String mode;
	private Integer queueInterval;
	
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
	public List<Play> getQueued() {
		return queued;
	}
	public void setQueued(List<Play> queued) {
		this.queued = queued;
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
	
	// rules mode is "the rules"
	// manual mode will allow instant plays, queue manager will only manage activity and refresh the userqueued
	// metered mode will play every X songs as set in the queue
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public Integer getQueueInterval() {
		return queueInterval;
	}
	public void setQueueInterval(Integer queueInterval) {
		this.queueInterval = queueInterval;
	}
	public Play addSongToQueue(Song song) {
		Play p = playFromSong(song);
		this.queue.add(p);
		return p;
		
	}
	private Play playFromSong(Song song) {
		Play play = new Play();
		play.setPlayId(UUID.randomUUID().toString());
		play.setArtist(song.getArtist());
		play.setId(song.getId());
		play.setTitle(song.getTitle());
		return play;
	}


}
