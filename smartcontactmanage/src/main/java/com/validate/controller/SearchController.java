package com.validate.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.validate.dao.ContactRepository;
import com.validate.dao.UserRepository;
import com.validate.entitie.Contact;
import com.validate.entitie.User;

@RestController
public class SearchController {
        
	//search handler
	@Autowired
	private UserRepository userRepositary;
	@Autowired
	private ContactRepository contactRepository;
	
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query ,Principal principal)
	{
		System.out.println(query);
		User user =this.userRepositary.getUserByUserName(principal.getName());
		List<Contact> contacts=this.contactRepository.findByNameContainingAndUser(query, user);
	    return ResponseEntity.ok(contacts);	
	}
	 
}
