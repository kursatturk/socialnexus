package com.socialnexus.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.socialnexus.domain.Credential;

public interface CredentialRepository extends JpaRepository<Credential, Long> {

    Optional<Credential> findByUsername(String username);

    boolean existsByUsername(String username);
}
