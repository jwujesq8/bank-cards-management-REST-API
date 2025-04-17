package com.api.service;

import com.api.entity.User;
import com.api.repository.UserRepository;
import com.api.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Class UserServiceImpl
 *
 * Service implementation for managing users, including retrieving user details by email.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves a user by their email.
     *
     * @param login The email of the user to retrieve.
     */
    @Override
    public Optional<User> getUserByEmail(String login) {
        return userRepository.findByEmail(login);
    }
}
