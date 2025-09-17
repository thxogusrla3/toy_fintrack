package com.thkim.toyproject.fintrack.domain.users;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users", uniqueConstraints =  {
        @UniqueConstraint(name = "uk_users_username", columnNames = "userName"),
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String userName;

    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
    private String email;

    public User(String userName, String password) {
        this(userName, password, Role.USER);
    }

    public User(String userName, String password, Role roll) {
        this.userName = userName;
        this.password = password;
    }

    public User(String username, String passwordHash, String email) {
        this.userName = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(passwordHash);
        this.email = Objects.requireNonNull(email);
        this.role = Role.USER;
    }
}
