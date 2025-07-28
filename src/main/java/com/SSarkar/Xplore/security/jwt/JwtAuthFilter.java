package com.SSarkar.Xplore.security.jwt;


import com.SSarkar.Xplore.service.implementation.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        log.debug("Processing JWT authentication filter for URI: {}", request.getRequestURI());

        try{
            // 1. Get JWT from the request header
            final String jwt = jwtUtils.getJwtFromHeader(request);

            if (jwt == null || !jwtUtils.validateToken(jwt)) {
                // If no token or invalid token, pass the request to the next filter
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Extract username and validate token
            final String username = jwtUtils.extractUsernameFromJwt(jwt);

            // Check if username is valid & if the user is not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 3. If token is valid, update the Security Context
                if (jwtUtils.validateToken(jwt)) { // We simplified validateToken in previous steps
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // Credentials are null for JWT auth
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // Set the authentication in the security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Roles of the user : {}",userDetails.getAuthorities());
                }
            }
        } catch (RuntimeException e) {
            // By catching the exception here, we prevent the app from crashing.
            // Spring Security will see that the SecurityContext is not populated
            // and will trigger the AuthenticationEntryPoint we configured.
            logger.error("Failed to process JWT and set user authentication: {}",e);
        }
        // 4. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}