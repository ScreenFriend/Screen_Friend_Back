package com.golf.screen.repository;

import com.golf.screen.entity.JoinPost;
import com.golf.screen.entity.JoinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface JoinPostRepository extends JpaRepository<JoinPost, Long> {
    List<JoinPost> findByDong(String dong);
    List<JoinPost> findByCreatorId(Long creatorId);
    List<JoinPost> findByCreatorIdAndStatus(Long creatorId, JoinStatus status);

    @EntityGraph(attributePaths = {"creator"})
    List<JoinPost> findByDongAndPlayDateTimeAfterAndStatus(String dong, java.time.LocalDateTime dateTime, JoinStatus status);

    @EntityGraph(attributePaths = {"creator"})
    List<JoinPost> findByPlayDateTimeAfterAndStatus(java.time.LocalDateTime dateTime, JoinStatus status);
    List<JoinPost> findByCreatorIdAndPlayDateTimeAfterOrderByPlayDateTimeDesc(Long creatorId, java.time.LocalDateTime dateTime);

    @org.springframework.data.jpa.repository.Query("SELECT j FROM JoinPost j WHERE " +
           "(j.creator.id = :userId OR j.id IN (SELECT ja.joinPost.id FROM JoinApplication ja WHERE ja.applicant.id = :userId AND ja.status = :status)) " +
           "AND j.playDateTime <= :limitTime " +
           "ORDER BY j.playDateTime DESC")
    List<JoinPost> findMyCompletedJoins(
        @org.springframework.data.repository.query.Param("userId") Long userId,
        @org.springframework.data.repository.query.Param("status") com.golf.screen.entity.ApplicationStatus status,
        @org.springframework.data.repository.query.Param("limitTime") java.time.LocalDateTime limitTime
    );
}
