package com.vpo.djvoxbox.domain;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ManagerRepository extends MongoRepository<Manager, String>, ManagerRepositoryCustom {
	
	Manager findByName(String name);
	
	List<Manager> findAll();
	
}
