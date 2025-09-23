package com.thkim.toyproject.fintrack.domain.auth;

import com.thkim.toyproject.fintrack.application.api.auth.dto.SignupRequest;
import com.thkim.toyproject.fintrack.application.api.auth.dto.SignupResponse;
import com.thkim.toyproject.fintrack.domain.users.*;
import com.thkim.toyproject.fintrack.domain.users.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SignupResponse signup(SignupRequest registerTarget) {
        if(userRepository.existsByUserName(registerTarget.getUserName())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if(userRepository.existsByEmail(registerTarget.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 EMAIL 입니다.");
        }

        User saveUser = new User(registerTarget.getUserName(), passwordEncoder.encode(registerTarget.getPassword()), registerTarget.getEmail());
        userRepository.save(saveUser);

        return new SignupResponse(
                saveUser.getId(),
                saveUser.getUserName(),
                saveUser.getPassword()
                );
    }
}
