package com.validate.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.validate.dao.UserRepository;
import com.validate.entitie.User;

public class UserDetailServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user=userRepository.getUserByUserName(username);
		if(user==null) {
			throw new UsernameNotFoundException("Could not found user !!");
		}
		CustomUserDeatils customUserDetails=new CustomUserDeatils(user);
		return customUserDetails;
	}
                  
}
