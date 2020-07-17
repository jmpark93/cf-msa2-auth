package com.jmworks.auth.controller;

import com.jmworks.auth.domain.User;
import com.jmworks.auth.exception.UserNotFoundException;
import com.jmworks.auth.payload.MessageResponse;
import com.jmworks.auth.payload.UserCommand;
import com.jmworks.auth.repository.UserRepository;
import com.jmworks.auth.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@EnableBinding(Source.class)
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Value("${s3.bucket}")
    String bucket;

    @Value("${s3.public}")
    String s3URL;

    @Autowired
    private Source rabbitSource;

    @Autowired
    UserRepository userRepository;

    @Autowired
    S3Service s3Service;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public User getProfile(@PathVariable("id") Long userId) {
        User userObj = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if( userObj.getImageURL() != null && ! userObj.getImageURL().isEmpty() ) {
            String imageURL =  s3URL + "/" + bucket + "/" + userObj.getImageURL();
            userObj.setImageURL(imageURL);
        }

        return userObj;
    }

    @SendTo(Source.OUTPUT)
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long userId) {
        System.out.println( "User(" + userId + ") will be deleted ... ");

        User userObj = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if( userObj.getImageURL() != null && ! userObj.getImageURL().isEmpty() ) {
            s3Service.deleteFile(userObj.getImageURL());
        }

        userRepository.deleteById(userId);

        UserCommand usrCmd = new UserCommand("DEL", userId);
        Message<UserCommand> message = MessageBuilder.withPayload(usrCmd).build();

        rabbitSource.output().send( message );

        return ResponseEntity.ok(new MessageResponse("User(" + userId + ") is deleted ... "));
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getUserList() {
        return new ResponseEntity<>( userRepository.findAll(), HttpStatus.OK);
    }
}