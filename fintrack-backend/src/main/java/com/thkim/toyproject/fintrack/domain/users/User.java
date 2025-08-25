package com.thkim.toyproject.fintrack.domain.users;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userName;
    private String password;
    private String role;
    private String email;

    public User(String userName, String password) {
        this(userName, password, "USER");
    }

    public User(String userName, String password, String roll) {
        this.userName = userName;
        this.password = password;
    }
}
