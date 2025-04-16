package com.api.repository;

import com.api.entity.User;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Class UserRepository
 *
 * Repository interface for performing CRUD operations on the User entity.
 * This interface extends JpaRepository to provide standard database operations and custom queries.
 */
@Hidden
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address (login) of the user.
     * @return An Optional containing the user with the specified email, or empty if no user is found.
     */
    Optional<User> findByEmail(String email);
    boolean existsByIdAndEmail(UUID id, String email);
}
