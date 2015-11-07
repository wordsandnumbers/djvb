package com.vpo.djvoxbox.domain;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document
public class User {

	@Id
	private String id;
	@Indexed(unique=true)
	@NotEmpty
	private String identifier;
	@Email
	private String email;
	private String phoneNumber;
	private String role;
	private String name;
	private String screenName;
	private String savedListsId;
	private String sessionId;
	private String color;
	
	
	public User() {
		super();
	}
	
	public User(String identifier, String phoneNumber, String email) {
		super();
		this.identifier = identifier;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.role = "USER";
	}


	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}


	public String getPhoneNumber() {
		return phoneNumber;
	}


	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}


	public String getRole() {
		return role;
	}


	public void setRole(String role) {
		this.role = role;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public String getSavedListsId() {
		return savedListsId;
	}

	public void setSavedListsId(String savedListsId) {
		this.savedListsId = savedListsId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
	
}
