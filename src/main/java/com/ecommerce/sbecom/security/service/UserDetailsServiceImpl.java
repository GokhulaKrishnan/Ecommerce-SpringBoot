package com.ecommerce.sbecom.security.service;

import com.ecommerce.sbecom.model.User;
import com.ecommerce.sbecom.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.beans.Transient;

// Here we are going to customize the builtin UserDetailsService to fetch the user details from the db

//- We are going to tell spring that this is the way you need to fetch the user data which will be used for authentication
//and authorization in my application.
//- When someone logs in this is the first method called to fetch the user details.
//        - We implement the builtin UserDetailsService.
//- We override the existing method.
//- The goal of this class is to fetch the user details from the database with the help of username and this method will
//    return the user details in the form of UserDetails so that the Spring Security will understand.
//- We autowire the userRepository.
//        - Using the username we fetch the user name which will be in the User Entity.
//- But we need to return it in the form of UserDetails.
//        - Here comes the UserDetailsImpl to our rescue.
//- We had already created a build method which is used to convert the User into UserDetails, we make use of that.

@NoArgsConstructor
@Data
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    // We are making this class transactional because either it needs to complete or not complete.
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User name not found" + username));

        return UserDetailsImpl.build(user);
    }
}
