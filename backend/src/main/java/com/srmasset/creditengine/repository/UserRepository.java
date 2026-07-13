package com.srmasset.creditengine.repository;

import com.srmasset.creditengine.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, java.util.UUID> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
