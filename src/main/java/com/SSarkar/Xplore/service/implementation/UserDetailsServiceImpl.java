package com.SSarkar.Xplore.service.implementation;


import com.SSarkar.Xplore.entity.User;
import com.SSarkar.Xplore.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j()
public class UserDetailsServiceImpl implements UserDetailsService {

    //private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        // 2. Use the UserRepository to find the user by username
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username/email: " + usernameOrEmail));

        log.info("User found: {}", user.getUsername());

        return user; // User implements UserDetails, so we can return it directly
    }
}
