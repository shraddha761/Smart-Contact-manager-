package com.validate.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.validate.dao.UserRepository;
import com.validate.entitie.User;
import com.validate.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

	Random random = new Random(1000);
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {

		int otp = random.nextInt(10000);
		String subject = "OTP from SCM";
		String message = " OTP = " + otp;
		String to = email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if (flag) {
			session.setAttribute("myotp",otp);
			session.setAttribute("email",email);
			return "verify";   
			
		} else {
			session.setAttribute("message", "Check your email id !!");
			return "forgot_email_form";
		}
	}
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session)
	{
	  int myotp=(int)session.getAttribute("myotp");
	  String email=(String)session.getAttribute("email");
	  if(myotp==otp)
	  {
		  User user=this.userRepository.getUserByUserName(email);
		  if(user==null)
		  {
			  //send error message
			  session.setAttribute("message","User does not exist with this email");
			  return "forgot_email_form";
		  }
		  else {
			  //send change password form
			  	System.out.println("HI i reached");  
		  }
		  return "password_change_form";
		  
		 
	  }
	  else {
		  session.setAttribute("message","You have entered Wrong Otp !!");
		  return "verify";
	  }
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("password") String password,HttpSession session)
	{
		String email=(String)session.getAttribute("email");
		User user=this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(password));
		userRepository.save(user);
	
		return "redirect:/signin?change=password changed successfully...";
	}
}
