package qnb.user.repository;

import qnb.user.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    // ✅ userId로 선호 정보 조회
    Optional<UserPreference> findByUser_UserId(Long userId);
}


