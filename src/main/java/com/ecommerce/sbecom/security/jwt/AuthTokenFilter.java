package com.ecommerce.sbecom.security.jwt;

import com.ecommerce.sbecom.security.service.UserDetailsImpl;
import com.ecommerce.sbecom.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
public class AuthTokenFilter extends OncePerRequestFilter {

    // Logger to log
    public static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    // To make use of jwtUtil class
    @Autowired
    private JwtUtils jwtUtils;

    // To store the UserDetails
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            logger.debug("AuthTokenFilter called for URI: {}",  request.getRequestURI());

            // Getting the token from the header
            String jwt = parseJwt(request);

            // Checking whether the token is valid
            if(jwt != null && jwtUtils.validateJwtToken(jwt)){

                // Get the username from the token
                String userName = jwtUtils.getUserNameFromJWTToken(jwt);

                // Get the particular userDetails using the userName
                UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(userName);

                // Create the authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                // Adding request details to the Authentication object
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Now adding this authentication object to the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

            }

        }catch(Exception e) {
            logger.debug("Roles from JWT: {}", e.getMessage());
        }

        // Now telling the spring security to move on to other filters
        filterChain.doFilter(request, response);
    }

    // Helper method to extract token from the header using Util
    public String parseJwt(HttpServletRequest request) {

        // We use util to extract the header
        String jwt = jwtUtils.getTokenFromCookies(request);
        logger.debug("AuthTokenFilter called for JWT: {}", jwt);
        return jwt;
    }
}
