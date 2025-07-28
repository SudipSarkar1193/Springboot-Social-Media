package com.SSarkar.Xplore.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;


@Component
public class JwtUtils {

    // 1. Initialize a logger for this class
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationInMs;

    /**
     * Extracts the JWT from the Authorization header.
     */
    public String getJwtFromHeader(HttpServletRequest request) {
        final String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            logger.debug("JWT found in Authorization header.");
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        logger.warn("JWT not found or is not a Bearer token in Authorization header.");

        return null; // Return null if no valid JWT is found

    }

    /**
     * Generates a JWT for a given username.
     */
    public String generateTokenFromUsername(UserDetails userDetails) {
        String username = userDetails.getUsername();
        logger.debug("Generating JWT for user: {}", username);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        logger.info("Successfully generated JWT for user: {}", username);
        return token;
    }

    /**
     * Extracts the username (subject) from the JWT.
     */
    public String extractUsernameFromJwt(String token) {
        logger.debug("Attempting to extract username from token.");
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Generates the signing key from the base64 encoded secret.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validates the JWT. Checks signature, expiration, and format.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith((SecretKey) getSigningKey()).build().parseSignedClaims(token);
            logger.debug("Token validation successful.");
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

}