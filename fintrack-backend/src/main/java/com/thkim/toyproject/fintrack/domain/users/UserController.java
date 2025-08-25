package com.thkim.toyproject.fintrack.domain.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

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

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUser(){
        List<User> allUser = userService.getAllUser();

        return ResponseEntity.ok(allUser);
    }
}
