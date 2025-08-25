package com.thkim.toyproject.fintrack.domain.users;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User registerUser(String username, String email, String password);

    List<User> getAllUser();
}