package com.vpo.djvoxbox.domain;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserQueueRepository extends MongoRepository<UserQueue, String> {
	List<UserQueue> findByOwnerId(String ownerId);
	UserQueue findByOwnerIdAndRoomCode(String ownerId, String roomCode);
	List<UserQueue> readAllOrderByRoomCode();
	List<UserQueue> findByRoomCode(String roomCode);
}
