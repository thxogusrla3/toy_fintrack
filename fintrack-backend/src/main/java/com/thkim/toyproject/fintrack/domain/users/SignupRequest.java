package com.thkim.toyproject.fintrack.domain.users;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    @NotBlank(message = "아이디는 필수입니다.")
    private String userName;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;
}
