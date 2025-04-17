package com.api.repository;

import com.api.entity.User;
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
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Retrieves a user by their email address.
     *
     * @param email The email address (login) of the user.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user exists with the specified ID and email address.
     *
     * @param id The ID of the user.
     * @param email The email address of the user.
     */
    boolean existsByIdAndEmail(UUID id, String email);
}
