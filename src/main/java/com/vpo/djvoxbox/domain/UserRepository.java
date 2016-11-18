package com.vpo.djvoxbox.domain;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
	List<User> findByEmail(String email);
	
	User findByIdentifier(String identifier);

	User findById(String id);

}
