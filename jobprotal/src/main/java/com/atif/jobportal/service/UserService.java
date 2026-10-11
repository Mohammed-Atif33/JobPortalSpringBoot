package com.atif.jobportal.service;

import com.atif.jobportal.dto.UserRequestDTO;
import com.atif.jobportal.dto.UserResponseDTO;
import com.atif.jobportal.entity.User;
import com.atif.jobportal.exception.UserNotFoundException;
import com.atif.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.atif.jobportal.security.UserRole;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository ,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder =passwordEncoder;
    }


    public UserResponseDTO createUser(UserRequestDTO request) {

        User user = new User();

        updateUserFields(user, request);

        // Public registration always creates a Job Seeker
        user.setRole(UserRole.JOB_SEEKER.name());

        // Encrypt the password before saving
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        User savedUser = userRepository.save(user);

        return convertToResponseDTO(savedUser);
    }


    public List<UserResponseDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    public UserResponseDTO getUserById(Long id) {

        User user = findUserById(id);

        return convertToResponseDTO(user);
    }

    public UserResponseDTO updateUser(
            Long id,
            UserRequestDTO request) {

        User user = findUserById(id);

        updateUserFields(user, request);

        User updatedUser = userRepository.save(user);

        return convertToResponseDTO(updatedUser);
    }

    public void deleteUser(Long id) {

        User user = findUserById(id);

        userRepository.delete(user);
    }

    private User findUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id
                        )
                );
    }

    private void updateUserFields(
            User user,
            UserRequestDTO request) {

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
    }

    private UserResponseDTO convertToResponseDTO(User user) {

        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}