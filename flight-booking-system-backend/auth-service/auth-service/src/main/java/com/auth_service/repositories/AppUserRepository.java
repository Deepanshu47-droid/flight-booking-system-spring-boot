package com.auth_service.repositories;

import com.auth_service.model.AppUser;
import com.auth_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByEmailAndRole(String email, Role role);

    boolean existsByUsernameAndRole(String username, Role role);

    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByEmailAndRole(String email, Role role);
    void deleteByUsername(String username);
}