package com.vpo.djvoxbox.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.vpo.djvoxbox.domain.User;

public interface UserRepository extends MongoRepository<User, String> {
	User findByEmail(String email);
	
	User findByIdentifier(String identifier);

	User findById(String id);

}
