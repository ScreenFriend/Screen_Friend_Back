package com.golf.screen.repository;

import com.golf.screen.entity.JoinApplication;
import com.golf.screen.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoinApplicationRepository extends JpaRepository<JoinApplication, Long> {
    List<JoinApplication> findByJoinPostId(Long joinPostId);
    Optional<JoinApplication> findByJoinPostIdAndApplicantId(Long joinPostId, Long applicantId);
    List<JoinApplication> findByApplicantIdAndStatus(Long applicantId, ApplicationStatus status);
    
    boolean existsByJoinPostIdAndStatus(Long joinPostId, ApplicationStatus status);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM JoinApplication ja WHERE ja.joinPost.id = :joinPostId")
    void deleteByJoinPostId(@org.springframework.data.repository.query.Param("joinPostId") Long joinPostId);

    @org.springframework.data.jpa.repository.Query("SELECT ja FROM JoinApplication ja WHERE " +
           "ja.applicant.id = :applicantId AND ja.status IN :statuses " +
           "AND ja.joinPost.playDateTime > :dateTime " +
           "ORDER BY ja.joinPost.playDateTime DESC")
    List<JoinApplication> findActiveAppliedJoins(
        @org.springframework.data.repository.query.Param("applicantId") Long applicantId,
        @org.springframework.data.repository.query.Param("statuses") List<ApplicationStatus> statuses,
        @org.springframework.data.repository.query.Param("dateTime") java.time.LocalDateTime dateTime
    );
}
