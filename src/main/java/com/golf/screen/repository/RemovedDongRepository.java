package com.golf.screen.repository;

import com.golf.screen.entity.RemovedDong;
import com.golf.screen.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RemovedDongRepository extends JpaRepository<RemovedDong, Long> {
    List<RemovedDong> findByUser(User user);
    Optional<RemovedDong> findByUserAndDongName(User user, String dongName);
    void deleteByUser(User user);
    void deleteByUserAndDongName(User user, String dongName);
}
