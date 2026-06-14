package com.golf.screen.repository;

import com.golf.screen.entity.WithdrawnUserStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WithdrawnUserStatsRepository extends JpaRepository<WithdrawnUserStats, Long> {
    Optional<WithdrawnUserStats> findByPhoneNumberHash(String phoneNumberHash);
    void deleteByPhoneNumberHash(String phoneNumberHash);
}
