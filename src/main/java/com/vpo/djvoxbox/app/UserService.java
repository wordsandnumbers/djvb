package com.vpo.djvoxbox.app;

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
			lists = playlistsRepository.findOne(user.getSavedListsId());
			if(lists == null) {
				lists = createNewSavedLists(user);
			}
		}
		return lists;
	}
	
	public Playlist getPlaylist(User user, String listName) {
		Playlists lists = getPlaylists(user);
		Playlist list = lists.getLists().get(listName);
		if(list == null) {
			list = new Playlist();
			lists.getLists().put(listName, list);
			playlistsRepository.save(lists);
		}
		return list;
	}
	
	public Playlist updatePlaylist(User user, String listName, Playlist list) {
		Playlists lists = getPlaylists(user);
		lists.getLists().put(listName, list);
		playlistsRepository.save(lists);
		return list;
	}

	public void removePlaylist(User user, String listName) {
		Playlists lists = getPlaylists(user);
		lists.getLists().remove(listName);
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
