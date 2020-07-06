package com.jmworks.auth;

import com.jmworks.auth.domain.Role;
import com.jmworks.auth.domain.RoleType;
import com.jmworks.auth.domain.User;
import com.jmworks.auth.repository.RoleRepository;
import com.jmworks.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@SpringBootApplication
public class AuthApplication {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @PostConstruct
    public void init() {
        Optional<Role> roleUser = roleRepository.findByName(RoleType.ROLE_USER);
        if ( roleUser.isPresent() )
            return;

        // Role 레코드 생성 ...
        Role userRole = new Role(RoleType.ROLE_USER);
        Role auditRole = new Role(RoleType.ROLE_AUDITOR);
        Role adminRole = new Role(RoleType.ROLE_ADMIN);

        roleRepository.save(userRole);
        roleRepository.save(auditRole);
        roleRepository.save(adminRole);

        // 관리자 계정 생성 ...
        User adminUser = new User("admin", "admin@admin.com", encoder.encode("kosgov"), "관리자");

        Set<Role> adminRoles = new HashSet<>();

        adminRoles.add(userRole);
        adminRoles.add(adminRole);

        adminUser.setRoles(adminRoles);

        userRepository.save(adminUser);
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
