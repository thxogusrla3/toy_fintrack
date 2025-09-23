package com.thkim.toyproject.fintrack.application.api.users;

import com.thkim.toyproject.fintrack.domain.users.UserService;
import com.thkim.toyproject.fintrack.domain.users.UserServiceImpl;
import com.thkim.toyproject.fintrack.domain.users.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserServiceImpl userServiceImpl) {
        this.userService = userServiceImpl;
    }

    @GetMapping("/find_user")
    public String getHome(
            @RequestParam(value = "userName") String userName
    ) throws Exception {
        String result = "";
        try {
            User findUser = userService.findByUsername(userName).orElseThrow(Exception::new);

            result = findUser.getUserName();
        } catch (Exception e) {
            return userName + " 은 없는 사람임";
        }
        return result;
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUser(){
        List<User> allUser = userService.getAllUser();

        return ResponseEntity.ok(allUser);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        ));
    }

}
