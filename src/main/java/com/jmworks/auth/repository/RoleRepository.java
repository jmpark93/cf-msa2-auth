package com.jmworks.auth.repository;

import com.jmworks.auth.domain.Role;
import com.jmworks.auth.domain.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleType name);

}
