package com.vpo.djvoxbox.app;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.vpo.djvoxbox.domain.UserQueue;
import com.vpo.djvoxbox.domain.UserQueueRepository;
import com.vpo.vbclient.model.Play;
import com.vpo.vbclient.model.Queue;
import com.vpo.vbclient.queue.PlayRequest;
import com.vpo.vbclient.queue.QueueClient;

@Service
public class QueueManagementService {
	
	@Autowired
	UserQueueRepository userQueueRepository;
	
	@Autowired
	QueueClient queueClient;

	@Value("${vb.organization}")
	private String vbOrganization;
	
	// get all the UserQueues
	private List<UserQueue> getAllQueues() {
		return userQueueRepository.findAll(new Sort(Sort.Direction.ASC, "roomCode"));
	}
	// look up the room queue on vb
	// TODO: move error handling into the client
	private ConvenientQueue getQueue(final String roomCode, final ConvenientQueue queue) {
		if(queue != null && queue.getQueue() != null && queue.getQueue().getRoomCode().equals(roomCode)) {
			return queue;
		}
		ConvenientQueue q = null;
		try {
			q = new ConvenientQueue(queueClient.getQueue(roomCode));
		} catch(HttpClientErrorException e) {
			if(!e.getStatusCode().equals(HttpStatus.SC_FORBIDDEN)) {
				throw e;
			}
		}
		return q;
	}
	
	public void manageQueues(){
		List<UserQueue> uqs = getAllQueues();
		ConvenientQueue q = null;
		for (UserQueue uq : uqs) {
			if(!sameOrg(uq)) {
				continue;
			}
			q = getQueue(uq.getRoomCode(), q);
			if(q.getQueue() == null) {
				downgradeQueueStatus(uq);
				continue;
			} else if (!uq.isActive()) {
				// if the queue is valid and userqueue is inactive, mark active
				uq.setActive(true);
			}
			// TODO: if queue is valid, lock room_code
			// determine if any of current user plays are in playlist
			// if there are no plays, add top song.
			int playCount = 0;
			Integer next = null;
			for (int i = 0; i < uq.getQueued().size(); i++) {
				Play up = uq.getQueued().get(i);
				if(next == null && up.getPlayId() == null) {
					next = i;
				}
				if(q.getPlayIds().contains(up.getPlayId())) {
					playCount++;
				} else if(up.getPlayId() != null) {
					uq.getQueued().remove(i--);
				}
			}
			if(playCount == 0 && uq.getQueue().size() != 0) {
				playNext(uq, next);

			} else if (playCount != 0 && uq.getQueue().size() != 0) {
				// TODO: look up if we want to add 1 song per X songs and do it...
			}
			userQueueRepository.save(uq);
		}
		
	}
	
	public void playNext(UserQueue uq, Integer next) {
		Play nextPlay = uq.getQueue().get(next);
		Play newPlay = queueClient.addSong(new PlayRequest(uq.getRoomCode(), nextPlay.getId()), uq.getSession());
		nextPlay.setPlayId(newPlay.getPlayId());
		nextPlay.setPosition(newPlay.getPosition());
		nextPlay.setIndex(newPlay.getIndex());
		uq.getQueued().add(nextPlay);
		uq.getQueue().remove(next);
	}
	
	private boolean sameOrg(UserQueue uq) {
		if(uq.getOrganization() == null && vbOrganization == null) 
			return true;
		if(uq.getOrganization() != null && uq.getOrganization().equals(vbOrganization))
			return true;
		return false;
	}
	// TODO: if next user is in different room, unlock room
	// repeat
	
	// if 401, mark inactive OR delete if inactive
	private void downgradeQueueStatus(UserQueue uq) {
		if(uq.isActive()) {
			uq.setActive(false);
			userQueueRepository.save(uq);
		} else {
			userQueueRepository.delete(uq);
		}
	}
	
	private static class ConvenientQueue {
		
		public ConvenientQueue(Queue queue) {
			super();
			this.queue = queue;
			if(queue != null) {
				extractPlayIds();
			}
		}
		
		private void extractPlayIds() {
			if(queue != null && queue.getCurrentSong() != null) {
				this.playIds.add(queue.getCurrentSong().getPlayId());
			}
			if(queue != null && queue.getQueue() != null) {
				for (Play play : queue.getQueue()) {
					this.playIds.add(play.getPlayId());
				}
			}
		}

		private Queue queue;
		private List<String> playIds = new ArrayList<String>();
		
		public Queue getQueue() {
			return queue;
		}

		public void setQueue(Queue queue) {
			this.queue = queue;
		}

		public List<String> getPlayIds() {
			return playIds;
		}

		public void setPlayIds(List<String> playIds) {
			this.playIds = playIds;
		}
		
		
		
		
	}
	
	
	
}
