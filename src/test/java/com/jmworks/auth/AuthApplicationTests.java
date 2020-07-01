package com.jmworks.auth;

import com.jmworks.auth.domain.Role;
import com.jmworks.auth.domain.RoleType;
import com.jmworks.auth.domain.User;
import com.jmworks.auth.repository.RoleRepository;
import com.jmworks.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
class AuthApplicationTests {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Test
    void contextLoads() {

    }

//    @Test
//    void initDB() {
//        User user = new User("jmpark93",
//                "jmpark93@koscom.co.kr",
//                encoder.encode("koscom!234"));
//
//        Set<Role> roles = new HashSet<>();
//        Role userRole = roleRepository.findByName(RoleType.ROLE_USER)
//                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//        roles.add(userRole);
//
//        user.setRoles(roles);
//        userRepository.save(user);
//    }
}
