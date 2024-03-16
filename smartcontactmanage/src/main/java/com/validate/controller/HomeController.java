package com.validate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.validate.dao.UserRepository;
import com.validate.entitie.User;
import com.validate.helper.Message;
import jakarta.servlet.http.HttpSession;

@Controller

public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";     	
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";     	
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Signup - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";     	
	}
	
	//handle register user
	
	@RequestMapping(value="/do_register",method=RequestMethod.POST)
	public String registerUser(@ModelAttribute("user") User user,@RequestParam(value="agreement",defaultValue = "false")boolean agreement,Model model,BindingResult result1, HttpSession session) {
		try {
		if(!agreement)
		{
			throw new Exception("You have not agreed the terms and conditons");
		}
		if(result1.hasErrors())
		{
			System.out.println("Error "+result1.toString());
			model.addAttribute("user",user);
			return "signup";
		}
		user.setRole("ROLE_USER");
		user.setEnable(true);
		user.setImageUrl("default.png");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		System.out.println("user "+ user);
		System.out.println("agreement "+ agreement);
		User result=this.userRepository.save(user);
		model.addAttribute("user",result);
		model.addAttribute("user",new User());
		session.setAttribute("message",new Message("Successfully Registered !!","alert-success"));
		return "signup";
		}catch(Exception e)
		{
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("Something Went Wrong!!"+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
	}
	
	@RequestMapping("/signin")
	public String customLogin(Model model)
	{
		model.addAttribute("title","Login Page");
		return "login";
	}
}
