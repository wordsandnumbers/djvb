package com.vpo.djvoxbox.domain;

import java.util.Date;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Manager {

	@Id
	String id;
	@Indexed(unique=true)
	@NotEmpty
	private String name;
	private Date lastUpdate;
	private Date workLock;
	private Date usurping;
	private String error;
	private Boolean active;
	

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public Date getWorkLock() {
		return workLock;
	}
	public void setWorkLock(Date workLock) {
		this.workLock = workLock;
	}
	public Date getUsurping() {
		return usurping;
	}
	public void setUsurping(Date usurping) {
		this.usurping = usurping;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public Boolean getActive() {
		return active;
	}
	public void setActive(Boolean active) {
		this.active = active;
	}
	
}
