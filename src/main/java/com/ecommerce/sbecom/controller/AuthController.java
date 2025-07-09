package com.ecommerce.sbecom.controller;

import com.ecommerce.sbecom.model.AppRole;
import com.ecommerce.sbecom.model.Role;
import com.ecommerce.sbecom.model.User;
import com.ecommerce.sbecom.repositories.RoleRepository;
import com.ecommerce.sbecom.repositories.UserRepository;
import com.ecommerce.sbecom.security.jwt.JwtUtils;
import com.ecommerce.sbecom.security.request.LoginRequest;
import com.ecommerce.sbecom.security.request.SignupRequest;
import com.ecommerce.sbecom.security.response.LoginResponse;
import com.ecommerce.sbecom.security.response.MessageResponse;
import com.ecommerce.sbecom.security.service.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    // Autowiring Authentication Manager
    @Autowired
    AuthenticationManager authenticationManager;

    // Autowiring JwtUtils
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;


    // ***** SIGN IN *****

    // Here we are going to create the endpoint for the user signin
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {

        // A successful login will create an authentication object

        // Here we are declaring it
        Authentication authentication;

        // We use try and catch to throw error if the authentication is not successful
        try{

            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

        }catch (AuthenticationException exception){

            // Here we create a map of error and it status code
            Map<String, Object> map = new HashMap<>();

            map.put("message", exception.getMessage());
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }

        // After successful login, it will come here
        // Now we need to store the authentication object in the security context.
        // This place will be accessed everytime.
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Now we need to prepare the response to the user
        // Generating the token with the help of utils
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        // Now getting the roles from the authentication and converting it into list
        List<String> roles = authentication.getAuthorities().stream().map(
                authority -> authority.getAuthority()
        ).toList();

        // Getting the username and id
        String username = userDetails.getUsername();
        Long id = userDetails.getId();

        // Now creating the LoginResponse
        LoginResponse loginResponse = new LoginResponse(id, username, roles);

        // Here we are returning the cookie in the header to the client
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(loginResponse);

    }

    // ***** SIGN IN *****

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {

        // Performing validation
        if(userRepository.existsByUserName(signupRequest.getUserName())){
            // We need to throw an error
            ResponseEntity.badRequest().body(new MessageResponse("Error: Username already taken."));
        }

        if(userRepository.existsByEmail(signupRequest.getEmail())){
            // We need to throw an error
            ResponseEntity.badRequest().body(new MessageResponse("Error: Email already in use."));
        }

        // Now creating the User object
        User user = new User(
                signupRequest.getUserName(),
                // Here we should not have the password stored as string directly so we encode it
                passwordEncoder.encode(signupRequest.getPassword()),
                signupRequest.getEmail());

        // Now we need to validate and assign roles.

        // We get the roles as String
        Set<String> strRoles = signupRequest.getRoles();

        // To store it as a Set of Role
        Set<Role> roles = new HashSet<>();

        // First validating whether roles were given by the user else assigning default role
        if(strRoles == null){

            // We get the ROLE_USER from the db and assign it.
            // We cannot directly assign user role.
            // We need to fetch it from the db
            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).
                    orElseThrow(() -> new RuntimeException("Error: Role not found."));
            roles.add(userRole);
        }else{

            // if the roles had been given by the user, we assign the correct roles
            strRoles.forEach(role -> {

                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN).
                                orElseThrow(() -> new RuntimeException("Error: Role not found."));
                        roles.add(adminRole);
                        break;
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER).
                                orElseThrow(() -> new RuntimeException("Error: Role not found."));
//                        System.out.println(sellerRole);
                        roles.add(sellerRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER).
                                orElseThrow(() -> new RuntimeException("Error: Role not found."));
                        roles.add(userRole);
                        break;
                }
            });
        }

        // Now we are adding roles to the User entity
        user.setRoles(roles);
        // Saving the user in the repository.
        userRepository.save(user);

        // Now returning Ok to the user
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    // Endpoint to get the authenticated user name
    @GetMapping("/username")
    public String getUserName(Authentication authentication) {
        if(authentication != null){
            return authentication.getName();
        }else{
            return null;
        }
    }


    @GetMapping("/user")
    public ResponseEntity<?> getUserDetails(Authentication authentication){

        // This will give all the user details
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Now getting the roles from the authentication and converting it into list
        List<String> roles = authentication.getAuthorities().stream().map(
                authority -> authority.getAuthority()
        ).toList();

        // Getting the username and id
        String username = userDetails.getUsername();
        Long id = userDetails.getId();

        // Now creating the LoginResponse
        LoginResponse loginResponse = new LoginResponse(id, username, roles);

        // Here we are returning the cookie in the header to the client
        return ResponseEntity.ok().body(loginResponse);
    }

    // Endpoint to signout
    @PostMapping("/signout")
    public ResponseEntity<?> logout() {

        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You have been logged out successfully!"));
    }

}
