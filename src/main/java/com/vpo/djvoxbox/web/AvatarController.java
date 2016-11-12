package com.vpo.djvoxbox.web;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.vpo.djvoxbox.domain.Avatar;
import com.vpo.djvoxbox.domain.AvatarRepository;
import com.vpo.djvoxbox.domain.User;
import com.vpo.djvoxbox.domain.UserRepository;

@Controller
public class AvatarController {

	@Autowired AvatarRepository avatarRepository;
	@Autowired UserRepository userRepository;
	
    @RequestMapping(value="/api/v1/user/avatar", method=RequestMethod.POST)
    @ResponseBody
    public User uploadAvatar(@RequestParam("file") MultipartFile file, Principal principal) throws IOException {
		User user = userRepository.findById(principal.getName());
		Avatar avatar = avatarRepository.findByOwnerId(principal.getName());
		
		if (avatar == null) {
			avatar = new Avatar();	
		}
    	avatar.setOwnerId(principal.getName());
    	avatar.image = file.getBytes();
    	avatar.imageType = file.getContentType();
    	avatarRepository.save(avatar);
    	
    	// We use this to for browser image cache reasons. Really just need a unique-ish number.
    	user.setAvatarShortcut(String.valueOf(System.currentTimeMillis()));
    	userRepository.save(user);

    	return user;
    }

    @RequestMapping(value="/api/v1/user/avatar/{id}", method=RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> downloadAvatar(@PathVariable("id") String id, Principal principal) {
		Avatar avatar = avatarRepository.findByOwnerId(principal.getName());
		final HttpHeaders headers = new HttpHeaders();

		if (avatar != null) {
			headers.setContentType(MediaType.parseMediaType(avatar.getImageType()));
			headers.setExpires(DateUtils.addDays(new Date(), 30).getTime());
	    	return new ResponseEntity<byte[]>(avatar.getImage(), headers, HttpStatus.OK);
		} else {
			return null;
		}
    }

}
