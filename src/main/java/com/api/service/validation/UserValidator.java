package com.api.service.validation;

import com.api.entity.User;
import com.api.exception.BadRequestException;
import com.api.exception.ForbiddenException;
import com.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public User getUserByIdOrThrowBadRequest(UUID id){
        return userRepository.findById(id).orElseThrow(() ->
                new BadRequestException("There is no such user"));
    }

    public User getUserByEmailOrThrowBadRequest(String email){
        return userRepository.findByEmail(email).
                orElseThrow(() -> new BadRequestException("User not found"));
    }
    public User getUserByEmailOrThrowForbidden(String email){
        return userRepository.findByEmail(email).
                orElseThrow(() -> new ForbiddenException("User not found"));
    }

    public boolean doesUserPasswordEqualTo(User user, String givenPassword){
        return user.getPassword().equals(givenPassword);
    }
}
