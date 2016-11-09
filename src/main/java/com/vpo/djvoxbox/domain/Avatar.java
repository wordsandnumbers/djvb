package com.vpo.djvoxbox.domain;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document
public class Avatar {

	@Id
	private String id;
	@NotNull
	@Indexed
	private String ownerId;
	public String getOwnerId() {
		return ownerId;
	}
	public byte[] image;
	public String imageType;

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}
	
	
}
