package com.vpo.djvoxbox.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class UserQueueListener extends AbstractMongoEventListener<UserQueue> {

	@Autowired
    SimpMessagingTemplate template;
	
	@Override
	public void onAfterConvert(AfterConvertEvent<UserQueue> event) {
	    template.convertAndSend("/topic/queue/" + event.getSource().getSession().getSession().toString(), event.getSource());
	}

	@Override
	public void onAfterSave(AfterSaveEvent<UserQueue> event) {
	    template.convertAndSend("/topic/queue/update/" + event.getSource().getId(), event.getSource());
	}

	@Override
	public void onAfterDelete(AfterDeleteEvent<UserQueue> event) {
	    template.convertAndSend("/topic/queue/delete/" + event.getDBObject().get("id"), event.getDBObject());
	}

}
