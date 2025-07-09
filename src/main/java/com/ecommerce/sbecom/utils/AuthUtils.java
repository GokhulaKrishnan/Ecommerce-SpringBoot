package com.ecommerce.sbecom.utils;

import com.ecommerce.sbecom.model.User;
import com.ecommerce.sbecom.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtils {

    @Autowired
    UserRepository userRepository;

    // Method to get the logged-in user email
    public String loggedInEmail(){

        // Getting the current authentication details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Fetching the user from the database using the name
        User user = userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        return user.getEmail();
    }

    // Method to get the user ID
    public Long loggedInUserId(){

        // Getting the current authentication details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Fetching the user from the database using the name
        User user = userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        return user.getUserId();
    }


    // Method to get the user
    public User loggedInUser(){

        // Getting the current authentication details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Fetching the user from the database using the name
        User user = userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        return user;
    }
}
