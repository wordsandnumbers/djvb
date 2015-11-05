package com.vpo.djvoxbox.util;


import com.vpo.djvoxbox.domain.User;
import com.vpo.vbclient.model.Session;

public class SessionUtils {

	public static Session makeSession(User user) {
		Session s = new Session();
		if(user.getSessionId() != null) {
			s.setSession(user.getSessionId());
		}
		s.setHideHandle(false);
		return s;
	}
}