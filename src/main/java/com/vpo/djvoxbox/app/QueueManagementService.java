package com.vpo.djvoxbox.app;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
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
		if(queue != null && queue.getQueue() != null && queue.getQueue().getRoomCode() != null &&queue.getQueue().getRoomCode().equals(roomCode)) {
			return queue;
		}
		ConvenientQueue q = null;
		try {
			q = new ConvenientQueue(queueClient.getQueue(roomCode));
		} catch(HttpClientErrorException e) {
			if(e.getStatusCode().value() != HttpStatus.SC_UNAUTHORIZED) {
				throw e;
			}
		}
		return q;
	}
	
	public void manageQueues(){
		// all queues in the db
		List<UserQueue> uqs = getAllQueues();
		ConvenientQueue q = null;
		// loop over the queues
		for (UserQueue uq : uqs) {
			// ignore test queues
			if(!sameOrg(uq)) {
				continue;
			}
			// find the queue for the current room 
			// we only poll VB when the room code is different
			q = getQueue(uq.getRoomCode(), q);
			if(q == null || q.getQueue() == null) {
				// if the roomcode doesnt work anymore, mark uq for deletion or delete it
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
			for (int i = 0; i < uq.getQueued().size(); i++) {
				Play up = uq.getQueued().get(i);
	
				if(q.getPlayData().containsKey((up.getPlayId()))) {
					up.setEstimatedPlayTime(q.getPlayData().get(up.getPlayId()));
					playCount++;
				} else if(up.getPlayId() != null) {
					uq.getQueued().remove(i--);
				}
			}
			switch (uq.getMode()) {
			// put in a song as soon as there are at least X songs after you in the queue
			case "metered":
				if(playCount == 0 && uq.getQueue().size() != 0) {
					playNext(uq);
				} else if (playCount != 0 && uq.getQueue().size() != 0) {
					Play lastPlay = uq.getQueued().get(uq.getQueued().size()-1);
					if(lastPlay != null) {
						Integer location =  q.getQueue().getQueue().indexOf(lastPlay);
						if(location != null && uq.getQueueInterval() != null
								&& (q.getQueue().getQueue().size() - (location + 1) >= uq.getQueueInterval())) {
							playNext(uq);
						}
					}
					
				}
				break;
			case "manual":
				// we don't do anything in manual mode
				break;

			default:
				if(playCount == 0 && uq.getQueue().size() != 0) {
					playNext(uq);
				}
				break;
			}
			
			userQueueRepository.save(uq);
		}
		
	}
	
	public void playNext(UserQueue uq) {
		Play nextPlay = uq.getQueue().get(0);
		Play newPlay = null;
		try {
			newPlay = queueClient.addSong(new PlayRequest(uq.getRoomCode(), nextPlay.getId()), uq.getSession());
		} catch(HttpClientErrorException e) {
			if(e.getStatusCode().value() !=  HttpStatus.SC_NOT_FOUND) {
				// if it's not found, skip it?  
				// TODO: how to communicate better with the client
				throw e;
			}
		}
		if(newPlay != null) {
			nextPlay.setPlayId(newPlay.getPlayId());
			nextPlay.setPosition(newPlay.getPosition());
			nextPlay.setIndex(newPlay.getIndex());
			uq.getQueued().add(nextPlay);
		}
		uq.getQueue().remove(0);
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
			Long time = System.currentTimeMillis();
			if(queue != null && queue.getCurrentSong() != null) {
				
				this.playData.put(queue.getCurrentSong().getPlayId(), time);
				int playTime = 0;
				if(queue.getCurrentSong().getDuration() != null && queue.getCurrentSong().getPosition() != null) {
						playTime = queue.getCurrentSong().getDuration() - queue.getCurrentSong().getPosition();
				}
				time = makeTime(time, playTime);
			}
			if(queue != null && queue.getQueue() != null) {
				for (Play play : queue.getQueue()) {
					this.playData.put(play.getPlayId(), time);
					time = makeTime(time, play.getDuration());
				}
			}
		}

		private Long makeTime(Long time, Integer add ) {
			
			return time + 
					((add != null) ? add.longValue() : 240000L);
		}
		private Queue queue;
		private Map<String, Long> playData = new HashMap<String, Long>();
		
		public Queue getQueue() {
			return queue;
		}

		public void setQueue(Queue queue) {
			this.queue = queue;
		}

		public Map<String, Long> getPlayData() {
			return playData;
		}

		public void setPlayData(Map<String, Long> playData) {
			this.playData = playData;
		}
		
		
		
		
	}
	
	
	
}
