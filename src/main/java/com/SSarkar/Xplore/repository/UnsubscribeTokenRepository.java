package com.SSarkar.Xplore.repository;

import com.SSarkar.Xplore.entity.UnsubscribeToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnsubscribeTokenRepository extends JpaRepository<UnsubscribeToken, Long> {

    Optional<UnsubscribeToken> findByToken(String token);
}