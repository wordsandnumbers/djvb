package com.vpo.djvoxbox.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	@RequestMapping("/")
	public String redirect() {
		return "redirect:/resources/index.html";
	}
}
