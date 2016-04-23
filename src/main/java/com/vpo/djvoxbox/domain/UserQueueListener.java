package com.vpo.djvoxbox.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.mongodb.DBObject;

public class UserQueueListener extends AbstractMongoEventListener<UserQueue> {

	@Autowired
    SimpMessagingTemplate template;
	
	@Override
	public void onAfterSave(UserQueue uq, DBObject dbo) {
		//template.convertAndSendToUser(((Principal) principal).getName(), "/topic/queue", uq);
	    template.convertAndSend("/topic/queue/" + uq.getSession().getSession().toString(), uq);
	}

}
