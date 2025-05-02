package com.api.controller;

import com.api.dto.*;
import com.api.dto.error.ValidationErrorMessageResponseDto;
import com.api.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Class UserController
 *
 * UserController handles API requests related to users.
 * It provides endpoints for adding, updating, deleting, and retrieving users, with appropriate authorization checks.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
@Tag(name="User controller", description="Manipulations with users and their info")
public class UserController {

    private final UserService userService;


    /**
     * Get a user by its ID.
     * Accessible only by the admin.
     *
     * @param userIdDto DTO containing the ID of the user
     * @return ResponseEntity containing the UserDto of the requested id
     */
    @Operation(summary = "get user by id - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PostMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@RequestBody @Valid IdDto userIdDto){
        return ResponseEntity.ok(userService.getUserById(userIdDto));
    }

    /**
     * Creates a new user.
     * Accessible only by the admin.
     *
     * @param userDtoNoId DTO containing the details of the user to create
     * @return ResponseEntity containing the created UserDto
     */
    @Operation(summary = "add new user - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "New user is created", content = @Content(schema = @Schema(implementation = UserDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (non valid data)",  content = @Content(schema = @Schema(implementation = ValidationErrorMessageResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PostMapping("/new")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<UserDto> addUser(@RequestBody @Valid UserDtoNoId userDtoNoId){
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.addUser(userDtoNoId));
    }

    /**
     * Updates an existing user.
     * Accessible only by the admin.
     *
     * @param userDto DTO containing the updated user details
     * @return ResponseEntity containing the updated UserDto
     */
    @Operation(summary = "update user that already exists in the database - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User is updated", content = @Content(schema = @Schema(implementation = UserDto.class), mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request (non valid data)",  content = @Content(schema = @Schema(implementation = ValidationErrorMessageResponseDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @PutMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<UserDto> updateUser(@RequestBody @Valid UserDto userDto){
        return ResponseEntity.ok(userService.updateUser(userDto));
    }


    /**
     * Deletes a user by its ID (only if there are no related cards and transactions).
     * Accessible only by the admin.
     *
     * @param userIdDto DTO containing the ID of the user to delete
     */
    @Operation(summary = "delete user by id - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User is deleted or does not exist"),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @DeleteMapping
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public void deleteUserById(@RequestBody @Valid IdDto userIdDto){
        userService.deleteUser(userIdDto);
    }

    /**
     * Fetches all users with pagination.
     * Accessible only by the admin.
     *
     * @param page The page number (default 0)
     * @param size The page size (default 3)
     * @return ResponseEntity containing a Page of UserDto objects
     */
    @Operation(summary = "get all users (paging) - only for admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",  content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (non authenticated) or access denied",  content = @Content(mediaType = "none"))}
    )
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated() && hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> findAll(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "3") int size){
        return ResponseEntity.ok(userService.findAll(PageRequest.of(page, size)));
    }
}
