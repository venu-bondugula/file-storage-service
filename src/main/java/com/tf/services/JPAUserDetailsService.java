package com.tf.services;

import com.tf.models.SecurityUser;
import com.tf.models.User;
import com.tf.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class JPAUserDetailsService implements UserDetailsService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username)
                .map(SecurityUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));
    }

    public User saveUser(String username, String password) {
        User user = new User(username, this.encoder.encode(password), "ROLE_USER, USER");
        return userRepo.save(user);
    }

    public Optional<User> findByUsername(final String username) {
        return userRepo.findByUsername(username);
    }

}