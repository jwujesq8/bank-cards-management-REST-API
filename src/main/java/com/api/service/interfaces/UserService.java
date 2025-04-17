package com.api.service.interfaces;

import com.api.entity.User;
import java.util.Optional;

/**
 * Class UserService
 *
 * Service interface for managing user-related operations.
 * Provides methods for retrieving user details.
 */
public interface UserService {

    /**
     * Retrieves a user by their email (login).
     *
     * @param login The email (login) of the user to be retrieved.
     */
    Optional<User> getUserByEmail(String login);
}
