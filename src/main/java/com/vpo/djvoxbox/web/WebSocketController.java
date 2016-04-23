package com.vpo.djvoxbox.web;

import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    //@MessageMapping("/queue")
    //@SendToUser("/topic/queue")
    public String handle(String message) {
        return message;
    }
	
}
