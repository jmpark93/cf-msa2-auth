package com.jmworks.auth.controller;

import com.jmworks.auth.domain.User;
import com.jmworks.auth.payload.SignupRequest;
import com.jmworks.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String getProfile(@PathVariable("id") String userId) {

        return "사용자 상세정보 (Message From : Auth API 서버) ";
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUserList() {
        return new ResponseEntity<>( userRepository.findAll(), HttpStatus.OK);
    }
}