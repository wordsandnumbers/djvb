package com.vpo.djvoxbox.web;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vpo.djvoxbox.app.QueueManagementService;
import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserQueue;
import com.vpo.djvoxbox.domain.UserQueueRepository;
import com.vpo.djvoxbox.domain.UserRepository;
import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Queue;
import com.vpo.vbclient.model.Session;
import com.vpo.vbclient.model.Song;
import com.vpo.vbclient.queue.PlayRequest;
import com.vpo.vbclient.queue.QueueClient;
import com.vpo.vbclient.session.SessionClient;
import com.vpo.vbclient.song.SongClient;

@RestController
@RequestMapping("/api/v1/queue")
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
	@Autowired
	QueueManagementService queueManagementService;
	
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
	
	@RequestMapping(value="/queue", method=RequestMethod.POST)
	public @ResponseBody UserQueue addToQueue(@RequestBody PlayRequest request, Principal principal) {
		UserQueue uq = createUserQueue(new QueueRequest(request.getRoomCode()) , principal);
		Song song = songClient.getSongById(request.getSongId());
		uq.addSongToQueue(song);
		// if this is your first song, put it in now!
		if(uq.getQueued().size() == 0 && uq.getQueue().size() == 1) {
			queueManagementService.playNext(uq, 0);
		}
		userQueueRepository.save(uq);
		return uq;
	}
	
	@RequestMapping(value="/qeued", method=RequestMethod.PUT)
	public @ResponseBody UserQueue replaceQueued(@RequestBody PlayRequest request, Principal principal) {
		UserQueue uq = createUserQueue(new QueueRequest(request.getRoomCode()) , principal);
		Song song = songClient.getSongById(request.getSongId());		
		// if it's a new song, then just post the replacement and delete out of the queue
		List<Play> queued = uq.getQueued();
		Play newPlay = null;
		User user = userRepository.findById(principal.getName());
		Session s = sessionClient.getSessionById(user.getSessionId());
		for (Play play : queued) {
			if(play.getPlayId().equals(request.getTo())) {
				newPlay = queueClient.replace(request.getRoomCode(), play, request, s);
				uq.getQueued().remove(play);
			}
		}
		if(newPlay != null) {
			uq.getQueued().add(newPlay);
		}
		List<Play> queue = uq.getQueue();
		// if it's a current queue play, then remove it too
		if(newPlay != null) {
			for (Play play : queue) {
				if(play.getPlayId().equals(request.getFrom())) {
					uq.getQueue().remove(play);
				}
				
			}
		}
		userQueueRepository.save(uq);
		return uq;
	}
	
	@RequestMapping(value="/queue", method=RequestMethod.PUT)
	public @ResponseBody UserQueue reorderQueue(@RequestBody PlayRequest request, Principal principal) {
		UserQueue uq = createUserQueue(new QueueRequest(request.getRoomCode()) , principal);
		Play from = null;
		Integer toIndex = null;
		Integer fromIndex = null;
		try {
			fromIndex = Integer.parseInt(request.getFrom());
			from = uq.getQueue().get(fromIndex);
			toIndex = Integer.parseInt(request.getTo());
		} catch (Exception e) {
			// either it wasn't a number or it was out of bounds
		}
		if(from != null && from.getPlayId().equals(request.getPlayId()) && toIndex != null && uq.getQueue().size() > toIndex) {
			movePlay(uq, fromIndex, toIndex);
		} else {
			// throw an appropriate error
		}
		userQueueRepository.save(uq);
		return uq;
	}
	
	@RequestMapping(value="/queue", method=RequestMethod.DELETE)
	public @ResponseBody UserQueue removeFromQueue(@RequestBody PlayRequest request, Principal principal) {
		UserQueue uq = createUserQueue(new QueueRequest(request.getRoomCode()) , principal);
		Play from = null;
		Integer fromIndex = null;
		try {
			fromIndex = Integer.parseInt(request.getFrom());
			from = uq.getQueue().get(fromIndex);
		} catch (Exception e) {
			// either it wasn't a number or it was out of bounds
		}
		if(from != null && from.getPlayId().equals(request.getPlayId())) {
			uq.getQueue().remove(fromIndex);
		} else {
			// throw an appropriate error
		}
		userQueueRepository.save(uq);
		return uq;
	}
	
	
	
	private void  movePlay(UserQueue uq, Integer fromIndex, Integer toIndex) {
		if(fromIndex > toIndex) {
			Play play = uq.getQueue().get(fromIndex);
			uq.getQueue().remove(fromIndex);
			uq.getQueue().add(toIndex, play);
		} else {
			Play play = uq.getQueue().get(fromIndex);
			uq.getQueue().remove(fromIndex);
			if(uq.getQueue().size() == toIndex) {
				uq.getQueue().add(play);
			} else {
				uq.getQueue().add(toIndex, play);
			}
		}
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
