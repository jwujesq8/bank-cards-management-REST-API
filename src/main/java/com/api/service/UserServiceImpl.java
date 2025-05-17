package com.api.service;

import com.api.dto.IdDto;
import com.api.dto.UserDto;
import com.api.dto.UserDtoNoId;
import com.api.entity.User;
import com.api.exception.BadRequestException;
import com.api.repository.UserRepository;
import com.api.service.interfaces.UserService;
import com.api.service.validation.UserValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final UserValidator userValidator;
    private final ModelMapper modelMapper;

    /**
     * Retrieves a user by their email.
     *
     * @param login The email of the user to retrieve.
     */
    @Override
    public Optional<User> getUserByEmail(String login) {
        return userRepository.findByEmail(login);
    }

    /**
     * Get a user by id.
     *
     * @param idDto
     */
    @Override
    public UserDto getUserById(IdDto idDto) {
        return modelMapper.map(userValidator.getUserByIdOrThrowBadRequest(idDto.getId()),
                UserDto.class);
    }

    /**
     * Update user that is already exists in the database.
     *
     * @param userDto
     */
    @Override
    public UserDto updateUser(UserDto userDto) {
        User user = modelMapper.map(userDto, User.class);
        return modelMapper.map(userRepository.save(user), UserDto.class);
    }

    /**
     * Add a new user.
     *
     * @param userDtoNoId
     */
    @Override
    public UserDto addUser(UserDtoNoId userDtoNoId) {
        User user = modelMapper.map(userDtoNoId, User.class);
        return modelMapper.map(userRepository.save(user), UserDto.class);
    }

    /**
     * Delete user by id.
     *
     * @param idDto
     */
    @Override
    public void deleteUser(IdDto idDto) {
        userRepository.deleteById(idDto.getId());
    }

    /**
     * Retrieves a paginated list of all users.
     *
     * @param pageable The pagination information.
     */
    @Override
    public Page<UserDto> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(
                user -> modelMapper.map(user, UserDto.class));
    }
}
