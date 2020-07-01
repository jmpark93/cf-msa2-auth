package com.jmworks.auth;

import com.jmworks.auth.domain.Role;
import com.jmworks.auth.domain.RoleType;
import com.jmworks.auth.domain.User;
import com.jmworks.auth.repository.RoleRepository;
import com.jmworks.auth.repository.UserRepository;
import com.jmworks.auth.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
public class AuthApplication {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    PasswordEncoder encoder;

    @PostConstruct
    public void init() {
        Optional<Role> roleUser = roleRepository.findByName(RoleType.ROLE_USER);
        if ( roleUser.isPresent() )
            return;

        Role userRole = new Role(RoleType.ROLE_USER);
        Role auditRole = new Role(RoleType.ROLE_AUDITOR);
        Role adminRole = new Role(RoleType.ROLE_ADMIN);

        roleRepository.save(userRole);
        roleRepository.save(auditRole);
        roleRepository.save(adminRole);

        User adminUser = new User("admin", "admin@admin.com", encoder.encode("kosgov"));
        User normalUser = new User("jmpark93", "jmpark93@gmail.com", encoder.encode("koscom"));

        Set<Role> adminRoles = new HashSet<>();
        Set<Role> userRoles = new HashSet<>();

        adminRoles.add(userRole);
        adminRoles.add(adminRole);

        userRoles.add(userRole);

        adminUser.setRoles(adminRoles);
        normalUser.setRoles(userRoles);

        userRepository.save(adminUser);
        userRepository.save(normalUser);

//        String userName = "admin";
//        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
//        List<String> roles = userDetails.getAuthorities().stream()
//                .map(item -> item.getAuthority())
//                .collect(Collectors.toList());
//
//        System.out.println(">>> User Name : " +  userName + ", Roles : " + roles);
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
