package com.vpo.djvoxbox.domain;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AvatarRepository extends MongoRepository<Avatar, String> {

	Avatar findByOwnerId(String ownerId);

	Avatar findByShortcut(String shortcut);

}
