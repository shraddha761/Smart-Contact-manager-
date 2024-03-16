package com.validate.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;


	@Service
	public class EmailService {
         
		@Autowired
		private JavaMailSender mailSender;
		
		public boolean sendEmail(String subject,String body,String to) {
			boolean b=false;
		   SimpleMailMessage message=new SimpleMailMessage();	
		   message.setFrom("smartcontactmanager27@gmail.com");
		   message.setTo(to);
		   message.setText(body);
		   message.setSubject(subject);
		   
		   mailSender.send(message);
		   
		   System.out.println("Mail Sent Successfully...");
		   b=true;
		   return b;
		   
		}
		
		
	}

