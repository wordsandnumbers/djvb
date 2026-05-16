package com.vpo.djvoxbox.app;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.vpo.djvoxbox.domain.Playlist;
import com.vpo.djvoxbox.domain.Playlists;
import com.vpo.djvoxbox.domain.PlaylistsRepository;
import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserRepository;



@Service
@Component
public class UserService {
	
	@Autowired
	PlaylistsRepository playlistsRepository;
	@Autowired
	UserRepository userRepository;

	
	public Playlists getPlaylists(User user) {
		Playlists lists = null;
		if(user.getSavedListsId() == null) {
			lists = createNewSavedLists(user);
		} else {
			lists = playlistsRepository.findById(user.getSavedListsId()).orElse(null);
			if(lists == null) {
				lists = createNewSavedLists(user);
			}
		}
		return lists;
	}
	
	public Playlist getPlaylist(User user, String id) {
		Playlists lists = getPlaylists(user);
		for (Playlist l : lists.getLists()) {
			if(l.getId().equals(id)) {
				return l;
			}
		}
		return null;
	}
	
	public Playlist addPlaylist(User user, Playlist list) {
		Playlists lists = getPlaylists(user);
		list.setId(UUID.randomUUID().toString());
		lists.getLists().add(list);
		playlistsRepository.save(lists);
		return list;
	}
	
	
	public Playlist updatePlaylist(User user, Playlist list) {
		Playlists lists = getPlaylists(user);
		lists.getLists().remove(list);
		lists.getLists().add(list);
		playlistsRepository.save(lists);
		return list;
	}

	public void removePlaylist(User user, Playlist list) {
		Playlists lists = getPlaylists(user);
		lists.getLists().remove(list);
		playlistsRepository.save(lists);
	}

	protected Playlists createNewSavedLists(User user) {
		Playlists lists = new Playlists(user.getId());
		playlistsRepository.save(lists);
		user.setSavedListsId(lists.getId());
		userRepository.save(user);
		return lists;
	}
}
