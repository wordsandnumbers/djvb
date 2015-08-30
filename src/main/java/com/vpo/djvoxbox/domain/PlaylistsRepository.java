package com.vpo.djvoxbox.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlaylistsRepository extends MongoRepository<Playlists, String> {
	Playlists findByOwnerId(String ownerId);
}
