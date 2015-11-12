package com.vpo.djvoxbox.security.web.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.RememberMeServices;

public class SpringSessionRememberMeServices implements RememberMeServices {

	@Override
	public Authentication autoLogin(HttpServletRequest request,
			HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loginFail(HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession().invalidate();
		
	}

	@Override
	public void loginSuccess(HttpServletRequest request,
			HttpServletResponse response,
			Authentication successfulAuthentication) {
        request.getSession().setMaxInactiveInterval(31557600); // one year

		
	}

}
