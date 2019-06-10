package com.example.alipay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class IndexController {
	
	@RequestMapping("/alipay")
	public String fileTest(HttpServletRequest request, HttpSession session) {
		return "redirect:alipay.html";
	}
}