package com.vpo.djvoxbox.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitsResponse {
	@JsonProperty("phone_number")
	private String phoneNumber;
	@JsonProperty("id_str")
	private String id;
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "DigitsResponse [phoneNumber=" + phoneNumber + ", id=" + id
				+ "]";
	}
	
}
