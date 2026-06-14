package com.golf.screen.repository;

import com.golf.screen.entity.MannerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MannerReviewRepository extends JpaRepository<MannerReview, Long> {
    boolean existsByJoinPostIdAndReviewerIdAndTargetUserId(Long joinPostId, Long reviewerId, Long targetUserId);
}
