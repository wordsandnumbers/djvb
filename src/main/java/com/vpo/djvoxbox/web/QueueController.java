package com.vpo.djvoxbox.web;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserQueue;
import com.vpo.djvoxbox.domain.UserQueueRepository;
import com.vpo.djvoxbox.domain.UserRepository;
import com.vpo.vbclient.model.Queue;
import com.vpo.vbclient.model.Session;
import com.vpo.vbclient.model.Song;
import com.vpo.vbclient.queue.PlayRequest;
import com.vpo.vbclient.queue.QueueClient;
import com.vpo.vbclient.session.SessionClient;
import com.vpo.vbclient.song.SongClient;

@RestController
@RequestMapping("/queue")
public class QueueController {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	QueueClient queueClient;
	@Autowired
	SongClient songClient;
	@Autowired
	SessionClient sessionClient;
	@Value("${vb.organization}")
	private String vbOrganization;
	@Autowired
	UserQueueRepository userQueueRepository;
	
	@RequestMapping(value="/join", method=RequestMethod.POST)
	public @ResponseBody UserQueue createUserQueue(@RequestBody QueueRequest request, Principal principal) {
		User user = userRepository.findById(principal.getName());
		Queue q = queueClient.getQueue(request.roomCode);
		UserQueue uq = null;
		
		if(q != null) {
			uq = userQueueRepository.findByOwnerIdAndRoomCode(user.getIdentifier(), request.getRoomCode());
			if (uq == null) {
				uq = new UserQueue(request.getRoomCode());
				uq.setOrganization(vbOrganization);
				uq.setOwnerId(user.getIdentifier());
				Session s = sessionClient.getSessionById(user.getSessionId());
				uq.setSession(s);
				userQueueRepository.save(uq);
			}
		}
		return uq;
	}
	
	@RequestMapping(value="/add", method=RequestMethod.PUT)
	public @ResponseBody UserQueue addToQueue(@RequestBody PlayRequest request, Principal principal) {
		//Queue q = queueClient.getQueue(request.getRoomCode());
		UserQueue uq = createUserQueue(new QueueRequest(request.getRoomCode()) , principal);
		Song song = songClient.getSongById(request.getSongId());
		uq.addSongToQueue(song);
		userQueueRepository.save(uq);
		return uq;
	}
		
	@JsonIgnoreProperties(ignoreUnknown = true)
	private static class QueueRequest{
		
		public QueueRequest(String roomCode) {
			super();
			this.roomCode = roomCode;
		}

		public QueueRequest() {
			super();
		}

		private String roomCode;

		public String getRoomCode() {
			return roomCode;
		}

		public void setRoomCode(String roomCode) {
			this.roomCode = roomCode;
		}
		
	}
	
}
