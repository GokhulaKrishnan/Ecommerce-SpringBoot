package com.ecommerce.sbecom.security.jwt;

import com.ecommerce.sbecom.security.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // To Log
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    // Variable for expiration in ms
    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    // Secret Key from Application.properties
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.app.jwtCookieName}")
    private String cookieName;

    // Method to extract token from the header
//    public String getJwtFromHeader(HttpServletRequest request){
//
//        // Getting the Authorization Header
//        String bearerToken = request.getHeader("Authorization");
//
//        logger.debug("Authorization Header: {}", bearerToken);
//
//        // Now if the header is not null and the header starts with Bearer then return the token
//        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
//            // Remove Bearer substring
//            return bearerToken.substring(7);
//        }
//
//        return null;
//    }


    // Method to Generate token from the username
    public String generateTokenFromUsername(String username) {

        // Extract the username from the user details
        // No need because of using cookies
        // String username = userDetails.getUsername();

        // Build the token using jwts
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + jwtExpirationMs)))
                .signWith(key())
                .compact();
    }

    // Here we are going to use Cookies
    public String getTokenFromCookies(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request, cookieName);

        // Check whether cookie exists
        if(cookie != null){
            return cookie.getValue();
        }else{
            return null;
        }
    }


    // Here we are going to get the cookie from the logged in user
    public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {

        // Passing the logged-in username to get the jwt token
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());

        // Creating a response cookie
        ResponseCookie cookie = ResponseCookie.from(cookieName, jwt).path("/api")
                .maxAge(24 * 60 * 60)
                .httpOnly(false)
                .build();

        return cookie;
    }

    // Method to extract username from the token
    public String getUserNameFromJWTToken(String token) {

        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Method to generate signing key
    public Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Method to validate the token
    public Boolean validateJwtToken(String authToken){

        try {

            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;

        } catch (MalformedJwtException e){
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e){
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e){
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e){
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    // Method to signout from the current session
    public ResponseCookie getCleanJwtCookie() {

        // Creating a response cookie
        ResponseCookie cookie = ResponseCookie.from(cookieName, null).path("/api").build();

        return cookie;
    }
}
