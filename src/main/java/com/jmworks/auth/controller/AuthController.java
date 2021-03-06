package com.jmworks.auth.controller;

import com.jmworks.auth.domain.Role;
import com.jmworks.auth.domain.RoleType;
import com.jmworks.auth.domain.User;
import com.jmworks.auth.payload.JwtResponse;
import com.jmworks.auth.payload.LoginRequest;
import com.jmworks.auth.payload.MessageResponse;
import com.jmworks.auth.payload.SignupRequest;
import com.jmworks.auth.repository.RoleRepository;
import com.jmworks.auth.repository.UserRepository;
import com.jmworks.auth.service.S3Service;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.http.*;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${config.oauth2.clientId}")
    private String clientId;

    @Value("${config.oauth2.clientSecret}")
    private String clientSecret;

    @Value("${config.oauth2.url}")
    private String oauthURL;

    @Value("${s3.bucket}")
    String bucket;

    @Value("${s3.public}")
    String s3URL;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenStore jwtTokenStore;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    S3Service s3Service;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("grant_type", "password");
        map.add("username", loginRequest.getUsername());
        map.add("password", loginRequest.getPassword());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = null;

        try {
            response =
                    restTemplate.exchange(oauthURL,
                            HttpMethod.POST,
                            entity,
                            String.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        }

        JSONObject jsonObject = new JSONObject(response.getBody());

        OAuth2Authentication auth = jwtTokenStore.readAuthentication(jsonObject.getString("access_token"));

        List<String> roles = auth.getUserAuthentication().getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        Object imageURL = jsonObject.get("imageURL");
        String strImageURL = "";
        if (imageURL != JSONObject.NULL) {
            strImageURL = s3URL + "/" + bucket + "/" + imageURL;
        }

//        String imageURL =
        return ResponseEntity.ok(new JwtResponse(
                jsonObject.getString("access_token"),
                jsonObject.getLong("id"),
                loginRequest.getUsername(),
                jsonObject.getString("email"),
                strImageURL,
                roles));
    }

    @PostMapping(value = "/signup", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> registerUser(
            @RequestParam(value = "profile", required = true) @Valid String jsonSignup,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile
    ) throws IOException {
        String accessURL = "";

        ObjectMapper objectMapper = new ObjectMapper();
        SignupRequest signUpRequest = objectMapper.readValue(jsonSignup, SignupRequest.class);

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFullname());

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "auditor":
                        Role modRole = roleRepository.findByName(RoleType.ROLE_AUDITOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // 사용자 프로파일 이미지 --> Upload (minio : S3)
        if (imageFile != null) {

            System.out.println("Saved User ID (" + user.getUsername() +") : " + savedUser.getId()) ;
            user.setId( savedUser.getId() );

            accessURL = s3Service.uploadFile(imageFile, savedUser.getId() + "-" + signUpRequest.getUsername());
            user.setImageURL(accessURL);

            String sourceFileName = imageFile.getOriginalFilename();
            System.out.println("Original File Name : " + sourceFileName);
            System.out.println("Access URL : " + accessURL);

            userRepository.save(user);
        }

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}