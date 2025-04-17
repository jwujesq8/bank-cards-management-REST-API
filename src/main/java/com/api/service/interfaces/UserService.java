package com.api.service.interfaces;

import com.api.dto.IdDto;
import com.api.dto.UserDto;
import com.api.dto.UserDtoNoId;
import com.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * Retrieves a user by their ID.
     *
     * @param idDto DTO containing the ID of the user.
     */
    UserDto getUserById(IdDto idDto);

    /**
     * Updates the details of an existing user.
     *
     * @param userDto DTO containing the updated user data.
     */
    UserDto updateUser(UserDto userDto);

    /**
     * Adds a new user to the system.
     *
     * @param userDtoNoId DTO containing the data of the new user (without ID).
     */
    UserDto addUser(UserDtoNoId userDtoNoId);

    /**
     * Deletes a user by their ID.
     *
     * @param idDto DTO containing the ID of the user to delete.
     */
    void deleteUser(IdDto idDto);

    /**
     * Retrieves a paginated list of all users.
     *
     * @param pageable The pagination information.
     */
    Page<UserDto> findAll(Pageable pageable);
}
